package com.axzl.biz.service;

import com.axzl.biz.domain.AIModelConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * AI大模型配置Service接口
 * 
 * @author liuchao
 */
public interface IAIModelConfigService extends IService<AIModelConfig>
{
    /**
     * 查询AI大模型配置列表
     * 
     * @param aiModelConfig 查询条件
     * @return AI大模型配置列表
     */
    List<AIModelConfig> selectAIModelConfigList(AIModelConfig aiModelConfig);

    /**
     * 根据模型名称查询配置
     * 
     * @param modelName 模型名称
     * @return AI大模型配置
     */
    AIModelConfig selectByModelName(String modelName);

    /**
     * 查询默认模型配置
     * 
     * @param modelType 模型类型(text/embedding/image/audio)
     * @return AI大模型配置
     */
    AIModelConfig selectDefaultModelByType(String modelType);

    /**
     * 新增AI大模型配置
     * 
     * @param aiModelConfig AI大模型配置
     * @param username 创建人
     * @return 结果
     */
    boolean insertAIModelConfig(AIModelConfig aiModelConfig, String username);

    /**
     * 修改AI大模型配置
     * 
     * @param aiModelConfig AI大模型配置
     * @param username 更新人
     * @return 结果
     */
    boolean updateAIModelConfig(AIModelConfig aiModelConfig, String username);

    /**
     * 切换默认模型
     * 
     * @param id 模型ID
     * @return 结果
     */
    boolean switchDefaultModel(Long id);

    /**
     * 批量删除AI大模型配置
     * 
     * @param ids 需要删除的模型ID数组
     * @return 结果
     */
    boolean deleteAIModelConfigByIds(Long[] ids);

    /**
     * 校验模型名称是否唯一
     * 
     * @param aiModelConfig AI大模型配置
     * @return 结果
     */
    boolean checkModelNameUnique(AIModelConfig aiModelConfig);
}
