package org.xhy.community.domain.resource.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.resource.valueobject.ResourceType;

public class ResourceQuery extends BasePageQuery {
    
    private String userId;
    private ResourceType resourceType;

    public ResourceQuery() {
    }
    
    public ResourceQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public ResourceType getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

}