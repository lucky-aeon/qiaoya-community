package org.xhy.community.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;

@Configuration
public class AwsConfig {
    
    private final AwsProperties awsProperties;
    
    public AwsConfig(AwsProperties awsProperties) {
        this.awsProperties = awsProperties;
    }
    
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsProperties.getS3().getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsProperties.getAccessKeyId(),
                                awsProperties.getSecretAccessKey()
                        )
                ))
                .build();
    }
    
    @Bean
    public StsClient stsClient() {
        return StsClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsProperties.getAccessKeyId(),
                                awsProperties.getSecretAccessKey()
                        )
                ))
                .build();
    }
}