# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

### Backend (Maven, Java 21, Spring Boot 4.0.3)

```bash
# Build all modules, skip tests
mvn clean install -DskipTests

# Build only axzl-admin and its dependencies
mvn clean install -pl axzl-admin -am -DskipTests

# Run the application (from axzl-admin directory)
cd axzl-admin && mvn spring-boot:run
```

### Frontend (Vue 3, Vite 6, from axzl-vue directory)

```bash
npm run dev           # Dev server on port 80, hot reload
npm run build:prod    # Production build
npm run build:stage   # Staging build
npm run preview       # Preview built output
```

## Module Architecture

```
axzl-admin (web entry: controllers, startup, configs)
  ├── axzl-framework (security, aspects, interceptors, core services)
  │     ├── axzl-system (domain: user/role/menu/dict, mappers, services)
  │     └── axzl-common (utils, annotations, base classes, enums, Redis)
  └── axzl-biz (AI business: consultation, knowledge, profile)
        └── axzl-common
```

- **axzl-admin**: `src/main/java/com/axzl/web/controller/` — controllers grouped in `system/`, `monitor/`, `biz/`, `common/`, `tool/`
- **axzl-framework**: Spring Security config, JWT filter, TokenService, aspectj, datasource switching
- **axzl-system**: MyBatis mappers + service impls for user/role/menu/dict
- **axzl-common**: `BaseController`, `AjaxResult`, `BaseEntity`, `SecurityUtils`, `AxzlConfig`, annotations (`@Log`, `@Anonymous`, `@RateLimiter`, `@RepeatSubmit`)
- **axzl-biz**: AI consultation service (Spring AI + RAG), chat session/message domain, document/knowledge import

## Controllers

All controllers extend `BaseController` which provides:
- `startPage()` — sets up PageHelper pagination
- `getDataTable(list)` — wraps paginated results in `TableDataInfo`
- `success()` / `error()` / `warn()` — returns `AjaxResult` (HashMap subclass)
- `getUserId()` / `getUsername()` / `getLoginUser()` — current user helpers

Use `@PreAuthorize("@ss.hasPermi('system:user:list')")` for method-level permission control. `@Log(title, businessType)` triggers operation logging via AOP.

## Security & JWT

- **Stateless**: `SessionCreationPolicy.STATELESS`, no CSRF
- **JWT signing**: HS512, secret from `token.secret` config (default: `abcdefghijklmnopqrstuvwxyz`)
- **Token lifecycle**: `/login` → `TokenService.createToken()` → stored in Redis (`login_tokens:{uuid}`) → returned as `Bearer {token}`. Filter `JwtAuthenticationTokenFilter` extracts and validates on every request. Auto-refresh when < 20 min remaining.
- **Password**: `BCryptPasswordEncoder`, max retry count (5) with lock time (10 min) tracked in Redis
- **Anonymous endpoints**: `/login`, `/register`, `/captchaImage`, `/test/**`, static resources, `/swagger-ui/**`, `/druid/**`
- **Admin user**: ID=1, role key `admin`, has `*:*:*` wildcard permission

## Frontend-Backend Communication

- **Dev**: Axios baseURL = `/dev-api`, Vite proxy rewrites to `http://localhost:8080` stripping the prefix
- **Prod**: Axios baseURL = `/prod-api`
- **Token**: Stored in cookie (`Admin-Token`), sent as `Authorization: Bearer {token}` header
- **SSE streaming**: `ConsultationController.chatStream` uses `SseEmitter` + `Flux<String>`, frontend uses native `fetch` with `text/event-stream`

## AI / RAG Flow (axzl-biz)

`AiConsultationService` implements: user input → Embedding → Milvus vector search → user profile context → chat history → prompt assembly → DeepSeek API (via Spring AI) → save messages → return. Two modes: sync `chat()` and SSE streaming `chatStream()`.

## Database

- **ORM**: MyBatis-Plus 3.5.16 with MyBatis native coexistence
- **Mapper scan**: `com.axzl.**.mapper`, XML at `classpath*:mapper/**/*Mapper.xml`
- **Double datasource**: master/slave via Druid, switch with `@DataSource` annotation
- **MyBatis-Plus plugins**: pagination, optimistic locker, block-attack interceptor

## Key Config (axzl-admin/src/main/resources/application.yml)

- `server.port`: 8080, `context-path`: /
- `axzl.profile`: file upload path (default `D:/axzl/uploadPath`)
- `spring.ai.openai`: DeepSeek API (`https://api.deepseek.com`, model: `deepseek-v4-flash`)
- `spring.ai.vectorstore.milvus`: Milvus on `localhost:19530`, COSINE metric, IVF_FLAT index
- `token.expireTime`: 30 minutes