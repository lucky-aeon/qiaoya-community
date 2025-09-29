package org.xhy.community.interfaces.expression.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateExpressionRequest {

    @NotBlank(message = "code 不能为空")
    @Size(max = 50, message = "code 最长 50 字符")
    private String code;

    @NotBlank(message = "name 不能为空")
    @Size(max = 100, message = "name 最长 100 字符")
    private String name;

    @NotBlank(message = "imageUrl 不能为空")
    @Size(max = 255, message = "imageUrl 最长 255 字符")
    private String imageUrl;

    private Integer sortOrder;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}

