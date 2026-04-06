package org.xhy.community.domain.config.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IndependentServiceConfig {

    private String serviceCode;
    private boolean enabled = true;
    private boolean visibleInHome = true;
    private Integer sortOrder = 0;
    private String title;
    private String price;
    private String priceUnit;
    private String summary;
    private String description;
    private List<String> highlights = new ArrayList<>();
    private String ctaText;
    private String wechatNumber;
    private String wechatTip;
    private List<String> serviceProcess = new ArrayList<>();
    private List<String> targetUsers = new ArrayList<>();
    private List<String> topics = new ArrayList<>();
    private List<String> notes = new ArrayList<>();

    public static IndependentServiceConfig defaultMockInterview() {
        IndependentServiceConfig config = new IndependentServiceConfig();
        config.setServiceCode("MOCK_INTERVIEW");
        config.setEnabled(true);
        config.setVisibleInHome(true);
        config.setSortOrder(0);
        config.setTitle("模拟面试");
        config.setPrice("150");
        config.setPriceUnit("/h");
        config.setSummary("一对一模拟面试，先微信沟通后安排时间");
        config.setHighlights(new ArrayList<>(List.of(
                "一对一模拟面试",
                "微信沟通后安排时间",
                "针对岗位和目标定制问题"
        )));
        config.setCtaText("加微信咨询");
        return config;
    }

    public IndependentServiceConfig normalizedCopy() {
        IndependentServiceConfig copy = new IndependentServiceConfig();
        copy.setServiceCode(trimToNull(serviceCode));
        copy.setEnabled(enabled);
        copy.setVisibleInHome(visibleInHome);
        copy.setSortOrder(sortOrder == null ? 0 : Math.max(sortOrder, 0));
        copy.setTitle(trimToNull(title));
        copy.setPrice(trimToNull(price));
        copy.setPriceUnit(trimToNull(priceUnit));
        copy.setSummary(trimToNull(summary));
        copy.setDescription(trimToNull(description));
        copy.setHighlights(normalizeTextList(highlights));
        copy.setCtaText(trimToNull(ctaText));
        copy.setWechatNumber(trimToNull(wechatNumber));
        copy.setWechatTip(trimToNull(wechatTip));
        copy.setServiceProcess(normalizeTextList(serviceProcess));
        copy.setTargetUsers(normalizeTextList(targetUsers));
        copy.setTopics(normalizeTextList(topics));
        copy.setNotes(normalizeTextList(notes));
        return copy;
    }

    public boolean isValid() {
        return StringUtils.hasText(serviceCode)
                && StringUtils.hasText(title)
                && StringUtils.hasText(price)
                && StringUtils.hasText(priceUnit)
                && StringUtils.hasText(summary)
                && hasTextItems(highlights)
                && sortOrder != null
                && sortOrder >= 0;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static List<String> normalizeTextList(List<String> source) {
        List<String> result = new ArrayList<>();
        if (source == null) {
            return result;
        }
        for (String item : source) {
            String trimmed = trimToNull(item);
            if (trimmed != null) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private static boolean hasTextItems(List<String> items) {
        return items != null && !items.isEmpty();
    }

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
