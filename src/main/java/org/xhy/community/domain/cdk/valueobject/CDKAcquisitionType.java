package org.xhy.community.domain.cdk.valueobject;

/**
 * CDK获得方式枚举
 */
public enum CDKAcquisitionType {
    PURCHASE("购买获得"),
    GIFT("赠送获得");

    private final String description;

    CDKAcquisitionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}