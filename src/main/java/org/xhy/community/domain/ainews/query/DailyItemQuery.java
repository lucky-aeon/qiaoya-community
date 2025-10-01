package org.xhy.community.domain.ainews.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;

/**
 * AI 日报列表查询对象
 */
public class DailyItemQuery extends BasePageQuery {

    private String date;
    private Boolean withContent = Boolean.FALSE;
    private AccessLevel accessLevel;

    public DailyItemQuery() {
        super();
    }

    public DailyItemQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Boolean getWithContent() {
        return withContent;
    }

    public void setWithContent(Boolean withContent) {
        this.withContent = withContent;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }
}

