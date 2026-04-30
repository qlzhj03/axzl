package com.axzl.biz.knowledge.service;

import com.axzl.biz.knowledge.domain.BizDocument;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 上传文档 服务层
 *
 * @author liuchao
 */
public interface IBizDocumentService extends IService<BizDocument> {

    /**
     * 查询文档列表
     *
     * @param document 查询条件
     * @return 文档集合
     */
    List<BizDocument> selectDocumentList(BizDocument document);

    /**
     * 通过ID查询文档
     *
     * @param id 文档ID
     * @return 文档信息
     */
    BizDocument selectDocumentById(Long id);

    /**
     * 新增文档
     *
     * @param document 文档信息
     * @return 结果
     */
    int insertDocument(BizDocument document);

    /**
     * 修改文档
     *
     * @param document 文档信息
     * @return 结果
     */
    int updateDocument(BizDocument document);

    /**
     * 批量删除文档
     *
     * @param ids 需要删除的文档ID
     * @return 结果
     */
    int deleteDocumentByIds(Long[] ids);

    /**
     * 更新文档向量状态
     *
     * @param id 文档ID
     * @param vectorStatus 向量状态
     * @return 结果
     */
    int updateVectorStatus(Long id, String vectorStatus);
}
