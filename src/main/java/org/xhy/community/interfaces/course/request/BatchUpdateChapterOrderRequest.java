package org.xhy.community.interfaces.course.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BatchUpdateChapterOrderRequest {
    
    @NotNull(message = "章节ID列表不能为空")
    @Size(min = 1, message = "至少需要一个章节ID")
    private List<String> chapterIds;
    
    public BatchUpdateChapterOrderRequest() {
    }
    
    // Getters and Setters
    public List<String> getChapterIds() {
        return chapterIds;
    }
    
    public void setChapterIds(List<String> chapterIds) {
        this.chapterIds = chapterIds;
    }
}