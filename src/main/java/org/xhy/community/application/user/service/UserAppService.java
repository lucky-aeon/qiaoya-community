package org.xhy.community.application.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xhy.community.application.user.assembler.UserAssembler;
import org.xhy.community.application.user.dto.LoginResponseDTO;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.config.JwtUtil;

@Service
public class UserAppService {
    
    private final UserDomainService userDomainService;
    private final JwtUtil jwtUtil;
    
    @Autowired
    public UserAppService(UserDomainService userDomainService, JwtUtil jwtUtil) {
        this.userDomainService = userDomainService;
        this.jwtUtil = jwtUtil;
    }
    
    public LoginResponseDTO login(String email, String password) {
        if (!userDomainService.authenticateUser(email, password)) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }
        
        UserEntity user = userDomainService.getUserByEmail(email);
        UserDTO userDTO = UserAssembler.toDTO(user);
        
        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        
        return new LoginResponseDTO(token, userDTO);
    }
    
    public UserDTO register(String email, String emailVerificationCode, String password) {
        if (userDomainService.isEmailExists(email, null)) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }
        
        UserEntity user = userDomainService.registerUser(email, password);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO updateProfile(String userId, String description) {
        UserEntity user = userDomainService.updateUserProfile(userId, null, description, null);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO changePassword(String userId, String oldPassword, String newPassword) {
        UserEntity user = userDomainService.changeUserPassword(userId, oldPassword, newPassword);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO updateEmailNotification(String userId, Boolean emailNotificationEnabled) {
        UserEntity user = userDomainService.updateUserSettings(userId, emailNotificationEnabled, null);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO toggleEmailNotification(String userId) {
        UserEntity currentUser = userDomainService.getUserById(userId);
        Boolean currentSetting = currentUser.getEmailNotificationEnabled();
        Boolean newSetting = !Boolean.TRUE.equals(currentSetting);
        
        UserEntity user = userDomainService.updateUserSettings(userId, newSetting, null);
        return UserAssembler.toDTO(user);
    }
}