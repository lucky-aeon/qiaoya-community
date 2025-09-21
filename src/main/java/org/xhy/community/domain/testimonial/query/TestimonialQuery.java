package org.xhy.community.domain.testimonial.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.testimonial.valueobject.TestimonialStatus;

public class TestimonialQuery extends BasePageQuery {

    private TestimonialStatus status;
    private AccessLevel accessLevel;
    private String currentUserId;

    public TestimonialQuery() {
    }

    public TestimonialQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    public TestimonialStatus getStatus() {
        return status;
    }

    public void setStatus(TestimonialStatus status) {
        this.status = status;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }
}