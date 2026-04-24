<template>
  <div class="app-container home">
    <el-row :gutter="24" class="main-content">
      <el-col :xs="24" :sm="24" :md="12" :lg="11" :xl="11">
        <div class="card intro-card">
          <div class="card-icon-wrap">
            <span class="card-icon">🏥</span>
          </div>
          <h2>安心AI医疗问诊平台</h2>
          <p>
            安心AI医疗问诊平台基于人工智能技术，整合AI诊断能力与专业医疗资源，为用户提供智能症状分析、个性化健康建议及在线问诊、健康咨询、病历管理等一站式医疗健康服务。
          </p>
        </div>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" :lg="13" :xl="13">
        <h2 class="section-title">核心功能</h2>
        <el-row :gutter="16" class="function-row">
          <el-col :xs="24" :sm="24" :md="12">
            <div class="card function-card diagnosis-card">
              <div class="card-icon-wrap">
                <span class="card-icon">🧠</span>
              </div>
              <h4>智能问诊</h4>
              <ul>
                <li><span class="icon">✓</span>AI症状分析</li>
                <li><span class="icon">✓</span>智能诊断建议</li>
                <li><span class="icon">✓</span>健康风险评估</li>
              </ul>
            </div>
          </el-col>
          <el-col :xs="24" :sm="24" :md="12">
            <div class="card function-card service-card">
              <div class="card-icon-wrap">
                <span class="card-icon">💬</span>
              </div>
              <h4>在线服务</h4>
              <ul>
                <li><span class="icon">✓</span>病历管理</li>
                <li><span class="icon">✓</span>健康档案</li>
              </ul>
            </div>
          </el-col>
        </el-row>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="features-section">
      <el-col :xs="24" class="section-header">
        <h2 class="section-title">平台优势</h2>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="6">
        <div class="card feature-card">
          <div class="card-icon-wrap">
            <span class="card-icon">🤖</span>
          </div>
          <h3>AI智能诊断</h3>
          <p>基于人工智能技术的智能问诊系统，快速准确</p>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="6">
        <div class="card feature-card">
          <div class="card-icon-wrap">
            <span class="card-icon">🔒</span>
          </div>
          <h3>隐私安全保障</h3>
          <p>严格的数据加密，保护用户隐私安全</p>
        </div>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="6">
        <div class="card feature-card">
          <div class="card-icon-wrap">
            <span class="card-icon">⏰</span>
          </div>
          <h3>7x24小时服务</h3>
          <p>全天候在线问诊，随时为您健康护航</p>
        </div>
      </el-col>
    </el-row>

  </div>
</template>

<script setup lang="ts" name="Index">

function goTarget(url: string): void {
  window.open(url, '__blank')
}
</script>

<style scoped lang="scss">
.home {
  min-height: 100vh;
  padding: 24px 16px;
  background: var(--el-bg-color, #f8fafc);
  transition: background-color 0.3s ease;

  @media (min-width: 576px) {
    padding: 28px 20px;
  }

  @media (min-width: 768px) {
    padding: 32px 24px;
  }

  @media (min-width: 992px) {
    padding: 36px 32px;
  }

  @media (min-width: 1200px) {
    padding: 48px;
    max-width: 1600px;
    margin: 0 auto;
  }

  blockquote {
    padding: 12px 24px;
    margin: 0 0 24px;
    font-size: 17.5px;
    border-left: 5px solid var(--el-border-color-light, #e2e8f0);
    background: var(--el-bg-color-overlay, #fff);
    border-radius: 0 8px 8px 0;
    color: var(--el-text-color-regular, #64748b);
  }

  hr {
    margin-top: 24px;
    margin-bottom: 24px;
    border: 0;
    border-top: 1px solid var(--el-border-color-light, #e2e8f0);
  }

  .col-item {
    margin-bottom: 24px;
  }

  ul {
    padding: 0;
    margin: 0;
    list-style-type: none;
  }

  font-family: "Inter", "open sans", "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 14px;
  color: var(--el-text-color-regular, #475569);
  overflow-x: hidden;
  line-height: 1.6;

  h4 {
    margin-top: 0;
    margin-bottom: 0;
  }

  h2 {
    margin-top: 0;
    margin-bottom: 16px;
    font-size: 24px;
    font-weight: 700;
    color: var(--el-text-color-primary, #1e293b);
    line-height: 1.3;

    @media (min-width: 576px) {
      font-size: 26px;
    }

    @media (min-width: 768px) {
      font-size: 28px;
    }

    @media (min-width: 1200px) {
      font-size: 32px;
    }
  }

  h3 {
    margin-top: 0;
    margin-bottom: 8px;
    font-weight: 600;
    color: var(--el-text-color-primary, #1e293b);
  }

  p {
    margin-top: 0;
    margin-bottom: 12px;
    font-size: 14px;
    line-height: 1.75;
    color: var(--el-text-color-regular, #64748b);

    @media (min-width: 576px) {
      font-size: 15px;
    }

    @media (min-width: 768px) {
      line-height: 1.85;
    }

    b {
      font-weight: 600;
    }
  }

  .main-content {
    margin-bottom: 32px;

    @media (min-width: 768px) {
      margin-bottom: 40px;
    }

    @media (min-width: 1200px) {
      margin-bottom: 48px;
    }
  }

  .features-section {
    margin-top: 0;

    @media (min-width: 768px) {
      margin-top: -20px;
    }
  }

  .section-header {
    margin-bottom: 20px;

    @media (min-width: 768px) {
      margin-bottom: 24px;
    }
  }

  /* 功能卡片行 - 确保高度一致 */
  .function-row {
    display: flex;
    flex-direction: row;
    align-items: stretch;
  }

  /* 统一卡片基础样式 */
  .card {
    border-radius: 16px;
    padding: 24px;
    transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.35s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100%;
    will-change: transform, box-shadow;

    @media (min-width: 576px) {
      padding: 26px;
      border-radius: 18px;
    }

    @media (min-width: 768px) {
      padding: 28px;
      border-radius: 20px;
    }

    .card-icon-wrap {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;
      width: 72px;
      height: 72px;
      flex-shrink: 0;

      @media (min-width: 768px) {
        margin-bottom: 20px;
        width: 80px;
        height: 80px;
      }
    }

    .card-icon {
      font-size: 36px;
      display: flex;
      align-items: center;
      justify-content: center;

      @media (min-width: 768px) {
        font-size: 40px;
      }
    }
  }

  /* 介绍卡片 */
  .intro-card {
    background: var(--el-bg-color-overlay, #ffffff);
    border: 1px solid var(--el-border-color-light, #e2e8f0);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
    flex-direction: column;
    justify-content: flex-start;
    contain: layout paint style;

    @media (min-width: 768px) {
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
    }

    .card-icon-wrap {
      width: auto;
      height: auto;
      justify-content: flex-start;
      margin-bottom: 16px;

      @media (min-width: 768px) {
        margin-bottom: 20px;
      }
    }

    .card-icon {
      font-size: 40px;

      @media (min-width: 576px) {
        font-size: 48px;
      }

      @media (min-width: 768px) {
        font-size: 56px;
      }
    }

    h2 {
      position: relative;
      z-index: 1;
      margin-bottom: 16px;
      font-size: 26px;

      @media (min-width: 576px) {
        font-size: 28px;
      }

      @media (min-width: 768px) {
        font-size: 32px;
        margin-bottom: 20px;
      }
    }

    p {
      position: relative;
      z-index: 1;
      color: var(--el-text-color-regular, #64748b);
      line-height: 1.85;
      font-size: 15px;
      margin-bottom: 0;
      flex: 1;

      @media (min-width: 768px) {
        font-size: 16px;
      }
    }
  }

  /* 标题样式 */
  .section-title {
    color: var(--el-text-color-primary, #1e293b);
    font-size: 22px;
    font-weight: 700;
    margin-bottom: 20px;
    padding-left: 16px;
    border-left: 4px solid var(--el-color-primary, #667eea);
    position: relative;

    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 0;
      width: 4px;
      height: 60%;
      background: linear-gradient(180deg, var(--el-color-primary, #667eea) 0%, #764ba2 100%);
      transform: translateY(-50%);
      border-radius: 0 4px 4px 0;
    }

    @media (min-width: 576px) {
      font-size: 24px;
      padding-left: 20px;
      border-left-width: 5px;
    }

    @media (min-width: 768px) {
      font-size: 26px;
      margin-bottom: 24px;
      padding-left: 24px;
    }
  }

  /* 功能卡片 */
  .function-card {
    background: var(--el-bg-color-overlay, #ffffff);
    border: 1px solid var(--el-border-color-light, #e2e8f0);
    box-shadow: 0 2px 14px rgba(0, 0, 0, 0.03);
    justify-content: flex-start;
    contain: layout paint style;

    @media (min-width: 768px) {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
      border-color: var(--el-border-color, #cbd5e1);
    }

    &:active {
      transform: translateY(-2px);
    }

    h4 {
      color: var(--el-text-color-primary, #1e293b);
      font-size: 17px;
      font-weight: 600;
      margin-bottom: 14px;

      @media (min-width: 768px) {
        font-size: 18px;
        margin-bottom: 16px;
      }
    }

    ul {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: flex-start;

      li {
        padding: 7px 0;
        font-size: 14px;
        color: var(--el-text-color-regular, #64748b);
        display: flex;
        align-items: center;

        .icon {
          color: var(--el-color-success, #10b981);
          margin-right: 10px;
          font-size: 14px;
          flex-shrink: 0;
        }
      }
    }

    &.diagnosis-card {
      border-top: 4px solid var(--el-color-primary, #3b82f6);

      .card-icon-wrap {
        background: rgba(59, 130, 246, 0.08);
        border-radius: 12px;
        border: 1px solid rgba(59, 130, 246, 0.15);
      }
    }

    &.service-card {
      border-top: 4px solid var(--el-color-success, #10b981);

      .card-icon-wrap {
        background: rgba(16, 185, 129, 0.08);
        border-radius: 12px;
        border: 1px solid rgba(16, 185, 129, 0.15);
      }
    }
  }

  /* 特色卡片 */
  .feature-card {
    background: linear-gradient(135deg, var(--el-color-primary, #667eea) 0%, #764ba2 100%);
    text-align: center;
    color: #ffffff;
    box-shadow: 0 6px 24px rgba(102, 126, 234, 0.3);
    border: 1px solid rgba(255, 255, 255, 0.1);
    justify-content: center;
    align-items: center;
    will-change: transform, box-shadow;
    contain: layout paint style;

    @media (min-width: 768px) {
      box-shadow: 0 8px 30px rgba(102, 126, 234, 0.35);
    }

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 1px;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    }

    &:hover {
      transform: translateY(-6px);
      box-shadow: 0 12px 35px rgba(102, 126, 234, 0.45);
    }

    &:active {
      transform: translateY(-3px);
    }

    .card-icon-wrap {
      width: 72px;
      height: 72px;
      background: rgba(255, 255, 255, 0.15);
      border-radius: 20px;
      border: 1px solid rgba(255, 255, 255, 0.2);
      margin-bottom: 16px;

      @media (min-width: 576px) {
        width: 80px;
        height: 80px;
        border-radius: 22px;
      }

      @media (min-width: 768px) {
        margin-bottom: 20px;
        width: 88px;
        height: 88px;
        border-radius: 24px;
      }
    }

    .card-icon {
      font-size: 40px;

      @media (min-width: 576px) {
        font-size: 44px;
      }

      @media (min-width: 768px) {
        font-size: 48px;
      }
    }

    h3 {
      margin: 0 0 12px;
      font-size: 18px;
      color: #ffffff;

      @media (min-width: 576px) {
        font-size: 19px;
        margin-bottom: 14px;
      }

      @media (min-width: 768px) {
        font-size: 20px;
        margin-bottom: 16px;
      }
    }

    p {
      margin: 0;
      font-size: 14px;
      opacity: 0.92;
      line-height: 1.7;
      color: rgba(255, 255, 255, 0.9);
      text-align: center;
    }
  }
}
</style>