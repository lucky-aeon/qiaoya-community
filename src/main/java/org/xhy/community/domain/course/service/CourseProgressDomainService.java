package org.xhy.community.domain.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.course.entity.UserChapterProgressEntity;
import org.xhy.community.domain.course.entity.UserCourseProgressEntity;
import org.xhy.community.domain.course.event.CourseCompletedEvent;
import org.xhy.community.domain.course.repository.UserChapterProgressRepository;
import org.xhy.community.domain.course.repository.UserCourseProgressRepository;
import org.xhy.community.domain.course.repository.ChapterRepository;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;
import org.xhy.community.domain.course.valueobject.ChapterProgressReport;
import org.xhy.community.domain.course.entity.UserChapterProgressEntity;

import java.time.LocalDateTime;

@Service
public class CourseProgressDomainService {

    private static final int COMPLETE_THRESHOLD_PERCENT = 95; // 完成阈值（视频/图文均适用）

    private final UserChapterProgressRepository userChapterProgressRepository;
    private final UserCourseProgressRepository userCourseProgressRepository;
    private final ChapterRepository chapterRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CourseProgressDomainService(UserChapterProgressRepository userChapterProgressRepository,
                                       UserCourseProgressRepository userCourseProgressRepository,
                                       ChapterRepository chapterRepository,
                                       ApplicationEventPublisher eventPublisher) {
        this.userChapterProgressRepository = userChapterProgressRepository;
        this.userCourseProgressRepository = userCourseProgressRepository;
        this.chapterRepository = chapterRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 上报章节学习进度（只增不减，与心跳/阈值打点配合）
     */
    public void updateChapterProgress(ChapterProgressReport report) {
        String userId = report.getUserId();
        String courseId = report.getCourseId();
        String chapterId = report.getChapterId();
        Integer reportedPercent = report.getProgressPercent();
        Integer positionSec = report.getPositionSec();
        Integer timeSpentDeltaSec = report.getTimeSpentDeltaSec();
        // 1) 基础校验：章节必须属于课程
        ChapterEntity chapter = chapterRepository.selectById(chapterId);
        if (chapter == null || !StringUtils.hasText(chapter.getCourseId()) || !chapter.getCourseId().equals(courseId)) {
            throw new BusinessException(CourseErrorCode.CHAPTER_NOT_FOUND);
        }

        // 2) 查询/初始化章节进度
        UserChapterProgressEntity rec = userChapterProgressRepository.selectOne(
                new LambdaQueryWrapper<UserChapterProgressEntity>()
                        .eq(UserChapterProgressEntity::getUserId, userId)
                        .eq(UserChapterProgressEntity::getChapterId, chapterId)
        );

        LocalDateTime now = LocalDateTime.now();

        if (rec == null) {
            rec = new UserChapterProgressEntity();
            rec.setUserId(userId);
            rec.setCourseId(courseId);
            rec.setChapterId(chapterId);
            rec.setProgressPercent(sanitizePercent(reportedPercent));
            rec.setLastPositionSec(safeNonNegative(positionSec));
            rec.setTimeSpentSec(Math.max(0, safeDelta(timeSpentDeltaSec)));
            rec.setLastAccessTime(now);
            if (rec.getProgressPercent() != null && rec.getProgressPercent() >= COMPLETE_THRESHOLD_PERCENT) {
                rec.setCompletedAt(now);
            }
            userChapterProgressRepository.insert(rec);
        } else {
            // 只增不减
            Integer newPercent = Math.max(nullSafe(rec.getProgressPercent()), sanitizePercent(reportedPercent));
            Integer newPosition = Math.max(nullSafe(rec.getLastPositionSec()), safeNonNegative(positionSec));
            int safeAdd = safeDelta(timeSpentDeltaSec);

            rec.setProgressPercent(newPercent);
            rec.setLastPositionSec(newPosition);
            rec.setTimeSpentSec(nullSafe(rec.getTimeSpentSec()) + safeAdd);
            rec.setLastAccessTime(now);
            if (rec.getCompletedAt() == null && newPercent >= COMPLETE_THRESHOLD_PERCENT) {
                rec.setCompletedAt(now);
            }
            userChapterProgressRepository.updateById(rec);
        }

        // 3) 汇总课程进度
        recalcCourseProgress(userId, courseId, chapterId, now);
    }

    /**
     * 重新计算用户的课程层进度（统计完成章节数）
     */
    public void recalcCourseProgress(String userId, String courseId, String lastAccessChapterId, LocalDateTime accessTime) {
        // 统计 courseId 的章节总数
        int totalChapters = Math.toIntExact(
                chapterRepository.selectCount(new LambdaQueryWrapper<ChapterEntity>()
                        .eq(ChapterEntity::getCourseId, courseId))
        );

        Long completed = userChapterProgressRepository.selectCount(
                new LambdaQueryWrapper<UserChapterProgressEntity>()
                        .eq(UserChapterProgressEntity::getUserId, userId)
                        .eq(UserChapterProgressEntity::getCourseId, courseId)
                        .isNotNull(UserChapterProgressEntity::getCompletedAt)
        );
        int completedChapters = completed == null ? 0 : completed.intValue();
        int progressPercent = totalChapters > 0 ? (int) Math.ceil((completedChapters * 100.0) / totalChapters) : 0;

        UserCourseProgressEntity agg = userCourseProgressRepository.selectOne(
                new LambdaQueryWrapper<UserCourseProgressEntity>()
                        .eq(UserCourseProgressEntity::getUserId, userId)
                        .eq(UserCourseProgressEntity::getCourseId, courseId)
        );
        boolean prevCompleted = (agg != null && agg.getCompletedAt() != null);
        UserCourseProgressEntity target = (agg != null) ? agg : new UserCourseProgressEntity();
        target.setUserId(userId);
        target.setCourseId(courseId);
        target.setTotalChapters(totalChapters);
        target.setCompletedChapters(completedChapters);
        target.setProgressPercent(progressPercent);
        target.setLastAccessChapterId(lastAccessChapterId);
        target.setLastAccessTime(accessTime);

        boolean completeNow = totalChapters > 0 && completedChapters >= totalChapters;
        if (completeNow && target.getCompletedAt() == null) {
            target.setCompletedAt(accessTime);
        }

        // 新增或更新（基于是否带有主键ID）
        userCourseProgressRepository.insertOrUpdate(target);

        // 仅在“首次完成”时发布事件
        if (!prevCompleted && target.getCompletedAt() != null) {
            eventPublisher.publishEvent(new CourseCompletedEvent(userId, courseId, target.getCompletedAt()));

        }
    }

    /** 获取课程聚合进度实体（应用层只读查询使用） */
    public UserCourseProgressEntity getAggregate(String userId, String courseId) {
        return userCourseProgressRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCourseProgressEntity>()
                        .eq(UserCourseProgressEntity::getUserId, userId)
                        .eq(UserCourseProgressEntity::getCourseId, courseId)
        );
    }

    /** 分页查询用户的课程聚合进度 */
    public com.baomidou.mybatisplus.core.metadata.IPage<UserCourseProgressEntity> listAggregatesByUser(String userId, int pageNum, int pageSize) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserCourseProgressEntity> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        return userCourseProgressRepository.selectPage(page,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCourseProgressEntity>()
                        .eq(UserCourseProgressEntity::getUserId, userId)
                        .orderByDesc(UserCourseProgressEntity::getLastAccessTime)
        );
    }

    /**
     * 批量获取用户在若干章节的 last_position_sec
     */
    public java.util.Map<String, Integer> getChapterPositionsForUser(String userId, java.util.Set<String> chapterIds) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        if (chapterIds == null || chapterIds.isEmpty()) return map;
        var list = userChapterProgressRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserChapterProgressEntity>()
                        .eq(UserChapterProgressEntity::getUserId, userId)
                        .in(UserChapterProgressEntity::getChapterId, chapterIds)
        );
        for (var rec : list) {
            map.put(rec.getChapterId(), rec.getLastPositionSec());
        }
        return map;
    }

    private int sanitizePercent(Integer p) {
        if (p == null) return 0;
        return Math.max(0, Math.min(100, p));
    }

    private int safeNonNegative(Integer v) {
        if (v == null) return 0;
        return Math.max(0, v);
    }

    private int safeDelta(Integer delta) {
        if (delta == null) return 0;
        // 心跳周期（10s）+ 容差（2s）上限
        int MAX_DELTA = 12;
        return Math.max(0, Math.min(MAX_DELTA, delta));
    }

    private int nullSafe(Integer v) {
        return v == null ? 0 : v;
    }
}
