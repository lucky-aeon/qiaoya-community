package org.xhy.community.application.course.transcript;

public class TranscriptSubmitCommand {

    private String courseId;
    private String chapterId;
    private String resourceId;
    private String title;
    private String fileUrl;
    private String language;
    private String model;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
