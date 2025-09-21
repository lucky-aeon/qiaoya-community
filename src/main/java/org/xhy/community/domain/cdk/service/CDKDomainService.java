package org.xhy.community.domain.cdk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.cdk.entity.CDKEntity;
import org.xhy.community.domain.cdk.event.CDKActivatedEvent;
import org.xhy.community.domain.cdk.repository.CDKRepository;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKStatus;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CDKErrorCode;
import org.xhy.community.domain.cdk.query.CDKQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CDKDomainService {
    
    private final CDKRepository cdkRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private static final Logger log = LoggerFactory.getLogger(CDKDomainService.class);
    
    public CDKDomainService(CDKRepository cdkRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.cdkRepository = cdkRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }
    
    public List<CDKEntity> createCDKBatch(CDKType cdkType, String targetId, int quantity) {
        String batchId = UUID.randomUUID().toString();
        List<CDKEntity> cdkList = new ArrayList<>();
        
        for (int i = 0; i < quantity; i++) {
            String code = generateCDKCode();
            CDKEntity cdk = new CDKEntity(code, cdkType, targetId, batchId);
            cdkRepository.insert(cdk);
            cdkList.add(cdk);
        }
        
        return cdkList;
    }
    
    public void activateCDK(String userId, String cdkCode) {
        String masked = mask(cdkCode);
        log.info("[CDK激活] 开始处理: userId={}, cdk={}", userId, masked);
        // 1. 验证CDK有效性
        CDKEntity cdk = getCDKByCode(cdkCode);
        log.debug("[CDK激活] CDK可用性检查通过: type={}, targetId={}", cdk.getCdkType(), cdk.getTargetId());
        
        if (!cdk.isUsable()) {
            log.warn("[CDK激活] CDK不可用，拒绝处理: userId={}, cdk={}", userId, masked);
            throw new BusinessException(CDKErrorCode.CDK_NOT_USABLE);
        }
        
        // 2. 标记CDK已使用
        markCDKAsUsed(cdkCode, userId);
        log.info("[CDK激活] 已标记为已使用: userId={}, cdk={}", userId, masked);
        
        // 3. 发布CDK激活事件
        CDKActivatedEvent event = new CDKActivatedEvent(userId, cdkCode, cdk.getCdkType(), cdk.getTargetId());
        applicationEventPublisher.publishEvent(event);
        log.info("[CDK激活] 已发布事件: userId={}, type={}, targetId={}", userId, cdk.getCdkType(), cdk.getTargetId());
    }
    
    public CDKEntity getCDKById(String id) {
        CDKEntity cdk = cdkRepository.selectById(id);
        if (cdk == null) {
            throw new BusinessException(CDKErrorCode.CDK_NOT_FOUND);
        }
        return cdk;
    }
    
    public CDKEntity getCDKByCode(String code) {
        LambdaQueryWrapper<CDKEntity> queryWrapper = new LambdaQueryWrapper<CDKEntity>()
            .eq(CDKEntity::getCode, code);
        
        CDKEntity cdk = cdkRepository.selectOne(queryWrapper);
        if (cdk == null) {
            throw new BusinessException(CDKErrorCode.CDK_NOT_FOUND);
        }
        return cdk;
    }
    
    public void deleteCDK(String id) {
        CDKEntity cdk = getCDKById(id);
        if (cdk.getStatus() == CDKStatus.USED) {
            throw new BusinessException(CDKErrorCode.CDK_ALREADY_USED);
        }
        cdkRepository.deleteById(id);
    }
    
    public void markCDKAsUsed(String cdkCode, String userId) {
        CDKEntity cdk = getCDKByCode(cdkCode);
        if (!cdk.isUsable()) {
            log.warn("[CDK激活] 尝试标记已使用但CDK不可用: userId={}, cdk={}", userId, mask(cdkCode));
            throw new BusinessException(CDKErrorCode.CDK_NOT_USABLE);
        }
        cdk.markAsUsed(userId);
        cdkRepository.updateById(cdk);
    }
    
    public IPage<CDKEntity> getPagedCDKs(CDKQuery query) {
        Page<CDKEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CDKEntity> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(query.getCdkType() != null, CDKEntity::getCdkType, query.getCdkType())
                   .eq(StringUtils.hasText(query.getTargetId()), CDKEntity::getTargetId, query.getTargetId())
                   .eq(query.getStatus() != null, CDKEntity::getStatus, query.getStatus())
                   .like(StringUtils.hasText(query.getCode()), CDKEntity::getCode, query.getCode())
                   .orderByDesc(CDKEntity::getCreateTime);
        
        return cdkRepository.selectPage(page, queryWrapper);
    }
    
    private String generateCDKCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (cdkCodeExists(code));
        return code;
    }
    
    private String generateRandomCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
    
    private boolean cdkCodeExists(String code) {
        LambdaQueryWrapper<CDKEntity> queryWrapper = new LambdaQueryWrapper<CDKEntity>()
            .eq(CDKEntity::getCode, code);
        return cdkRepository.exists(queryWrapper);
    }

    private String mask(String code) {
        if (code == null || code.length() <= 4) return "****";
        int len = code.length();
        return code.substring(0, Math.min(4, len)) + "****" + code.substring(len - 2);
    }
}
