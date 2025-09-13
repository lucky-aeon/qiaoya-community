package org.xhy.community.infrastructure.service;

import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.config.AwsProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

@Service
public class S3Service {
    
    private final S3Client s3Client;
    private final StsClient stsClient;
    private final AwsProperties awsProperties;
    
    public S3Service(S3Client s3Client, StsClient stsClient, AwsProperties awsProperties) {
        this.s3Client = s3Client;
        this.stsClient = stsClient;
        this.awsProperties = awsProperties;
    }
    
    public String uploadFile(String fileKey, InputStream inputStream, String contentType, long contentLength) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(fileKey)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
        
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
        
        return fileKey;
    }
    
    public String generatePresignedUploadUrl(String fileKey, String contentType) {
        try (S3Presigner presigner = S3Presigner.create()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .key(fileKey)
                    .contentType(contentType)
                    .build();
            
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(awsProperties.getS3().getPresignedUrlExpiration()))
                    .putObjectRequest(putObjectRequest)
                    .build();
            
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            return presignedRequest.url().toString();
        }
    }
    
    public String generatePresignedDownloadUrl(String fileKey) {
        try (S3Presigner presigner = S3Presigner.create()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .key(fileKey)
                    .build();
            
            return presigner.presignGetObject(r -> r.signatureDuration(Duration.ofSeconds(awsProperties.getS3().getPresignedUrlExpiration()))
                    .getObjectRequest(getObjectRequest))
                    .url().toString();
        }
    }
    
    public void deleteFile(String fileKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(awsProperties.getS3().getBucket())
                .key(fileKey)
                .build();
        
        s3Client.deleteObject(deleteObjectRequest);
    }
    
    public boolean fileExists(String fileKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .key(fileKey)
                    .build();
            
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }
    
    public Map<String, String> getTemporaryCredentials(String roleArn, String sessionName, int durationSeconds) {
        AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                .roleArn(roleArn)
                .roleSessionName(sessionName)
                .durationSeconds(durationSeconds)
                .build();
        
        AssumeRoleResponse assumeRoleResponse = stsClient.assumeRole(assumeRoleRequest);
        Credentials credentials = assumeRoleResponse.credentials();
        
        return Map.of(
                "accessKeyId", credentials.accessKeyId(),
                "secretAccessKey", credentials.secretAccessKey(),
                "sessionToken", credentials.sessionToken(),
                "expiration", credentials.expiration().toString()
        );
    }
}