package org.xhy.community.domain.config.valueobject;

public class DefaultSubscriptionConfig {

    private String subscriptionPlanId;
    private Integer validityMonths;
    private Boolean enabled;

    public DefaultSubscriptionConfig() {
    }

    public DefaultSubscriptionConfig(String subscriptionPlanId, Integer validityMonths, Boolean enabled) {
        this.subscriptionPlanId = subscriptionPlanId;
        this.validityMonths = validityMonths;
        this.enabled = enabled;
    }

    public boolean isValid() {
        return enabled != null && enabled &&
               subscriptionPlanId != null && !subscriptionPlanId.trim().isEmpty() &&
               validityMonths != null && validityMonths > 0;
    }

    public String getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public void setSubscriptionPlanId(String subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }

    public Integer getValidityMonths() {
        return validityMonths;
    }

    public void setValidityMonths(Integer validityMonths) {
        this.validityMonths = validityMonths;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}