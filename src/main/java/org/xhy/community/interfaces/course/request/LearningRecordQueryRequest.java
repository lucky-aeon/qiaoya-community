package org.xhy.community.interfaces.course.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class LearningRecordQueryRequest extends PageRequest {
    public LearningRecordQueryRequest() { super(); }
    public LearningRecordQueryRequest(Integer pageNum, Integer pageSize) { super(pageNum, pageSize); }
}

