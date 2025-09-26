package org.xhy.community.domain.subscription.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("subscription_plan_menus")
public class SubscriptionPlanMenuEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String subscriptionPlanId;
    // 注意：列名为 menu_id，语义上作为“菜单码”使用
    private String menuId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public SubscriptionPlanMenuEntity() {}

    public SubscriptionPlanMenuEntity(String subscriptionPlanId, String menuCode) {
        this.subscriptionPlanId = subscriptionPlanId;
        this.menuId = menuCode;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubscriptionPlanId() { return subscriptionPlanId; }
    public void setSubscriptionPlanId(String subscriptionPlanId) { this.subscriptionPlanId = subscriptionPlanId; }

    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
