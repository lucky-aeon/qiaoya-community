package org.xhy.community.domain.resource.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.domain.resource.entity.ResourceEntity;
import org.xhy.community.domain.resource.repository.ResourceRepository;
import org.xhy.community.domain.resource.valueobject.ResourceType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.ResourceErrorCode;
import org.xhy.community.infrastructure.service.S3Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ResourceDomainServiceTest {
    
    @Autowired
    private ResourceDomainService resourceDomainService;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private S3Service s3Service;
    
    private String testUserId;
    private String testFileName;
    private String testContentType;
    private byte[] testFileContent;
    private long testFileSize;
    
    @BeforeEach
    void setUp() {
        testUserId = "test-user-123";
        testFileName = "test-image.jpg";
        testContentType = "image/jpeg";
        testFileContent = "test file content for upload".getBytes();
        testFileSize = testFileContent.length;
        
        // 清理测试数据
        resourceRepository.selectList(null).forEach(resource -> {
            resourceRepository.deleteById(resource.getId());
        });
    }
    
    @Test
    void testUploadFile_Success() {
        // Given
        InputStream inputStream = new ByteArrayInputStream(testFileContent);
        
        // When
        ResourceEntity result = resourceDomainService.uploadFile(
                testUserId, testFileName, testContentType, testFileSize, inputStream);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testUserId, result.getUserId());
        assertEquals(testFileName, result.getOriginalName());
        assertEquals("jpg", result.getFormat());
        assertEquals(testFileSize, result.getSize());
        assertEquals(ResourceType.IMAGE, result.getResourceType());
        assertTrue(result.getFileKey().contains(testUserId));
        assertTrue(result.getFileKey().endsWith(".jpg"));
        
        // 验证数据库中是否保存
        ResourceEntity saved = resourceRepository.selectById(result.getId());
        assertNotNull(saved);
        assertEquals(result.getFileKey(), saved.getFileKey());
    }
    
    @Test
    void testGeneratePresignedUploadUrl_Success() {
        // When
        String presignedUrl = resourceDomainService.generatePresignedUploadUrl(
                testUserId, testFileName, testContentType);
        
        // Then
        assertNotNull(presignedUrl);
        assertTrue(presignedUrl.startsWith("https://") || presignedUrl.startsWith("http://"));
        System.out.println("Generated presigned URL: " + presignedUrl);
    }
    

    @Test
    void testGetResourceById_Success() {
        // Given - 先上传一个文件
        InputStream inputStream = new ByteArrayInputStream(testFileContent);
        ResourceEntity uploaded = resourceDomainService.uploadFile(
                testUserId, testFileName, testContentType, testFileSize, inputStream);
        
        // When
        ResourceEntity result = resourceDomainService.getResourceById(uploaded.getId());
        
        // Then
        assertNotNull(result);
        assertEquals(uploaded.getId(), result.getId());
        assertEquals(uploaded.getFileKey(), result.getFileKey());
    }
    
    @Test
    void testGetResourceById_NotFound() {
        // Given
        String nonExistentId = "non-existent-id";
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            resourceDomainService.getResourceById(nonExistentId);
        });
        
        assertEquals(ResourceErrorCode.RESOURCE_NOT_FOUND.getCode(), exception.getErrorCode().getCode());
    }
    
    @Test
    void testGetDownloadUrl_Success() {
        // Given - 先上传一个文件
        InputStream inputStream = new ByteArrayInputStream(testFileContent);
        ResourceEntity uploaded = resourceDomainService.uploadFile(
                testUserId, testFileName, testContentType, testFileSize, inputStream);
        
        // When
        String downloadUrl = resourceDomainService.getDownloadUrl(uploaded.getId());
        
        // Then
        assertNotNull(downloadUrl);
        assertTrue(downloadUrl.startsWith("https://") || downloadUrl.startsWith("http://"));
        System.out.println("Generated download URL: " + downloadUrl);
    }
    
    @Test
    void testGetUserResources_Success() {
        // Given - 上传多个文件
        String fileName1 = "image1.png";
        String fileName2 = "document.pdf";
        
        InputStream inputStream1 = new ByteArrayInputStream("content1".getBytes());
        InputStream inputStream2 = new ByteArrayInputStream("content2".getBytes());
        
        resourceDomainService.uploadFile(testUserId, fileName1, "image/png", 8, inputStream1);
        resourceDomainService.uploadFile(testUserId, fileName2, "application/pdf", 8, inputStream2);
        
        // When
        List<ResourceEntity> userResources = resourceDomainService.getUserResources(testUserId);
        
        // Then
        assertEquals(2, userResources.size());
        assertTrue(userResources.stream().allMatch(r -> r.getUserId().equals(testUserId)));
    }
    

    

    @Test
    void testResourceTypeFromFileExtension() {
        // Test image files
        assertEquals(ResourceType.IMAGE, ResourceType.fromFileExtension("jpg"));
        assertEquals(ResourceType.IMAGE, ResourceType.fromFileExtension("png"));
        assertEquals(ResourceType.IMAGE, ResourceType.fromFileExtension("gif"));
        
        // Test video files
        assertEquals(ResourceType.VIDEO, ResourceType.fromFileExtension("mp4"));
        assertEquals(ResourceType.VIDEO, ResourceType.fromFileExtension("avi"));
        
        // Test document files
        assertEquals(ResourceType.DOCUMENT, ResourceType.fromFileExtension("pdf"));
        assertEquals(ResourceType.DOCUMENT, ResourceType.fromFileExtension("doc"));
        
        // Test audio files
        assertEquals(ResourceType.AUDIO, ResourceType.fromFileExtension("mp3"));
        assertEquals(ResourceType.AUDIO, ResourceType.fromFileExtension("wav"));
        
        // Test unknown extension
        assertEquals(ResourceType.OTHER, ResourceType.fromFileExtension("unknown"));
        assertEquals(ResourceType.OTHER, ResourceType.fromFileExtension(null));
    }
    
    @Test
    void testFileKeyGeneration() {
        // Given
        InputStream inputStream = new ByteArrayInputStream(testFileContent);
        
        // When
        ResourceEntity result1 = resourceDomainService.uploadFile(
                testUserId, testFileName, testContentType, testFileSize, inputStream);
        
        InputStream inputStream2 = new ByteArrayInputStream(testFileContent);
        ResourceEntity result2 = resourceDomainService.uploadFile(
                testUserId, testFileName, testContentType, testFileSize, inputStream2);
        
        // Then - 每次生成的文件键应该不同
        assertNotEquals(result1.getFileKey(), result2.getFileKey());
        assertTrue(result1.getFileKey().contains(testUserId));
        assertTrue(result2.getFileKey().contains(testUserId));
        assertTrue(result1.getFileKey().contains("uploads/"));
        assertTrue(result2.getFileKey().contains("uploads/"));
    }
}