package com.axzl.biz.service;

import com.axzl.biz.domain.BizKnowledgeMeta;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 知识库元数据 服务层
 *
 * @author liuchao
 */
public interface IBizKnowledgeMetaService extends IService<BizKnowledgeMeta> {

    /**
     * 查询知识库列表
     *
     * @param knowledgeMeta 查询条件
     * @return 知识库集合
     */
    List<BizKnowledgeMeta> selectKnowledgeMetaList(BizKnowledgeMeta knowledgeMeta);

    /**
     * 通过ID查询知识
     *
     * @param id 知识ID
     * @return 知识信息
     */
    BizKnowledgeMeta selectKnowledgeMetaById(Long id);

    /**
     * 新增知识
     *
     * @param knowledgeMeta 知识信息
     * @return 结果
     */
    int insertKnowledgeMeta(BizKnowledgeMeta knowledgeMeta);

    /**
     * 修改知识
     *
     * @param knowledgeMeta 知识信息
     * @return 结果
     */
    int updateKnowledgeMeta(BizKnowledgeMeta knowledgeMeta);

    /**
     * 批量删除知识
     *
     * @param ids 需要删除的知识ID
     * @return 结果
     */
    int deleteKnowledgeMetaByIds(Long[] ids);

    /**
     * 通过向量ID查询知识
     *
     * @param vectorId Milvus向量ID
     * @return 知识信息
     */
    BizKnowledgeMeta selectKnowledgeMetaByVectorId(String vectorId);
}
