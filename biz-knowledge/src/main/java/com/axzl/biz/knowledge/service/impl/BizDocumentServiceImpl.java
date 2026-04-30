package com.axzl.biz.knowledge.service.impl;

import com.axzl.biz.knowledge.domain.BizDocument;
import com.axzl.biz.knowledge.mapper.BizDocumentMapper;
import com.axzl.biz.knowledge.service.IBizDocumentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 上传文档 服务层实现
 *
 * @author liuchao
 */
@Service
public class BizDocumentServiceImpl extends ServiceImpl<BizDocumentMapper, BizDocument>
        implements IBizDocumentService {

    @Override
    public List<BizDocument> selectDocumentList(BizDocument document) {
        LambdaQueryWrapper<BizDocument> wrapper = new LambdaQueryWrapper<>();
        if (document.getFileName() != null && !document.getFileName().isEmpty()) {
            wrapper.like(BizDocument::getFileName, document.getFileName());
        }
        if (document.getFileType() != null && !document.getFileType().isEmpty()) {
            wrapper.eq(BizDocument::getFileType, document.getFileType());
        }
        if (document.getVectorStatus() != null && !document.getVectorStatus().isEmpty()) {
            wrapper.eq(BizDocument::getVectorStatus, document.getVectorStatus());
        }
        if (document.getUploadBy() != null) {
            wrapper.eq(BizDocument::getUploadBy, document.getUploadBy());
        }
        wrapper.orderByDesc(BizDocument::getCreateTime);
        return list(wrapper);
    }

    @Override
    public BizDocument selectDocumentById(Long id) {
        return getById(id);
    }

    @Override
    public int insertDocument(BizDocument document) {
        return save(document) ? 1 : 0;
    }

    @Override
    public int updateDocument(BizDocument document) {
        return updateById(document) ? 1 : 0;
    }

    @Override
    public int deleteDocumentByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            if (removeById(id)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int updateVectorStatus(Long id, String vectorStatus) {
        BizDocument document = new BizDocument();
        document.setId(id);
        document.setVectorStatus(vectorStatus);
        return updateById(document) ? 1 : 0;
    }
}
