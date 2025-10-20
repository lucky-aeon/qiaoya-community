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

import java.time.LocalDateTime;

@Service
public class UnreadAppService {

    private final ReadDomainService readDomainService;
    private final PostDomainService postDomainService;
    private final InterviewQuestionDomainService interviewQuestionDomainService;
    private final ChapterDomainService chapterDomainService;

    public UnreadAppService(ReadDomainService readDomainService,
                            PostDomainService postDomainService,
                            InterviewQuestionDomainService interviewQuestionDomainService,
                            ChapterDomainService chapterDomainService) {
        this.readDomainService = readDomainService;
        this.postDomainService = postDomainService;
        this.interviewQuestionDomainService = interviewQuestionDomainService;
        this.chapterDomainService = chapterDomainService;
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
        return UnreadAssembler.toDTO(postsUnread, questionsUnread, chaptersUnread);
    }

    /**
     * 进入具体频道列表后，更新 Last Seen（清零语义）。
     */
    public void visitChannel(String userId, ReadChannel channel) {
        LocalDateTime serverNow = LocalDateTime.now();
        readDomainService.updateLastSeen(userId, channel, serverNow);
    }
}
