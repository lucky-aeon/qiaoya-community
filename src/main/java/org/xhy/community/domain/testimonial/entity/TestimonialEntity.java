package org.xhy.community.domain.testimonial.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.testimonial.valueobject.TestimonialStatus;
import org.xhy.community.infrastructure.converter.TestimonialStatusConverter;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.TestimonialErrorCode;

@TableName("student_testimonials")
public class TestimonialEntity extends BaseEntity {

    /** 发布用户ID */
    private String userId;

    /** 评价内容 */
    private String content;

    /** 评分(1-5) */
    private Integer rating;

    /** 状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝，PUBLISHED-已发布 */
    @TableField(typeHandler = TestimonialStatusConverter.class)
    private TestimonialStatus status;

    /** 排序权重（数值越大排序越靠前） */
    private Integer sortOrder;

    public TestimonialEntity() {
    }

    public TestimonialEntity(String userId, String content, Integer rating) {
        this.userId = userId;
        this.content = content;
        this.rating = rating;
        this.status = TestimonialStatus.PENDING;
        this.sortOrder = 0;
    }

    public void approve() {
        this.status = TestimonialStatus.APPROVED;
    }

    public void reject() {
        this.status = TestimonialStatus.REJECTED;
    }

    public void publish() {
        this.status = TestimonialStatus.PUBLISHED;
    }

    public void updateContent(String content, Integer rating) {
        if (this.status == TestimonialStatus.PENDING || this.status == TestimonialStatus.REJECTED) {
            this.content = content;
            this.rating = rating;
            // 如果是拒绝状态修改后，重置为待审核状态
            if (this.status == TestimonialStatus.REJECTED) {
                this.status = TestimonialStatus.PENDING;
            }
        } else {
            throw new BusinessException(
                TestimonialErrorCode.TESTIMONIAL_NOT_MODIFIABLE
            );
        }
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder != null && sortOrder >= 0 ? sortOrder : 0;
    }

    public boolean isPending() {
        return TestimonialStatus.PENDING.equals(this.status);
    }

    public boolean isPublished() {
        return TestimonialStatus.PUBLISHED.equals(this.status);
    }

    public boolean canBeModified() {
        return TestimonialStatus.PENDING.equals(this.status) || TestimonialStatus.REJECTED.equals(this.status);
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public TestimonialStatus getStatus() { return status; }
    public void setStatus(TestimonialStatus status) { this.status = status; }

    public Integer getSortOrder() { return sortOrder; }
}
