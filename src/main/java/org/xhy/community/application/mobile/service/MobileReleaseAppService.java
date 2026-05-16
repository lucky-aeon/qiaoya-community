package org.xhy.community.application.mobile.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.mobile.dto.MobileReleaseDTO;
import org.xhy.community.infrastructure.integration.github.GithubReleaseClient;

@Service
public class MobileReleaseAppService {

    private final GithubReleaseClient githubReleaseClient;

    public MobileReleaseAppService(GithubReleaseClient githubReleaseClient) {
        this.githubReleaseClient = githubReleaseClient;
    }

    public MobileReleaseDTO getLatestAndroidRelease() {
        return githubReleaseClient.getLatestAndroidRelease();
    }
}
