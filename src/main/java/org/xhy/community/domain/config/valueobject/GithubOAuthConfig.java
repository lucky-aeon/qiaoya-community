package org.xhy.community.domain.config.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub OAuth 配置，存储于 system_configs 表的 data 字段（JSON）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubOAuthConfig {

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private List<String> scopes = new ArrayList<>(List.of("read:user", "user:email"));
    private String authorizeBaseUri = "https://github.com/login/oauth/authorize";
    private String tokenUri = "https://github.com/login/oauth/access_token";
    private String userApi = "https://api.github.com/user";
    private String emailApi = "https://api.github.com/user/emails";

    // 合并相关策略
    private boolean requireVerifiedEmailForMerge = true;
    private boolean fetchEmailFromApi = true;
    private boolean updateUserProfileIfEmpty = true;

    public boolean isValid() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank()
                && redirectUri != null && !redirectUri.isBlank();
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }

    public List<String> getScopes() { return scopes; }
    public void setScopes(List<String> scopes) { this.scopes = scopes; }

    public String getAuthorizeBaseUri() { return authorizeBaseUri; }
    public void setAuthorizeBaseUri(String authorizeBaseUri) { this.authorizeBaseUri = authorizeBaseUri; }

    public String getTokenUri() { return tokenUri; }
    public void setTokenUri(String tokenUri) { this.tokenUri = tokenUri; }

    public String getUserApi() { return userApi; }
    public void setUserApi(String userApi) { this.userApi = userApi; }

    public String getEmailApi() { return emailApi; }
    public void setEmailApi(String emailApi) { this.emailApi = emailApi; }

    public boolean isRequireVerifiedEmailForMerge() { return requireVerifiedEmailForMerge; }
    public void setRequireVerifiedEmailForMerge(boolean requireVerifiedEmailForMerge) { this.requireVerifiedEmailForMerge = requireVerifiedEmailForMerge; }

    public boolean isFetchEmailFromApi() { return fetchEmailFromApi; }
    public void setFetchEmailFromApi(boolean fetchEmailFromApi) { this.fetchEmailFromApi = fetchEmailFromApi; }

    public boolean isUpdateUserProfileIfEmpty() { return updateUserProfileIfEmpty; }
    public void setUpdateUserProfileIfEmpty(boolean updateUserProfileIfEmpty) { this.updateUserProfileIfEmpty = updateUserProfileIfEmpty; }
}
