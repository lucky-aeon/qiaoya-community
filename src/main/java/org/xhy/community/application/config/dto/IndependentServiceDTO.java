package org.xhy.community.application.config.dto;

import java.util.List;

public class IndependentServiceDTO {

    private String serviceCode;
    private boolean enabled;
    private boolean visibleInHome;
    private Integer sortOrder;
    private String title;
    private String price;
    private String priceUnit;
    private String summary;
    private String description;
    private List<String> highlights;
    private String ctaText;
    private String wechatNumber;
    private String wechatTip;
    private List<String> serviceProcess;
    private List<String> targetUsers;
    private List<String> topics;
    private List<String> notes;

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isVisibleInHome() {
        return visibleInHome;
    }

    public void setVisibleInHome(boolean visibleInHome) {
        this.visibleInHome = visibleInHome;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getHighlights() {
        return highlights;
    }

    public void setHighlights(List<String> highlights) {
        this.highlights = highlights;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public String getWechatNumber() {
        return wechatNumber;
    }

    public void setWechatNumber(String wechatNumber) {
        this.wechatNumber = wechatNumber;
    }

    public String getWechatTip() {
        return wechatTip;
    }

    public void setWechatTip(String wechatTip) {
        this.wechatTip = wechatTip;
    }

    public List<String> getServiceProcess() {
        return serviceProcess;
    }

    public void setServiceProcess(List<String> serviceProcess) {
        this.serviceProcess = serviceProcess;
    }

    public List<String> getTargetUsers() {
        return targetUsers;
    }

    public void setTargetUsers(List<String> targetUsers) {
        this.targetUsers = targetUsers;
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }
}
