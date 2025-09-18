package org.xhy.community.domain.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

/**
 * 用户课程权限实体
 * 记录用户直接拥有的课程权限（永久有效）
 */
@TableName("user_courses")
public class UserCourseEntity extends BaseEntity {
    
    /** 用户ID */
    private String userId;
    
    /** 课程ID */
    private String courseId;
    
    // 默认构造函数
    public UserCourseEntity() {}
    
    // 带参构造函数
    public UserCourseEntity(String userId, String courseId) {
        this.userId = userId;
        this.courseId = courseId;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getCourseId() {
        return courseId;
    }
    
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}