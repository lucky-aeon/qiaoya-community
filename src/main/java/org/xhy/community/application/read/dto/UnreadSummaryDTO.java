package org.xhy.community.application.read.dto;

import java.util.List;

public class UnreadSummaryDTO {
    private Long postsUnread;
    private Long questionsUnread;
    private Long chaptersUnread;
    // 新增：聊天室未读（按房间聚合后的总数）
    private Long chatsUnread;
    // 细粒度未读 ID，用于首页等列表逐条展示“新”标识
    private List<String> postIds;
    private List<String> questionIds;
    private List<String> chapterIds;

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

    public Long getChatsUnread() {
        return chatsUnread;
    }

    public void setChatsUnread(Long chatsUnread) {
        this.chatsUnread = chatsUnread;
    }

    public List<String> getPostIds() {
        return postIds;
    }

    public void setPostIds(List<String> postIds) {
        this.postIds = postIds;
    }

    public List<String> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(List<String> questionIds) {
        this.questionIds = questionIds;
    }

    public List<String> getChapterIds() {
        return chapterIds;
    }

    public void setChapterIds(List<String> chapterIds) {
        this.chapterIds = chapterIds;
    }
}
