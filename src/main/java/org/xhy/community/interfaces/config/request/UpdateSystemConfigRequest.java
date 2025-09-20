package org.xhy.community.interfaces.config.request;

import jakarta.validation.constraints.NotNull;

public class UpdateSystemConfigRequest {

    @NotNull(message = "配置数据不能为空")
    private Object data;

    public UpdateSystemConfigRequest() {
    }

    public UpdateSystemConfigRequest(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}