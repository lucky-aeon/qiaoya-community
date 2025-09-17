package org.xhy.community.interfaces.user.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 更新用户设备数量请求
 * 管理员修改用户最大并发设备数量
 */
public class UpdateUserDevicesRequest {
    
    @NotNull(message = "设备数量不能为空")
    @Min(value = 1, message = "设备数量不能少于1")
    @Max(value = 10, message = "设备数量不能超过10")
    private Integer maxConcurrentDevices;
    
    public UpdateUserDevicesRequest() {
    }
    
    public UpdateUserDevicesRequest(Integer maxConcurrentDevices) {
        this.maxConcurrentDevices = maxConcurrentDevices;
    }
    
    // Getters and Setters
    public Integer getMaxConcurrentDevices() {
        return maxConcurrentDevices;
    }
    
    public void setMaxConcurrentDevices(Integer maxConcurrentDevices) {
        this.maxConcurrentDevices = maxConcurrentDevices;
    }
}