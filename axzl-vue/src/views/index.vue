<template>
  <div class="admin-home" v-loading="loading">
    <!-- 顶部问候区 -->
    <section class="hero-banner">
      <div class="hero-content">
        <div class="hero-text">
          <p class="hero-greeting">{{ greeting }}，管理员</p>
          <h1 class="hero-title">安心诊疗控制台</h1>
          <p class="hero-desc">AI 驱动的智能医疗问诊平台，一站式管理您的医疗服务</p>
        </div>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="$router.push('/system/user')">
            <el-icon><User /></el-icon>用户管理
          </el-button>
          <el-button size="large" round plain @click="$router.push('/monitor/online')">
            <el-icon><Monitor /></el-icon>系统监控
          </el-button>
        </div>
      </div>
      <div class="hero-decoration">
        <div class="deco-circle c1"></div>
        <div class="deco-circle c2"></div>
        <div class="deco-circle c3"></div>
      </div>
    </section>

    <!-- 数据概览 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div v-for="(stat, i) in stats" :key="i" class="stat-card" :style="{ animationDelay: `${i * 0.08}s` }">
          <div class="stat-icon-wrap" :style="{ background: stat.bg }">
            <el-icon :size="22"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <span class="stat-label">{{ stat.label }}</span>
            <span class="stat-value">{{ formatNum(stat.value) }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷操作 -->
    <section class="actions-section">
      <h2 class="section-heading">快捷操作</h2>
      <div class="actions-grid">
        <div
          v-for="(action, i) in quickActions"
          :key="i"
          class="action-card"
          :style="{ animationDelay: `${0.3 + i * 0.06}s` }"
          @click="$router.push(action.path)"
        >
          <div class="action-icon-box" :style="{ color: action.color, background: action.bg }">
            <el-icon :size="24"><component :is="action.icon" /></el-icon>
          </div>
          <div class="action-info">
            <h4>{{ action.title }}</h4>
            <p>{{ action.desc }}</p>
          </div>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </section>

    <!-- 底部两栏 -->
    <section class="bottom-section">
      <!-- 系统信息 -->
      <div class="info-card">
        <div class="card-head">
          <h3>系统信息</h3>
          <el-tag size="small" effect="plain">v3.9.1</el-tag>
        </div>
        <div class="info-grid">
          <div class="info-row">
            <span class="info-key">运行环境</span>
            <span class="info-val">Spring Boot 4.0.3</span>
          </div>
          <div class="info-row">
            <span class="info-key">前端框架</span>
            <span class="info-val">Vue 3 + Vite 6</span>
          </div>
          <div class="info-row">
            <span class="info-key">数据库</span>
            <span class="info-val">MySQL 8.0 + MyBatis-Plus</span>
          </div>
          <div class="info-row">
            <span class="info-key">AI 引擎</span>
            <span class="info-val">DeepSeek + Spring AI</span>
          </div>
          <div class="info-row">
            <span class="info-key">向量数据库</span>
            <span class="info-val">Milvus</span>
          </div>
          <div class="info-row">
            <span class="info-key">缓存</span>
            <span class="info-val">Redis</span>
          </div>
        </div>
      </div>

      <!-- 功能入口 -->
      <div class="info-card">
        <div class="card-head">
          <h3>核心能力</h3>
        </div>
        <div class="capability-list">
          <div v-for="(cap, i) in capabilities" :key="i" class="cap-item">
            <div class="cap-dot" :style="{ background: cap.color }"></div>
            <div class="cap-body">
              <h4>{{ cap.title }}</h4>
              <p>{{ cap.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  User, Monitor, ArrowRight,
  UserFilled, ChatDotRound, Document,
  Setting, FolderOpened, Connection
} from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/biz/dashboard'

defineOptions({ name: 'Index' })

const loading = ref(true)
const dashboardData = ref<Record<string, number>>({})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 17) return '下午好'
  if (h < 19) return '傍晚好'
  return '晚上好'
})

const stats = computed(() => [
  { label: '用户总数', value: dashboardData.value.userCount ?? 0, icon: UserFilled, bg: 'rgba(64,158,255,0.1)' },
  { label: '在线用户', value: dashboardData.value.onlineCount ?? 0, icon: UserFilled, bg: 'rgba(103,194,58,0.1)' },
  { label: '问诊会话', value: dashboardData.value.sessionCount ?? 0, icon: ChatDotRound, bg: 'rgba(230,162,60,0.1)' },
  { label: '今日问诊', value: dashboardData.value.todaySessions ?? 0, icon: ChatDotRound, bg: 'rgba(245,108,108,0.1)' },
  { label: '健康档案', value: dashboardData.value.profileCount ?? 0, icon: Document, bg: 'rgba(144,147,153,0.1)' },
  { label: '知识条目', value: dashboardData.value.knowledgeCount ?? 0, icon: FolderOpened, bg: 'rgba(99,102,241,0.1)' }
])

function formatNum(n: number) {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k'
  return String(n)
}

const quickActions = [
  { title: '用户管理', desc: '管理系统用户与权限', icon: UserFilled, path: '/system/user', color: '#409EFF', bg: 'rgba(64,158,255,0.08)' },
  { title: '智能问诊', desc: '查看 AI 问诊记录', icon: ChatDotRound, path: '/biz/consultation', color: '#67C23A', bg: 'rgba(103,194,58,0.08)' },
  { title: '知识库管理', desc: '维护医学知识文档', icon: FolderOpened, path: '/biz/document', color: '#E6A23C', bg: 'rgba(230,162,60,0.08)' },
  { title: '系统设置', desc: '配置系统参数', icon: Setting, path: '/system/config', color: '#909399', bg: 'rgba(144,147,153,0.08)' },
  { title: '数据监控', desc: '查看服务运行状态', icon: Monitor, path: '/monitor/server', color: '#F56C6C', bg: 'rgba(245,108,108,0.08)' },
  { title: '向量存储', desc: '管理 Milvus 向量库', icon: Connection, path: '/biz/document', color: '#6366f1', bg: 'rgba(99,102,241,0.08)' }
]

const capabilities = [
  { title: 'AI 智能问诊', desc: '基于 DeepSeek 大模型，结合 RAG 知识库进行专业医疗咨询', color: '#409EFF' },
  { title: '知识库管理', desc: '支持文档导入、向量化存储、语义检索的完整 RAG 流程', color: '#67C23A' },
  { title: '流式对话', desc: 'SSE 实时流式输出，打字机效果，即时响应用户咨询', color: '#E6A23C' },
  { title: '健康档案', desc: '患者健康档案管理，支持病史记录、体检数据、过敏信息', color: '#F56C6C' },
  { title: '权限控制', desc: '基于 RBAC 的细粒度权限管理，支持菜单、按钮级别控制', color: '#909399' }
]

async function loadStats() {
  loading.value = true
  try {
    const res = await getDashboardStats()
    dashboardData.value = res.data || {}
  } catch (e) {
    console.error('加载统计数据失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.admin-home {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* ===== 顶部问候区 ===== */
.hero-banner {
  position: relative;
  background: linear-gradient(135deg, #2b7de9 0%, #409EFF 40%, #66b1ff 100%);
  border-radius: 16px;
  padding: 36px 40px;
  color: #fff;
  overflow: hidden;
  margin-bottom: 24px;
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.hero-greeting {
  font-size: 14px;
  opacity: 0.85;
  margin: 0 0 4px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.hero-desc {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
  max-width: 400px;
  line-height: 1.6;
}

.hero-actions {
  flex-shrink: 0;
  display: flex;
  gap: 10px;

  .el-button--primary {
    --el-button-bg-color: rgba(255,255,255,0.2);
    --el-button-border-color: rgba(255,255,255,0.3);
    --el-button-hover-bg-color: rgba(255,255,255,0.3);
    --el-button-hover-border-color: rgba(255,255,255,0.5);
    backdrop-filter: blur(4px);
  }

  .el-button.is-plain {
    --el-button-bg-color: rgba(255,255,255,0.1);
    --el-button-border-color: rgba(255,255,255,0.25);
    --el-button-text-color: #fff;
    --el-button-hover-bg-color: rgba(255,255,255,0.2);
    --el-button-hover-border-color: rgba(255,255,255,0.4);
    --el-button-hover-text-color: #fff;
    backdrop-filter: blur(4px);
  }
}

.hero-decoration {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 50%;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  border: 1.5px solid rgba(255,255,255,0.12);

  &.c1 {
    width: 260px;
    height: 260px;
    top: -80px;
    right: -40px;
    animation: float 8s ease-in-out infinite;
  }
  &.c2 {
    width: 160px;
    height: 160px;
    bottom: -50px;
    right: 100px;
    animation: float 6s ease-in-out infinite reverse;
  }
  &.c3 {
    width: 80px;
    height: 80px;
    top: 20px;
    right: 200px;
    background: rgba(255,255,255,0.06);
    animation: float 10s ease-in-out infinite;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

/* ===== 数据概览 ===== */
.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-card {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid var(--el-border-color-lighter, #e4e7ed);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  animation: cardIn 0.4s ease-out both;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  }
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--el-color-primary);
}

.stat-body {
  flex: 1;
  min-width: 0;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
  margin-bottom: 4px;
}

.stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: var(--el-text-color-primary, #303133);
  letter-spacing: -0.5px;
  font-variant-numeric: tabular-nums;
}

/* ===== 快捷操作 ===== */
.actions-section {
  margin-bottom: 24px;
}

.section-heading {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
  margin: 0 0 16px;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.action-card {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid var(--el-border-color-lighter, #e4e7ed);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  animation: cardIn 0.4s ease-out both;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.06);
    border-color: var(--el-color-primary-light-5);

    .action-arrow { opacity: 1; transform: translateX(0); }
  }
}

.action-icon-box {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-info {
  flex: 1;
  min-width: 0;

  h4 {
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary, #303133);
    margin: 0 0 2px;
  }

  p {
    font-size: 12px;
    color: var(--el-text-color-secondary, #909399);
    margin: 0;
  }
}

.action-arrow {
  color: var(--el-text-color-placeholder, #c0c4cc);
  opacity: 0;
  transform: translateX(-4px);
  transition: all 0.25s;
  flex-shrink: 0;
}

/* ===== 底部两栏 ===== */
.bottom-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-card {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--el-border-color-lighter, #e4e7ed);
  animation: cardIn 0.5s ease-out 0.4s both;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;

  h3 {
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary, #303133);
    margin: 0;
  }
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 11px 0;
  border-bottom: 1px solid var(--el-border-color-extra-light, #f2f3f5);

  &:last-child { border-bottom: none; }
}

.info-key {
  font-size: 13px;
  color: var(--el-text-color-secondary, #909399);
}

.info-val {
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-primary, #303133);
}

/* ===== 核心能力 ===== */
.capability-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cap-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  transition: background 0.2s;

  &:hover { background: var(--el-fill-color-light, #f5f7fa); }
}

.cap-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
}

.cap-body {
  h4 {
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary, #303133);
    margin: 0 0 2px;
  }

  p {
    font-size: 12px;
    color: var(--el-text-color-secondary, #909399);
    margin: 0;
    line-height: 1.5;
  }
}

/* ===== 响应式 ===== */
@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .actions-grid { grid-template-columns: repeat(2, 1fr); }
  .bottom-section { grid-template-columns: 1fr; }
}

@media (max-width: 768px) {
  .admin-home { padding: 16px; }

  .hero-content { flex-direction: column; align-items: flex-start; }
  .hero-title { font-size: 22px; }

  .stats-grid { grid-template-columns: 1fr; }
  .actions-grid { grid-template-columns: 1fr; }
}
</style>
