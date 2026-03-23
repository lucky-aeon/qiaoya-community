package org.xhy.community.interfaces.skill.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class SkillQueryRequest extends PageRequest {

    @Override
    public void setPageNum(Integer pageNum) {
        if (pageNum == null || pageNum < 1) {
            super.setPageNum(1);
            return;
        }
        super.setPageNum(pageNum);
    }

    @Override
    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            super.setPageSize(10);
            return;
        }
        super.setPageSize(Math.min(pageSize, 50));
    }
}
