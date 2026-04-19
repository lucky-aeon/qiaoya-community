package org.xhy.community.infrastructure.integration.github;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import org.xhy.community.domain.config.valueobject.CreatorAboutPageConfig;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GithubRepositoryStarClient {

    private static final Logger log = LoggerFactory.getLogger(GithubRepositoryStarClient.class);
    private static final Duration TTL = Duration.ofHours(6);
    private static final String USER_AGENT = "qiaoya-community-about-page";
    private static final Pattern COUNT_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)([kKmMbB]?)");

    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final String githubApiToken;

    public GithubRepositoryStarClient(@Value("${github.api-token:}") String githubApiToken) {
        this.githubApiToken = githubApiToken;
    }

    public Integer getStarCount(String githubUrl) {
        CreatorAboutPageConfig.RepoCoordinates coordinates = CreatorAboutPageConfig.RepoCoordinates.fromUrl(githubUrl);
        String cacheKey = coordinates.owner() + "/" + coordinates.repo();
        log.info("【GitHubStar】开始获取 Star：githubUrl={}, repo={}", githubUrl, cacheKey);
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.info("【GitHubStar】命中缓存：repo={}, stars={}", cacheKey, cached.starCount());
            return cached.starCount();
        }

        try {
            Integer starCount = fetchStarCount(coordinates.owner(), coordinates.repo(), githubUrl);
            if (starCount != null) {
                cache.put(cacheKey, new CacheEntry(starCount, Instant.now().plus(TTL)));
                log.info("【GitHubStar】拉取成功并写入缓存：repo={}, stars={}", cacheKey, starCount);
            } else {
                cache.remove(cacheKey);
                log.warn("【GitHubStar】GitHub 返回中未找到 stargazers_count：repo={}", cacheKey);
            }
            return starCount;
        } catch (RuntimeException e) {
            log.warn("【GitHubStar】拉取 Star 失败：githubUrl={}, repo={}", githubUrl, cacheKey, e);
            cache.remove(cacheKey);
            return null;
        }
    }

    protected Integer fetchStarCount(String owner, String repo, String githubUrl) {
        Integer apiStarCount = fetchStarCountViaApi(owner, repo);
        if (apiStarCount != null) {
            return apiStarCount;
        }
        return fetchStarCountViaHtml(owner, repo, githubUrl);
    }

    private Integer fetchStarCountViaApi(String owner, String repo) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(MediaType.parseMediaTypes("application/vnd.github+json"));
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        headers.set("User-Agent", USER_AGENT);
        if (StringUtils.hasText(githubApiToken)) {
            headers.setBearerAuth(githubApiToken);
        }

        try {
            log.info("【GitHubStar】请求 GitHub API：owner={}, repo={}, url={}, authEnabled={}",
                    owner, repo, url, StringUtils.hasText(githubApiToken));
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            log.info("【GitHubStar】GitHub API 响应：owner={}, repo={}, status={}", owner, repo, response.getStatusCode().value());
            Map<?, ?> body = response.getBody();
            if (body == null) {
                log.warn("【GitHubStar】GitHub API 响应体为空：owner={}, repo={}", owner, repo);
                return null;
            }
            Object raw = body.get("stargazers_count");
            if (raw instanceof Number number) {
                log.info("【GitHubStar】API 解析到 Star 数：owner={}, repo={}, stars={}", owner, repo, number.intValue());
                return number.intValue();
            }
            log.warn("【GitHubStar】API 响应体缺少 stargazers_count：owner={}, repo={}, bodyKeys={}", owner, repo, body.keySet());
            return null;
        } catch (RuntimeException e) {
            log.warn("【GitHubStar】GitHub API 拉取失败，准备回退 HTML 解析：owner={}, repo={}, authEnabled={}",
                    owner, repo, StringUtils.hasText(githubApiToken), e);
            return null;
        }
    }

    private Integer fetchStarCountViaHtml(String owner, String repo, String githubUrl) {
        try {
            log.info("【GitHubStar】开始回退 HTML 解析：owner={}, repo={}, url={}", owner, repo, githubUrl);
            Document document = Jsoup.connect(githubUrl)
                    .userAgent(USER_AGENT)
                    .timeout((int) Duration.ofSeconds(8).toMillis())
                    .get();

            List<String> selectors = List.of(
                    "a[href='/" + owner + "/" + repo + "/stargazers']",
                    "a[href='https://github.com/" + owner + "/" + repo + "/stargazers']",
                    "a[href$='/" + owner + "/" + repo + "/stargazers']"
            );

            for (String selector : selectors) {
                Elements candidates = document.select(selector);
                for (Element candidate : candidates) {
                    Integer parsed = extractStarCount(candidate);
                    if (parsed != null) {
                        log.info("【GitHubStar】HTML 解析到 Star 数：owner={}, repo={}, stars={}, selector={}",
                                owner, repo, parsed, selector);
                        return parsed;
                    }
                }
            }

            log.warn("【GitHubStar】HTML 未解析到 Star 数：owner={}, repo={}", owner, repo);
            return null;
        } catch (IOException e) {
            log.warn("【GitHubStar】HTML 回退失败：owner={}, repo={}, url={}", owner, repo, githubUrl, e);
            return null;
        }
    }

    private Integer extractStarCount(Element candidate) {
        Integer fromAriaLabel = parseCount(candidate.attr("aria-label"));
        if (fromAriaLabel != null) {
            return fromAriaLabel;
        }

        Integer fromTitle = parseCount(candidate.attr("title"));
        if (fromTitle != null) {
            return fromTitle;
        }

        Integer fromText = parseCount(candidate.text());
        if (fromText != null) {
            return fromText;
        }

        for (Element child : candidate.children()) {
            Integer fromChild = parseCount(child.text());
            if (fromChild != null) {
                return fromChild;
            }
        }
        return null;
    }

    private Integer parseCount(String rawValue) {
        if (!StringUtils.hasText(rawValue)) {
            return null;
        }

        String normalized = rawValue
                .replace(",", "")
                .replace(" ", "")
                .replace("\u00A0", "")
                .trim();
        Matcher matcher = COUNT_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }

        double value = Double.parseDouble(matcher.group(1));
        String suffix = matcher.group(2).toLowerCase(Locale.ROOT);
        double multiplier = switch (suffix) {
            case "k" -> 1_000;
            case "m" -> 1_000_000;
            case "b" -> 1_000_000_000;
            default -> 1;
        };
        return (int) Math.round(value * multiplier);
    }

    private record CacheEntry(Integer starCount, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
