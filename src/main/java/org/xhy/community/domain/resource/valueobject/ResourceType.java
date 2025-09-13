package org.xhy.community.domain.resource.valueobject;



public enum ResourceType {
    
    IMAGE("图片"),
    VIDEO("视频"),
    DOCUMENT("文档"),
    AUDIO("音频"),
    OTHER("其他");
    
    private final String description;
    
    ResourceType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ResourceType fromFileExtension(String extension) {
        if (extension == null) {
            return OTHER;
        }
        
        String ext = extension.toLowerCase();
        
        // 图片类型
        if (ext.matches("jpg|jpeg|png|gif|bmp|webp|svg")) {
            return IMAGE;
        }
        
        // 视频类型
        if (ext.matches("mp4|avi|mov|wmv|flv|webm|mkv")) {
            return VIDEO;
        }
        
        // 音频类型
        if (ext.matches("mp3|wav|flac|aac|ogg|wma")) {
            return AUDIO;
        }
        
        // 文档类型
        if (ext.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|rtf")) {
            return DOCUMENT;
        }
        
        return OTHER;
    }

    public static ResourceType fromCode(String code) {
        for (ResourceType resourceType : values()) {
            if (resourceType.name().equals(code)) {
                return resourceType;
            }
        }
        throw new IllegalArgumentException("Unknown post status code: " + code);
    }
}