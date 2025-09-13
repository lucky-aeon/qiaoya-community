package org.xhy.community.domain.course.service;

import org.springframework.stereotype.Service;
import org.xhy.community.domain.course.repository.ChapterRepository;

@Service
public class ChapterDomainService {
    
    private final ChapterRepository chapterRepository;
    
    public ChapterDomainService(ChapterRepository chapterRepository) {
        this.chapterRepository = chapterRepository;
    }
}