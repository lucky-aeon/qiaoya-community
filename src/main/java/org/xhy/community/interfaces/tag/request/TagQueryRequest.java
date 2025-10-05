package org.xhy.community.interfaces.tag.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class TagQueryRequest extends PageRequest {
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

