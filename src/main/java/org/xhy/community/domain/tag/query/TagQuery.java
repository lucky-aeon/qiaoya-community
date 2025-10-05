package org.xhy.community.domain.tag.query;

import org.xhy.community.domain.common.query.BasePageQuery;

/**
 * 标签定义查询对象（包含分页）
 */
public class TagQuery extends BasePageQuery {
    private String name;
    private String category;
    private Boolean enabled;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
