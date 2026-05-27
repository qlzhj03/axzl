# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AXZL (安心诊聊平台) is an AI-powered medical consultation system built with Spring Boot 4 + Vue 3. It uses RAG (Retrieval-Augmented Generation) with Milvus vector store and DeepSeek LLM to provide professional medical Q&A.

## Build & Run Commands

### Backend (Maven multi-module, Java 21)

```bash
# Full build
mvn clean install -DskipTests

# Run backend (port 8080)
cd axzl-admin && mvn spring-boot:run

# Package for production
mvn clean package -DskipTests
```

### Frontend (Vue 3 + Vite, Node 18+)

```bash
cd axzl-vue
npm install
npm run dev          # Dev server on port 80, proxies /dev-api to localhost:8080
npm run build:prod   # Production build → dist/
```

### Infrastructure

```bash
docker-compose up -d   # Starts MySQL (3306), Redis (6379), Milvus (19530), MinIO (9000/9001), BGE Embedding (8718), Attu (8000)
mysql -u root -p < sql/init.sql   # Initialize database
```

### AI Model Config

- DeepSeek API key via env var: `AI_DEEPSEEK_API_KEY`
- Embedding service: BGE-large-zh-v1.5 at `http://localhost:8718`
- Vector store: Milvus with COSINE metric, IVF_FLAT index, 1024 dimensions

## Architecture

### Backend Modules (Maven)

| Module | Package | Purpose |
|--------|---------|---------|
| `axzl-admin` | `com.axzl.web.controller` | Web entry point — controllers, application bootstrap. Controllers organized under `biz/`, `system/`, `monitor/`, `common/` |
| `axzl-biz` | `com.axzl.biz` | Business logic — AI consultation, knowledge base (RAG), user profiles, medication reminders. Contains `SpringAIClientFactory` and `PromptTemplateLoader` |
| `axzl-framework` | `com.axzl.framework` | Cross-cutting infrastructure — security config, AOP aspects, interceptors, data source config |
| `axzl-system` | `com.axzl.system` | System management — user/role/menu/dictionary CRUD (MyBatis-Plus mappers) |
| `axzl-common` | `com.axzl.common` | Shared utilities — `BaseController`, `AjaxResult` response wrapper, annotations, enums, constants |

### Key Patterns

- **Controllers** extend `BaseController` which provides `success()`/`error()` response helpers, pagination via `startPage()`, and `getLoginUser()` for current user context.
- **Security**: JWT (HS512) + Spring Security + RBAC. Anonymous endpoints: `/login`, `/register`, `/captchaImage`. Use `@PreAuthorize` for role-based access.
- **ORM**: MyBatis-Plus with mapper XML in `classpath*:mapper/**/*Mapper.xml`. Domain aliases scanned from `com.axzl.**.domain`.
- **AI/Spring AI**: Uses Spring AI 2.0.0-M3 OpenAI-compatible client (pointing to DeepSeek). SSE streaming for real-time chat. Vector store uses Spring AI's Milvus integration.

### Frontend Structure

- `src/views/biz/` — AI consultation, knowledge management, dashboard views
- `src/views/system/` — User/role/menu management
- `src/api/biz/` — Axios API calls for business modules
- `src/store/` — Pinia state management
- `src/router/` — Vue Router config
- Path aliases: `@` → `./src`, `~` → project root

### API Proxy

Frontend dev server proxies `/dev-api/*` to `http://localhost:8080` (stripping the prefix). API docs available at `/swagger-ui.html` (Knife4j/Springdoc).

## Conventions

- Backend follows Alibaba Java coding guidelines
- Frontend uses Vue 3 Composition API with `<script setup>` + TypeScript
- All API responses wrapped via `AjaxResult` (success/error/warn pattern)
- Operation logging via `@Log` annotation
- File uploads limited to 10MB single / 20MB total
