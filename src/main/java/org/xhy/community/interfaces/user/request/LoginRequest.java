package org.xhy.community.interfaces.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 设备标识（可选）。
     * 用于区分同一账号下的不同设备，配合并发设备数限制。
     * Web 端建议由前端在首次登录生成并持久化（localStorage/Cookie），
     * App 端建议存储在 Keychain/Keystore。
     */
     private String deviceId;
    
    public LoginRequest() {
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
