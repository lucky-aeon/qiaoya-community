package org.xhy.community.interfaces.expression.request;

import jakarta.validation.constraints.Size;

public class UpdateExpressionRequest extends CreateExpressionRequest {

    @Override
    @Size(max = 50, message = "code 最长 50 字符")
    public String getCode() { return super.getCode(); }
}

