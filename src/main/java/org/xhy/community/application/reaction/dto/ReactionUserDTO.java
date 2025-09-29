package org.xhy.community.application.reaction.dto;

public class ReactionUserDTO {
    private String userId;
    private String userName;
    private String userAvatar;

    public ReactionUserDTO() {}
    public ReactionUserDTO(String userId, String userName, String userAvatar) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
}

