package org.xhy.community.infrastructure.integration.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.xhy.community.application.mobile.dto.MobileReleaseDTO;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GithubReleaseClient {

    private static final Logger log = LoggerFactory.getLogger(GithubReleaseClient.class);
    private static final String USER_AGENT = "qiaoya-community-mobile-release";
    private static final Pattern APK_VERSION_PATTERN = Pattern.compile(".*-v?([0-9]+(?:\\.[0-9]+){1,3})-(\\d+)\\.apk$");
    private static final Pattern FORCE_UPDATE_PATTERN = Pattern.compile("(?im)^\\s*forceUpdate\\s*[:=]\\s*(true|false)\\s*$");
    private static final Pattern MIN_SUPPORTED_PATTERN = Pattern.compile("(?im)^\\s*minSupportedVersionCode\\s*[:=]\\s*(\\d+)\\s*$");
    private static final Pattern VERSION_CODE_PATTERN = Pattern.compile("(?im)^\\s*versionCode\\s*[:=]\\s*(\\d+)\\s*$");

    private final RestTemplate restTemplate = new RestTemplate();
    private final String githubApiToken;
    private final String owner;
    private final String repo;
    private final Duration ttl;

    private CacheEntry cache;

    public GithubReleaseClient(
            @Value("${github.api-token:}") String githubApiToken,
            @Value("${github.mobile-release.owner:xhyqaq}") String owner,
            @Value("${github.mobile-release.repo:qiaoya-community-mobile}") String repo,
            @Value("${github.mobile-release.cache-ttl-seconds:300}") long ttlSeconds
    ) {
        this.githubApiToken = githubApiToken;
        this.owner = owner;
        this.repo = repo;
        this.ttl = Duration.ofSeconds(Math.max(30, ttlSeconds));
    }

    public synchronized MobileReleaseDTO getLatestAndroidRelease() {
        if (cache != null && !cache.isExpired()) {
            return cache.release();
        }

        MobileReleaseDTO release = fetchLatestAndroidRelease();
        cache = new CacheEntry(release, Instant.now().plus(ttl));
        return release;
    }

    @SuppressWarnings("unchecked")
    private MobileReleaseDTO fetchLatestAndroidRelease() {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest";
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(MediaType.parseMediaTypes("application/vnd.github+json"));
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        headers.set("User-Agent", USER_AGENT);
        if (StringUtils.hasText(githubApiToken)) {
            headers.setBearerAuth(githubApiToken);
        }

        log.info("【MobileRelease】请求 GitHub 最新 Release：owner={}, repo={}, authEnabled={}",
                owner, repo, StringUtils.hasText(githubApiToken));
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("GitHub 最新 Release 响应为空");
        }

        List<Map<String, Object>> assets = (List<Map<String, Object>>) body.getOrDefault("assets", List.of());
        Map<String, Object> apkAsset = assets.stream()
                .filter(asset -> String.valueOf(asset.get("name")).toLowerCase().endsWith(".apk"))
                .max((left, right) -> Integer.compare(
                        parseAssetVersionCode(stringValue(left.get("name"))),
                        parseAssetVersionCode(stringValue(right.get("name")))
                ))
                .orElseThrow(() -> new IllegalStateException("GitHub 最新 Release 中未找到 APK 资源"));

        String tagName = stringValue(body.get("tag_name"));
        String releaseName = stringValue(body.get("name"));
        String releaseBody = stringValue(body.get("body"));
        String assetName = stringValue(apkAsset.get("name"));
        VersionInfo versionInfo = parseVersionInfo(assetName, tagName, releaseBody);

        List<String> notes = parseReleaseNotes(releaseBody);
        if (notes.isEmpty() && StringUtils.hasText(releaseName)) {
            notes = List.of(releaseName);
        }
        if (notes.isEmpty()) {
            notes = List.of("Android 新版本已发布");
        }

        MobileReleaseDTO release = new MobileReleaseDTO(
                "android",
                versionInfo.versionName(),
                versionInfo.versionCode(),
                versionInfo.minSupportedVersionCode(),
                stringValue(apkAsset.get("browser_download_url")),
                parseSha256(apkAsset),
                longValue(apkAsset.get("size")),
                versionInfo.forceUpdate(),
                notes,
                parseDateTime(body.get("published_at")),
                "github",
                stringValue(body.get("html_url")),
                tagName
        );
        log.info("【MobileRelease】解析 GitHub APK 成功：tag={}, asset={}, versionName={}, versionCode={}",
                tagName, assetName, release.getVersionName(), release.getVersionCode());
        return release;
    }

    private VersionInfo parseVersionInfo(String assetName, String tagName, String releaseBody) {
        String versionName = stripLeadingV(tagName);
        Integer versionCode = parseIntegerDirective(releaseBody, VERSION_CODE_PATTERN);

        Matcher matcher = APK_VERSION_PATTERN.matcher(assetName);
        if (matcher.matches()) {
            versionName = matcher.group(1);
            versionCode = Integer.parseInt(matcher.group(2));
        }

        if (!StringUtils.hasText(versionName)) {
            versionName = "0.0.0";
        }
        if (versionCode == null || versionCode <= 0) {
            throw new IllegalStateException("无法从 Release 元数据或 APK 文件名解析 versionCode");
        }

        boolean forceUpdate = parseBooleanDirective(releaseBody, FORCE_UPDATE_PATTERN, false);
        int minSupportedVersionCode = parseIntegerDirective(releaseBody, MIN_SUPPORTED_PATTERN, 1);
        return new VersionInfo(versionName, versionCode, minSupportedVersionCode, forceUpdate);
    }

    private int parseAssetVersionCode(String assetName) {
        if (!StringUtils.hasText(assetName)) {
            return 0;
        }
        Matcher matcher = APK_VERSION_PATTERN.matcher(assetName);
        return matcher.matches() ? Integer.parseInt(matcher.group(2)) : 0;
    }

    private List<String> parseReleaseNotes(String body) {
        if (!StringUtils.hasText(body)) {
            return List.of();
        }

        String cleaned = FORCE_UPDATE_PATTERN.matcher(body).replaceAll("");
        cleaned = MIN_SUPPORTED_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = VERSION_CODE_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = cleaned.replaceAll("(?m)^\\s*---\\s*$", "");

        List<String> notes = new ArrayList<>();
        for (String line : cleaned.split("\\R")) {
            String note = line.trim().replaceFirst("^[-*]\\s+", "").trim();
            if (StringUtils.hasText(note) && !isReleaseMetadataNote(note)) {
                notes.add(note);
            }
        }
        return notes;
    }

    private boolean isReleaseMetadataNote(String note) {
        String lower = note.toLowerCase();
        return lower.startsWith("versionname:")
                || lower.startsWith("versioncode:")
                || lower.startsWith("apk:")
                || lower.startsWith("sha256:")
                || lower.contains("apk 自动构建发布");
    }

    private String parseSha256(Map<String, Object> asset) {
        String digest = stringValue(asset.get("digest"));
        if (digest != null && digest.startsWith("sha256:")) {
            return digest.substring("sha256:".length());
        }
        return null;
    }

    private boolean parseBooleanDirective(String body, Pattern pattern, boolean defaultValue) {
        if (!StringUtils.hasText(body)) {
            return defaultValue;
        }
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : defaultValue;
    }

    private Integer parseIntegerDirective(String body, Pattern pattern) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        Matcher matcher = pattern.matcher(body);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private int parseIntegerDirective(String body, Pattern pattern, int defaultValue) {
        Integer value = parseIntegerDirective(body, pattern);
        return value == null ? defaultValue : value;
    }

    private OffsetDateTime parseDateTime(Object value) {
        String raw = stringValue(value);
        return StringUtils.hasText(raw) ? OffsetDateTime.parse(raw) : null;
    }

    private String stripLeadingV(String value) {
        String raw = stringValue(value);
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        return raw.startsWith("v") || raw.startsWith("V") ? raw.substring(1) : raw;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private record VersionInfo(String versionName, Integer versionCode, Integer minSupportedVersionCode, Boolean forceUpdate) {}

    private record CacheEntry(MobileReleaseDTO release, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
