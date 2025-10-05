package org.xhy.community.application.course.assembler;

import org.xhy.community.domain.course.valueobject.ChapterProgressReport;
import org.xhy.community.interfaces.course.request.ReportChapterProgressRequest;

public class LearningProgressAssembler {

    public static ChapterProgressReport fromReportRequest(String userId, ReportChapterProgressRequest req) {
        if (req == null) return null;
        return new ChapterProgressReport(
                userId,
                req.getCourseId(),
                req.getChapterId(),
                req.getProgressPercent(),
                req.getPositionSec(),
                req.getTimeSpentDeltaSec()
        );
    }
}

