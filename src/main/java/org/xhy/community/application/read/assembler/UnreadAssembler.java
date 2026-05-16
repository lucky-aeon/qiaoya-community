package org.xhy.community.application.read.assembler;

import org.xhy.community.application.read.dto.UnreadSummaryDTO;

import java.util.List;

public class UnreadAssembler {

    private UnreadAssembler() {}

    public static UnreadSummaryDTO toDTO(Long postsUnread, Long questionsUnread, Long chaptersUnread) {
        UnreadSummaryDTO dto = new UnreadSummaryDTO();
        dto.setPostsUnread(postsUnread == null ? 0L : postsUnread);
        dto.setQuestionsUnread(questionsUnread == null ? 0L : questionsUnread);
        dto.setChaptersUnread(chaptersUnread == null ? 0L : chaptersUnread);
        return dto;
    }

    /**
     * 重载：包含聊天室未读聚合（方案A）。
     */
    public static UnreadSummaryDTO toDTO(Long postsUnread, Long questionsUnread, Long chaptersUnread, Long chatsUnread) {
        UnreadSummaryDTO dto = toDTO(postsUnread, questionsUnread, chaptersUnread);
        dto.setChatsUnread(chatsUnread == null ? 0L : chatsUnread);
        return dto;
    }

    public static UnreadSummaryDTO toDetailDTO(Long postsUnread,
                                               Long questionsUnread,
                                               Long chaptersUnread,
                                               Long chatsUnread,
                                               List<String> postIds,
                                               List<String> questionIds,
                                               List<String> chapterIds) {
        UnreadSummaryDTO dto = toDTO(postsUnread, questionsUnread, chaptersUnread, chatsUnread);
        dto.setPostIds(postIds == null ? List.of() : postIds);
        dto.setQuestionIds(questionIds == null ? List.of() : questionIds);
        dto.setChapterIds(chapterIds == null ? List.of() : chapterIds);
        return dto;
    }
}
