package org.xhy.community.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.repository.UserRepository;
import org.xhy.community.domain.user.valueobject.UserStatus;

@Service
public class UserDomainService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
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
    
    public UserEntity updateUserProfile(Long userId, String name, String description, String avatar) {
        UserEntity user = getUserById(userId);
        user.updateProfile(name, description, avatar);
        userRepository.updateById(user);
        return user;
    }
    
    public UserEntity changeUserEmail(Long userId, String newEmail) {
        UserEntity user = getUserById(userId);
        user.changeEmail(newEmail.trim().toLowerCase());
        userRepository.updateById(user);
        return user;
    }
    
    public UserEntity changeUserPassword(Long userId, String oldPassword, String newPassword) {
        UserEntity user = getUserById(userId);
        if (!verifyPassword(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        
        String encryptedPassword = encryptPassword(newPassword);
        user.changePassword(encryptedPassword);
        userRepository.updateById(user);
        return user;
    }
    
    public UserEntity updateUserStatus(Long userId, UserStatus status) {
        UserEntity user = getUserById(userId);
        switch (status) {
            case ACTIVE -> user.activate();
            case INACTIVE -> user.deactivate();
            case BANNED -> user.ban();
        }
        userRepository.updateById(user);
        return user;
    }
    
    public UserEntity updateUserSettings(Long userId, Boolean subscribeExternalMessages, Integer maxConcurrentDevices) {
        UserEntity user = getUserById(userId);
        if (subscribeExternalMessages != null) {
            user.updateSubscriptionSettings(subscribeExternalMessages);
        }
        if (maxConcurrentDevices != null) {
            user.updateMaxConcurrentDevices(maxConcurrentDevices);
        }
        userRepository.updateById(user);
        return user;
    }
    
    public void deleteUser(Long userId) {
        getUserById(userId); // 验证用户存在
        userRepository.deleteById(userId);
    }
    
    public UserEntity getUserById(Long userId) {
        UserEntity user = userRepository.selectById(userId);
        if (user == null || user.getDeleted()) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }
    
    public UserEntity getUserByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        UserEntity user = userRepository.selectOne(
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, normalizedEmail)
                .eq(UserEntity::getDeleted, false)
        );
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }
    
    public IPage<UserEntity> getUsersByStatus(UserStatus status, int pageNum, int pageSize) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        return userRepository.selectPage(page,
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getStatus, status)
                .eq(UserEntity::getDeleted, false)
                .orderByDesc(UserEntity::getCreateTime));
    }
    
    public IPage<UserEntity> searchUsersByName(String name, int pageNum, int pageSize) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        return userRepository.selectPage(page,
            new LambdaQueryWrapper<UserEntity>()
                .like(UserEntity::getName, name)
                .eq(UserEntity::getDeleted, false)
                .orderByDesc(UserEntity::getCreateTime));
    }
    
    public IPage<UserEntity> getAllUsers(int pageNum, int pageSize) {
        Page<UserEntity> page = new Page<>(pageNum, pageSize);
        return userRepository.selectPage(page, 
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getDeleted, false)
                .orderByDesc(UserEntity::getCreateTime));
    }
    
    public boolean authenticateUser(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        UserEntity user = userRepository.selectOne(
            new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, normalizedEmail)
                .eq(UserEntity::getDeleted, false)
        );
        if (user == null) {
            return false;
        }
        return user.isActive() && verifyPassword(password, user.getPassword());
    }
    
    public boolean isEmailExists(String email, Long excludeUserId) {
        String normalizedEmail = email.trim().toLowerCase();
        
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, normalizedEmail)
                .eq(UserEntity::getDeleted, false);
        
        if (excludeUserId != null) {
            queryWrapper.ne(UserEntity::getId, excludeUserId);
        }
        
        return userRepository.exists(queryWrapper);
    }
}