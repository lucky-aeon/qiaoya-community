package org.xhy.community.domain.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.repository.UserRepository;
import org.xhy.community.domain.user.valueobject.UserStatus;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserDomainServiceTest {
    
    @Autowired
    private UserDomainService userDomainService;
    
    @Autowired
    private UserRepository userRepository;
    
    private String testUserEmail;
    private String testUserPassword;
    

    @BeforeEach
    void setUp() {
        testUserEmail = "xhy@qq.com";
        testUserPassword = "123456";

    }
    
    @Test
    void testCreateUser_Success() {
        // When - 创建普通用户
        UserEntity result = userDomainService.registerUser(testUserEmail, testUserPassword);
        
        // Then - 验证用户信息
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testUserEmail, result.getEmail());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertTrue(result.isActive());
        assertFalse(result.getEmailNotificationEnabled());
        assertEquals(Integer.valueOf(5), result.getMaxConcurrentDevices());
        
        // 验证密码是否被正确加密
        assertNotNull(result.getPassword());
        assertNotEquals(testUserPassword, result.getPassword()); // 密码应该被加密
        assertTrue(userDomainService.verifyPassword(testUserPassword, result.getPassword()));
        
        // 验证数据库中是否保存
        UserEntity saved = userRepository.selectById(result.getId());
        assertNotNull(saved);
    }

}