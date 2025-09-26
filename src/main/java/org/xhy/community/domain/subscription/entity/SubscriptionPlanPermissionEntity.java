package org.xhy.community.domain.subscription.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("subscription_plan_permissions")
public class SubscriptionPlanPermissionEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String subscriptionPlanId;
    private String permissionCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public SubscriptionPlanPermissionEntity() {}

    public SubscriptionPlanPermissionEntity(String subscriptionPlanId, String permissionCode) {
        this.subscriptionPlanId = subscriptionPlanId;
        this.permissionCode = permissionCode;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubscriptionPlanId() { return subscriptionPlanId; }
    public void setSubscriptionPlanId(String subscriptionPlanId) { this.subscriptionPlanId = subscriptionPlanId; }

    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
