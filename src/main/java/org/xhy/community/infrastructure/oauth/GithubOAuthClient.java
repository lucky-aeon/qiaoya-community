package org.xhy.community.infrastructure.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.xhy.community.infrastructure.exception.AuthErrorCode;
import org.xhy.community.infrastructure.exception.BusinessException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class GithubOAuthClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class TokenResponse {
        private String accessToken;
        public TokenResponse(String accessToken) { this.accessToken = accessToken; }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class RawTokenResponse {
        @JsonProperty("access_token") public String accessToken;
        @JsonProperty("scope") public String scope;
        @JsonProperty("token_type") public String tokenType;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GithubUser {
        public String id;
        public String login;
        public String name;
        @JsonProperty("avatar_url")
        public String avatarUrl;
        public String email; // may be null
        public boolean emailVerified;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class GithubEmail {
        public String email;
        public Boolean primary;
        public Boolean verified;
        public String visibility;
    }

    public TokenResponse exchangeCodeForToken(String tokenUri, String clientId, String clientSecret, String code, String redirectUri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("code", code);
            form.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(tokenUri, req, String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new BusinessException(AuthErrorCode.OAUTH_CODE_EXCHANGE_FAILED, "GitHub token 请求失败");
            }
            RawTokenResponse raw = objectMapper.readValue(resp.getBody(), RawTokenResponse.class);
            if (raw == null || raw.accessToken == null || raw.accessToken.isBlank()) {
                throw new BusinessException(AuthErrorCode.OAUTH_CODE_EXCHANGE_FAILED, "GitHub token 为空");
            }
            return new TokenResponse(raw.accessToken);
        } catch (RestClientException e) {
            throw new BusinessException(AuthErrorCode.OAUTH_CODE_EXCHANGE_FAILED, e);
        } catch (Exception e) {
            throw new BusinessException(AuthErrorCode.OAUTH_CODE_EXCHANGE_FAILED, e);
        }
    }

    public GithubUser fetchUserInfo(String userApi, String emailApi, String accessToken, boolean fetchEmailFromApi) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("User-Agent", "qiaoya-community");

            ResponseEntity<String> userResp = restTemplate.exchange(userApi, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (!userResp.getStatusCode().is2xxSuccessful() || userResp.getBody() == null) {
                throw new BusinessException(AuthErrorCode.OAUTH_USERINFO_FAILED, "GitHub 用户信息拉取失败");
            }
            Map<String, Object> userMap = objectMapper.readValue(userResp.getBody(), new TypeReference<Map<String, Object>>(){});
            GithubUser gu = new GithubUser();
            Object idObj = userMap.get("id");
            gu.id = idObj != null ? String.valueOf(idObj) : null;
            gu.login = (String) userMap.get("login");
            gu.name = (String) userMap.get("name");
            gu.avatarUrl = (String) userMap.get("avatar_url");
            gu.email = (String) userMap.get("email");
            gu.emailVerified = false; // default

            if ((gu.email == null || gu.email.isBlank()) && fetchEmailFromApi) {
                ResponseEntity<String> emailResp = restTemplate.exchange(emailApi, HttpMethod.GET, new HttpEntity<>(headers), String.class);
                if (emailResp.getStatusCode().is2xxSuccessful() && emailResp.getBody() != null) {
                    List<GithubEmail> emails = objectMapper.readValue(emailResp.getBody(), new TypeReference<List<GithubEmail>>(){});
                    if (emails != null && !emails.isEmpty()) {
                        // 优先选择 verified & primary；否则选择 verified；否则第一个
                        GithubEmail best = emails.stream()
                                .sorted(Comparator.comparing((GithubEmail e) -> e.verified != null && e.verified).reversed()
                                        .thenComparing((GithubEmail e) -> e.primary != null && e.primary).reversed())
                                .findFirst().orElse(null);
                        if (best != null) {
                            gu.email = best.email;
                            gu.emailVerified = Boolean.TRUE.equals(best.verified);
                        }
                    }
                }
            }

            return gu;
        } catch (RestClientException e) {
            throw new BusinessException(AuthErrorCode.OAUTH_USERINFO_FAILED, e);
        } catch (Exception e) {
            throw new BusinessException(AuthErrorCode.OAUTH_USERINFO_FAILED, e);
        }
    }
}
