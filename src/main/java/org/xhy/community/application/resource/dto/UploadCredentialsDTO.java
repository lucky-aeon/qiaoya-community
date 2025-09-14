package org.xhy.community.application.resource.dto;

import java.time.LocalDateTime;

public class UploadCredentialsDTO {
    
    // STS临时凭证
    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;
    private String expiration;
    
    // OSS信息
    private String region;
    private String bucket;
    private String endpoint;
    
    // 上传策略和签名
    private String policy;
    private String signature;
    private String key;
    
    // 回调参数
    private String callback;
    
    // 原有字段保持兼容性
    private String uploadUrl;
    private String fileKey;
    private LocalDateTime expirationTime;
    private Long maxFileSize;
    
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
    
    public String getSecurityToken() {
        return securityToken;
    }
    
    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
    
    public String getExpiration() {
        return expiration;
    }
    
    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getBucket() {
        return bucket;
    }
    
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getPolicy() {
        return policy;
    }
    
    public void setPolicy(String policy) {
        this.policy = policy;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getCallback() {
        return callback;
    }
    
    public void setCallback(String callback) {
        this.callback = callback;
    }
    
    // 保持兼容性的方法
    public String getUploadUrl() {
        return uploadUrl != null ? uploadUrl : endpoint;
    }
    
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
    
    public String getFileKey() {
        return fileKey != null ? fileKey : key;
    }
    
    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
    
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }
    
    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
    
    public Long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}