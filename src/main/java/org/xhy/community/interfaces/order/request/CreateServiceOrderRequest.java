package org.xhy.community.interfaces.order.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateServiceOrderRequest {

    @NotBlank(message = "服务编码不能为空")
    private String serviceCode;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "金额必须大于0")
    private BigDecimal amount;

    private String remark;

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
