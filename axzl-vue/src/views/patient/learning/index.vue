<template>
  <div class="learning-page">
    <!-- 顶部 Banner -->
    <div class="hero-banner">
      <div class="hero-content">
        <h1>健康知识库</h1>
        <p>科学养生，从学习开始</p>
      </div>
      <div class="hero-illustration">
        <svg viewBox="0 0 200 120" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="100" cy="60" r="50" fill="rgba(255,255,255,0.15)" />
          <circle cx="100" cy="60" r="35" fill="rgba(255,255,255,0.1)" />
          <path d="M100 30 L100 90 M75 55 L100 30 L125 55" stroke="rgba(255,255,255,0.6)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
          <circle cx="60" cy="40" r="6" fill="rgba(255,255,255,0.3)" />
          <circle cx="145" cy="50" r="4" fill="rgba(255,255,255,0.25)" />
          <circle cx="75" cy="90" r="3" fill="rgba(255,255,255,0.2)" />
        </svg>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchTitle" placeholder="搜索健康知识文章..." clearable size="large" @keyup.enter="handleSearch">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <div
        class="category-chip"
        :class="{ active: !queryParams.categoryId }"
        @click="filterByCategory(undefined)"
      >
        <div class="chip-icon all">
          <el-icon><Grid /></el-icon>
        </div>
        <span>全部</span>
      </div>
      <div
        v-for="cat in categories"
        :key="cat.categoryId"
        class="category-chip"
        :class="{ active: queryParams.categoryId === cat.categoryId }"
        @click="filterByCategory(cat.categoryId)"
      >
        <div class="chip-icon" :style="getCategoryStyle(cat.categoryId)">
          <el-icon><component :is="getCategoryIcon(cat)" /></el-icon>
        </div>
        <span>{{ cat.categoryName }}</span>
      </div>
    </div>

    <!-- 推荐阅读 -->
    <div v-if="recommended.length && !queryParams.categoryId" class="section">
      <div class="section-header">
        <h3><el-icon><Star /></el-icon> 推荐阅读</h3>
      </div>
      <el-row :gutter="16">
        <el-col :span="8" v-for="article in recommended" :key="article.articleId">
          <div class="featured-card" @click="goToDetail(article.articleId!)">
            <div class="featured-cover">
              <img :src="getCoverUrl(article)" :alt="article.title" />
              <div class="featured-overlay">
                <div class="overlay-tags">
                  <span v-if="article.isTop" class="badge top">置顶</span>
                  <span v-if="article.isFeatured" class="badge featured">精选</span>
                </div>
              </div>
            </div>
            <div class="featured-info">
              <h4>{{ article.title }}</h4>
              <p>{{ article.summary }}</p>
              <div class="meta">
                <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
                <span><el-icon><Pointer /></el-icon> {{ article.likeCount }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 文章列表 -->
    <div class="section">
      <div class="section-header" v-if="!queryParams.categoryId">
        <h3><el-icon><Document /></el-icon> 最新文章</h3>
      </div>
      <el-row :gutter="16">
        <el-col :span="12" v-for="article in articleList" :key="article.articleId">
          <div class="article-card" @click="goToDetail(article.articleId!)">
            <div class="article-cover">
              <img :src="getCoverUrl(article)" :alt="article.title" />
            </div>
            <div class="article-body">
              <div class="article-tags">
                <el-tag size="small" v-if="article.categoryName" :color="getCategoryColor(article.categoryId)" effect="dark" class="cat-tag">{{ article.categoryName }}</el-tag>
                <el-tag size="small" type="info" v-for="tag in parseTags(article.tags)" :key="tag">{{ tag }}</el-tag>
              </div>
              <h3>{{ article.title }}</h3>
              <p class="summary">{{ article.summary }}</p>
              <div class="article-footer">
                <div class="meta">
                  <span>{{ article.author }}</span>
                  <span>{{ formatDate(article.publishTime) }}</span>
                </div>
                <div class="stats">
                  <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
                  <span><el-icon><Pointer /></el-icon> {{ article.likeCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <div v-if="!articleList.length && !loading" class="empty">
        <el-empty description="暂无文章" />
      </div>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="queryParams.pageSize"
          v-model:current-page="queryParams.pageNum"
          @current-change="getList"
        />
      </div>
    </div>
  </div>
</template>

<script setup name="PatientLearning" lang="ts">
import { getPublishedArticles, getCategoryTree, getRecommended } from '@/api/biz/learning'
import type { LearningArticle, LearningCategory } from '@/types/api/biz/learning'
import { Search, Grid, Star, View, Pointer, Document, FirstAidKit, Apple, ChatDotRound, User as UserIcon, MagicStick } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const { proxy } = getCurrentInstance() as any

const articleList = ref<LearningArticle[]>([])
const recommended = ref<LearningArticle[]>([])
const categories = ref<LearningCategory[]>([])
const searchTitle = ref('')
const loading = ref(false)
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: undefined as string | undefined,
  categoryId: undefined as number | undefined
})

// 分类配色方案
const categoryColors: Record<number, { bg: string; color: string; gradient: string }> = {
  100: { bg: '#fef2f2', color: '#ef4444', gradient: 'linear-gradient(135deg, #ef4444, #f97316)' },  // 常见疾病
  101: { bg: '#f0fdf4', color: '#22c55e', gradient: 'linear-gradient(135deg, #22c55e, #10b981)' },  // 健康生活
  102: { bg: '#faf5ff', color: '#a855f7', gradient: 'linear-gradient(135deg, #a855f7, #8b5cf6)' },  // 心理健康
  103: { bg: '#fdf2f8', color: '#ec4899', gradient: 'linear-gradient(135deg, #ec4899, #f472b6)' },  // 母婴健康
  104: { bg: '#fffbeb', color: '#f59e0b', gradient: 'linear-gradient(135deg, #f59e0b, #eab308)' },  // 中医养生
  105: { bg: '#fef2f2', color: '#ef4444', gradient: 'linear-gradient(135deg, #ef4444, #dc2626)' },  // 呼吸系统
  106: { bg: '#fef2f2', color: '#e11d48', gradient: 'linear-gradient(135deg, #e11d48, #f43f5e)' },  // 心血管
  107: { bg: '#fff7ed', color: '#f97316', gradient: 'linear-gradient(135deg, #f97316, #fb923c)' },  // 消化系统
  108: { bg: '#f0fdf4', color: '#16a34a', gradient: 'linear-gradient(135deg, #16a34a, #22c55e)' },  // 饮食营养
  109: { bg: '#eff6ff', color: '#3b82f6', gradient: 'linear-gradient(135deg, #3b82f6, #60a5fa)' },  // 运动健身
  110: { bg: '#f5f3ff', color: '#7c3aed', gradient: 'linear-gradient(135deg, #7c3aed, #8b5cf6)' },  // 睡眠健康
}

// 分类默认封面 (CSS 渐变)
const categoryGradients: Record<number, string> = {
  100: 'linear-gradient(135deg, #fee2e2 0%, #fecaca 50%, #fca5a5 100%)',
  101: 'linear-gradient(135deg, #dcfce7 0%, #bbf7d0 50%, #86efac 100%)',
  102: 'linear-gradient(135deg, #f3e8ff 0%, #e9d5ff 50%, #d8b4fe 100%)',
  103: 'linear-gradient(135deg, #fce7f3 0%, #fbcfe8 50%, #f9a8d4 100%)',
  104: 'linear-gradient(135deg, #fef3c7 0%, #fde68a 50%, #fcd34d 100%)',
  105: 'linear-gradient(135deg, #fee2e2 0%, #fecaca 50%, #fca5a5 100%)',
  106: 'linear-gradient(135deg, #ffe4e6 0%, #fecdd3 50%, #fda4af 100%)',
  107: 'linear-gradient(135deg, #ffedd5 0%, #fed7aa 50%, #fdba74 100%)',
  108: 'linear-gradient(135deg, #dcfce7 0%, #bbf7d0 50%, #86efac 100%)',
  109: 'linear-gradient(135deg, #dbeafe 0%, #bfdbfe 50%, #93c5fd 100%)',
  110: 'linear-gradient(135deg, #ede9fe 0%, #ddd6fe 50%, #c4b5fd 100%)',
}

// 分类图标映射
const categoryIconMap: Record<number, any> = {
  100: FirstAidKit,
  101: Apple,
  102: ChatDotRound,
  103: UserIcon,
  104: MagicStick,
}

function getCategoryStyle(categoryId?: number) {
  const c = categoryId ? categoryColors[categoryId] : null
  if (!c) return { background: '#e5e7eb', color: '#6b7280' }
  return { background: c.bg, color: c.color }
}

function getCategoryColor(categoryId?: number): string {
  return categoryId && categoryColors[categoryId] ? categoryColors[categoryId].color : '#909399'
}

function getCategoryIcon(cat: LearningCategory) {
  return categoryIconMap[cat.categoryId!] || Document
}

function getCoverUrl(article: LearningArticle): string {
  if (article.coverImage) return article.coverImage
  // 无封面时用分类渐变色 + 文字作为占位
  return ''
}

function getList() {
  loading.value = true
  getPublishedArticles(queryParams).then((response: any) => {
    articleList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function loadCategories() {
  getCategoryTree().then((response: any) => {
    categories.value = response.data || []
  })
}

function loadRecommended() {
  getRecommended().then((response: any) => {
    recommended.value = response.data || []
  })
}

function handleSearch() {
  queryParams.title = searchTitle.value || undefined
  queryParams.pageNum = 1
  getList()
}

function filterByCategory(categoryId?: number) {
  queryParams.categoryId = categoryId
  queryParams.pageNum = 1
  getList()
}

function goToDetail(articleId: number) {
  router.push('/patient/learning/detail/' + articleId)
}

function parseTags(tags?: string): string[] {
  if (!tags) return []
  return tags.split(',').map(t => t.trim()).filter(Boolean).slice(0, 3)
}

function formatDate(date?: string): string {
  if (!date) return ''
  return date.substring(0, 10)
}

getList()
loadCategories()
loadRecommended()
</script>

<style scoped lang="scss">
.learning-page {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Hero Banner */
.hero-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px 40px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  overflow: hidden;

  .hero-content {
    h1 {
      margin: 0 0 8px;
      font-size: 1.8rem;
      font-weight: 700;
    }
    p {
      margin: 0;
      font-size: 1rem;
      opacity: 0.85;
    }
  }

  .hero-illustration {
    width: 160px;
    flex-shrink: 0;
    opacity: 0.8;
  }
}

/* 搜索栏 */
.search-bar {
  margin-bottom: 24px;

  :deep(.el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  }
}

/* 分类筛选 */
.category-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 28px;

  .category-chip {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px 8px 8px;
    border-radius: 24px;
    background: var(--el-fill-color-lighter);
    cursor: pointer;
    transition: all 0.2s;
    border: 2px solid transparent;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }

    &.active {
      background: var(--el-color-primary-light-9);
      border-color: var(--el-color-primary);
      color: var(--el-color-primary);
    }

    .chip-icon {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;

      &.all {
        background: #f3f4f6;
        color: #6b7280;
      }
    }

    span {
      font-size: 14px;
      font-weight: 500;
    }
  }
}

/* 区块标题 */
.section {
  margin-bottom: 32px;
}

.section-header {
  margin-bottom: 16px;

  h3 {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 0;
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--el-text-color-primary);

    .el-icon {
      color: var(--el-color-primary);
    }
  }
}

/* 推荐卡片 */
.featured-card {
  border-radius: 12px;
  overflow: hidden;
  background: var(--el-bg-color);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 16px;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);

    .featured-cover img {
      transform: scale(1.05);
    }
  }

  .featured-cover {
    height: 160px;
    overflow: hidden;
    position: relative;
    background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }

    .featured-overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(to top, rgba(0,0,0,0.4) 0%, transparent 60%);
      display: flex;
      align-items: flex-start;
      padding: 12px;

      .overlay-tags {
        display: flex;
        gap: 6px;

        .badge {
          padding: 2px 10px;
          border-radius: 10px;
          font-size: 12px;
          font-weight: 600;

          &.top {
            background: rgba(239, 68, 68, 0.9);
            color: #fff;
          }
          &.featured {
            background: rgba(245, 158, 11, 0.9);
            color: #fff;
          }
        }
      }
    }
  }

  .featured-info {
    padding: 16px;

    h4 {
      margin: 0 0 8px;
      font-size: 1rem;
      color: var(--el-text-color-primary);
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    p {
      color: var(--el-text-color-secondary);
      font-size: 0.85rem;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin: 0 0 12px;
    }

    .meta {
      display: flex;
      gap: 16px;
      font-size: 0.75rem;
      color: var(--el-text-color-placeholder);

      span {
        display: flex;
        align-items: center;
        gap: 3px;
      }
    }
  }
}

/* 文章列表卡片 */
.article-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  background: var(--el-bg-color);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 16px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);

    .article-cover img {
      transform: scale(1.05);
    }
  }

  .article-cover {
    width: 180px;
    min-height: 140px;
    flex-shrink: 0;
    overflow: hidden;
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }
  }

  .article-body {
    flex: 1;
    padding: 16px;
    display: flex;
    flex-direction: column;
    min-width: 0;

    .article-tags {
      display: flex;
      gap: 6px;
      margin-bottom: 8px;
      flex-wrap: wrap;

      .cat-tag {
        border: none;
      }
    }

    h3 {
      margin: 0 0 8px;
      font-size: 1rem;
      color: var(--el-text-color-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .summary {
      flex: 1;
      color: var(--el-text-color-secondary);
      font-size: 0.85rem;
      line-height: 1.6;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin: 0 0 12px;
    }

    .article-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .meta, .stats {
        display: flex;
        gap: 12px;
        font-size: 0.75rem;
        color: var(--el-text-color-placeholder);

        span {
          display: flex;
          align-items: center;
          gap: 3px;
        }
      }
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty {
  padding: 40px 0;
}
</style>
