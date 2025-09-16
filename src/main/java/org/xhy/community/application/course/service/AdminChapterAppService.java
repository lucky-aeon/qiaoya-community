package org.xhy.community.application.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.ChapterAssembler;
import org.xhy.community.application.course.dto.ChapterDTO;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.interfaces.course.request.CreateChapterRequest;
import org.xhy.community.interfaces.course.request.UpdateChapterRequest;
import org.xhy.community.interfaces.course.request.ChapterQueryRequest;
import org.xhy.community.domain.course.query.ChapterQuery;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminChapterAppService {
    
    private final ChapterDomainService chapterDomainService;
    
    public AdminChapterAppService(ChapterDomainService chapterDomainService) {
        this.chapterDomainService = chapterDomainService;
    }
    
    public ChapterDTO createChapter(CreateChapterRequest request, String authorId) {
        ChapterEntity chapter = ChapterAssembler.fromCreateRequest(request, authorId);
        
        ChapterEntity createdChapter = chapterDomainService.createChapter(chapter);
        
        return ChapterAssembler.toDTO(createdChapter);
    }
    
    public ChapterDTO updateChapter(String chapterId, UpdateChapterRequest request) {
        ChapterEntity chapter = ChapterAssembler.fromUpdateRequest(request, chapterId);
        
        ChapterEntity updatedChapter = chapterDomainService.updateChapter(chapter);
        
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
}