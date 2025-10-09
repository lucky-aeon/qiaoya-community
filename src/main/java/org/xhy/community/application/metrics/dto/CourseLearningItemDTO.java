package org.xhy.community.application.metrics.dto;

/**
 * 课程学习人数条目DTO（当前周期：天/周/月）
 */
public class CourseLearningItemDTO {

    private String courseId;
    private String courseTitle;
    private Long learners;

    public CourseLearningItemDTO() {}

    public CourseLearningItemDTO(String courseId, String courseTitle, Long learners) {
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.learners = learners;
    }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public Long getLearners() { return learners; }
    public void setLearners(Long learners) { this.learners = learners; }
}

