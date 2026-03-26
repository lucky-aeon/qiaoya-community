package org.xhy.community.application.comment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xhy.community.application.comment.dto.CommentDTO;
import org.xhy.community.application.comment.dto.LatestCommentDTO;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.interview.entity.InterviewQuestionEntity;
import org.xhy.community.domain.interview.service.InterviewQuestionDomainService;
import org.xhy.community.domain.like.service.LikeDomainService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.service.SkillDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.interfaces.comment.request.CommentQueryRequest;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserCommentAppServiceUnitTest {

    private FakeCommentDomainService commentDomainService;
    private FakeUserDomainService userDomainService;
    private FakePostDomainService postDomainService;
    private FakeCourseDomainService courseDomainService;
    private FakeChapterDomainService chapterDomainService;
    private FakeLikeDomainService likeDomainService;
    private FakeInterviewQuestionDomainService interviewQuestionDomainService;
    private FakeSkillDomainService skillDomainService;
    private UserCommentAppService userCommentAppService;

    @BeforeEach
    void setUp() {
        commentDomainService = new FakeCommentDomainService();
        userDomainService = new FakeUserDomainService();
        postDomainService = new FakePostDomainService();
        courseDomainService = new FakeCourseDomainService();
        chapterDomainService = new FakeChapterDomainService();
        likeDomainService = new FakeLikeDomainService();
        interviewQuestionDomainService = new FakeInterviewQuestionDomainService();
        skillDomainService = new FakeSkillDomainService();
        userCommentAppService = new UserCommentAppService(
                commentDomainService,
                userDomainService,
                postDomainService,
                courseDomainService,
                chapterDomainService,
                likeDomainService,
                interviewQuestionDomainService,
                skillDomainService
        );
    }

    @Test
    void getLatestCommentsShouldMapSkillBusinessName() {
        CommentEntity comment = buildComment("comment-1", "skill-1", BusinessType.SKILL, "content-1", "user-1", null);
        commentDomainService.latestComments = List.of(comment);
        userDomainService.userMap = Map.of("user-1", buildUser("user-1", "Alice"));
        skillDomainService.skillTitleMap = Map.of("skill-1", "Skill Name");

        List<LatestCommentDTO> result = userCommentAppService.getLatestComments();

        assertEquals(1, result.size());
        assertEquals("Skill Name", result.get(0).getBusinessName());
        assertEquals("Alice", result.get(0).getCommentUserName());
        assertEquals(BusinessType.SKILL, result.get(0).getBusinessType());
    }

    @Test
    void getUserRelatedCommentsShouldIgnoreUnsupportedCollectionsGracefully() {
        CommentEntity skillComment = buildComment("comment-1", "skill-1", BusinessType.SKILL, "skill content", "user-1", null);
        CommentEntity meetingComment = buildComment("comment-2", "meeting-1", BusinessType.MEETING, "meeting content", "user-2", null);
        commentDomainService.relatedCommentsPage = pageOf(1, 10, List.of(skillComment, meetingComment));
        userDomainService.userMap = Map.of(
                "user-1", buildUser("user-1", "Alice"),
                "user-2", buildUser("user-2", "Bob")
        );
        skillDomainService.skillTitleMap = Map.of("skill-1", "Skill Name");

        IPage<CommentDTO> result = userCommentAppService.getUserRelatedComments(new CommentQueryRequest(1, 10), "target-user");

        assertEquals(2, result.getRecords().size());
        assertEquals("Skill Name", result.getRecords().get(0).getBusinessName());
        assertNull(result.getRecords().get(1).getBusinessName());
        assertEquals(BusinessType.MEETING, result.getRecords().get(1).getBusinessType());
    }

    @Test
    void getUserRelatedCommentsShouldResolveSkillBusinessName() {
        CommentEntity skillComment = buildComment("comment-1", "skill-1", BusinessType.SKILL, "skill content", "user-1", "user-2");
        commentDomainService.relatedCommentsPage = pageOf(1, 10, List.of(skillComment));
        userDomainService.userMap = Map.of(
                "user-1", buildUser("user-1", "Alice"),
                "user-2", buildUser("user-2", "Bob")
        );
        skillDomainService.skillTitleMap = Map.of("skill-1", "Skill Name");

        IPage<CommentDTO> result = userCommentAppService.getUserRelatedComments(new CommentQueryRequest(1, 10), "target-user");

        CommentDTO dto = result.getRecords().get(0);
        assertEquals("Skill Name", dto.getBusinessName());
        assertEquals("Alice", dto.getCommentUserName());
        assertEquals("Bob", dto.getReplyUserName());
    }

    private Page<CommentEntity> pageOf(long current, long size, List<CommentEntity> records) {
        Page<CommentEntity> page = new Page<>(current, size, records.size());
        page.setRecords(records);
        return page;
    }

    private CommentEntity buildComment(String id, String businessId, BusinessType businessType, String content, String commentUserId, String replyUserId) {
        CommentEntity comment = new CommentEntity();
        comment.setId(id);
        comment.setBusinessId(businessId);
        comment.setBusinessType(businessType);
        comment.setContent(content);
        comment.setCommentUserId(commentUserId);
        comment.setReplyUserId(replyUserId);
        comment.setCreateTime(LocalDateTime.of(2026, 3, 26, 10, 0));
        return comment;
    }

    private UserEntity buildUser(String id, String name) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private static class FakeCommentDomainService extends CommentDomainService {
        private IPage<CommentEntity> relatedCommentsPage = new Page<>();
        private List<CommentEntity> latestComments = List.of();

        FakeCommentDomainService() {
            super(null, null);
        }

        @Override
        public IPage<CommentEntity> getUserRelatedComments(org.xhy.community.domain.comment.query.CommentQuery query) {
            return relatedCommentsPage;
        }

        @Override
        public List<CommentEntity> getLatestComments() {
            return latestComments;
        }
    }

    private static class FakeUserDomainService extends UserDomainService {
        private Map<String, UserEntity> userMap = Map.of();

        FakeUserDomainService() {
            super(null, null, null, null);
        }

        @Override
        public Map<String, UserEntity> getUserEntityMapByIds(Collection<String> userIds) {
            return userMap;
        }
    }

    private static class FakePostDomainService extends PostDomainService {
        FakePostDomainService() {
            super(null, null, null, null, null);
        }

        @Override
        public Set<String> getAcceptedCommentIds(String postId) {
            return Set.of();
        }

        @Override
        public Map<String, String> getPostTitleMapByIds(Collection<String> postIds) {
            return Map.of();
        }

        @Override
        public Map<String, PostEntity> getPostEntityMapByIds(Collection<String> postIds) {
            return Map.of();
        }

        @Override
        public void removeAcceptanceByCommentId(String commentId) {
        }

        @Override
        public Map<String, Set<String>> getAcceptedCommentIdsMap(Set<String> postIds) {
            return Map.of();
        }
    }

    private static class FakeCourseDomainService extends CourseDomainService {
        FakeCourseDomainService() {
            super(null, null);
        }

        @Override
        public Map<String, String> getCourseTitleMapByIds(Collection<String> courseIds) {
            return Map.of();
        }
    }

    private static class FakeChapterDomainService extends ChapterDomainService {
        FakeChapterDomainService() {
            super(null, null, null);
        }

        @Override
        public Map<String, String> getChapterTitleMapByIds(Collection<String> chapterIds) {
            return Map.of();
        }

        @Override
        public Map<String, ChapterEntity> getChapterEntityMapByIds(Collection<String> chapterIds) {
            return Map.of();
        }
    }

    private static class FakeLikeDomainService extends LikeDomainService {
        FakeLikeDomainService() {
            super(null);
        }

        @Override
        public Map<String, Long> batchCountLikes(Map<String, LikeTargetType> targets) {
            return Map.of();
        }
    }

    private static class FakeInterviewQuestionDomainService extends InterviewQuestionDomainService {
        FakeInterviewQuestionDomainService() {
            super(null, null);
        }

        @Override
        public Map<String, String> getQuestionTitleMapByIds(Collection<String> questionIds) {
            return Map.of();
        }

        @Override
        public Map<String, InterviewQuestionEntity> getQuestionEntityMapByIds(Collection<String> questionIds) {
            return Map.of();
        }
    }

    private static class FakeSkillDomainService extends SkillDomainService {
        private Map<String, String> skillTitleMap = Map.of();

        FakeSkillDomainService() {
            super(null);
        }

        @Override
        public Map<String, String> getSkillTitleMapByIds(Collection<String> skillIds) {
            return skillTitleMap;
        }
    }
}
