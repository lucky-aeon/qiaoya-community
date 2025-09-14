package org.xhy.community.application.subscription.dto;

public class CDKActivationResultDTO {
    
    private boolean success;
    private String message;
    private String targetName;
    private UserSubscriptionDTO subscription;
    
    public CDKActivationResultDTO() {
    }
    
    public CDKActivationResultDTO(boolean success, String message, String targetName, UserSubscriptionDTO subscription) {
        this.success = success;
        this.message = message;
        this.targetName = targetName;
        this.subscription = subscription;
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    
    public UserSubscriptionDTO getSubscription() { return subscription; }
    public void setSubscription(UserSubscriptionDTO subscription) { this.subscription = subscription; }
}