package org.xhy.community.interfaces.subscription.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class UpdateSubscriptionPlanMenusRequest {
    @NotNull
    private List<String> menus;

    public List<String> getMenus() { return menus; }
    public void setMenus(List<String> menus) { this.menus = menus; }
}

