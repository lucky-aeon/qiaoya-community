package org.xhy.community.application.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.ChapterAssembler;
import org.xhy.community.application.course.dto.ChapterDTO;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.resourcebinding.service.ResourceBindingDomainService;
import org.xhy.community.interfaces.course.request.CreateChapterRequest;
import org.xhy.community.interfaces.course.request.UpdateChapterRequest;
import org.xhy.community.interfaces.course.request.ChapterQueryRequest;
import org.xhy.community.interfaces.course.request.BatchUpdateChapterOrderRequest;
import org.xhy.community.domain.course.query.ChapterQuery;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminChapterAppService {
    
    private final ChapterDomainService chapterDomainService;
    private final ResourceBindingDomainService resourceBindingDomainService;
    
    public AdminChapterAppService(ChapterDomainService chapterDomainService,
                                  ResourceBindingDomainService resourceBindingDomainService) {
        this.chapterDomainService = chapterDomainService;
        this.resourceBindingDomainService = resourceBindingDomainService;
    }
    
    public ChapterDTO createChapter(CreateChapterRequest request, String authorId) {
        ChapterEntity chapter = ChapterAssembler.fromCreateRequest(request, authorId);
        
        ChapterEntity createdChapter = chapterDomainService.createChapter(chapter);

        // 同步章节中的资源绑定（Domain 解析 Markdown 并抽取业务ID）
        try {
            resourceBindingDomainService.syncBindingsForChapterFromMarkdown(createdChapter.getId(), request.getContent());
        } catch (Exception ignore) {}

        return ChapterAssembler.toDTO(createdChapter);
    }
    
    public ChapterDTO updateChapter(String chapterId, UpdateChapterRequest request) {
        ChapterEntity chapter = ChapterAssembler.fromUpdateRequest(request, chapterId);
        
        ChapterEntity updatedChapter = chapterDomainService.updateChapter(chapter);

        // 同步章节中的资源绑定（Domain 解析 Markdown 并抽取业务ID）
        try {
            resourceBindingDomainService.syncBindingsForChapterFromMarkdown(updatedChapter.getId(), request.getContent());
        } catch (Exception ignore) {}

        return ChapterAssembler.toDTO(updatedChapter);
    }
    
    public void deleteChapter(String chapterId) {
        chapterDomainService.deleteChapter(chapterId);
    }
    
    public ChapterDTO getChapterById(String chapterId) {
        ChapterEntity chapter = chapterDomainService.getChapterById(chapterId);
        return ChapterAssembler.toDTO(chapter);
    }
    
    public List<ChapterDTO> getChaptersByCourseId(String courseId) {
        List<ChapterEntity> chapters = chapterDomainService.getChaptersByCourseId(courseId);
        return chapters.stream()
            .map(ChapterAssembler::toDTO)
            .collect(Collectors.toList());
    }
    
    public IPage<ChapterDTO> getPagedChapters(ChapterQueryRequest request) {
        ChapterQuery query = new ChapterQuery(request.getPageNum(), request.getPageSize());
        query.setCourseId(request.getCourseId());
        
        IPage<ChapterEntity> chapterPage = chapterDomainService.getPagedChapters(query);
        
        return chapterPage.convert(ChapterAssembler::toDTO);
    }
    
    public void batchUpdateChapterOrder(BatchUpdateChapterOrderRequest request) {
        chapterDomainService.batchUpdateChapterOrder(request.getChapterIds());
    }

    // 资源ID解析逻辑：由 Domain 调用 Infrastructure MarkdownParser 完成
}
