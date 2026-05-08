# AGENTS.md

Instructions for AI coding agents working in this repository.

## Build & Run

```bash
# Backend (Java 21, Spring Boot 4.0.3, Maven multi-module)
mvn clean install -DskipTests                     # build all
mvn clean install -pl axzl-admin -am -DskipTests  # build admin + deps only
cd axzl-admin && mvn spring-boot:run              # run (port 8080)

# Frontend (Vue 3, Vite 6, from axzl-vue directory)
npm run dev           # dev server on port 443 (HTTPS), hot reload
npm run build:prod    # production build
npm run build:stage   # staging build
```

## No Tests

This project has no tests (no Java test classes, no frontend test runner). Never try to run tests.

## No Lint / Typecheck Scripts

The frontend package.json has no `lint` or `typecheck` commands. Do not attempt to run them.

## Infrastructure Requirements

Running the app requires these services (see `docker-compose.yml`):
- **MySQL 8.0**: `root/root`, database `axzl`, port 3306
- **Redis 7**: port 6379, no password
- **Milvus 2.6.8** + etcd + minio: port 19530, vector store for RAG
- **Embedding service**: port 8718 (Docker image `embedding-service`, BGE model)

Init database: `mysql -u root -p < sql/init.sql`

## Module Architecture

```
axzl-admin (web entry: controllers, startup, configs)
  ├── axzl-framework (security, JWT, aspects, datasource switch)
  │     ├── axzl-system (user/role/menu/dict domain + MyBatis mappers)
  │     └── axzl-common (utils, annotations, BaseController, enums, Redis)
  └── axzl-biz (AI business: consultation, knowledge, profile)
        └── axzl-common
```

All modules are declared in root `pom.xml` `<modules>` block.

**Critical rule**: Never modify `axzl-admin`, `axzl-framework`, `axzl-system`, or `axzl-common`. New business code goes in `axzl-biz` only.

## Controller Conventions

All controllers extend `BaseController` which provides:
- `startPage()` — sets up PageHelper pagination
- `getDataTable(list)` — wraps paginated results in `TableDataInfo`
- `success()` / `error()` / `warn()` — returns `AjaxResult` (HashMap subclass)
- `getUserId()` / `getUsername()` / `getLoginUser()` — current user helpers

Permission control: `@PreAuthorize("@ss.hasPermi('system:user:list')")`

Audit logging: `@Log(title = "...", businessType = BusinessType.xxx)`

## Security

- **Stateless JWT**: no sessions, no CSRF
- **Admin user**: ID=1, role key `admin`, has `*:*:*` wildcard permission
- **Anonymous endpoints**: `/login`, `/register`, `/captchaImage`, `/test/**`, `/swagger-ui/**`, `/druid/**`
- Token stored in Redis (`login_tokens:{uuid}`), auto-refresh when < 20 min remaining

## Database (MyBatis-Plus + MyBatis native)

- **No XML mappers for new code**: use MyBatis-Plus API directly
- Mapper scan: `com.axzl.**.mapper`, existing XML at `classpath*:mapper/**/*Mapper.xml`
- Master/slave datasources via Druid, switch with `@DataSource` annotation
- PageHelper for pagination (triggered by `startPage()`)

### Gotcha: Entities inheriting `BaseEntity`

Must declare these fields as non-column:

```java
@TableField(exist = false)
private Map<String, Object> params;

@TableField(exist = false)
private String searchValue;
```

Otherwise MyBatis-Plus INSERT throws "Type handler was null on parameter mapping".

## Frontend ↔ Backend

- **Dev**: Axios baseURL = `/dev-api`, Vite proxy rewrites to `https://localhost:8080` stripping the prefix
- **Prod**: Axios baseURL = `/prod-api`
- Token in cookie `Admin-Token`, sent as `Authorization: Bearer {token}` header

## Spring AI 2.0.0-M3 (Milestone — API quirks)

- `OpenAiApi` uses Builder: `OpenAiApi.builder().baseUrl(url).apiKey(key).build()`
- `OpenAiEmbeddingModel` created via constructor: `new OpenAiEmbeddingModel(openAiApi)`
- `OpenAiChatOptions` uses `maxCompletionTokens()`, NOT `maxTokens()`
- AI clients created dynamically from DB via `SpringAIClientFactory`; always use the factory, never inject `ChatClient`/`EmbeddingModel` directly
- Embedding service is NOT DeepSeek — it's a local BGE model on `http://localhost:8718`

### Gotcha: Circular dependency

`VectorStoreConfigListener` injects `SpringAIClientFactory` with `@Lazy` to break `AIModelConfigServiceImpl → VectorStoreConfigListener → SpringAIClientFactoryImpl → AIModelConfigServiceImpl` cycle. Do not remove `@Lazy`.

## Service Naming

- Interface: `I` + PascalCase + `Service` (e.g. `ISysOrderService`)
- Implementation: PascalCase + `ServiceImpl` (e.g. `SysOrderServiceImpl`)
- Mapper: PascalCase + `Mapper` (e.g. `SysOrderMapper`)

## IDE Gotchas

If IDE reports "cannot resolve constructor" for Spring AI classes but Maven compiles fine: run `mvn clean install -DskipTests` then restart IDE. This is a known Spring AI milestone artifact caching issue.