package org.xhy.community.interfaces.testimonial.request;

import org.xhy.community.interfaces.common.request.PageRequest;
import org.xhy.community.domain.testimonial.valueobject.TestimonialStatus;

public class QueryTestimonialRequest extends PageRequest {

    private TestimonialStatus status;

    public QueryTestimonialRequest() {
    }

    // Getters and Setters
    public TestimonialStatus getStatus() { return status; }
    public void setStatus(TestimonialStatus status) { this.status = status; }
}