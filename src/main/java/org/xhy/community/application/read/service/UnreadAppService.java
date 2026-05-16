package org.xhy.community.application.read.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.read.assembler.UnreadAssembler;
import org.xhy.community.application.read.dto.UnreadSummaryDTO;
import org.xhy.community.domain.common.valueobject.ReadChannel;
import org.xhy.community.domain.interview.service.InterviewQuestionDomainService;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.read.entity.UserLastSeenEntity;
import org.xhy.community.domain.read.service.ReadDomainService;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.chat.service.ChatRoomReadDomainService;
import org.xhy.community.domain.chat.service.ChatMessageDomainService;
import org.xhy.community.domain.chat.service.ChatRoomDomainService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UnreadAppService {

    private final ReadDomainService readDomainService;
    private final PostDomainService postDomainService;
    private final InterviewQuestionDomainService interviewQuestionDomainService;
    private final ChapterDomainService chapterDomainService;
    private final ChatRoomDomainService chatRoomDomainService;
    private final ChatRoomReadDomainService chatRoomReadDomainService;
    private final ChatMessageDomainService chatMessageDomainService;

    public UnreadAppService(ReadDomainService readDomainService,
                            PostDomainService postDomainService,
                            InterviewQuestionDomainService interviewQuestionDomainService,
                            ChapterDomainService chapterDomainService,
                            ChatRoomDomainService chatRoomDomainService,
                            ChatRoomReadDomainService chatRoomReadDomainService,
                            ChatMessageDomainService chatMessageDomainService) {
        this.readDomainService = readDomainService;
        this.postDomainService = postDomainService;
        this.interviewQuestionDomainService = interviewQuestionDomainService;
        this.chapterDomainService = chapterDomainService;
        this.chatRoomDomainService = chatRoomDomainService;
        this.chatRoomReadDomainService = chatRoomReadDomainService;
        this.chatMessageDomainService = chatMessageDomainService;
    }

    /**
     * 获取用户的未读汇总（文章/题目）。
     * 首次调用会初始化 lastSeen（默认清零策略）。
     */
    public UnreadSummaryDTO getUnreadSummary(String userId) {
        // 获取/初始化三个频道的 lastSeen
        UserLastSeenEntity postSeen = readDomainService.getOrInit(userId, ReadChannel.POSTS);
        UserLastSeenEntity questionSeen = readDomainService.getOrInit(userId, ReadChannel.QUESTIONS);
        UserLastSeenEntity chapterSeen = readDomainService.getOrInit(userId, ReadChannel.CHAPTERS);

        Long postsUnread = postDomainService.countPublishedSince(postSeen.getLastSeenAt());
        Long questionsUnread = interviewQuestionDomainService.countPublishedSince(questionSeen.getLastSeenAt());
        Long chaptersUnread = chapterDomainService.countSince(chapterSeen.getLastSeenAt());

        Long chatsUnread = getChatsUnread(userId);

        return UnreadAssembler.toDTO(postsUnread, questionsUnread, chaptersUnread, chatsUnread);
    }

    /**
     * 获取用户未读详情：聚合数量 + 最近未读内容 ID。
     * ID 列表有上限，主要服务首页、列表页的逐条“新”标识。
     */
    public UnreadSummaryDTO getUnreadDetails(String userId) {
        UserLastSeenEntity postSeen = readDomainService.getOrInit(userId, ReadChannel.POSTS);
        UserLastSeenEntity questionSeen = readDomainService.getOrInit(userId, ReadChannel.QUESTIONS);
        UserLastSeenEntity chapterSeen = readDomainService.getOrInit(userId, ReadChannel.CHAPTERS);

        Long postsUnread = postDomainService.countPublishedSince(postSeen.getLastSeenAt());
        Long questionsUnread = interviewQuestionDomainService.countPublishedSince(questionSeen.getLastSeenAt());
        Long chaptersUnread = chapterDomainService.countSince(chapterSeen.getLastSeenAt());
        Long chatsUnread = getChatsUnread(userId);

        List<String> postIds = postDomainService.listPublishedIdsSince(postSeen.getLastSeenAt(), 200);
        List<String> questionIds = interviewQuestionDomainService.listPublishedIdsSince(questionSeen.getLastSeenAt(), 200);
        List<String> chapterIds = chapterDomainService.listIdsSince(chapterSeen.getLastSeenAt(), 200);

        return UnreadAssembler.toDetailDTO(
                postsUnread,
                questionsUnread,
                chaptersUnread,
                chatsUnread,
                postIds,
                questionIds,
                chapterIds
        );
    }

    private Long getChatsUnread(String userId) {
        try {
            java.util.Set<String> joinedRoomIds = chatRoomDomainService.listJoinedRoomIdsByUser(userId);
            if (joinedRoomIds != null && !joinedRoomIds.isEmpty()) {
                java.util.Map<String, java.time.LocalDateTime> lastSeens = chatRoomReadDomainService.getLastSeenForRooms(userId, joinedRoomIds);
                java.util.Map<String, Long> unreadMap = chatMessageDomainService.countUnreadByRoomsForUser(joinedRoomIds, lastSeens, userId);
                long sum = 0L;
                if (unreadMap != null && !unreadMap.isEmpty()) {
                    for (Long v : unreadMap.values()) {
                        if (v != null) sum += v;
                    }
                }
                return sum;
            }
        } catch (Exception ignore) {
            // 聚合失败不影响其他频道，保持容错
        }
        return 0L;
    }

    /**
     * 进入具体频道列表后，更新 Last Seen（清零语义）。
     */
    public void visitChannel(String userId, ReadChannel channel) {
        LocalDateTime serverNow = LocalDateTime.now();
        readDomainService.updateLastSeen(userId, channel, serverNow);
    }
}
