package org.xhy.community.application.favorite.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xhy.community.application.favorite.dto.FavoriteListItemDTO;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.favorite.entity.FavoriteEntity;
import org.xhy.community.domain.favorite.query.FavoriteQuery;
import org.xhy.community.domain.favorite.service.FavoriteDomainService;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;
import org.xhy.community.domain.interview.entity.InterviewQuestionEntity;
import org.xhy.community.domain.interview.service.InterviewQuestionDomainService;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.skill.entity.SkillEntity;
import org.xhy.community.domain.skill.service.SkillDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.favorite.request.MyFavoritesQueryRequest;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FavoriteAppServiceUnitTest {

    private FakeFavoriteDomainService favoriteDomainService;
    private FakePostDomainService postDomainService;
    private FakeChapterDomainService chapterDomainService;
    private FakeCommentDomainService commentDomainService;
    private FakeInterviewQuestionDomainService interviewQuestionDomainService;
    private FakeSkillDomainService skillDomainService;
    private FakeUserDomainService userDomainService;
    private FavoriteAppService favoriteAppService;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId("user-current");
        favoriteDomainService = new FakeFavoriteDomainService();
        postDomainService = new FakePostDomainService();
        chapterDomainService = new FakeChapterDomainService();
        commentDomainService = new FakeCommentDomainService();
        interviewQuestionDomainService = new FakeInterviewQuestionDomainService();
        skillDomainService = new FakeSkillDomainService();
        userDomainService = new FakeUserDomainService();
        favoriteAppService = new FavoriteAppService(
                favoriteDomainService,
                postDomainService,
                chapterDomainService,
                commentDomainService,
                interviewQuestionDomainService,
                skillDomainService,
                userDomainService
        );
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void pageMyFavoritesShouldResolveSkillTitleSummaryAndAuthor() {
        MyFavoritesQueryRequest request = new MyFavoritesQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        FavoriteEntity favorite = buildFavorite("fav-1", "skill-1", FavoriteTargetType.SKILL);
        favoriteDomainService.pageResult = pageOf(1, 10, List.of(favorite));

        SkillEntity skill = buildSkill("skill-1", "user-1", "Skill Name", "Skill Summary");
        skillDomainService.skillMap = Map.of("skill-1", skill);

        UserEntity author = buildUser("user-1", "Alice");
        userDomainService.userMap = Map.of("user-1", author);

        IPage<FavoriteListItemDTO> result = favoriteAppService.pageMyFavorites(request);

        FavoriteListItemDTO dto = result.getRecords().get(0);
        assertEquals("Skill Name", dto.getTitle());
        assertEquals("Skill Summary", dto.getSnippet());
        assertEquals("user-1", dto.getAuthorId());
        assertEquals("Alice", dto.getAuthorName());
    }

    @Test
    void pageMyFavoritesShouldKeepPageShapeWhenSkillTargetMissing() {
        MyFavoritesQueryRequest request = new MyFavoritesQueryRequest();
        request.setPageNum(2);
        request.setPageSize(20);

        FavoriteEntity favorite = buildFavorite("fav-1", "skill-missing", FavoriteTargetType.SKILL);
        favoriteDomainService.pageResult = pageOf(2, 20, List.of(favorite));
        skillDomainService.skillMap = Map.of();
        userDomainService.userMap = Map.of();

        IPage<FavoriteListItemDTO> result = favoriteAppService.pageMyFavorites(request);

        assertEquals(2L, result.getCurrent());
        assertEquals(20L, result.getSize());
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getRecords().size());
        FavoriteListItemDTO dto = result.getRecords().get(0);
        assertNull(dto.getTitle());
        assertNull(dto.getSnippet());
        assertNull(dto.getAuthorName());
    }

    @Test
    void pageMyFavoritesShouldNotBreakOtherFavoriteTargetTypes() {
        MyFavoritesQueryRequest request = new MyFavoritesQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        FavoriteEntity postFavorite = buildFavorite("fav-post", "post-1", FavoriteTargetType.POST);
        FavoriteEntity interviewFavorite = buildFavorite("fav-question", "question-1", FavoriteTargetType.INTERVIEW_QUESTION);
        favoriteDomainService.pageResult = pageOf(1, 10, List.of(postFavorite, interviewFavorite));

        PostEntity post = new PostEntity();
        post.setId("post-1");
        post.setTitle("Post Title");
        post.setSummary("Post Summary");
        post.setAuthorId("user-post");
        postDomainService.postMap = Map.of("post-1", post);

        InterviewQuestionEntity question = new InterviewQuestionEntity();
        question.setId("question-1");
        question.setTitle("Question Title");
        question.setAuthorId("user-question");
        interviewQuestionDomainService.questionMap = Map.of("question-1", question);

        userDomainService.userMap = Map.of(
                "user-post", buildUser("user-post", "Post Author"),
                "user-question", buildUser("user-question", "Question Author")
        );

        IPage<FavoriteListItemDTO> result = favoriteAppService.pageMyFavorites(request);

        FavoriteListItemDTO postDto = result.getRecords().get(0);
        FavoriteListItemDTO questionDto = result.getRecords().get(1);
        assertEquals("Post Title", postDto.getTitle());
        assertEquals("Post Summary", postDto.getSnippet());
        assertEquals("Post Author", postDto.getAuthorName());
        assertEquals("Question Title", questionDto.getTitle());
        assertEquals("Question Author", questionDto.getAuthorName());
    }

    private Page<FavoriteEntity> pageOf(long current, long size, List<FavoriteEntity> records) {
        Page<FavoriteEntity> page = new Page<>(current, size, records.size());
        page.setRecords(records);
        return page;
    }

    private FavoriteEntity buildFavorite(String id, String targetId, FavoriteTargetType targetType) {
        FavoriteEntity favorite = new FavoriteEntity();
        favorite.setId(id);
        favorite.setTargetId(targetId);
        favorite.setTargetType(targetType);
        favorite.setCreateTime(LocalDateTime.of(2026, 3, 26, 10, 0));
        return favorite;
    }

    private SkillEntity buildSkill(String id, String userId, String name, String summary) {
        SkillEntity skill = new SkillEntity();
        skill.setId(id);
        skill.setUserId(userId);
        skill.setName(name);
        skill.setSummary(summary);
        return skill;
    }

    private UserEntity buildUser(String id, String name) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private static class FakeFavoriteDomainService extends FavoriteDomainService {
        private IPage<FavoriteEntity> pageResult = new Page<>();

        FakeFavoriteDomainService() {
            super(null);
        }

        @Override
        public IPage<FavoriteEntity> pageMyFavorites(FavoriteQuery query) {
            return pageResult;
        }
    }

    private static class FakePostDomainService extends PostDomainService {
        private Map<String, PostEntity> postMap = Map.of();

        FakePostDomainService() {
            super(null, null, null, null, null);
        }

        @Override
        public Map<String, PostEntity> getPostEntityMapByIds(Collection<String> postIds) {
            return postMap;
        }
    }

    private static class FakeChapterDomainService extends ChapterDomainService {
        FakeChapterDomainService() {
            super(null, null, null);
        }

        @Override
        public Map<String, ChapterEntity> getChapterEntityMapByIds(Collection<String> chapterIds) {
            return Map.of();
        }
    }

    private static class FakeCommentDomainService extends CommentDomainService {
        private Map<String, CommentEntity> commentMap = Map.of();

        FakeCommentDomainService() {
            super(null, null);
        }

        @Override
        public Map<String, CommentEntity> getCommentEntityMapByIds(Collection<String> commentIds) {
            return commentMap;
        }
    }

    private static class FakeInterviewQuestionDomainService extends InterviewQuestionDomainService {
        private Map<String, InterviewQuestionEntity> questionMap = Map.of();

        FakeInterviewQuestionDomainService() {
            super(null, null);
        }

        @Override
        public Map<String, InterviewQuestionEntity> getQuestionEntityMapByIds(Collection<String> questionIds) {
            return questionMap;
        }
    }

    private static class FakeSkillDomainService extends SkillDomainService {
        private Map<String, SkillEntity> skillMap = Map.of();

        FakeSkillDomainService() {
            super(null);
        }

        @Override
        public Map<String, SkillEntity> getSkillEntityMapByIds(Collection<String> skillIds) {
            return skillMap;
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
}
