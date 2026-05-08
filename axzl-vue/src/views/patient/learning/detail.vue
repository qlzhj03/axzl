<template>
  <div class="detail-page" v-loading="loading">
    <!-- 阅读进度条 -->
    <div class="progress-bar" :style="{ width: readProgress + '%' }"></div>

    <!-- 返回按钮 -->
    <div class="back-nav">
      <el-button text icon="ArrowLeft" @click="router.push('/patient/learning')">返回文章列表</el-button>
    </div>

    <template v-if="article">
      <!-- 封面 Hero -->
      <div class="article-hero" :style="heroStyle">
        <div class="hero-overlay">
          <div class="article-tags">
            <el-tag v-if="article.categoryName" type="success" effect="dark">{{ article.categoryName }}</el-tag>
            <el-tag v-for="tag in parseTags(article.tags)" :key="tag" type="info" size="small">{{ tag }}</el-tag>
          </div>
          <h1>{{ article.title }}</h1>
          <div class="article-meta">
            <span v-if="article.author"><el-icon><User /></el-icon> {{ article.author }}</span>
            <span v-if="article.source"><el-icon><OfficeBuilding /></el-icon> {{ article.source }}</span>
            <span><el-icon><Clock /></el-icon> {{ article.publishTime }}</span>
            <span><el-icon><View /></el-icon> {{ article.viewCount }} 阅读</span>
          </div>
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <el-button
          :type="article.isBookmarked ? 'warning' : 'default'"
          :icon="article.isBookmarked ? 'StarFilled' : 'Star'"
          round
          @click="handleBookmark"
        >
          {{ article.isBookmarked ? '已收藏' : '收藏' }}
        </el-button>
        <el-button
          :type="isLiked ? 'danger' : 'default'"
          round
          @click="handleLike"
        >
          <el-icon style="margin-right: 4px"><Pointer /></el-icon>
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
            <el-icon><MagicStick /></el-icon>
            <span>AI 摘要</span>
          </div>
        </template>
        <p>{{ article.aiSummary }}</p>
      </el-card>

      <!-- 推荐文章 -->
      <div v-if="recommended.length" class="recommended-section">
        <h3><el-icon><Connection /></el-icon> 相关推荐</h3>
        <el-row :gutter="16">
          <el-col :span="8" v-for="item in recommended" :key="item.articleId">
            <div class="rec-card" @click="goToDetail(item.articleId!)">
              <div class="rec-cover" :style="{ background: getRecGradient(item.categoryId) }">
                <img v-if="item.coverImage" :src="item.coverImage" :alt="item.title" />
                <div v-else class="rec-placeholder">
                  <el-icon :size="28"><Document /></el-icon>
                </div>
              </div>
              <div class="rec-body">
                <h4>{{ item.title }}</h4>
                <p>{{ item.summary }}</p>
              </div>
            </div>
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
import { User, OfficeBuilding, Clock, View, Pointer, MagicStick, Connection, Document } from '@element-plus/icons-vue'

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

// 推荐卡片渐变色
const recGradients: Record<number, string> = {
  100: 'linear-gradient(135deg, #fee2e2, #fecaca)',
  101: 'linear-gradient(135deg, #dcfce7, #bbf7d0)',
  102: 'linear-gradient(135deg, #f3e8ff, #e9d5ff)',
  103: 'linear-gradient(135deg, #fce7f3, #fbcfe8)',
  104: 'linear-gradient(135deg, #fef3c7, #fde68a)',
  105: 'linear-gradient(135deg, #fee2e2, #fecaca)',
  106: 'linear-gradient(135deg, #ffe4e6, #fecdd3)',
  107: 'linear-gradient(135deg, #ffedd5, #fed7aa)',
  108: 'linear-gradient(135deg, #dcfce7, #bbf7d0)',
  109: 'linear-gradient(135deg, #dbeafe, #bfdbfe)',
  110: 'linear-gradient(135deg, #ede9fe, #ddd6fe)',
}

const heroStyle = computed(() => {
  if (article.value?.coverImage) {
    return {
      backgroundImage: `url(${article.value.coverImage})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  }
  return {
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  }
})

function getRecGradient(categoryId?: number): string {
  return recGradients[categoryId || 0] || 'linear-gradient(135deg, #f0f9ff, #e0f2fe)'
}

function loadArticle() {
  const articleId = Number(route.params.id)
  if (!articleId) return

  loading.value = true
  readArticle(articleId).then((response: any) => {
    article.value = response.data
    isLiked.value = false
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

function startProgressTracking() {
  window.addEventListener('scroll', handleScroll)
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

/* Hero 封面 */
.article-hero {
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 24px;
  min-height: 220px;
  position: relative;

  .hero-overlay {
    position: relative;
    padding: 40px 36px;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.92) 0%, rgba(118, 75, 162, 0.92) 100%);
    color: #fff;
  }

  .article-tags {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
  }

  h1 {
    font-size: 1.8rem;
    font-weight: 700;
    margin: 0 0 16px;
    line-height: 1.4;
  }

  .article-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    font-size: 0.85rem;
    opacity: 0.9;

    span {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }
}

/* 操作栏 */
.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

/* 文章内容 */
.content-card {
  margin-bottom: 24px;
  border-radius: 12px;

  .article-body {
    line-height: 1.9;
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
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }

    :deep(ul), :deep(ol) {
      padding-left: 1.5em;
      margin-bottom: 1em;
    }

    :deep(blockquote) {
      border-left: 4px solid var(--el-color-primary);
      padding: 12px 16px;
      margin: 16px 0;
      background: var(--el-fill-color-lighter);
      border-radius: 0 8px 8px 0;
    }
  }
}

/* AI 摘要 */
.summary-card {
  margin-bottom: 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, #faf5ff 0%, #f5f3ff 100%);
  border: 1px solid #e9d5ff;

  .card-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    color: #7c3aed;

    .el-icon {
      font-size: 18px;
    }
  }

  p {
    color: var(--el-text-color-secondary);
    line-height: 1.8;
    margin: 0;
  }
}

/* 推荐文章 */
.recommended-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);

  h3 {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 1.1rem;
    margin-bottom: 16px;

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .rec-card {
    border-radius: 12px;
    overflow: hidden;
    background: var(--el-bg-color);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    transition: all 0.3s;
    margin-bottom: 12px;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
    }

    .rec-cover {
      height: 100px;
      overflow: hidden;
      display: flex;
      align-items: center;
      justify-content: center;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .rec-placeholder {
        color: rgba(0, 0, 0, 0.2);
      }
    }

    .rec-body {
      padding: 14px;

      h4 {
        margin: 0 0 6px;
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
}
</style>
