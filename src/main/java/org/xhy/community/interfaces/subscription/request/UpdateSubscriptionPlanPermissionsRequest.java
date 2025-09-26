package org.xhy.community.interfaces.subscription.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class UpdateSubscriptionPlanPermissionsRequest {
    @NotNull
    private List<String> permissions;

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}

