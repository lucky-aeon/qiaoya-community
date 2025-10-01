package org.xhy.community.domain.ainews.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.executor.BatchResult;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.ainews.entity.DailyItemEntity;
import org.xhy.community.domain.ainews.query.DailyItemQuery;
import org.xhy.community.domain.ainews.repository.DailyItemRepository;
import org.xhy.community.domain.ainews.valueobject.DailyItemStatus;
import org.xhy.community.domain.ainews.valueobject.DailySource;
import org.xhy.community.domain.common.valueobject.AccessLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DailyItemDomainService {

    private final DailyItemRepository dailyItemRepository;

    public DailyItemDomainService(DailyItemRepository dailyItemRepository) {
        this.dailyItemRepository = dailyItemRepository;
    }

    public int upsertItems(List<DailyItemEntity> items) {
        if (items == null || items.isEmpty()) return 0;

        // 1) 收集待判重键集合（urlHash 和 (source, sourceItemId)）
        List<String> urlHashes = new ArrayList<>();
        Map<String, Set<Long>> sourceIdMap = new HashMap<>(); // source -> ids

        for (DailyItemEntity it : items) {
            String hash = it.getUrlHash();
            if (hash != null && !hash.isBlank()) {
                urlHashes.add(hash);
            }
            if (it.getSource() != null && it.getSourceItemId() != null) {
                sourceIdMap.computeIfAbsent(it.getSource().name(), k -> new HashSet<>())
                    .add(it.getSourceItemId());
            }
        }

        // 2) 一次查出已存在的 urlHash
        Set<String> existingHashes = new HashSet<>();
        if (!urlHashes.isEmpty()) {
            QueryWrapper<DailyItemEntity> qw = new QueryWrapper<>();
            qw.select("url_hash").in("url_hash", urlHashes);
            List<Object> objs = dailyItemRepository.selectObjs(qw);
            for (Object o : objs) {
                if (o != null) existingHashes.add(o.toString());
            }
        }

        // 3) 分组按 source 查询已存在的 source_item_id
        Set<String> existingPairs = new HashSet<>(); // key: source|id
        for (Map.Entry<String, Set<Long>> e : sourceIdMap.entrySet()) {
            String source = e.getKey();
            Set<Long> ids = e.getValue();
            if (ids.isEmpty()) continue;
            QueryWrapper<DailyItemEntity> qw = new QueryWrapper<>();
            qw.select("source", "source_item_id")
              .eq("source", source)
              .in("source_item_id", ids);
            List<DailyItemEntity> list = dailyItemRepository.selectList(qw);
            for (DailyItemEntity ex : list) {
                if (ex.getSource() != null && ex.getSourceItemId() != null) {
                    existingPairs.add(ex.getSource().name() + "|" + ex.getSourceItemId());
                }
            }
        }

        // 4) 过滤出需要插入的记录（避免批内重复）
        Set<String> seenHashes = new HashSet<>();
        Set<String> seenPairs = new HashSet<>();
        List<DailyItemEntity> toInsert = new ArrayList<>();

        for (DailyItemEntity it : items) {
            String hash = it.getUrlHash();
            String pair = (it.getSource() != null && it.getSourceItemId() != null)
                ? (it.getSource().name() + "|" + it.getSourceItemId()) : null;

            boolean dupByHash = (hash != null && !hash.isBlank()) && (existingHashes.contains(hash) || !seenHashes.add(hash));
            boolean dupByPair = (pair != null) && (existingPairs.contains(pair) || !seenPairs.add(pair));

            if (!dupByHash && !dupByPair) {
                toInsert.add(it);
            }
        }

        List<BatchResult> results = dailyItemRepository.insert(toInsert);

        return results.size();
    }

    public IPage<DailyItemEntity> pageByDate(String date, int pageNum, int pageSize, boolean withContent, AccessLevel accessLevel) {
        Page<DailyItemEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DailyItemEntity> qw = new QueryWrapper<>();
        // 使用时间范围避免字符串拼接
        LocalDate start = LocalDate.parse(date);
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = start.plusDays(1).atStartOfDay();
        qw.ge("published_at", from)
          .lt("published_at", to)
          .orderByDesc("create_time");
        if (accessLevel == AccessLevel.USER) {
            qw.eq("status", DailyItemStatus.PUBLISHED.name());
        }
        if (!withContent) {
            qw.select("id", "source", "title", "summary", "url", "source_item_id", "published_at", "fetched_at", "url_hash", "status");
        }
        return dailyItemRepository.selectPage(page, qw);
    }

    /**
     * App 端按查询对象分页
     */
    public IPage<DailyItemEntity> pageByQuery(DailyItemQuery query) {
        Page<DailyItemEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        QueryWrapper<DailyItemEntity> qw = new QueryWrapper<>();

        // 日期范围
        LocalDate start = LocalDate.parse(query.getDate());
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = start.plusDays(1).atStartOfDay();
        qw.ge("published_at", from)
          .lt("published_at", to)
          .orderByDesc("create_time");

        // 权限访问级别
        if (query.getAccessLevel() == AccessLevel.USER) {
            qw.eq("status", DailyItemStatus.PUBLISHED.name());
        }

        // 字段裁剪
        if (!Boolean.TRUE.equals(query.getWithContent())) {
            qw.select("id", "source", "title", "summary", "url", "source_item_id", "published_at", "fetched_at", "url_hash", "status");
        }

        return dailyItemRepository.selectPage(page, qw);
    }

    public DailyItemEntity getById(String id, AccessLevel accessLevel) {
        LambdaQueryWrapper<DailyItemEntity> w = new LambdaQueryWrapper<DailyItemEntity>()
            .eq(DailyItemEntity::getId, id);
        if (accessLevel == AccessLevel.USER) {
            w.eq(DailyItemEntity::getStatus, DailyItemStatus.PUBLISHED);
        }
        return dailyItemRepository.selectOne(w);
    }

    public IPage<DailyItemEntity> pageAdmin(String date, DailyItemStatus status, int pageNum, int pageSize, boolean withContent) {
        Page<DailyItemEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<DailyItemEntity> qw = new QueryWrapper<>();
        if (date != null && !date.isBlank()) {
            LocalDate start = LocalDate.parse(date);
            LocalDateTime from = start.atStartOfDay();
            LocalDateTime to = start.plusDays(1).atStartOfDay();
            qw.ge("published_at", from).lt("published_at", to);
        }
        if (status != null) {
            qw.eq("status", status.name());
        }
        qw.orderByDesc("id");
        if (!withContent) {
            qw.select("id", "source", "title", "summary", "url", "source_item_id", "published_at", "fetched_at", "url_hash", "status");
        }
        return dailyItemRepository.selectPage(page, qw);
    }

    public void updateStatus(String id, DailyItemStatus status) {
        if (id == null || status == null) return;
        DailyItemEntity entity = new DailyItemEntity();
        entity.setId(id);
        entity.setStatus(status);
        dailyItemRepository.updateById(entity);
    }

    public Map<String, Integer> getHistoryDateCounts() {
        // Select only published_at to reduce payload
        QueryWrapper<DailyItemEntity> qw = new QueryWrapper<>();
        qw.select("published_at");
        List<DailyItemEntity> list = dailyItemRepository.selectList(qw);
        Map<String, Integer> counts = new HashMap<>();
        for (DailyItemEntity e : list) {
            if (e.getPublishedAt() == null) continue;
            LocalDate d = e.getPublishedAt().toLocalDate();
            String key = d.toString();
            counts.put(key, counts.getOrDefault(key, 0) + 1);
        }
        return counts;
    }

    public String getLatestDate() {
        QueryWrapper<DailyItemEntity> qw = new QueryWrapper<>();
        qw.select("published_at").orderByDesc("published_at").last("LIMIT 1");
        List<DailyItemEntity> list = dailyItemRepository.selectList(qw);
        if (list.isEmpty() || list.get(0).getPublishedAt() == null) return null;
        return list.get(0).getPublishedAt().toLocalDate().toString();
    }

    public Long getMaxSourceItemId(DailySource source) {
        if (source == null) return null;
        QueryWrapper<DailyItemEntity> qw = new QueryWrapper<>();
        qw.select("MAX(source_item_id) AS max_source_item_id").eq("source", source.name());
        // Use selectObjs to fetch scalar
        List<Object> objs = dailyItemRepository.selectObjs(qw);
        if (objs == null || objs.isEmpty() || objs.get(0) == null) return null;
        if (objs.get(0) instanceof Number) {
            return ((Number) objs.get(0)).longValue();
        }
        try {
            return Long.parseLong(objs.get(0).toString());
        } catch (Exception e) {
            return null;
        }
    }
}
