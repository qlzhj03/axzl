<template>
  <div class="detail-page" v-loading="loading">
    <!-- 阅读进度条 -->
    <div class="progress-bar" :style="{ width: readProgress + '%' }"></div>

    <!-- 返回按钮 -->
    <div class="back-nav">
      <el-button text icon="ArrowLeft" @click="router.push('/patient/learning')">返回文章列表</el-button>
    </div>

    <template v-if="article">
      <!-- 文章头部 -->
      <div class="article-header">
        <div class="article-tags">
          <el-tag v-if="article.categoryName" type="success">{{ article.categoryName }}</el-tag>
          <el-tag v-for="tag in parseTags(article.tags)" :key="tag" type="info" size="small">{{ tag }}</el-tag>
        </div>
        <h1>{{ article.title }}</h1>
        <div class="article-meta">
          <span v-if="article.author">作者：{{ article.author }}</span>
          <span v-if="article.source">来源：{{ article.source }}</span>
          <span>发布时间：{{ article.publishTime }}</span>
          <span>{{ article.viewCount }} 阅读</span>
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <el-button
          :type="article.isBookmarked ? 'warning' : 'default'"
          :icon="article.isBookmarked ? 'StarFilled' : 'Star'"
          @click="handleBookmark"
        >
          {{ article.isBookmarked ? '已收藏' : '收藏' }}
        </el-button>
        <el-button
          :type="isLiked ? 'danger' : 'default'"
          :icon="isLiked ? 'Pointer' : 'Pointer'"
          @click="handleLike"
        >
          {{ isLiked ? '已点赞' : '点赞' }} {{ article.likeCount }}
        </el-button>
      </div>

      <!-- 文章内容 -->
      <el-card shadow="never" class="content-card">
        <div class="article-body" v-html="article.content"></div>
      </el-card>

      <!-- AI摘要 -->
      <el-card v-if="article.aiSummary" shadow="never" class="summary-card">
        <template #header>
          <div class="card-header">
            <span>AI 摘要</span>
          </div>
        </template>
        <p>{{ article.aiSummary }}</p>
      </el-card>

      <!-- 推荐文章 -->
      <div v-if="recommended.length" class="recommended-section">
        <h3>相关推荐</h3>
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in recommended" :key="item.articleId">
            <el-card shadow="hover" class="rec-card" @click="goToDetail(item.articleId!)">
              <h4>{{ item.title }}</h4>
              <p>{{ item.summary }}</p>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </template>
  </div>
</template>

<script setup name="PatientLearningDetail" lang="ts">
import { readArticle, toggleBookmark, toggleLike, updateReadProgress, getRecommended } from '@/api/biz/learning'
import type { LearningArticle } from '@/types/api/biz/learning'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const { proxy } = getCurrentInstance() as any

const article = ref<LearningArticle | null>(null)
const recommended = ref<LearningArticle[]>([])
const loading = ref(false)
const isLiked = ref(false)
const readProgress = ref(0)
const startTime = ref(0)
let progressTimer: ReturnType<typeof setInterval> | null = null

function loadArticle() {
  const articleId = Number(route.params.id)
  if (!articleId) return

  loading.value = true
  readArticle(articleId).then((response: any) => {
    article.value = response.data
    isLiked.value = false // 后端暂未返回isLiked
    loading.value = false
    startTime.value = Date.now()
    startProgressTracking()
  }).catch(() => {
    loading.value = false
  })
}

function loadRecommended() {
  getRecommended().then((response: any) => {
    recommended.value = (response.data || []).filter((item: LearningArticle) => item.articleId !== Number(route.params.id)).slice(0, 3)
  })
}

function handleBookmark() {
  if (!article.value) return
  toggleBookmark(article.value.articleId!).then(() => {
    article.value!.isBookmarked = article.value!.isBookmarked ? 0 : 1
    proxy.$modal.msgSuccess(article.value!.isBookmarked ? '已收藏' : '已取消收藏')
  })
}

function handleLike() {
  if (!article.value) return
  toggleLike(article.value.articleId!).then(() => {
    isLiked.value = !isLiked.value
    article.value!.likeCount = (article.value!.likeCount || 0) + (isLiked.value ? 1 : -1)
    proxy.$modal.msgSuccess(isLiked.value ? '已点赞' : '已取消点赞')
  })
}

function goToDetail(articleId: number) {
  router.push('/patient/learning/detail/' + articleId)
}

function parseTags(tags?: string): string[] {
  if (!tags) return []
  return tags.split(',').map(t => t.trim()).filter(Boolean).slice(0, 3)
}

// 阅读进度跟踪
function startProgressTracking() {
  window.addEventListener('scroll', handleScroll)
  // 每30秒上报一次阅读时长
  progressTimer = setInterval(() => {
    reportProgress()
  }, 30000)
}

function handleScroll() {
  const scrollTop = window.scrollY
  const docHeight = document.documentElement.scrollHeight - window.innerHeight
  if (docHeight > 0) {
    readProgress.value = Math.min(100, Math.round((scrollTop / docHeight) * 100))
  }
}

function reportProgress() {
  if (!article.value) return
  const duration = Math.round((Date.now() - startTime.value) / 1000)
  updateReadProgress({
    articleId: article.value.articleId!,
    progress: readProgress.value,
    duration: duration
  })
  startTime.value = Date.now()
}

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (progressTimer) {
    clearInterval(progressTimer)
  }
  // 离开页面时最终上报
  reportProgress()
})

loadArticle()
loadRecommended()
</script>

<style scoped lang="scss">
.detail-page {
  max-width: 800px;
  margin: 0 auto;
  animation: fadeIn 0.3s ease;
  position: relative;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.progress-bar {
  position: fixed;
  top: 60px;
  left: 0;
  height: 3px;
  background: var(--el-color-primary);
  transition: width 0.1s;
  z-index: 100;
}

.back-nav {
  margin-bottom: 16px;
}

.article-header {
  margin-bottom: 24px;

  .article-tags {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }

  h1 {
    font-size: 1.8rem;
    font-weight: 700;
    color: var(--el-text-color-primary);
    margin: 0 0 12px;
    line-height: 1.4;
  }

  .article-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    font-size: 0.85rem;
    color: var(--el-text-color-secondary);
  }
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.content-card {
  margin-bottom: 24px;

  .article-body {
    line-height: 1.8;
    font-size: 1rem;
    color: var(--el-text-color-primary);

    :deep(h1), :deep(h2), :deep(h3) {
      margin-top: 1.5em;
      margin-bottom: 0.5em;
    }

    :deep(p) {
      margin-bottom: 1em;
    }

    :deep(img) {
      max-width: 100%;
      border-radius: 4px;
    }

    :deep(ul), :deep(ol) {
      padding-left: 1.5em;
      margin-bottom: 1em;
    }
  }
}

.summary-card {
  margin-bottom: 24px;
  background: var(--el-fill-color-lighter);

  .card-header {
    font-weight: 600;
  }

  p {
    color: var(--el-text-color-secondary);
    line-height: 1.8;
    margin: 0;
  }
}

.recommended-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);

  h3 {
    font-size: 1.1rem;
    margin-bottom: 16px;
  }

  .rec-card {
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: 12px;

    &:hover {
      transform: translateY(-2px);
    }

    h4 {
      margin: 0 0 8px;
      font-size: 0.95rem;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    p {
      color: var(--el-text-color-secondary);
      font-size: 0.8rem;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin: 0;
    }
  }
}
</style>
