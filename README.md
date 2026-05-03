# 安心诊聊平台（AXZL）项目文档

## 快速开始

```bash
# 1. 初始化数据库（会自动创建axzl库，包含系统表+业务表+AI模型+测试数据）
mysql -u root -p < sql/init.sql

# 2. 启动后端
cd axzl-admin && mvn spring-boot:run

# 3. 启动前端
cd axzl-vue && npm run dev
```

---

## 一、项目概述

基于 **Spring Boot 4.0.3 + Spring AI 2.0.0-M3 + Milvus 2.6.8** 构建面向C端用户的智能问诊平台。

**核心约束**：
- **不修改现有模块**（axzl-admin、axzl-framework、axzl-system、axzl-common）
- **仅新增业务代码**，遵循若依框架规范
- **混合存储方案**：MySQL存储结构化元数据，Milvus存储向量和源文件

### 技术架构

```
表现层 (axzl-admin): RESTful API Controller
    ↓
业务层 (axzl-biz): 问诊/知识库/档案/AI服务
    ↓
数据访问层: MyBatis Plus (MySQL) + Spring AI ChatClient + MilvusVectorStore
    ↓
基础设施层: MySQL 8.0 | Redis 7.0 | Milvus 2.6.8 | DeepSeek API | 本地Embedding
```

### RAG问诊流程

```
用户输入症状 → Embedding向量化 → Milvus向量检索
→ 构建Prompt(系统提示+检索结果+历史) → ChatClient生成回复
→ 保存对话消息 → 返回响应
```

### 文档上传处理流程

```
用户上传文件 → 保存到本地 → 创建数据库记录 → 异步处理:
  ├─ 读取文档内容
  ├─ 文本切分(小文档整体向量化，大文档按段落切分)
  ├─ Embedding向量化
  └─ 存储到Milvus + 更新MySQL状态
```

---

## 二、开发规范

### 2.1 命名规范

#### 文件命名

| 类型 | 规则 | 示例 |
| :--- | :--- | :--- |
| Controller | 大驼峰 + Controller | `SysOrderController.java` |
| Service接口 | I + 大驼峰 + Service | `ISysOrderService.java` |
| Service实现 | 大驼峰 + ServiceImpl | `SysOrderServiceImpl.java` |
| Mapper | 大驼峰 + Mapper | `SysOrderMapper.java` |
| Entity | 大驼峰 | `SysOrder.java` |

#### 方法命名

| 操作 | 前缀 | 示例 |
| :--- | :--- | :--- |
| 查询列表 | `selectList`, `list` | `selectOrderList` |
| 查询单个 | `selectById`, `get` | `selectOrderById` |
| 新增 | `insert`, `add` | `insertOrder` |
| 更新 | `update`, `edit` | `updateOrder` |
| 删除 | `delete`, `remove` | `deleteOrderById` |

### 2.2 分层开发规范

#### Controller层

**关键注解**：
- `@PreAuthorize("@ss.hasPermi('xxx')")` - 权限校验
- `@Log(title = "xxx", businessType = BusinessType.xxx)` - 操作日志
- `@Validated` - 参数校验

#### Service / Mapper / Entity 层

使用 MyBatis-Plus 编写代码实现数据库操作，不再使用 XML 文件。

### 2.3 数据库操作规范

#### 表设计规范

所有业务表必须包含通用字段：

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `create_by` | VARCHAR(64) | 创建人 |
| `create_time` | DATETIME | 创建时间 |
| `update_by` | VARCHAR(64) | 更新人 |
| `update_time` | DATETIME | 更新时间 |
| `remark` | VARCHAR(500) | 备注 |

### 2.4 API接口规范

#### 接口命名

| 操作 | HTTP方法 | 路径示例 |
| :--- | :--- | :--- |
| 查询列表 | GET | `/order/list` |
| 查询单个 | GET | `/order/{orderId}` |
| 新增 | POST | `/order` |
| 更新 | PUT | `/order` |
| 删除 | DELETE | `/order/{orderIds}` |

#### 响应格式

```json
// 成功响应
{ "code": 200, "msg": "操作成功", "data": {...} }

// 分页响应
{ "code": 200, "msg": "操作成功", "data": { "rows": [...], "total": 100 } }
```

#### 状态码

| 状态码 | 含义 |
| :--- | :--- |
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 500 | 服务器错误 |

### 2.5 异常处理

| 异常 | 使用场景 |
| :--- | :--- |
| `ServiceException` | 业务逻辑异常 |
| `UserException` | 用户相关异常 |
| `FileException` | 文件操作异常 |

### 2.6 安全规范

- 所有接口必须添加 `@PreAuthorize` 权限注解
- 使用 MyBatis-Plus 参数绑定 `#{}`，禁止字符串拼接 SQL
- 启用 XSS 过滤器

### 2.7 Controller模板

```java
@RestController
@RequestMapping("/system/example")
public class SysExampleController extends BaseController {

    @Autowired
    private ISysExampleService exampleService;

    @PreAuthorize("@ss.hasPermi('system:example:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysExample example) {
        startPage();
        return getDataTable(exampleService.selectExampleList(example));
    }

    @PreAuthorize("@ss.hasPermi('system:example:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(exampleService.selectExampleById(id));
    }

    @PreAuthorize("@ss.hasPermi('system:example:add')")
    @Log(title = "示例管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysExample example) {
        return toAjax(exampleService.insertExample(example));
    }

    @PreAuthorize("@ss.hasPermi('system:example:edit')")
    @Log(title = "示例管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysExample example) {
        return toAjax(exampleService.updateExample(example));
    }

    @PreAuthorize("@ss.hasPermi('system:example:remove')")
    @Log(title = "示例管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(exampleService.deleteExampleByIds(ids));
    }
}
```

---

## 三、数据库设计

### 3.1 业务表

| 表名 | 说明 |
|------|------|
| `biz_knowledge_meta` | 知识库元数据（问题、答案、标签、向量ID） |
| `biz_document` | 上传文档（文件名、类型、向量状态） |
| `biz_chat_session` | 问诊会话（用户、标题、满意度） |
| `biz_chat_message` | 对话消息（角色、内容、检索结果） |
| `biz_user_profile` | 用户健康档案（过敏史、病史） |
| `biz_ai_model_config` | AI模型配置（提供商、参数、默认标识） |

### 3.2 Milvus Collection

**Collection: `vector_store`** - 1024维浮点向量，IVF_FLAT索引，COSINE相似度

---

## 四、API接口设计

### 4.1 前端API（JWT认证）

| HTTP方法 | 路径 | 说明 |
|----------|------|------|
| POST | `/biz/consultation/chat` | 智能问诊对话 |
| GET | `/biz/consultation/list` | 问诊历史列表 |
| GET | `/biz/consultation/{sessionId}` | 会话详情 |
| DELETE | `/biz/consultation/{sessionId}` | 删除会话 |
| POST | `/biz/knowledge/document/upload` | 上传文档 |
| GET | `/biz/knowledge/document/list` | 文档列表 |

### 4.2 后台管理接口（权限控制）

| HTTP方法 | 路径 | 权限标识 |
|----------|------|----------|
| GET | `/biz/knowledge/list` | `biz:knowledge:list` |
| POST | `/biz/knowledge` | `biz:knowledge:add` |
| PUT | `/biz/knowledge` | `biz:knowledge:edit` |
| DELETE | `/biz/knowledge/{ids}` | `biz:knowledge:remove` |

### 4.3 文档管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/biz/knowledge/document/list` | GET | 查询文档列表 |
| `/biz/knowledge/document/{id}` | GET | 查询文档详情 |
| `/biz/knowledge/document/upload` | POST | 上传文档 |
| `/biz/knowledge/document/{id}` | DELETE | 删除文档 |
| `/biz/knowledge/document/reprocess/{id}` | POST | 重新处理 |

### 4.4 AI模型管理接口

```
GET    /system/aiModel/list              # 查询列表
GET    /system/aiModel/{id}              # 查询详情
POST   /system/aiModel                   # 新增
PUT    /system/aiModel                   # 修改
PUT    /system/aiModel/switchDefault/{id} # 切换默认
DELETE /system/aiModel/{ids}             # 删除
POST   /system/aiModel/export            # 导出
```

### 4.5 问诊记录管理接口

```
PUT /biz/consultation/title/{sessionId}   # 修改会话标题
PUT /biz/consultation/pin/{sessionId}     # 置顶/取消置顶
```

---

## 五、Spring AI 集成

### 5.1 架构设计

```
应用层 (AiConsultationService / DocumentProcessorService)
    ↓
SpringAIClientFactory (工厂模式，根据DB配置动态创建客户端)
    ↓
biz_ai_model_config (数据库配置表)
    ↓
Spring AI Framework (OpenAiChatModel / OpenAiEmbeddingModel / MilvusVectorStore)
    ↓
底层AI提供商 (DeepSeek / OpenAI / Anthropic / 本地模型)
```

### 5.2 核心组件

#### SpringAIClientFactory

```java
// 获取默认对话客户端
ChatClient client = clientFactory.getDefaultChatClient();

// 根据模型ID获取
ChatClient client = clientFactory.getChatClientByModelId(1L);

// 根据模型编码获取
ChatClient client = clientFactory.getChatClientByModelCode("deepseek-v4-flash");

// 获取默认Embedding模型
EmbeddingModel model = clientFactory.getDefaultEmbeddingModel();

// 刷新客户端缓存
clientFactory.refreshCache();
```

#### VectorStoreConfigListener

- 应用启动时自动初始化，通过反射更新 VectorStore 的 EmbeddingModel
- 配置变更时自动刷新客户端缓存

#### 使用示例

```java
@Service
public class YourService {
    @Autowired
    private SpringAIClientFactory clientFactory;

    public String callAI(String userInput) {
        ChatClient chatClient = clientFactory.getDefaultChatClient();
        return chatClient.prompt().user(userInput).call().content();
    }

    public float[] getEmbedding(String text) {
        EmbeddingModel model = clientFactory.getDefaultEmbeddingModel();
        return model.embed(text);
    }
}
```

### 5.3 Spring AI 2.0 M3 API 变化

#### OpenAiApi 创建

```java
// 使用 Builder 模式
OpenAiApi openAiApi = OpenAiApi.builder()
    .baseUrl(baseUrl)
    .apiKey(apiKey)
    .embeddingsPath("/v1/embeddings")
    .build();
```

#### OpenAiEmbeddingModel 创建

```java
OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(openAiApi);
```

#### OpenAiChatOptions 参数

```java
OpenAiChatOptions options = OpenAiChatOptions.builder()
    .model("deepseek-v4-flash")
    .temperature(0.7)
    .maxCompletionTokens(4096)  // 注意：不是 maxTokens
    .build();
```

#### IDE 错误提示

如果 IDE 报 `无法解析构造函数` 或 `无法解析方法`，但代码能正常编译运行，这是 IDE 缓存问题：

```bash
mvn clean install -DskipTests
# 然后 Invalidate Caches / Restart IDE
```

### 5.4 支持的提供商

| 提供商 | Provider标识 | API兼容 | 说明 |
|--------|-------------|---------|------|
| DeepSeek | `deepseek` | OpenAI兼容 | 性价比高 |
| OpenAI | `openai` | 原生 | 需配置API密钥 |
| Anthropic | `anthropic` | OpenAI兼容 | 待完善 |
| 本地模型 | `localhost` | OpenAI兼容 | 如 bge-large-zh-v1.5 |

### 5.5 配置说明

#### 数据库配置（优先）

在"系统工具 → AI模型管理"中配置，保存后自动生效，无需重启。

#### application.yml 配置（备用）

```yaml
spring:
  ai:
    openai:
      api-key: ${AI_DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-v4-flash
      embedding:
        base-url: http://localhost:8718
        api-key: sk-xxx
        options:
          model: bge-large-zh-v1.5
          dimensions: 1024
        embeddings-path: /v1/embeddings
    vectorstore:
      milvus:
        client:
          host: localhost
          port: 19530
        collectionName: vector_store
        embeddingDimension: 1024
        indexType: IVF_FLAT
        metricType: COSINE
```

### 5.6 工作流程

#### 启动流程
```
应用启动 → VectorStoreConfigListener.onApplicationEvent()
→ 从数据库读取默认模型 → 创建客户端 → 更新VectorStore的EmbeddingModel → 缓存
```

#### 配置变更流程
```
用户修改配置 → 更新数据库 → 调用refresh() → 清除缓存 → 下次调用时重建客户端
```

### 5.7 关键依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-milvus</artifactId>
</dependency>
```

### 5.8 注意事项

1. 每种类型（chat/embedding）必须有一个默认且启用的模型
2. 首次调用会创建客户端（约100-500ms），后续使用缓存
3. API密钥当前为明文存储，生产环境建议加密

---

## 六、AI模型管理

### 6.1 功能概述

在系统工具菜单中新增"AI模型管理"功能：
- 管理和配置底层AI大模型（DeepSeek、OpenAI、Anthropic、本地模型）
- 切换默认使用的AI模型
- 配置模型参数（温度、Token数、惩罚系数等）
- 支持多种模型类型（对话、Embedding、图像）

### 6.2 访问入口

登录系统 → **系统工具** > **AI模型管理**

### 6.3 预设模型

| 模型名称 | 类型 | 提供商 | 状态 |
|---------|------|--------|------|
| DeepSeek V4 Flash | 对话 | DeepSeek | 默认 |
| DeepSeek V4 Pro | 对话 | DeepSeek | 备用 |
| bge-large-zh-v1.5 | Embedding | localhost | 默认 |
| GPT-4o | 对话 | OpenAI | 停用 |
| text-embedding-3-large | Embedding | OpenAI | 停用 |
| Claude 3.7 Sonnet | 对话 | Anthropic | 停用 |

### 6.4 常用操作

#### 切换默认模型

找到目标模型 → 点击"设为默认" → 确认。同类型只能有一个默认模型。

#### 调整模型参数

- **温度（temperature）**：0.0-0.3 准确，0.5-0.7 平衡，0.8-1.0 创意
- **最大Token（maxTokens）**：控制回答长度
- **频率惩罚**：减少重复内容

#### 添加新模型

点击"新增" → 填写模型名称、提供商、API地址、密钥、参数 → 保存

### 6.5 权限说明

| 角色 | 查看 | 新增 | 修改 | 删除 | 切换默认 | 导出 |
|------|------|------|------|------|---------|------|
| 超级管理员 | Y | Y | Y | Y | Y | Y |
| 医疗专家 | Y | N | N | N | Y | N |

### 6.6 技术架构

#### 后端文件

```
axzl-biz/
├── domain/AIModelConfig.java              # 实体类
├── mapper/AIModelConfigMapper.java        # Mapper接口
├── service/IAIModelConfigService.java     # Service接口
├── service/impl/AIModelConfigServiceImpl.java
├── service/SpringAIClientFactory.java     # AI客户端工厂接口
└── service/impl/SpringAIClientFactoryImpl.java

axzl-admin/
└── controller/biz/AIModelConfigController.java
```

#### 前端文件

```
axzl-vue/
├── src/api/system/aiModel.ts              # API接口
└── src/views/system/aiModel/index.vue     # 页面组件
```

#### 数据库

```
sql/
└── init.sql                               # 统一初始化脚本（含AI模型配置）
```

### 6.7 故障排查

| 问题 | 原因 | 解决 |
|------|------|------|
| 看不到菜单 | 权限未分配 | 退出重新登录，清除缓存 |
| 模型列表为空 | SQL未执行 | 执行 `sql/init.sql` |
| 切换默认后未生效 | 缓存未更新 | 重启应用或清除Redis缓存 |
| 404 Embedding错误 | 数据库URL错误 | 更新 `biz_ai_model_config` 中的 `api_base_url` |

### 6.8 后续优化

1. API密钥加密存储
2. 连接测试功能
3. 使用统计（Token消耗、调用次数）
4. 负载均衡（多个同类型模型）
5. 实时监控面板

---

## 七、文档管理

### 7.1 功能概述

文档管理模块提供完整的文档上传、查看、删除和向量化处理功能，支持与 Milvus 向量数据库集成。

**核心特性**：
- 支持 txt/word/docx/pdf/md/jsonl 格式
- 上传 → 保存 → 向量化 → 存储（全流程自动化）
- 异步处理，不阻塞用户操作
- 实时状态追踪

### 7.2 访问入口

登录系统 → **知识库管理** → **文档管理**

### 7.3 使用指南

#### 上传文档

1. 点击"上传文档"按钮
2. 拖拽或选择文件（支持 txt/word/pdf/md/jsonl，最大 10MB）
3. 点击确定，等待上传完成

#### 查看处理状态

- **未处理** → **处理中** → **已完成**
- 失败文档可点击"重试"重新处理

#### 搜索文档

支持按文件名（模糊）、文件类型、处理状态筛选。

### 7.4 技术架构

```
用户上传文件
    ↓
KnowledgeController (创建数据库记录)
    ↓
DocumentProcessorService (异步处理)
    ├─ CustomDocumentReader 读取文档内容
    ├─ 按段落切分（小文档<3000字符整体向量化）
    ├─ EmbeddingModel 向量化
    ├─ VectorStore 存储到 Milvus
    └─ 更新 biz_document 状态 + 创建 biz_knowledge_meta 记录
```

### 7.5 文件结构

```
axzl-vue/
├── src/api/biz/document.ts                # API接口
├── src/views/biz/document/index.vue       # 页面组件
└── src/types/api/biz/document.ts          # TypeScript类型

axzl-admin/
└── controller/biz/KnowledgeController.java # 文档管理接口

axzl-biz/
├── service/DocumentProcessorService.java  # 文档处理服务
└── utils/CustomDocumentReader.java        # 文档读取器

sql/
└── init.sql                               # 统一初始化脚本
```

### 7.6 权限控制

| 权限标识 | 说明 |
|---------|------|
| `biz:document:list` | 查看文档列表 |
| `biz:document:query` | 查看文档详情 |
| `biz:document:upload` | 上传文档 |
| `biz:document:remove` | 删除文档 |
| `biz:document:edit` | 重新处理 |

### 7.7 前端集成示例

```vue
<file-upload
  v-model="fileList"
  :file-type="['md', 'jsonl', 'txt', 'pdf', 'doc', 'docx']"
  :file-size="10"
  :limit="5"
  action="/biz/knowledge/document/upload"
/>
```

### 7.8 典型应用场景

#### 知识库构建
上传医学文档 → 自动提取文本切分 → 生成向量存储到 Milvus → AI问诊时检索相关知识

#### JSONL数据导入
```jsonl
{"question": "什么是高血压？", "answer": "高血压是指...", "label": "心血管疾病"}
```

### 7.9 关键配置

| 参数 | 值 | 说明 |
|------|-----|------|
| 小文档阈值 | <3000字符 | 整体向量化 |
| 切分块大小 | 800字符 | 按段落边界切分 |
| 重叠大小 | 50字符 | 保持上下文连续 |
| 批量向量化 | 20条/批 | 控制API调用频率 |

### 7.10 支持的文件格式

| 格式 | 解析库 | 说明 |
|------|--------|------|
| txt | Java原生 | 纯文本 |
| word(.docx) | Apache POI | Office Open XML |
| pdf | Apache PDFBox | PDF文本提取 |
| md | 自定义解析 | Markdown转纯文本 |
| jsonl | 自定义解析 | 逐行JSON |

### 7.11 常见问题

| 问题 | 原因 | 解决 |
|------|------|------|
| 上传后一直是"未处理" | 异步任务排队中 | 等待几秒后刷新，检查日志 |
| 向量化失败 | Milvus/Embedding服务异常 | 检查服务状态，点击"重试" |
| 上传失败 | 文件过大或格式不支持 | 检查文件大小(<10MB)和格式 |

### 7.12 扩展开发

#### 添加新文件格式
1. `MimeTypeUtils.java` 添加扩展名
2. `CustomDocumentReader.java` 添加读取逻辑
3. `KnowledgeController.java` 的 `getFileType()` 添加判断

---

## 八、问诊记录管理

### 8.1 功能概述

完善了问诊记录管理的前后端代码，为不同角色分配合理权限。

**核心功能**：
1. **查看问诊记录** - 用户可以查看自己的问诊记录列表
2. **搜索功能** - 按用户昵称、会话标题、状态、时间范围搜索
3. **修改会话标题** - 用户可自定义会话标题
4. **置顶会话** - 置顶重要会话，排在列表前面
5. **删除会话** - 删除不需要的会话记录
6. **满意度评价** - 对问诊会话进行1-5星评分

### 8.2 数据库变更

#### 表结构变更

```sql
ALTER TABLE biz_chat_session ADD COLUMN is_pinned tinyint(1) DEFAULT 0 COMMENT '是否置顶(0否 1是)';
```

#### 新增菜单权限

| ID | 权限标识 | 说明 |
|----|---------|------|
| 4020 | `biz:consultation:edit` | 问诊编辑 |
| 4021 | `biz:consultation:pin` | 问诊置顶 |
| 4022 | `biz:consultation:satisfaction` | 满意度评价 |

### 8.3 角色权限分配

| 功能 | 超级管理员 | 医疗专家 | 普通医生 | 护士助理 | 患者 |
|------|-----------|---------|---------|---------|------|
| 查看所有问诊 | Y | Y | Y | Y(只读) | 仅自己 |
| 删除问诊 | Y | Y | N | N | 仅自己 |
| 编辑标题 | Y | Y | Y | N | 仅自己 |
| 置顶会话 | Y | Y | Y | N | 仅自己 |
| 满意度评价 | Y | N | N | N | 仅自己 |

### 8.4 后端实现

#### 实体类增强

`ChatSession.java` 添加 `isPinned` 字段。

#### Service层新增方法

```java
// 更新会话标题
updateTitle(String sessionId, String title)

// 更新置顶状态
updatePinStatus(String sessionId, Integer isPinned)
```

#### Controller层新增接口

```
PUT /biz/consultation/title/{sessionId}   # 修改会话标题
PUT /biz/consultation/pin/{sessionId}     # 置顶/取消置顶
```

### 8.5 前端实现

#### 新增功能

1. **置顶标识** - 会话标题前显示"置顶"标签（黄色）
2. **操作按钮** - 查看、编辑、置顶、删除
3. **编辑标题对话框** - 最大100字符，实时字数统计
4. **权限控制** - 无权限的按钮自动隐藏

### 8.6 注意事项

1. 患者只能操作自己的会话记录
2. 置顶的会话始终排在最前面，同级别按最后消息时间倒序
3. 删除操作需要二次确认
4. 所有异步操作都有 loading 状态

### 8.7 后续优化

1. 批量置顶/取消置顶
2. 会话归档功能
3. 会话导出（PDF/Word）
4. 会话标签分类

---

## 九、问题修复记录

### 9.1 MyBatis参数映射错误

#### 问题

```
Type handler was null on parameter mapping for property 'params'.
It was either not specified and/or could not be found for the javaType (java.util.Map) : jdbcType (null) combination.
```

#### 原因

`BizKnowledgeMeta` 继承了 `BaseEntity`，其中 `params` 字段（Map<String, Object>）不是数据库列，但 MyBatis-Plus INSERT 时尝试映射。

#### 解决

在实体类中显式声明并排除：

```java
@TableField(exist = false)
@JsonIgnore
private Map<String, Object> params;
```

#### 排查要点

继承 `BaseEntity` 的实体类，如果出现类似错误，检查 `params` 和 `searchValue` 字段是否加了 `@TableField(exist = false)`。

### 9.2 循环依赖问题

#### 问题

```
The dependencies of some of the beans in the application context form a cycle:
AIModelConfigServiceImpl → VectorStoreConfigListener → SpringAIClientFactoryImpl → AIModelConfigServiceImpl
```

#### 解决

在 `VectorStoreConfigListener` 中使用 `@Lazy` 注入 `SpringAIClientFactory`，并将初始化逻辑从 `@PostConstruct` 移到 `ContextRefreshedEvent`。

### 9.3 Embedding API 404错误

#### 问题

```
NonTransientAiException: 404 - {"detail":"Not Found"}
```

#### 原因

`DocumentProcessorService` 直接注入了 `application.yml` 自动配置的 `EmbeddingModel`（指向 DeepSeek），而不是从数据库动态获取。

#### 解决

改为通过 `SpringAIClientFactory.getDefaultEmbeddingModel()` 从数据库获取，并确保数据库中 embedding 模型的 `api_base_url` 正确（如 `http://localhost:8718`）。

---

**版本**: v1.0 | **更新日期**: 2026-05-03
