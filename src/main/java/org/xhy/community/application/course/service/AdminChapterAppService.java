package org.xhy.community.application.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.ChapterAssembler;
import org.xhy.community.application.course.dto.ChapterDTO;
import org.xhy.community.application.transcript.dto.AdminChapterTranscriptDTO;
import org.xhy.community.application.transcript.service.ChapterTranscriptAppService;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.resourcebinding.service.ResourceBindingDomainService;
import org.xhy.community.interfaces.course.request.CreateChapterRequest;
import org.xhy.community.interfaces.course.request.UpdateChapterRequest;
import org.xhy.community.interfaces.course.request.ChapterQueryRequest;
import org.xhy.community.interfaces.course.request.BatchUpdateChapterOrderRequest;
import org.xhy.community.domain.course.query.ChapterQuery;
import org.xhy.community.domain.like.service.LikeDomainService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.application.like.helper.LikeCountHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminChapterAppService {
    
    private final ChapterDomainService chapterDomainService;
    private final ResourceBindingDomainService resourceBindingDomainService;
    private final LikeDomainService likeDomainService;
    private final ChapterTranscriptAppService chapterTranscriptAppService;
    
    public AdminChapterAppService(ChapterDomainService chapterDomainService,
                                  ResourceBindingDomainService resourceBindingDomainService,
                                  LikeDomainService likeDomainService,
                                  ChapterTranscriptAppService chapterTranscriptAppService) {
        this.chapterDomainService = chapterDomainService;
        this.resourceBindingDomainService = resourceBindingDomainService;
        this.likeDomainService = likeDomainService;
        this.chapterTranscriptAppService = chapterTranscriptAppService;
    }
    
    public ChapterDTO createChapter(CreateChapterRequest request, String authorId) {
        ChapterEntity chapter = ChapterAssembler.fromCreateRequest(request, authorId);
        
        ChapterEntity createdChapter = chapterDomainService.createChapter(chapter);

        // 同步章节中的资源绑定（Domain 解析 Markdown 并抽取业务ID）
        try {
            resourceBindingDomainService.syncBindingsForChapterFromMarkdown(createdChapter.getId(), request.getContent());
        } catch (Exception ignore) {}
        try {
            chapterTranscriptAppService.createPendingTaskForChapter(createdChapter);
        } catch (Exception ignore) {}

        ChapterDTO dto = ChapterAssembler.toDTO(createdChapter);
        dto.setLikeCount(0);
        dto.setTranscript(chapterTranscriptAppService.getAdminTranscript(createdChapter.getId()));
        return dto;
    }
    
    public ChapterDTO updateChapter(String chapterId, UpdateChapterRequest request) {
        ChapterEntity chapter = ChapterAssembler.fromUpdateRequest(request, chapterId);
        
        ChapterEntity updatedChapter = chapterDomainService.updateChapter(chapter);

        // 同步章节中的资源绑定（Domain 解析 Markdown 并抽取业务ID）
        try {
            resourceBindingDomainService.syncBindingsForChapterFromMarkdown(updatedChapter.getId(), request.getContent());
        } catch (Exception ignore) {}
        try {
            chapterTranscriptAppService.createPendingTaskForChapter(updatedChapter);
        } catch (Exception ignore) {}

        ChapterDTO dto = ChapterAssembler.toDTO(updatedChapter);
        dto.setLikeCount(LikeCountHelper.getLikeCount(updatedChapter.getId(), LikeTargetType.CHAPTER, likeDomainService));
        dto.setTranscript(chapterTranscriptAppService.getAdminTranscript(updatedChapter.getId()));
        return dto;
    }
    
    public void deleteChapter(String chapterId) {
        chapterDomainService.deleteChapter(chapterId);
    }

    public ChapterDTO archiveChapter(String chapterId, String reason) {
        ChapterEntity chapter = chapterDomainService.archiveChapter(chapterId, reason);
        ChapterDTO dto = ChapterAssembler.toDTO(chapter);
        dto.setLikeCount(LikeCountHelper.getLikeCount(chapterId, LikeTargetType.CHAPTER, likeDomainService));
        dto.setTranscript(chapterTranscriptAppService.getAdminTranscript(chapterId));
        return dto;
    }

    public ChapterDTO unarchiveChapter(String chapterId) {
        ChapterEntity chapter = chapterDomainService.unarchiveChapter(chapterId);
        ChapterDTO dto = ChapterAssembler.toDTO(chapter);
        dto.setLikeCount(LikeCountHelper.getLikeCount(chapterId, LikeTargetType.CHAPTER, likeDomainService));
        dto.setTranscript(chapterTranscriptAppService.getAdminTranscript(chapterId));
        return dto;
    }
    
    public ChapterDTO getChapterById(String chapterId) {
        ChapterEntity chapter = chapterDomainService.getChapterById(chapterId);
        ChapterDTO dto = ChapterAssembler.toDTO(chapter);
        dto.setLikeCount(LikeCountHelper.getLikeCount(chapterId, LikeTargetType.CHAPTER, likeDomainService));
        dto.setTranscript(chapterTranscriptAppService.getAdminTranscript(chapterId));
        return dto;
    }
    
    public List<ChapterDTO> getChaptersByCourseId(String courseId) {
        List<ChapterEntity> chapters = chapterDomainService.getChaptersByCourseId(courseId);
        List<ChapterDTO> list = chapters.stream()
            .map(ChapterAssembler::toDTO)
            .collect(Collectors.toList());
        if (!list.isEmpty()) {
            LikeCountHelper.fillLikeCount(list, ChapterDTO::getId, LikeTargetType.CHAPTER, ChapterDTO::setLikeCount, likeDomainService);
            fillTranscripts(list);
        }
        return list;
    }
    
    public IPage<ChapterDTO> getPagedChapters(ChapterQueryRequest request) {
        ChapterQuery query = new ChapterQuery(request.getPageNum(), request.getPageSize());
        query.setCourseId(request.getCourseId());
        
        IPage<ChapterEntity> chapterPage = chapterDomainService.getPagedChapters(query);
        
        var dtoPage = chapterPage.convert(ChapterAssembler::toDTO);
        if (dtoPage.getRecords() != null && !dtoPage.getRecords().isEmpty()) {
            LikeCountHelper.fillLikeCount(dtoPage.getRecords(), ChapterDTO::getId, LikeTargetType.CHAPTER, ChapterDTO::setLikeCount, likeDomainService);
            fillTranscripts(dtoPage.getRecords());
        }
        return dtoPage;
    }
    
    public void batchUpdateChapterOrder(BatchUpdateChapterOrderRequest request) {
        chapterDomainService.batchUpdateChapterOrder(request.getChapterIds());
    }

    // 资源ID解析逻辑：由 Domain 调用 Infrastructure MarkdownParser 完成

    private void fillTranscripts(List<ChapterDTO> chapters) {
        List<String> chapterIds = chapters.stream()
            .map(ChapterDTO::getId)
            .toList();
        Map<String, AdminChapterTranscriptDTO> transcriptMap = chapterTranscriptAppService.getAdminTranscriptMap(chapterIds);
        chapters.forEach(chapter -> chapter.setTranscript(transcriptMap.get(chapter.getId())));
    }
}
