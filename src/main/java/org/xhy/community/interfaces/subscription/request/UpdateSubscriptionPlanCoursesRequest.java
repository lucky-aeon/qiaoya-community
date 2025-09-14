package org.xhy.community.interfaces.subscription.request;

import java.util.List;

/**
 * 更新套餐课程绑定请求
 * 
 * courseIds 字段说明：
 * - 传入包含课程ID的列表：全量替换为这些课程
 * - 传入空列表 [] 或 null：清空套餐的所有课程绑定
 */
public class UpdateSubscriptionPlanCoursesRequest {
    
    private List<String> courseIds;
    
    public UpdateSubscriptionPlanCoursesRequest() {
    }
    
    public List<String> getCourseIds() { return courseIds; }
    public void setCourseIds(List<String> courseIds) { this.courseIds = courseIds; }
}