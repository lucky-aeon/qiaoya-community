package org.xhy.community.application.ainews.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.ainews.assembler.DailyItemAssembler;
import org.xhy.community.application.ainews.dto.DailyItemDTO;
import org.xhy.community.application.ainews.dto.HistoryDateDTO;
import org.xhy.community.domain.ainews.entity.DailyItemEntity;
import org.xhy.community.domain.ainews.service.DailyItemDomainService;
import org.xhy.community.domain.common.valueobject.AccessLevel;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyAppService {

    private final DailyItemDomainService dailyItemDomainService;

    public DailyAppService(DailyItemDomainService dailyItemDomainService) {
        this.dailyItemDomainService = dailyItemDomainService;
    }

    public List<HistoryDateDTO> listHistoryDates() {
        Map<String, Integer> counts = dailyItemDomainService.getHistoryDateCounts();
        return counts.entrySet().stream()
            .map(e -> new HistoryDateDTO(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(HistoryDateDTO::getDate).reversed())
            .collect(Collectors.toList());
    }

    public String getLatestDate() {
        return dailyItemDomainService.getLatestDate();
    }

    public IPage<DailyItemDTO> pageDailyItems(String date, int pageNum, int pageSize, boolean withContent) {
        IPage<DailyItemEntity> page = dailyItemDomainService.pageByDate(date, pageNum, pageSize, withContent, AccessLevel.USER);
        return page.convert(entity -> DailyItemAssembler.toDTO(entity, withContent));
    }

    public DailyItemDTO getById(String id) {
        DailyItemEntity entity = dailyItemDomainService.getById(id, AccessLevel.USER);
        return DailyItemAssembler.toDTO(entity, true);
    }
}

