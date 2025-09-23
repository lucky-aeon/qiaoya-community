package org.xhy.community.application.subscription.dto;

import java.math.BigDecimal;
import java.util.List;

public class AppSubscriptionPlanDTO {

    private String id;

    private String name;

    private Integer level;

    private Integer validityMonths;

    private BigDecimal price;

    private List<String> benefits;

    public AppSubscriptionPlanDTO() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getValidityMonths() { return validityMonths; }
    public void setValidityMonths(Integer validityMonths) { this.validityMonths = validityMonths; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }
}