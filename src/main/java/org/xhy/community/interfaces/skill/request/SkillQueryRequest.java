package org.xhy.community.interfaces.skill.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class SkillQueryRequest extends PageRequest {

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
