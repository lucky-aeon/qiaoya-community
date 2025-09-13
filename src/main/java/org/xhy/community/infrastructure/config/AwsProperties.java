package org.xhy.community.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {
    
    private String region;
    private String accessKeyId;
    private String secretAccessKey;
    private S3 s3 = new S3();
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getAccessKeyId() {
        return accessKeyId;
    }
    
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }
    
    public String getSecretAccessKey() {
        return secretAccessKey;
    }
    
    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }
    
    public S3 getS3() {
        return s3;
    }
    
    public void setS3(S3 s3) {
        this.s3 = s3;
    }
    
    public static class S3 {
        private String bucket;
        private String region;
        private String endpoint;
        private Long presignedUrlExpiration = 3600L; // 默认1小时
        
        public String getBucket() {
            return bucket;
        }
        
        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
        
        public String getRegion() {
            return region;
        }
        
        public void setRegion(String region) {
            this.region = region;
        }
        
        public String getEndpoint() {
            return endpoint;
        }
        
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public Long getPresignedUrlExpiration() {
            return presignedUrlExpiration;
        }
        
        public void setPresignedUrlExpiration(Long presignedUrlExpiration) {
            this.presignedUrlExpiration = presignedUrlExpiration;
        }
    }
}