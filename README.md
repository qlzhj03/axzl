# 安心诊聊平台 (AXZL)

基于 Vue 3 + Spring Boot 4 的 AI 智能问诊系统，集成 RAG 检索增强生成、向量数据库、大语言模型等技术，提供专业的医疗咨询服务。

## 技术栈

### 后端
- **框架**: Spring Boot 4.0.3 + Java 21
- **ORM**: MyBatis-Plus 3.5.16
- **数据库**: MySQL 8.0 + Redis 7
- **向量数据库**: Milvus 2.6.8
- **AI 框架**: Spring AI 2.0.0-M3
- **大模型**: DeepSeek API
- **安全**: Spring Security + JWT (HS512)
- **连接池**: Druid
- **API 文档**: Knife4j (OpenAPI 3)

### 前端
- **框架**: Vue 3.5 + TypeScript
- **构建工具**: Vite 6.3
- **UI 组件**: Element Plus 2.13
- **状态管理**: Pinia 3.0
- **路由**: Vue Router 4.6
- **HTTP 客户端**: Axios
- **图表**: ECharts 5.6

### 基础设施
- **容器化**: Docker Compose
- **向量检索**: Milvus + BGE Embedding
- **对象存储**: MinIO
- **缓存**: Redis

## 项目结构

```
axzl/
├── axzl-admin          # Web 入口层 (控制器、启动配置)
├── axzl-framework      # 框架层 (安全、AOP、拦截器、核心服务)
├── axzl-system         # 系统模块 (用户/角色/菜单/字典)
├── axzl-common         # 通用工具 (工具类、注解、基类、枚举)
├── axzl-biz            # 业务模块 (AI问诊、知识库、用户画像)
├── axzl-vue            # 前端项目 (Vue 3 + Vite)
├── sql                 # 数据库脚本
├── script              # 部署脚本
├── data                # 数据持久化目录
└── docker-compose.yml  # Docker 编排配置
```

## 核心功能

### AI 智能问诊
- 基于 RAG (检索增强生成) 的专业医疗问答
- 支持 SSE 流式输出，实时展示回答
- 集成用户画像，提供个性化建议
- 对话历史管理与上下文保持

### 知识库管理
- 文档导入与解析 (PDF、Word 等)
- 文档向量化与索引
- 知识库同步与更新

### 系统管理
- 用户管理 (RBAC 权限控制)
- 角色与权限配置
- 菜单管理
- 字典管理
- 操作日志

### 用药提醒
- 用药计划设置
- 定时提醒通知

## 快速开始

### 环境要求

- JDK 21+
- Node.js 18+
- Maven 3.8+
- Docker & Docker Compose

### 1. 启动基础设施

```bash
# 启动 MySQL、Redis、Milvus 等服务
docker-compose up -d
```

服务说明：
| 服务 | 端口 | 说明 |
|------|------|------|
| MySQL | 3306 | 数据库 (root/root) |
| Redis | 6379 | 缓存 |
| Milvus | 19530 | 向量数据库 |
| Attu | 8000 | Milvus 可视化工具 |
| MinIO | 9000/9001 | 对象存储 |
| BGE Embedding | 8718 | 向量嵌入服务 |

### 2. 初始化数据库

```bash
# 执行 SQL 脚本初始化数据库
mysql -u root -p < sql/axzl.sql
```

### 3. 启动后端

```bash
# 编译项目
mvn clean install -DskipTests

# 启动应用
cd axzl-admin && mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 4. 启动前端

```bash
cd axzl-vue

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器默认运行在 `http://localhost:80`，通过 Vite 代理转发 API 请求到后端。

## 构建部署

### 生产构建

```bash
# 后端打包
mvn clean package -DskipTests

# 前端构建
cd axzl-vue && npm run build:prod
```

### 配置说明

主要配置文件: `axzl-admin/src/main/resources/application.yml`

```yaml
# 服务配置
server:
  port: 8080

# AI 模型配置
spring:
  ai:
    openai:
      base-url: https://api.deepseek.com
      api-key: your-api-key
      chat:
        options:
          model: deepseek-v4-flash

# 向量数据库配置
    vectorstore:
      milvus:
        host: localhost
        port: 19530
        collection-name: axzl_knowledge
        index-type: IVF_FLAT
        metric-type: COSINE
```

## API 接口

### 认证接口
- `POST /login` - 用户登录
- `POST /register` - 用户注册
- `GET /captchaImage` - 获取验证码

### AI 问诊接口
- `POST /biz/consultation/chat` - 同步问诊
- `GET /biz/consultation/chatStream` - 流式问诊 (SSE)

### 知识库接口
- `POST /biz/knowledge/import` - 导入文档
- `POST /biz/knowledge/sync` - 同步向量

### 系统管理接口
- `/system/user/**` - 用户管理
- `/system/role/**` - 角色管理
- `/system/menu/**` - 菜单管理
- `/system/dict/**` - 字典管理

## 安全机制

- **认证**: JWT Token (HS512 签名)
- **授权**: RBAC 基于角色的权限控制
- **密码**: BCrypt 加密存储
- **防护**: 登录失败锁定、接口限流、重复提交拦截
- **匿名接口**: `/login`, `/register`, `/captchaImage`

## 开发指南

### 代码规范
- 后端遵循阿里巴巴 Java 开发手册
- 前端使用 Vue 3 Composition API + TypeScript
- 统一使用 `BaseController` 提供的响应封装

### 添加新接口
1. 在对应模块的 `controller` 包下创建控制器
2. 继承 `BaseController`
3. 使用 `@PreAuthorize` 进行权限控制
4. 使用 `@Log` 记录操作日志

### 添加新业务模块
1. 在 `axzl-biz` 下创建新的业务包
2. 实现 Service 层业务逻辑
3. 创建 Controller 暴露接口
4. 如需向量检索，注入 `VectorStore`

## 相关文档

- [需求规格说明书](docs/01-需求规格说明书.md)
- [系统设计文档](docs/02-系统设计文档.md)
- [测试报告](docs/03-测试报告.md)
- [用户操作手册](docs/04-用户操作手册.md)
- [项目总结报告](docs/05-项目总结报告.md)

## 许可证

MIT License

## 联系方式

- 项目维护: 安心诊疗团队
- 问题反馈: 请提交 Issue
