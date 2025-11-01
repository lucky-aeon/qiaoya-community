package org.xhy.community.interfaces.user.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 管理员封禁IP请求
 */
public class BanIpRequest {

    /** 需要封禁的IP地址 */
    @NotBlank(message = "IP地址不能为空")
    private String ip;

    /** 封禁天数，默认为7天，必须>=1 */
    @Min(value = 1, message = "封禁天数必须至少为1天")
    private Long ttlDays;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getTtlDays() {
        return ttlDays;
    }

    public void setTtlDays(Long ttlDays) {
        this.ttlDays = ttlDays;
    }
}

