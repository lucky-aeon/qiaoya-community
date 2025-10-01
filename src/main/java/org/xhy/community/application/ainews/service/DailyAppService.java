package org.xhy.community.application.ainews.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.ainews.assembler.DailyItemAssembler;
import org.xhy.community.application.ainews.dto.DailyItemDTO;
import org.xhy.community.application.ainews.dto.HistoryDateDTO;
import org.xhy.community.application.ainews.dto.HistoryOverviewDTO;
import org.xhy.community.application.ainews.dto.TodayDailyDTO;
import org.xhy.community.domain.ainews.entity.DailyItemEntity;
import org.xhy.community.domain.ainews.query.DailyItemQuery;
import org.xhy.community.domain.ainews.service.DailyItemDomainService;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.interfaces.ainews.request.DailyQueryRequest;
import org.xhy.community.interfaces.common.request.PageRequest;

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

    public IPage<DailyItemDTO> pageDailyItems(DailyQueryRequest request) {
        DailyItemQuery query = DailyItemAssembler.fromAppRequest(request);
        IPage<DailyItemEntity> page = dailyItemDomainService.pageByQuery(query);
        boolean withContent = Boolean.TRUE.equals(query.getWithContent());
        return page.convert(entity -> DailyItemAssembler.toDTO(entity, withContent));
    }

    public DailyItemDTO getById(String id) {
        DailyItemEntity entity = dailyItemDomainService.getById(id, AccessLevel.USER);
        return DailyItemAssembler.toDTO(entity, true);
    }

    /**
     * 今日日报：仅返回当天全部标题列表（不含详情）
     */
    public TodayDailyDTO getTodayDaily(DailyQueryRequest request) {
        String latestDate = getLatestDate();
        TodayDailyDTO dto = new TodayDailyDTO();
        dto.setDate(latestDate);
        if (latestDate == null || latestDate.isBlank()) {
            dto.setTitles(List.of());
            return dto;
        }

        // 当天全部标题列表（不含 content）
        IPage<DailyItemEntity> allToday = dailyItemDomainService.pageByDate(latestDate, 1, 1000, false, AccessLevel.USER);
        List<String> titles = allToday.getRecords().stream()
            .map(DailyItemEntity::getTitle)
            .collect(Collectors.toList());
        dto.setTitles(titles);
        return dto;
    }

    /**
     * 往期概览分页：按日期分组 + 数量，排除最新日期
     */
    public IPage<HistoryOverviewDTO> pageHistoryOverview(PageRequest request) {
        String latest = getLatestDate();
        Map<String, Integer> counts = dailyItemDomainService.getHistoryDateCounts();

        List<String> sortedDates = counts.keySet().stream()
            .filter(d -> latest == null || !latest.equals(d))
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());

        int pageNum = request.getPageNum() != null ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 10;
        int fromIdx = Math.max(0, (pageNum - 1) * pageSize);
        int toIdx = Math.min(sortedDates.size(), fromIdx + pageSize);

        List<HistoryOverviewDTO> pageRecords = (fromIdx >= toIdx) ? List.of() :
            sortedDates.subList(fromIdx, toIdx).stream()
                .map(d -> {
                    // 查询当日前 5 条标题（不含 content）
                    IPage<DailyItemEntity> top5 = dailyItemDomainService.pageByDate(d, 1, 5, false, AccessLevel.USER);
                    String composedTitle = top5.getRecords().stream()
                        .map(DailyItemEntity::getTitle)
                        .filter(t -> t != null && !t.isBlank())
                        .collect(Collectors.joining("；"));
                    return new HistoryOverviewDTO(composedTitle, d, counts.getOrDefault(d, 0));
                })
                .collect(Collectors.toList());

        Page<HistoryOverviewDTO> page = new Page<>(pageNum, pageSize, sortedDates.size());
        page.setRecords(pageRecords);
        return page;
    }
}
