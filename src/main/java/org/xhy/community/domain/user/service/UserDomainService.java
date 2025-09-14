package org.xhy.community.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.UserErrorCode;
import org.xhy.community.domain.user.repository.UserRepository;
import org.xhy.community.domain.user.valueobject.UserStatus;

import java.util.Random;

@Service
public class UserDomainService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserDomainService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String encryptPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    public boolean verifyPassword(String plainPassword, String encryptedPassword) {
        return passwordEncoder.matches(plainPassword, encryptedPassword);
    }

    public UserEntity createUser(String name, String email, String password) {
        String encryptedPassword = encryptPassword(password);
        UserEntity user = new UserEntity(name, email.trim().toLowerCase(), encryptedPassword);
        userRepository.insert(user);
        return user;
    }


    public UserEntity registerUser(String email, String password) {
        String defaultNickname = generateDefaultNickname();
        return createUser(defaultNickname, email, password);
    }

    private String generateDefaultNickname() {
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000);
        return "敲鸭-" + randomNumber;
    }

    public UserEntity updateUserProfile(String userId, String name, String description, String avatar) {
        UserEntity user = getUserById(userId);
        user.updateProfile(name, description, avatar);
        userRepository.updateById(user);
        return user;
    }

    public UserEntity changeUserEmail(String userId, String newEmail) {
        UserEntity user = getUserById(userId);
        user.changeEmail(newEmail.trim().toLowerCase());
        userRepository.updateById(user);
        return user;
    }

    public UserEntity changeUserPassword(String userId, String oldPassword, String newPassword) {
        UserEntity user = getUserById(userId);
        if (!verifyPassword(oldPassword, user.getPassword())) {
            throw new BusinessException(UserErrorCode.WRONG_PASSWORD);
        }

        String encryptedPassword = encryptPassword(newPassword);
        user.changePassword(encryptedPassword);
        userRepository.updateById(user);
        return user;
    }

    public UserEntity updateUserStatus(String userId, UserStatus status) {
        UserEntity user = getUserById(userId);
        switch (status) {
            case ACTIVE -> user.activate();
            case INACTIVE -> user.deactivate();
            case BANNED -> user.ban();
        }
        userRepository.updateById(user);
        return user;
    }

    public UserEntity updateUserSettings(String userId, Boolean emailNotificationEnabled, Integer maxConcurrentDevices) {
        UserEntity user = getUserById(userId);
        if (emailNotificationEnabled != null) {
            user.updateEmailNotificationSettings(emailNotificationEnabled);
        }
        if (maxConcurrentDevices != null) {
            user.updateMaxConcurrentDevices(maxConcurrentDevices);
        }
        userRepository.updateById(user);
        return user;
    }

    public void deleteUser(String userId) {
        getUserById(userId); // 验证用户存在
        userRepository.deleteById(userId);
    }

    public UserEntity getUserById(String userId) {
        UserEntity user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public UserEntity getUserByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        UserEntity user = userRepository.selectOne(
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, normalizedEmail)
        );
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public IPage<UserEntity> getUsersByStatus(UserStatus status, int pageNum, int pageSize) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        return userRepository.selectPage(page,
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getStatus, status)
                .orderByDesc(UserEntity::getCreateTime));
    }

    public IPage<UserEntity> searchUsersByName(String name, int pageNum, int pageSize) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        return userRepository.selectPage(page,
            new LambdaQueryWrapper<UserEntity>()
                .like(UserEntity::getName, name)
                .orderByDesc(UserEntity::getCreateTime));
    }

    public IPage<UserEntity> getAllUsers(int pageNum, int pageSize) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        return userRepository.selectPage(page,
            new LambdaQueryWrapper<UserEntity>()
                .orderByDesc(UserEntity::getCreateTime));
    }

    public boolean authenticateUser(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        UserEntity user = userRepository.selectOne(
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, normalizedEmail)
        );
        if (user == null) {
            return false;
        }
        return user.isActive() && verifyPassword(password, user.getPassword());
    }
    
    public boolean isEmailExists(String email, String excludeUserId) {
        String normalizedEmail = email.trim().toLowerCase();
        
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, normalizedEmail)
                .ne(excludeUserId != null, UserEntity::getId, excludeUserId);
        
        return userRepository.exists(queryWrapper);
    }
    
    public java.util.List<UserEntity> getUsersByIds(java.util.Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userRepository.selectBatchIds(userIds);
    }
    
    public java.util.Map<String, String> getUserNameMapByIds(java.util.Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        
        java.util.List<UserEntity> users = userRepository.selectBatchIds(userIds);
        return users.stream()
                .collect(java.util.stream.Collectors.toMap(
                    UserEntity::getId,
                    UserEntity::getName
                ));
    }
}