package org.xhy.community.interfaces.expression.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class ExpressionQueryRequest extends PageRequest {

    private String code;
    private String name;
    private Boolean isActive;

    public ExpressionQueryRequest() {}

    public ExpressionQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

