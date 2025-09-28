package org.xhy.community.domain.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.entity.UserCourseEntity;
import org.xhy.community.domain.user.event.UserRegisteredEvent;
import org.xhy.community.domain.user.query.UserQuery;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.UserErrorCode;
import org.xhy.community.domain.user.repository.UserRepository;
import org.xhy.community.domain.user.repository.UserCourseRepository;
import org.xhy.community.domain.user.valueobject.UserStatus;

import java.util.Collection;
import java.util.Random;
import java.util.List;

@Service
public class UserDomainService {

    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserDomainService(UserRepository userRepository,
                           UserCourseRepository userCourseRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userCourseRepository = userCourseRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
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

    public UserEntity createUser(String name, String email, String password, String avatar) {
        String encryptedPassword = encryptPassword(password);
        UserEntity user = new UserEntity(name, email.trim().toLowerCase(), encryptedPassword, avatar);
        userRepository.insert(user);
        return user;
    }


    public UserEntity registerUser(String email, String password) {
        String defaultNickname = generateDefaultNickname();
        String defaultAvatar = generateRandomAvatar();
        UserEntity user = createUser(defaultNickname, email, password, defaultAvatar);

        // 发布用户注册事件
        eventPublisher.publishEvent(new UserRegisteredEvent(this, user.getId(), user.getEmail()));

        return user;
    }

    private String generateDefaultNickname() {
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000);
        return "敲鸭-" + randomNumber;
    }

    private String generateRandomAvatar() {
        Random random = new Random();
        int avatarNumber = random.nextInt(19) + 1;
        return "/avatars/avatar_" + avatarNumber + ".png";
    }

    public UserEntity updateUserProfile(String userId, String name, String description, String avatar) {
        UserEntity user = getUserById(userId);
        user.updateProfile(name, description, avatar);
        userRepository.updateById(user);
        return user;
    }

    /**
     * 统一更新路径：使用实体进行资料更新
     * 仅合并非空字段
     */
    public UserEntity updateUserProfile(UserEntity patch) {
        UserEntity user = getUserById(patch.getId());
        user.updateProfile(patch.getName(), patch.getDescription(), patch.getAvatar());
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

    public UserEntity resetUserPassword(String userId, String newPassword) {
        UserEntity user = getUserById(userId);
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
        }
        userRepository.updateById(user);
        return user;
    }
    
    public UserEntity toggleUserStatus(String userId) {
        UserEntity user = getUserById(userId);
        user.toggleStatus();
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

    public IPage<UserEntity> queryUsers(UserQuery query) {
        Page<UserEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(query.getStatus() != null, UserEntity::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getName()), UserEntity::getName, query.getName())
                .like(StringUtils.hasText(query.getEmail()), UserEntity::getEmail, query.getEmail())
                .orderByDesc(UserEntity::getCreateTime);
        
        return userRepository.selectPage(page, queryWrapper);
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
        if (!user.isActive()){
            throw new BusinessException(UserErrorCode.USER_BANNED);
        }

        return verifyPassword(password, user.getPassword());
    }
    
    public boolean isEmailExists(String email, String excludeUserId) {
        String normalizedEmail = email.trim().toLowerCase();
        
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getEmail, normalizedEmail)
                .ne(excludeUserId != null, UserEntity::getId, excludeUserId);
        
        return userRepository.exists(queryWrapper);
    }
    

    public List<UserEntity> getUsersByIds(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return userRepository.selectBatchIds(userIds);
    }

    public java.util.Map<String, UserEntity> getUserEntityMapByIds(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        java.util.List<UserEntity> users = userRepository.selectBatchIds(userIds);
        return users.stream()
                .collect(java.util.stream.Collectors.toMap(
                    UserEntity::getId,
                    user -> user
                ));
    }

    /**
     * 批量查询用户邮箱通知设置
     *
     * @param userIds 用户ID集合
     * @return 用户ID到邮箱通知开关的映射
     */
    public java.util.Map<String, Boolean> getUserEmailSettingsByIds(java.util.Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }

        java.util.List<UserEntity> users = userRepository.selectBatchIds(userIds);
        return users.stream()
                .collect(java.util.stream.Collectors.toMap(
                    UserEntity::getId,
                    user -> user.getEmailNotificationEnabled() != null ? user.getEmailNotificationEnabled() : false
                ));
    }

    // ==================== 用户课程权限管理方法 ====================
    
    /**
     * 授予用户课程权限
     * 
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    public void grantCourseToUser(String userId, String courseId) {

        // 检查是否已存在权限，避免重复添加
        if (hasUserCourse(userId, courseId)) {
            return;
        }
        
        UserCourseEntity userCourse = new UserCourseEntity(userId, courseId);
        userCourseRepository.insert(userCourse);
    }
    
    /**
     * 检查用户是否拥有课程权限
     * 
     * @param userId 用户ID
     * @param courseId 课程ID
     * @return 是否拥有权限
     */
    public boolean hasUserCourse(String userId, String courseId) {
        LambdaQueryWrapper<UserCourseEntity> queryWrapper = 
            new LambdaQueryWrapper<UserCourseEntity>()
                .eq(UserCourseEntity::getUserId, userId)
                .eq(UserCourseEntity::getCourseId, courseId);
        
        return userCourseRepository.exists(queryWrapper);
    }
    
    /**
     * 获取用户拥有的所有课程ID列表
     * 
     * @param userId 用户ID
     * @return 课程ID列表
     */
    public List<String> getUserCourses(String userId) {
        LambdaQueryWrapper<UserCourseEntity> queryWrapper = 
            new LambdaQueryWrapper<UserCourseEntity>()
                .eq(UserCourseEntity::getUserId, userId)
                .select(UserCourseEntity::getCourseId);
        
        return userCourseRepository.selectObjs(queryWrapper)
                .stream()
                .map(Object::toString)
                .toList();
    }
    
    /**
     * 移除用户的课程权限
     * 
     * @param userId 用户ID
     * @param courseId 课程ID
     */
    public void removeUserCourse(String userId, String courseId) {
        LambdaQueryWrapper<UserCourseEntity> queryWrapper = 
            new LambdaQueryWrapper<UserCourseEntity>()
                .eq(UserCourseEntity::getUserId, userId)
                .eq(UserCourseEntity::getCourseId, courseId);
        
        userCourseRepository.delete(queryWrapper);
    }
    
    /**
     * 获取用户的所有课程权限实体
     *
     * @param userId 用户ID
     * @return 用户课程权限实体列表
     */
    public List<UserCourseEntity> getUserCourseEntities(String userId) {
        LambdaQueryWrapper<UserCourseEntity> queryWrapper =
            new LambdaQueryWrapper<UserCourseEntity>()
                .eq(UserCourseEntity::getUserId, userId)
                .orderByDesc(UserCourseEntity::getCreateTime);

        return userCourseRepository.selectList(queryWrapper);
    }

    /**
     * 获取社区总用户数
     *
     * @return 用户总数
     */
    public long getTotalUserCount() {
        return userRepository.selectCount(null);
    }
}
