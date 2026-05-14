<template>
  <div class="patient-home">
    <!-- 欢迎横幅 -->
    <section class="welcome-banner">
      <div class="banner-content">
        <div class="banner-text">
          <h1>您好，{{ greeting }}</h1>
          <p>欢迎使用安心诊疗平台，您的健康是我们最大的心愿</p>
        </div>
        <div class="banner-illustration">
          <div class="health-icon">
            <el-icon :size="80"><FirstAidKit /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-actions">
      <h2 class="section-title">快捷服务</h2>
      <div class="action-grid">
        <router-link to="/patient/consultation" class="action-card consultation">
          <div class="action-icon">
            <el-icon :size="32"><ChatDotRound /></el-icon>
          </div>
          <h3>智能问诊</h3>
          <p>AI 助手为您提供专业的健康咨询</p>
        </router-link>

        <router-link to="/patient/health" class="action-card health">
          <div class="action-icon">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <h3>健康档案</h3>
          <p>查看和管理您的个人健康信息</p>
        </router-link>

        <router-link to="/patient/medication" class="action-card medication">
          <div class="action-icon">
            <el-icon :size="32"><AlarmClock /></el-icon>
          </div>
          <h3>用药提醒</h3>
          <p>管理用药计划，按时服药更健康</p>
        </router-link>

        <router-link to="/patient/profile" class="action-card profile">
          <div class="action-icon">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <h3>个人信息</h3>
          <p>管理您的账户和个人资料</p>
        </router-link>
      </div>
    </section>

    <!-- 健康提示 -->
    <section class="health-tips">
      <h2 class="section-title">健康小贴士</h2>
      <div class="tips-grid">
        <div class="tip-card" v-for="(tip, index) in healthTips" :key="index">
          <div class="tip-icon">{{ tip.icon }}</div>
          <h4>{{ tip.title }}</h4>
          <p>{{ tip.content }}</p>
        </div>
      </div>
    </section>

    <!-- 平台数据 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-number">7x24</div>
          <div class="stat-label">全天候服务</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">AI</div>
          <div class="stat-label">智能诊断</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">100%</div>
          <div class="stat-label">隐私保护</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ChatDotRound, Document, User, FirstAidKit, AlarmClock } from '@element-plus/icons-vue'

defineOptions({ name: 'PatientHome' })

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  return '晚上好'
})

const healthTips = [
  {
    icon: '💧',
    title: '充足饮水',
    content: '每天保持 8 杯水的摄入，有助于新陈代谢'
  },
  {
    icon: '😴',
    title: '规律作息',
    content: '保持每天 7-8 小时的优质睡眠'
  },
  {
    icon: '🏃',
    title: '适量运动',
    content: '每周至少进行 150 分钟中等强度运动'
  },
  {
    icon: '🥗',
    title: '均衡饮食',
    content: '多蔬果、少油盐，保持营养均衡'
  }
]
</script>

<style scoped lang="scss">
.patient-home {
  max-width: 960px;
  margin: 0 auto;
}

// 欢迎横幅
.welcome-banner {
  background: linear-gradient(135deg, #2b7de9 0%, #409EFF 40%, #66b1ff 100%);
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 32px;
  color: #fff;
  overflow: hidden;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -20%;
    width: 300px;
    height: 300px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 50%;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    right: 10%;
    width: 200px;
    height: 200px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 50%;
  }
}

.banner-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.banner-text {
  h1 {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 8px;
  }

  p {
    font-size: 16px;
    opacity: 0.9;
    margin: 0;
  }
}

.banner-illustration {
  flex-shrink: 0;
}

.health-icon {
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

// 快捷入口
.quick-actions {
  margin-bottom: 32px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--patient-text);
  margin: 0 0 20px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-card {
  background: var(--patient-card-bg);
  border-radius: 16px;
  padding: 28px 24px;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid var(--patient-border);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
    border-color: var(--el-color-primary-light-5);
  }

  &.consultation .action-icon {
    background: linear-gradient(135deg, #2b7de9 0%, #66b1ff 100%);
  }

  &.health .action-icon {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  }

  &.medication .action-icon {
    background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%);
  }

  &.profile .action-icon {
    background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  }
}

.action-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 16px;
}

.action-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--patient-text);
  margin: 0 0 8px;
}

.action-card p {
  font-size: 14px;
  color: var(--patient-text-secondary);
  margin: 0;
  line-height: 1.5;
}

// 健康提示
.health-tips {
  margin-bottom: 32px;
}

.tips-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.tip-card {
  background: var(--patient-card-bg);
  border-radius: 14px;
  padding: 24px 20px;
  text-align: center;
  border: 1px solid var(--patient-border);
  transition: all 0.25s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.06);
  }
}

.tip-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.tip-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--patient-text);
  margin: 0 0 8px;
}

.tip-card p {
  font-size: 13px;
  color: var(--patient-text-secondary);
  margin: 0;
  line-height: 1.5;
}

// 平台数据
.stats-section {
  margin-bottom: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  background: var(--patient-card-bg);
  border-radius: 16px;
  padding: 32px;
  border: 1px solid var(--patient-border);
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--el-color-primary);
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--patient-text-secondary);
}

// 响应式
@media (max-width: 1024px) {
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-banner {
    padding: 28px 20px;
  }

  .banner-content {
    flex-direction: column;
    text-align: center;
  }

  .banner-text h1 {
    font-size: 22px;
  }

  .health-icon {
    width: 80px;
    height: 80px;
    margin-top: 16px;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .tips-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stats-grid {
    padding: 20px;
  }

  .stat-number {
    font-size: 24px;
  }
}
</style>
