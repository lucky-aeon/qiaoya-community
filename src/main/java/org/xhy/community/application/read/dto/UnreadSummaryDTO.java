package org.xhy.community.application.read.dto;

public class UnreadSummaryDTO {
    private Long postsUnread;
    private Long questionsUnread;
    private Long chaptersUnread;

    public Long getPostsUnread() {
        return postsUnread;
    }

    public void setPostsUnread(Long postsUnread) {
        this.postsUnread = postsUnread;
    }

    public Long getQuestionsUnread() {
        return questionsUnread;
    }

    public void setQuestionsUnread(Long questionsUnread) {
        this.questionsUnread = questionsUnread;
    }

    public Long getChaptersUnread() {
        return chaptersUnread;
    }

    public void setChaptersUnread(Long chaptersUnread) {
        this.chaptersUnread = chaptersUnread;
    }
}
