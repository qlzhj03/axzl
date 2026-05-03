package com.axzl.biz.config;

import com.axzl.biz.service.SpringAIClientFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * VectorStore配置监听器
 * 在应用启动时和模型配置变更时，更新VectorStore使用的EmbeddingModel
 *
 * @author liuchao
 */
@Component
public class VectorStoreConfigListener implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfigListener.class);
    @Lazy
    @Autowired
    private SpringAIClientFactory clientFactory;

    @Autowired(required = false)
    private VectorStore vectorStore;

    private boolean initialized = false;

    /**
     * 应用上下文刷新后初始化，此时所有Bean已创建完成，可安全使用@Lazy代理
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (initialized) {
            return;
        }
        initialized = true;
        log.info("应用上下文刷新，更新VectorStore配置");
        updateVectorStoreEmbeddingModel();
    }

    /**
     * 更新VectorStore的EmbeddingModel
     * 注意：由于Spring AI的VectorStore实现可能不同，这里使用反射方式更新
     */
    private void updateVectorStoreEmbeddingModel() {
        if (vectorStore == null) {
            log.warn("VectorStore未配置，跳过更新");
            return;
        }

        try {
            // 获取默认的EmbeddingModel
            EmbeddingModel embeddingModel = clientFactory.getDefaultEmbeddingModel();
            
            if (embeddingModel == null) {
                log.warn("默认EmbeddingModel未配置，跳过更新");
                return;
            }

            // 尝试通过反射更新VectorStore中的embeddingModel字段
            Field field = findEmbeddingModelField(vectorStore.getClass());
            if (field != null) {
                field.setAccessible(true);
                field.set(vectorStore, embeddingModel);
                log.info("成功更新VectorStore的EmbeddingModel: {}", embeddingModel.getClass().getName());
            } else {
                log.warn("未在VectorStore中找到embeddingModel字段，可能需要手动配置");
            }
        } catch (Exception e) {
            log.error("更新VectorStore的EmbeddingModel失败", e);
        }
    }

    /**
     * 查找embeddingModel字段
     */
    private Field findEmbeddingModelField(Class<?> clazz) {
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("embeddingModel");
                return field;
            } catch (NoSuchFieldException e) {
                // 继续查找父类
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 公开方法：手动触发更新
     * 当模型配置变更时调用
     */
    public void refresh() {
        log.info("手动触发VectorStore配置刷新");
        // 清除客户端缓存
        clientFactory.refreshCache();
        // 重新设置EmbeddingModel
        updateVectorStoreEmbeddingModel();
    }
}
