package org.xhy.community.domain.subscription.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.entity.BaseEntity;

@TableName("subscription_plan_courses")
public class SubscriptionPlanCourseEntity extends BaseEntity {
    
    private String subscriptionPlanId;
    
    private String courseId;
    
    public SubscriptionPlanCourseEntity() {
    }
    
    public SubscriptionPlanCourseEntity(String subscriptionPlanId, String courseId) {
        this.subscriptionPlanId = subscriptionPlanId;
        this.courseId = courseId;
    }
    
    public String getSubscriptionPlanId() { return subscriptionPlanId; }
    public void setSubscriptionPlanId(String subscriptionPlanId) { this.subscriptionPlanId = subscriptionPlanId; }
    
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
}