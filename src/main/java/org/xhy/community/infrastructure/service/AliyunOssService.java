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
import org.xhy.community.infrastructure.exception.SystemException;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.ResourceErrorCode;
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

    /**
     * 统一标准化 Key：去除所有前导斜杠
     */
    private static String sanitizeKey(String key) {
        if (key == null) return "";
        String k = key.trim();
        while (k.startsWith("/")) {
            k = k.substring(1);
        }
        return k;
    }
    
    public Map<String, Object> getStsCredentials(String fileKey) {
        try {
            // 统一标准化：OSS 对象名不能以 '/' 开头
            String sanitizedKey = sanitizeKey(fileKey);

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
            // 会话策略：授权到具体对象（bucket/object），避免隐式拒绝
            request.setPolicy(generateDynamicPolicy(sanitizedKey));
            
            AssumeRoleResponse response = client.getAcsResponse(request);
            AssumeRoleResponse.Credentials credentials = response.getCredentials();
            
            // 生成上传策略 - 使用STS临时AccessKeySecret计算签名
            Map<String, Object> uploadPolicy = generateUploadPolicy(sanitizedKey, credentials.getAccessKeySecret());
            
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
            result.put("key", sanitizedKey);
            result.put("callback", generateCallback());
            
            return result;
        } catch (Exception e) {
            throw new SystemException(ResourceErrorCode.STS_CREDENTIALS_FAILED, e.getMessage(), e);
        }
    }
    
    /**
     * 生成 STS 会话策略：授权到具体对象（或可改为用户前缀）
     * Resource 格式必须是：acs:oss:*:*:bucket/object
     */
    private String generateDynamicPolicy(String objectKey) {
        String sanitizedKey = sanitizeKey(objectKey);
        String resourceExact = String.format("acs:oss:*:*:%s/%s", ossProperties.getBucketName(), sanitizedKey);
        return "{\n" +
               "  \"Version\": \"1\",\n" +
               "  \"Statement\": [\n" +
               "    {\n" +
               "      \"Effect\": \"Allow\",\n" +
               "      \"Action\": [\n" +
               "        \"oss:PutObject\",\n" +
               "        \"oss:PostObject\"\n" +
               "      ],\n" +
               "      \"Resource\": \"" + resourceExact + "\"\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
    
    private Map<String, Object> generateUploadPolicy(String fileKey, String tempAccessKeySecret) {
        try {
            String sanitizedKey = sanitizeKey(fileKey);
            LocalDateTime expiration = LocalDateTime.now().plusSeconds(DEFAULT_DURATION_SECONDS);
            String expirationString = expiration.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            
            // 构建上传策略（表单策略）：与前端提交的 key 完全一致
            String policyDocument = String.format(
                "{\n" +
                "  \"expiration\": \"%s\",\n" +
                "  \"conditions\": [\n" +
                "    {\"bucket\": \"%s\"},\n" +
                "    [\"eq\", \"$key\", \"%s\"],\n" +
                "    [\"content-length-range\", 0, 104857600]\n" +
                "  ]\n" +
                "}",
                expirationString,
                ossProperties.getBucketName(),
                sanitizedKey
            );
            
            String encodedPolicy = Base64.getEncoder().encodeToString(policyDocument.getBytes("UTF-8"));
            
            // 计算签名 - 使用STS临时AccessKeySecret
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(tempAccessKeySecret.getBytes("UTF-8"), "HmacSHA1"));
            String signature = Base64.getEncoder().encodeToString(mac.doFinal(encodedPolicy.getBytes("UTF-8")));
            
            Map<String, Object> result = new HashMap<>();
            result.put("policy", encodedPolicy);
            result.put("signature", signature);
            
            return result;
        } catch (Exception e) {
            throw new SystemException(ResourceErrorCode.UPLOAD_POLICY_GENERATION_FAILED, e.getMessage(), e);
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
            throw new SystemException(ResourceErrorCode.CALLBACK_PARAM_GENERATION_FAILED, e.getMessage(), e);
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
                    sanitizeKey(fileKey)
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
            ossClient.deleteObject(ossProperties.getBucketName(), sanitizeKey(fileKey));
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
            return ossClient.doesObjectExist(ossProperties.getBucketName(), sanitizeKey(fileKey));
        } finally {
            ossClient.shutdown();
        }
    }
}
