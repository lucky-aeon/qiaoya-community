package org.xhy.community.infrastructure.transcript;

public class TranscriptSubmitCommand {

    private String model;
    private String fileUrl;
    private String language;
    private Boolean enableWords;
    private Boolean enableItn;

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Boolean getEnableWords() { return enableWords; }
    public void setEnableWords(Boolean enableWords) { this.enableWords = enableWords; }

    public Boolean getEnableItn() { return enableItn; }
    public void setEnableItn(Boolean enableItn) { this.enableItn = enableItn; }
}
