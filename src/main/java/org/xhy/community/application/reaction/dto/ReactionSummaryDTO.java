package org.xhy.community.application.reaction.dto;

import java.util.List;

public class ReactionSummaryDTO {

    private String reactionType; // code
    private Integer count;
    private Boolean userReacted;
    private List<ReactionUserDTO> users;

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public Boolean getUserReacted() { return userReacted; }
    public void setUserReacted(Boolean userReacted) { this.userReacted = userReacted; }

    public List<ReactionUserDTO> getUsers() { return users; }
    public void setUsers(List<ReactionUserDTO> users) { this.users = users; }
}

