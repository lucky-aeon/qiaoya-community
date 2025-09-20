package org.xhy.community.domain.config.valueobject;

public class DefaultSubscriptionConfig {

    private String subscriptionPlanId;

    public DefaultSubscriptionConfig() {
    }

    public DefaultSubscriptionConfig(String subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }

    public boolean isValid() {
        return subscriptionPlanId != null && !subscriptionPlanId.trim().isEmpty();
    }

    public String getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public void setSubscriptionPlanId(String subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }
}