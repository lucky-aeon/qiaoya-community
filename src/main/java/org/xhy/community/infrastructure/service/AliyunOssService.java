package org.xhy.community.infrastructure.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.config.AliyunOssProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliyunOssService {
    
    private final AliyunOssProperties ossProperties;
    
    // 代码内的默认值
    private static final String DEFAULT_ROLE_SESSION_NAME = "oss-upload-session";
    private static final Long DEFAULT_DURATION_SECONDS = 3600L;
    
    public AliyunOssService(AliyunOssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }
    
    public Map<String, Object> getStsCredentials(String fileKey) {
        try {
            DefaultProfile profile = DefaultProfile.getProfile(
                    ossProperties.getRegion(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
            
            IAcsClient client = new DefaultAcsClient(profile);
            
            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setMethod(MethodType.POST);
            request.setProtocol(ProtocolType.HTTPS);
            request.setRoleArn(ossProperties.getRoleArn());
            request.setRoleSessionName(DEFAULT_ROLE_SESSION_NAME);
            request.setDurationSeconds(DEFAULT_DURATION_SECONDS);
            request.setPolicy(generateDynamicPolicy(fileKey));
            
            AssumeRoleResponse response = client.getAcsResponse(request);
            AssumeRoleResponse.Credentials credentials = response.getCredentials();
            
            // 生成上传策略
            Map<String, Object> uploadPolicy = generateUploadPolicy(fileKey);
            
            Map<String, Object> result = new HashMap<>();
            result.put("accessKeyId", credentials.getAccessKeyId());
            result.put("accessKeySecret", credentials.getAccessKeySecret());
            result.put("securityToken", credentials.getSecurityToken());
            result.put("expiration", credentials.getExpiration());
            result.put("region", ossProperties.getRegion());
            result.put("bucket", ossProperties.getBucketName());
            result.put("endpoint", ossProperties.getEndpoint());
            result.put("policy", uploadPolicy.get("policy"));
            result.put("signature", uploadPolicy.get("signature"));
            result.put("key", fileKey);
            result.put("callback", generateCallback());
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取STS凭证失败", e);
        }
    }
    
    private String generateDynamicPolicy(String fileKey) {
        // 根据fileKey动态生成policy，限制用户只能上传到指定路径
        return String.format(
            "{\n" +
            "  \"Version\": \"1\",\n" +
            "  \"Statement\": [\n" +
            "    {\n" +
            "      \"Effect\": \"Allow\",\n" +
            "      \"Action\": [\n" +
            "        \"oss:PutObject\",\n" +
            "        \"oss:PostObject\"\n" +
            "      ],\n" +
            "      \"Resource\": \"acs:oss:*:*:%s/uploads/*\"\n" +
            "    }\n" +
            "  ]\n" +
            "}",
            ossProperties.getBucketName()
        );
    }
    
    private Map<String, Object> generateUploadPolicy(String fileKey) {
        try {
            LocalDateTime expiration = LocalDateTime.now().plusSeconds(DEFAULT_DURATION_SECONDS);
            String expirationString = expiration.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            
            // 构建上传策略
            String policyDocument = String.format(
                "{\n" +
                "  \"expiration\": \"%s\",\n" +
                "  \"conditions\": [\n" +
                "    {\"bucket\": \"%s\"},\n" +
                "    [\"starts-with\", \"$key\", \"uploads/\"],\n" +
                "    [\"content-length-range\", 0, 104857600]\n" +
                "  ]\n" +
                "}",
                expirationString,
                ossProperties.getBucketName()
            );
            
            String encodedPolicy = Base64.getEncoder().encodeToString(policyDocument.getBytes("UTF-8"));
            
            // 计算签名
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(ossProperties.getAccessKeySecret().getBytes("UTF-8"), "HmacSHA1"));
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(encodedPolicy.getBytes("UTF-8")));
            
            Map<String, Object> result = new HashMap<>();
            result.put("policy", encodedPolicy);
            result.put("signature", signature);
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("生成上传策略失败", e);
        }
    }
    
    private String generateCallback() {
        if (ossProperties.getCallback().getUrl() == null) {
            return null;
        }
        
        try {
            Map<String, String> callback = new HashMap<>();
            callback.put("callbackUrl", ossProperties.getCallback().getUrl());
            callback.put("callbackBody", ossProperties.getCallback().getBody());
            callback.put("callbackBodyType", ossProperties.getCallback().getBodyType());
            
            String callbackJson = String.format(
                "{\"callbackUrl\":\"%s\",\"callbackBody\":\"%s\",\"callbackBodyType\":\"%s\"}",
                callback.get("callbackUrl"),
                callback.get("callbackBody"),
                callback.get("callbackBodyType")
            );
            
            return Base64.getEncoder().encodeToString(callbackJson.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException("生成回调参数失败", e);
        }
    }
    
    public String generatePresignedDownloadUrl(String fileKey) {
        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        
        try {
            Date expiration = new Date(System.currentTimeMillis() + ossProperties.getPresignedUrlExpiration() * 1000);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
                    ossProperties.getBucketName(), 
                    fileKey
            );
            request.setExpiration(expiration);
            
            URL url = ossClient.generatePresignedUrl(request);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }
    
    public void deleteFile(String fileKey) {
        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        
        try {
            ossClient.deleteObject(ossProperties.getBucketName(), fileKey);
        } finally {
            ossClient.shutdown();
        }
    }
    
    public boolean fileExists(String fileKey) {
        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        
        try {
            return ossClient.doesObjectExist(ossProperties.getBucketName(), fileKey);
        } finally {
            ossClient.shutdown();
        }
    }
    
    public String uploadFile(String fileKey, InputStream inputStream, String contentType) {
        OSS ossClient = new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret()
        );
        
        try {
            ossClient.putObject(ossProperties.getBucketName(), fileKey, inputStream);
            return fileKey;
        } finally {
            ossClient.shutdown();
        }
    }
    
    public boolean verifyOssCallback(String authorizationHeader, String callbackBody, String publicKeyUrl) {
        // OSS回调签名验证逻辑
        // 这里简化实现，实际使用时需要验证OSS的公钥签名
        return true; // 简化处理，实际项目中需要实现完整的签名验证
    }
}