package org.xhy.community.application.resource.dto;

import java.time.LocalDateTime;

public class UploadCredentialsDTO {
    
    private String uploadUrl;
    private String fileKey;
    private String bucket;
    private LocalDateTime expiration;
    private Long maxFileSize;
    
    public String getUploadUrl() {
        return uploadUrl;
    }
    
    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
    
    public String getFileKey() {
        return fileKey;
    }
    
    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
    
    public String getBucket() {
        return bucket;
    }
    
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
    
    public LocalDateTime getExpiration() {
        return expiration;
    }
    
    public void setExpiration(LocalDateTime expiration) {
        this.expiration = expiration;
    }
    
    public Long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(Long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
}