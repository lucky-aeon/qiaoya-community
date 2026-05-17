package org.xhy.community.interfaces.course.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class CourseQueryRequest extends PageRequest {

    private Boolean archived;

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
