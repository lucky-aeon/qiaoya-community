package org.xhy.community.application.reaction.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.reaction.dto.ReactionSummaryDTO;
import org.xhy.community.application.reaction.dto.ReactionUserDTO;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.expression.entity.ReactionEntity;
import org.xhy.community.domain.expression.service.ReactionDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.interfaces.reaction.request.ToggleReactionRequest;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReactionAppService {

    private final ReactionDomainService reactionDomainService;
    private final UserDomainService userDomainService;

    public ReactionAppService(ReactionDomainService reactionDomainService,
                              UserDomainService userDomainService) {
        this.reactionDomainService = reactionDomainService;
        this.userDomainService = userDomainService;
    }

    public boolean toggle(ToggleReactionRequest request, String userId) {
        BusinessType businessType = BusinessType.fromCode(request.getBusinessType());
        ReactionEntity reaction = new ReactionEntity(businessType, request.getBusinessId(), userId, request.getReactionType());
        return reactionDomainService.toggle(reaction);
    }

    public java.util.List<ReactionSummaryDTO> getSummary(String businessTypeCode, String businessId, String currentUserId) {
        BusinessType businessType = BusinessType.fromCode(businessTypeCode);

        Map<String, Integer> counts = reactionDomainService.getCounts(businessType, businessId);
        Set<String> userTypes = reactionDomainService.getUserTypes(businessType, businessId, currentUserId);
        Map<String, List<String>> usersByType = reactionDomainService.getUsersByType(businessType, businessId);

        // 批量拉取用户信息
        Set<String> allUserIds = usersByType.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        Map<String, UserEntity> userMap = userDomainService.getUserEntityMapByIds(allUserIds);

        List<ReactionSummaryDTO> list = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            String type = e.getKey();
            Integer cnt = e.getValue();
            ReactionSummaryDTO dto = new ReactionSummaryDTO();
            dto.setReactionType(type);
            dto.setCount(cnt);
            dto.setUserReacted(userTypes.contains(type));

            List<String> uids = usersByType.getOrDefault(type, Collections.emptyList());
            List<ReactionUserDTO> users = uids.stream()
                    .map(uid -> {
                        UserEntity u = userMap.get(uid);
                        return new ReactionUserDTO(uid, u != null ? u.getName() : null, u != null ? u.getAvatar() : null);
                    })
                    .collect(Collectors.toList());
            dto.setUsers(users);
            list.add(dto);
        }

        // 保证稳定顺序：按 reactionType 字母序
        list.sort(Comparator.comparing(ReactionSummaryDTO::getReactionType));
        return list;
    }

    public Map<String, List<ReactionSummaryDTO>> getSummaryBatch(String businessTypeCode, List<String> businessIds, String currentUserId) {
        BusinessType businessType = BusinessType.fromCode(businessTypeCode);

        Map<String, Map<String, Integer>> counts = reactionDomainService.getCountsBatch(businessType, businessIds);
        Map<String, Set<String>> userTypes = reactionDomainService.getUserTypesBatch(businessType, businessIds, currentUserId);
        Map<String, Map<String, List<String>>> usersByType = reactionDomainService.getUsersByTypeBatch(businessType, businessIds);

        // 批量用户信息
        Set<String> allUserIds = usersByType.values().stream()
                .flatMap(map -> map.values().stream())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        Map<String, UserEntity> userMap = userDomainService.getUserEntityMapByIds(allUserIds);

        Map<String, List<ReactionSummaryDTO>> result = new HashMap<>();
        for (String bid : counts.keySet()) {
            Map<String, Integer> c = counts.getOrDefault(bid, Collections.emptyMap());
            Set<String> ut = userTypes.getOrDefault(bid, Collections.emptySet());
            Map<String, List<String>> ubt = usersByType.getOrDefault(bid, Collections.emptyMap());

            List<ReactionSummaryDTO> list = new ArrayList<>();
            for (Map.Entry<String, Integer> e : c.entrySet()) {
                String type = e.getKey();
                Integer cnt = e.getValue();
                ReactionSummaryDTO dto = new ReactionSummaryDTO();
                dto.setReactionType(type);
                dto.setCount(cnt);
                dto.setUserReacted(ut.contains(type));

                List<String> uids = ubt.getOrDefault(type, Collections.emptyList());
                List<ReactionUserDTO> users = uids.stream()
                        .map(uid -> {
                            UserEntity u = userMap.get(uid);
                            return new ReactionUserDTO(uid, u != null ? u.getName() : null, u != null ? u.getAvatar() : null);
                        })
                        .collect(Collectors.toList());
                dto.setUsers(users);
                list.add(dto);
            }
            list.sort(Comparator.comparing(ReactionSummaryDTO::getReactionType));
            result.put(bid, list);
        }

        return result;
    }
}

