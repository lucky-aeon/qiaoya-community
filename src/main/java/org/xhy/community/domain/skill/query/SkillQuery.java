package org.xhy.community.domain.skill.query;

import org.xhy.community.domain.common.query.BasePageQuery;

public class SkillQuery extends BasePageQuery {

    private String userId;

    private String keyword;

    public SkillQuery() {
    }

    public SkillQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
