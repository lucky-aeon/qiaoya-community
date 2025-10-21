package org.xhy.community.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOssProperties {
    
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String region;
    private String roleArn;
    private String customDomain; // 自定义域名，如 https://oss.xhyovo.cn
    private Callback callback = new Callback();
    private Long presignedUrlExpiration = 3600L; // 默认1小时
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getAccessKeyId() {
        return accessKeyId;
    }
    
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }
    
    public String getAccessKeySecret() {
        return accessKeySecret;
    }
    
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }
    
    public String getBucketName() {
        return bucketName;
    }
    
    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getRoleArn() {
        return roleArn;
    }
    
    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }
    
    public Callback getCallback() {
        return callback;
    }
    
    public void setCallback(Callback callback) {
        this.callback = callback;
    }
    
    public Long getPresignedUrlExpiration() {
        return presignedUrlExpiration;
    }
    
    public void setPresignedUrlExpiration(Long presignedUrlExpiration) {
        this.presignedUrlExpiration = presignedUrlExpiration;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    public void setCustomDomain(String customDomain) {
        this.customDomain = customDomain;
    }

    public static class Callback {
        private String url;
        private String body;
        private String bodyType = "application/x-www-form-urlencoded";
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getBody() {
            return body;
        }
        
        public void setBody(String body) {
            this.body = body;
        }
        
        public String getBodyType() {
            return bodyType;
        }
        
        public void setBodyType(String bodyType) {
            this.bodyType = bodyType;
        }
    }
}