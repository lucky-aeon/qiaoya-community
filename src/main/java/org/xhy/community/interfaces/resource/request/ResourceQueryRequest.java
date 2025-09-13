package org.xhy.community.interfaces.resource.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class ResourceQueryRequest extends PageRequest {
    
    private String resourceType;
    
    public ResourceQueryRequest() {
        super();
    }
    
    public ResourceQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}