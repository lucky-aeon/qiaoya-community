package org.xhy.community.interfaces.resource.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OssCallbackRequest {
    
    @NotBlank(message = "文件名不能为空")
    private String filename;
    
    @NotNull(message = "文件大小不能为空")
    @Positive(message = "文件大小必须为正数")
    private Long size;
    
    @NotBlank(message = "文件类型不能为空")
    private String mimeType;
    
    private Integer height;
    
    private Integer width;

    @NotBlank(message = "token不能为空")
    private String token;
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public Integer getHeight() {
        return height;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    public Integer getWidth() {
        return width;
    }
    
    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
