package org.xhy.community.interfaces.interview.request;

import jakarta.validation.constraints.NotNull;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;

public class InterviewQuestionStatusRequest {
    @NotNull
    private ProblemStatus status;

    public ProblemStatus getStatus() { return status; }
    public void setStatus(ProblemStatus status) { this.status = status; }
}
