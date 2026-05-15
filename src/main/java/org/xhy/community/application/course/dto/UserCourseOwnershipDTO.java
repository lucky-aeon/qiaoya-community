package org.xhy.community.application.course.dto;

import org.xhy.community.domain.course.valueobject.CourseStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前用户课程权益
 */
public class UserCourseOwnershipDTO {

    private String courseId;
    private String courseTitle;
    private String coverImage;
    private CourseStatus courseStatus;
    private String ownershipStatus;
    private LocalDateTime effectiveTime;
    private LocalDateTime expireTime;
    private boolean permanent;
    private List<OwnershipSourceDTO> sources;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public CourseStatus getCourseStatus() {
        return courseStatus;
    }

    public void setCourseStatus(CourseStatus courseStatus) {
        this.courseStatus = courseStatus;
    }

    public String getOwnershipStatus() {
        return ownershipStatus;
    }

    public void setOwnershipStatus(String ownershipStatus) {
        this.ownershipStatus = ownershipStatus;
    }

    public LocalDateTime getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(LocalDateTime effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isPermanent() {
        return permanent;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public List<OwnershipSourceDTO> getSources() {
        return sources;
    }

    public void setSources(List<OwnershipSourceDTO> sources) {
        this.sources = sources;
    }

    public static class OwnershipSourceDTO {
        private String sourceType;
        private String sourceRecordId;
        private String sourceBusinessId;
        private String sourceName;
        private String status;
        private LocalDateTime effectiveTime;
        private LocalDateTime expireTime;
        private boolean permanent;

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getSourceRecordId() {
            return sourceRecordId;
        }

        public void setSourceRecordId(String sourceRecordId) {
            this.sourceRecordId = sourceRecordId;
        }

        public String getSourceBusinessId() {
            return sourceBusinessId;
        }

        public void setSourceBusinessId(String sourceBusinessId) {
            this.sourceBusinessId = sourceBusinessId;
        }

        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getEffectiveTime() {
            return effectiveTime;
        }

        public void setEffectiveTime(LocalDateTime effectiveTime) {
            this.effectiveTime = effectiveTime;
        }

        public LocalDateTime getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(LocalDateTime expireTime) {
            this.expireTime = expireTime;
        }

        public boolean isPermanent() {
            return permanent;
        }

        public void setPermanent(boolean permanent) {
            this.permanent = permanent;
        }
    }
}
