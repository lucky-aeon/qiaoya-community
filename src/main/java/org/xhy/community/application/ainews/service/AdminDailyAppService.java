package org.xhy.community.application.ainews.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.ainews.assembler.DailyItemAssembler;
import org.xhy.community.application.ainews.dto.AdminDailyItemDTO;
import org.xhy.community.domain.ainews.entity.DailyItemEntity;
import org.xhy.community.domain.ainews.service.DailyItemDomainService;
import org.xhy.community.domain.ainews.valueobject.DailyItemStatus;
import org.xhy.community.interfaces.ainews.request.AdminDailyQueryRequest;

@Service
public class AdminDailyAppService {

    private final AibaseIngestAppService aibaseIngestAppService;
    private final DailyItemDomainService dailyItemDomainService;

    public AdminDailyAppService(AibaseIngestAppService aibaseIngestAppService,
                                DailyItemDomainService dailyItemDomainService) {
        this.aibaseIngestAppService = aibaseIngestAppService;
        this.dailyItemDomainService = dailyItemDomainService;
    }

    /** 手动触发采集（仅AIBase） */
    public AibaseIngestAppService.IngestResult manualIngest() {
        return aibaseIngestAppService.ingestLatest();
    }

    /** 管理端分页查询 */
    public IPage<AdminDailyItemDTO> pageDailyItems(AdminDailyQueryRequest request) {
        boolean withContent = Boolean.TRUE.equals(request.getWithContent());
        IPage<DailyItemEntity> page = dailyItemDomainService.pageAdmin(
            request.getDate(),
            request.getStatus(),
            request.getPageNum(),
            request.getPageSize(),
            withContent
        );
        return page.convert(DailyItemAssembler::toAdminDTO);
    }

    public void publish(String id) {
        dailyItemDomainService.updateStatus(id, DailyItemStatus.PUBLISHED);
    }

    public void hide(String id) {
        dailyItemDomainService.updateStatus(id, DailyItemStatus.HIDDEN);
    }
}
