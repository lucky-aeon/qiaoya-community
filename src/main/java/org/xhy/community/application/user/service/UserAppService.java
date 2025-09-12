package org.xhy.community.application.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xhy.community.application.user.assembler.UserAssembler;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;

@Service
public class UserAppService {
    
    private final UserDomainService userDomainService;
    
    @Autowired
    public UserAppService(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }
    
    public UserDTO login(String email, String password) {
        if (!userDomainService.authenticateUser(email, password)) {
            throw new IllegalArgumentException("邮箱或密码错误");
        }
        
        UserEntity user = userDomainService.getUserByEmail(email);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO register(String email, String emailVerificationCode, String password) {
        if (userDomainService.isEmailExists(email, null)) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }
        
        UserEntity user = userDomainService.registerUser(email, password);
        return UserAssembler.toDTO(user);
    }
}