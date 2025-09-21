package org.xhy.community.interfaces.testimonial.request;

import jakarta.validation.constraints.NotNull;
import org.xhy.community.domain.testimonial.valueobject.TestimonialStatus;

public class ChangeStatusRequest {

    @NotNull(message = "状态不能为空")
    private TestimonialStatus status;

    public ChangeStatusRequest() {
    }

    // Getters and Setters
    public TestimonialStatus getStatus() { return status; }
    public void setStatus(TestimonialStatus status) { this.status = status; }
}