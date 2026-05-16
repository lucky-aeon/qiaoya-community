package org.xhy.community.application.mobile.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class MobileReleaseDTO {

    private final String platform;
    private final String versionName;
    private final Integer versionCode;
    private final Integer minSupportedVersionCode;
    private final String apkUrl;
    private final String apkSha256;
    private final Long fileSize;
    private final Boolean forceUpdate;
    private final List<String> releaseNotes;
    private final OffsetDateTime publishedAt;
    private final String source;
    private final String releaseUrl;
    private final String tagName;

    public MobileReleaseDTO(
            String platform,
            String versionName,
            Integer versionCode,
            Integer minSupportedVersionCode,
            String apkUrl,
            String apkSha256,
            Long fileSize,
            Boolean forceUpdate,
            List<String> releaseNotes,
            OffsetDateTime publishedAt,
            String source,
            String releaseUrl,
            String tagName
    ) {
        this.platform = platform;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.minSupportedVersionCode = minSupportedVersionCode;
        this.apkUrl = apkUrl;
        this.apkSha256 = apkSha256;
        this.fileSize = fileSize;
        this.forceUpdate = forceUpdate;
        this.releaseNotes = releaseNotes;
        this.publishedAt = publishedAt;
        this.source = source;
        this.releaseUrl = releaseUrl;
        this.tagName = tagName;
    }

    public String getPlatform() { return platform; }

    public String getVersionName() { return versionName; }

    public Integer getVersionCode() { return versionCode; }

    public Integer getMinSupportedVersionCode() { return minSupportedVersionCode; }

    public String getApkUrl() { return apkUrl; }

    public String getApkSha256() { return apkSha256; }

    public Long getFileSize() { return fileSize; }

    public Boolean getForceUpdate() { return forceUpdate; }

    public List<String> getReleaseNotes() { return releaseNotes; }

    public OffsetDateTime getPublishedAt() { return publishedAt; }

    public String getSource() { return source; }

    public String getReleaseUrl() { return releaseUrl; }

    public String getTagName() { return tagName; }
}
