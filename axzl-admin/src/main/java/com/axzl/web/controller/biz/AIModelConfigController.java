package com.axzl.web.controller.biz;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.axzl.common.annotation.Log;
import com.axzl.common.core.controller.BaseController;
import com.axzl.common.core.domain.AjaxResult;
import com.axzl.common.enums.BusinessType;
import com.axzl.common.utils.poi.ExcelUtil;
import com.axzl.biz.domain.AIModelConfig;
import com.axzl.biz.service.IAIModelConfigService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AI大模型配置Controller
 * 
 * @author liuchao
 */
@RestController
@RequestMapping("/biz/aiModel")
public class AIModelConfigController extends BaseController
{
    @Autowired
    private IAIModelConfigService aiModelConfigService;

    /**
     * 查询AI大模型配置列表
     */
    @PreAuthorize("@ss.hasPermi('biz:aiModel:list')")
    @GetMapping("/list")
    public AjaxResult list(AIModelConfig aiModelConfig)
    {
        List<AIModelConfig> list = aiModelConfigService.selectAIModelConfigList(aiModelConfig);
        return success(list);
    }

    /**
     * 获取AI大模型配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('biz:aiModel:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(aiModelConfigService.getById(id));
    }

    /**
     * 根据模型名称获取配置
     */
    @PreAuthorize("@ss.hasPermi('biz:aiModel:query')")
    @GetMapping(value = "/name/{modelName}")
    public AjaxResult getInfoByName(@PathVariable("modelName") String modelName)
    {
        return success(aiModelConfigService.selectByModelName(modelName));
    }

    /**
     * 获取默认模型配置
     */
    @GetMapping(value = "/default/{modelType}")
    public AjaxResult getDefaultModel(@PathVariable("modelType") String modelType)
    {
        return success(aiModelConfigService.selectDefaultModelByType(modelType));
    }

    /**
     * 新增AI大模型配置
     */
    @PreAuthorize("@ss.hasPermi('biz:aiModel:add')")
    @Log(title = "AI大模型配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AIModelConfig aiModelConfig)
    {
        return toAjax(aiModelConfigService.insertAIModelConfig(aiModelConfig, getUsername()) ? 1 : 0);
    }

    /**
     * 修改AI大模型配置
     */
    @PreAuthorize("@ss.hasPermi('biz:aiModel:edit')")
    @Log(title = "AI大模型配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AIModelConfig aiModelConfig)
    {
        return toAjax(aiModelConfigService.updateAIModelConfig(aiModelConfig, getUsername()) ? 1 : 0);
    }

    /**
     * 切换默认模型
     */
    @PreAuthorize("@ss.hasPermi('biz:aiModel:switch')")
    @Log(title = "AI大模型配置", businessType = BusinessType.UPDATE)
    @PutMapping("/switchDefault/{id}")
    public AjaxResult switchDefault(@PathVariable("id") Long id)
    {
        return toAjax(aiModelConfigService.switchDefaultModel(id) ? 1 : 0);
    }

    /**
     * 删除AI大模型配置
     */
    @PreAuthorize("@ss.hasPermi('biz:aiModel:remove')")
    @Log(title = "AI大模型配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(aiModelConfigService.deleteAIModelConfigByIds(ids) ? 1 : 0);
    }

    /**
     * 导出AI大模型配置列表
     */
    @PreAuthorize("@ss.hasPermi('biz:aiModel:export')")
    @Log(title = "AI大模型配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AIModelConfig aiModelConfig)
    {
        List<AIModelConfig> list = aiModelConfigService.selectAIModelConfigList(aiModelConfig);
        ExcelUtil<AIModelConfig> util = new ExcelUtil<AIModelConfig>(AIModelConfig.class);
        util.exportExcel(response, list, "AI大模型配置数据");
    }
}

