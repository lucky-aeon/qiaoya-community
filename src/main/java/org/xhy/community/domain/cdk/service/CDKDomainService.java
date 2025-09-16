package org.xhy.community.domain.cdk.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
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
        // 1. 验证CDK有效性
        CDKEntity cdk = getCDKByCode(cdkCode);
        
        if (!cdk.isUsable()) {
            throw new BusinessException(CDKErrorCode.CDK_NOT_USABLE);
        }
        
        // 2. 标记CDK已使用
        markCDKAsUsed(cdkCode, userId);
        
        // 3. 发布CDK激活事件
        CDKActivatedEvent event = new CDKActivatedEvent(userId, cdkCode, cdk.getCdkType(), cdk.getTargetId());
        applicationEventPublisher.publishEvent(event);
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
}