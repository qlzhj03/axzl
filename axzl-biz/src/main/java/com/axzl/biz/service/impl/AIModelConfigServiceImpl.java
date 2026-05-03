package com.axzl.biz.service.impl;

import com.axzl.biz.config.VectorStoreConfigListener;
import com.axzl.common.utils.StringUtils;
import com.axzl.biz.domain.AIModelConfig;
import com.axzl.biz.mapper.AIModelConfigMapper;
import com.axzl.biz.service.IAIModelConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * AI大模型配置Service业务层处理
 * 
 * @author liuchao
 */
@Service
public class AIModelConfigServiceImpl extends ServiceImpl<AIModelConfigMapper, AIModelConfig> implements IAIModelConfigService
{
    private static final Logger log = LoggerFactory.getLogger(AIModelConfigServiceImpl.class);

    @Autowired(required = false)
    private VectorStoreConfigListener vectorStoreConfigListener;

    /**
     * 查询AI大模型配置列表
     * 
     * @param aiModelConfig 查询条件
     * @return AI大模型配置列表
     */
    @Override
    public List<AIModelConfig> selectAIModelConfigList(AIModelConfig aiModelConfig)
    {
        LambdaQueryWrapper<AIModelConfig> wrapper = buildQueryWrapper(aiModelConfig);
        return list(wrapper);
    }

    /**
     * 构建查询条件
     * 
     * @param aiModelConfig 查询条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<AIModelConfig> buildQueryWrapper(AIModelConfig aiModelConfig)
    {
        LambdaQueryWrapper<AIModelConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(aiModelConfig.getProvider())) {
            wrapper.eq(AIModelConfig::getProvider, aiModelConfig.getProvider());
        }
        if (StringUtils.isNotEmpty(aiModelConfig.getModelName())) {
            wrapper.like(AIModelConfig::getModelName, aiModelConfig.getModelName());
        }
        if (StringUtils.isNotEmpty(aiModelConfig.getModelType())) {
            wrapper.eq(AIModelConfig::getModelType, aiModelConfig.getModelType());
        }
        if (aiModelConfig.getStatus() != null) {
            wrapper.eq(AIModelConfig::getStatus, aiModelConfig.getStatus());
        }
        if (aiModelConfig.getIsDefault() != null) {
            wrapper.eq(AIModelConfig::getIsDefault, aiModelConfig.getIsDefault());
        }
        wrapper.orderByAsc(AIModelConfig::getSortOrder)
               .orderByDesc(AIModelConfig::getCreateTime);
        return wrapper;
    }

    /**
     * 根据模型名称查询配置
     * 
     * @param modelName 模型名称
     * @return AI大模型配置
     */
    @Override
    public AIModelConfig selectByModelName(String modelName)
    {
        LambdaQueryWrapper<AIModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIModelConfig::getModelName, modelName);
        return getOne(wrapper);
    }

    /**
     * 查询默认模型配置
     * 
     * @param modelType 模型类型
     * @return AI大模型配置
     */
    @Override
    public AIModelConfig selectDefaultModelByType(String modelType)
    {
        LambdaQueryWrapper<AIModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIModelConfig::getModelType, modelType)
               .eq(AIModelConfig::getIsDefault, 1)
               .eq(AIModelConfig::getStatus, 1)
               .orderByAsc(AIModelConfig::getSortOrder)
               .last("LIMIT 1");
        return getOne(wrapper);
    }

    /**
     * 新增AI大模型配置
     * 
     * @param aiModelConfig AI大模型配置
     * @param username 创建人
     * @return 结果
     */
    @Override
    public boolean insertAIModelConfig(AIModelConfig aiModelConfig, String username)
    {
        if (!checkModelNameUnique(aiModelConfig))
        {
            throw new RuntimeException("新增模型'" + aiModelConfig.getModelName() + "'失败，模型名称已存在");
        }
        aiModelConfig.setCreateBy(username);
        return save(aiModelConfig);
    }

    /**
     * 修改AI大模型配置
     * 
     * @param aiModelConfig AI大模型配置
     * @param username 更新人
     * @return 结果
     */
    @Override
    @Transactional
    public boolean updateAIModelConfig(AIModelConfig aiModelConfig, String username)
    {
        if (!checkModelNameUnique(aiModelConfig))
        {
            throw new RuntimeException("修改模型'" + aiModelConfig.getModelName() + "'失败，模型名称已存在");
        }
        aiModelConfig.setUpdateBy(username);
        boolean result = updateById(aiModelConfig);
        
        // 如果修改成功，刷新AI客户端缓存
        if (result && vectorStoreConfigListener != null) {
            try {
                vectorStoreConfigListener.refresh();
            } catch (Exception e) {
                log.warn("刷新VectorStore配置失败", e);
            }
        }
        
        return result;
    }

    /**
     * 切换默认模型
     * 
     * @param id 模型ID
     * @return 结果
     */
    @Override
    @Transactional
    public boolean switchDefaultModel(Long id)
    {
        AIModelConfig model = getById(id);
        if (model == null)
        {
            throw new RuntimeException("模型不存在");
        }
        
        // 先取消同类型的其他默认模型
        LambdaUpdateWrapper<AIModelConfig> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AIModelConfig::getModelType, model.getModelType())
                     .set(AIModelConfig::getIsDefault, 0);
        update(updateWrapper);
        
        // 设置当前模型为默认
        AIModelConfig updateModel = new AIModelConfig();
        updateModel.setId(id);
        updateModel.setIsDefault(1);
        boolean result = updateById(updateModel);
        
        // 如果切换成功，刷新AI客户端缓存
        if (result && vectorStoreConfigListener != null) {
            try {
                vectorStoreConfigListener.refresh();
            } catch (Exception e) {
                log.warn("刷新VectorStore配置失败", e);
            }
        }
        
        return result;
    }

    /**
     * 批量删除AI大模型配置
     * 
     * @param ids 需要删除的模型ID数组
     * @return 结果
     */
    @Override
    public boolean deleteAIModelConfigByIds(Long[] ids)
    {
        return removeBatchByIds(Arrays.asList(ids));
    }

    /**
     * 校验模型名称是否唯一
     * 
     * @param aiModelConfig AI大模型配置
     * @return 结果
     */
    @Override
    public boolean checkModelNameUnique(AIModelConfig aiModelConfig)
    {
        Long id = StringUtils.isNull(aiModelConfig.getId()) ? -1L : aiModelConfig.getId();
        LambdaQueryWrapper<AIModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AIModelConfig::getModelName, aiModelConfig.getModelName());
        AIModelConfig info = getOne(wrapper);
        if (StringUtils.isNotNull(info) && info.getId().longValue() != id.longValue())
        {
            return false;
        }
        return true;
    }
}
