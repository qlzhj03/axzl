package com.axzl.biz.knowledge.service.impl;

import com.axzl.biz.knowledge.domain.BizKnowledgeMeta;
import com.axzl.biz.knowledge.mapper.BizKnowledgeMetaMapper;
import com.axzl.biz.knowledge.service.IBizKnowledgeMetaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库元数据 服务层实现
 *
 * @author liuchao
 */
@Service
public class BizKnowledgeMetaServiceImpl extends ServiceImpl<BizKnowledgeMetaMapper, BizKnowledgeMeta>
        implements IBizKnowledgeMetaService {

    @Override
    public List<BizKnowledgeMeta> selectKnowledgeMetaList(BizKnowledgeMeta knowledgeMeta) {
        LambdaQueryWrapper<BizKnowledgeMeta> wrapper = new LambdaQueryWrapper<>();
        if (knowledgeMeta.getLabel() != null && !knowledgeMeta.getLabel().isEmpty()) {
            wrapper.eq(BizKnowledgeMeta::getLabel, knowledgeMeta.getLabel());
        }
        if (knowledgeMeta.getQuestion() != null && !knowledgeMeta.getQuestion().isEmpty()) {
            wrapper.like(BizKnowledgeMeta::getQuestion, knowledgeMeta.getQuestion());
        }
        if (knowledgeMeta.getSourceType() != null && !knowledgeMeta.getSourceType().isEmpty()) {
            wrapper.eq(BizKnowledgeMeta::getSourceType, knowledgeMeta.getSourceType());
        }
        if (knowledgeMeta.getVectorStatus() != null && !knowledgeMeta.getVectorStatus().isEmpty()) {
            wrapper.eq(BizKnowledgeMeta::getVectorStatus, knowledgeMeta.getVectorStatus());
        }
        if (knowledgeMeta.getRelatedDiseases() != null && !knowledgeMeta.getRelatedDiseases().isEmpty()) {
            wrapper.like(BizKnowledgeMeta::getRelatedDiseases, knowledgeMeta.getRelatedDiseases());
        }
        wrapper.orderByDesc(BizKnowledgeMeta::getCreateTime);
        return list(wrapper);
    }

    @Override
    public BizKnowledgeMeta selectKnowledgeMetaById(Long id) {
        return getById(id);
    }

    @Override
    public int insertKnowledgeMeta(BizKnowledgeMeta knowledgeMeta) {
        return save(knowledgeMeta) ? 1 : 0;
    }

    @Override
    public int updateKnowledgeMeta(BizKnowledgeMeta knowledgeMeta) {
        return updateById(knowledgeMeta) ? 1 : 0;
    }

    @Override
    public int deleteKnowledgeMetaByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            if (removeById(id)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public BizKnowledgeMeta selectKnowledgeMetaByVectorId(String vectorId) {
        LambdaQueryWrapper<BizKnowledgeMeta> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizKnowledgeMeta::getVectorId, vectorId);
        return getOne(wrapper);
    }
}
