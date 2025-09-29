package org.xhy.community.domain.expression.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.expression.entity.ExpressionTypeEntity;
import org.xhy.community.domain.expression.entity.ReactionEntity;
import org.xhy.community.domain.expression.repository.ExpressionTypeRepository;
import org.xhy.community.domain.expression.repository.ReactionRepository;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.ExpressionErrorCode;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReactionDomainService {

    private final ReactionRepository reactionRepository;
    private final ExpressionTypeRepository expressionTypeRepository;

    public ReactionDomainService(ReactionRepository reactionRepository,
                                 ExpressionTypeRepository expressionTypeRepository) {
        this.reactionRepository = reactionRepository;
        this.expressionTypeRepository = expressionTypeRepository;
    }

    /**
     * 切换表情：存在则删除，不存在则新增
     * @return true 表示新增；false 表示移除
     */
    public boolean toggle(ReactionEntity reaction) {
        // 校验表情类型存在且启用
        boolean valid = expressionTypeRepository.exists(new LambdaQueryWrapper<ExpressionTypeEntity>()
                .eq(ExpressionTypeEntity::getCode, reaction.getReactionType())
                .eq(ExpressionTypeEntity::getIsActive, true));
        if (!valid) {
            throw new BusinessException(ExpressionErrorCode.EXPRESSION_NOT_FOUND);
        }

        LambdaQueryWrapper<ReactionEntity> existsQ = new LambdaQueryWrapper<ReactionEntity>()
                .eq(ReactionEntity::getBusinessType, reaction.getBusinessType())
                .eq(ReactionEntity::getBusinessId, reaction.getBusinessId())
                .eq(ReactionEntity::getUserId, reaction.getUserId())
                .eq(ReactionEntity::getReactionType, reaction.getReactionType());

        boolean exists = reactionRepository.exists(existsQ);
        if (exists) {
            // 逻辑删除
            reactionRepository.delete(existsQ);
            return false;
        } else {
            reactionRepository.insert(reaction);
            return true;
        }
    }

    /** 聚合：单业务对象的各表情计数 reactionType -> count */
    public Map<String, Integer> getCounts(BusinessType businessType, String businessId) {
        QueryWrapper<ReactionEntity> qw = new QueryWrapper<>();
        qw.select("reaction_type as reactionType", "count(*) as cnt")
                .eq("business_type", businessType.name())
                .eq("business_id", businessId)
                .groupBy("reaction_type");
        List<Map<String, Object>> rows = reactionRepository.selectMaps(qw);
        Map<String, Integer> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String type = Objects.toString(row.get("reactionType"), null);
            Number cntNum = (Number) row.get("cnt");
            int cnt = cntNum != null ? cntNum.intValue() : 0;
            result.put(type, cnt);
        }
        return result;
    }

    /** 当前用户在单业务对象上的已选择表情集合 */
    public Set<String> getUserTypes(BusinessType businessType, String businessId, String userId) {
        if (userId == null) return Collections.emptySet();
        List<ReactionEntity> list = reactionRepository.selectList(new LambdaQueryWrapper<ReactionEntity>()
                .eq(ReactionEntity::getBusinessType, businessType)
                .eq(ReactionEntity::getBusinessId, businessId)
                .eq(ReactionEntity::getUserId, userId));
        return list.stream().map(ReactionEntity::getReactionType).collect(Collectors.toSet());
    }

    /** 单业务对象：各 reactionType -> 用户ID列表 */
    public Map<String, List<String>> getUsersByType(BusinessType businessType, String businessId) {
        List<ReactionEntity> list = reactionRepository.selectList(new LambdaQueryWrapper<ReactionEntity>()
                .eq(ReactionEntity::getBusinessType, businessType)
                .eq(ReactionEntity::getBusinessId, businessId)
                .orderByAsc(ReactionEntity::getCreateTime));
        return list.stream().collect(Collectors.groupingBy(
                ReactionEntity::getReactionType,
                Collectors.mapping(ReactionEntity::getUserId, Collectors.toList())
        ));
    }

    /** 批量聚合：businessId -> (reactionType -> count) */
    public Map<String, Map<String, Integer>> getCountsBatch(BusinessType businessType, List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) return Collections.emptyMap();
        QueryWrapper<ReactionEntity> qw = new QueryWrapper<>();
        qw.select("business_id as businessId", "reaction_type as reactionType", "count(*) as cnt")
                .eq("business_type", businessType.name())
                .in("business_id", businessIds)
                .groupBy("business_id", "reaction_type");
        List<Map<String, Object>> rows = reactionRepository.selectMaps(qw);
        Map<String, Map<String, Integer>> res = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String bid = Objects.toString(row.get("businessId"));
            String type = Objects.toString(row.get("reactionType"));
            Number cntNum = (Number) row.get("cnt");
            int cnt = cntNum != null ? cntNum.intValue() : 0;
            res.computeIfAbsent(bid, k -> new HashMap<>()).put(type, cnt);
        }
        return res;
    }

    /** 批量：businessId -> 用户在该业务下选择的表情集合 */
    public Map<String, Set<String>> getUserTypesBatch(BusinessType businessType, List<String> businessIds, String userId) {
        if (userId == null || CollectionUtils.isEmpty(businessIds)) return Collections.emptyMap();
        List<ReactionEntity> list = reactionRepository.selectList(new LambdaQueryWrapper<ReactionEntity>()
                .eq(ReactionEntity::getBusinessType, businessType)
                .in(ReactionEntity::getBusinessId, businessIds)
                .eq(ReactionEntity::getUserId, userId));
        Map<String, Set<String>> res = new HashMap<>();
        for (ReactionEntity r : list) {
            res.computeIfAbsent(r.getBusinessId(), k -> new HashSet<>()).add(r.getReactionType());
        }
        return res;
    }

    /** 批量：businessId -> (reactionType -> 用户ID列表) */
    public Map<String, Map<String, List<String>>> getUsersByTypeBatch(BusinessType businessType, List<String> businessIds) {
        if (CollectionUtils.isEmpty(businessIds)) return Collections.emptyMap();
        List<ReactionEntity> list = reactionRepository.selectList(new LambdaQueryWrapper<ReactionEntity>()
                .eq(ReactionEntity::getBusinessType, businessType)
                .in(ReactionEntity::getBusinessId, businessIds)
                .orderByAsc(ReactionEntity::getCreateTime));

        Map<String, Map<String, List<String>>> res = new HashMap<>();
        for (ReactionEntity r : list) {
            res
                .computeIfAbsent(r.getBusinessId(), k -> new HashMap<>())
                .computeIfAbsent(r.getReactionType(), k -> new ArrayList<>())
                .add(r.getUserId());
        }
        return res;
    }
}
