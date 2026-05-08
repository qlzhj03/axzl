<template>
  <div class="bookmarks-page">
    <div class="page-header">
      <div class="header-left">
        <el-button text icon="ArrowLeft" @click="router.push('/patient/learning')">返回</el-button>
        <h2><el-icon><Star /></el-icon> 我的收藏</h2>
      </div>
      <span class="count" v-if="bookmarks.length">{{ bookmarks.length }} 篇文章</span>
    </div>

    <div class="bookmark-list" v-loading="loading">
      <div v-for="article in bookmarks" :key="article.articleId" class="bookmark-card" @click="goToDetail(article.articleId!)">
        <div class="card-cover" :style="{ background: getGradient(article.categoryId) }">
          <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
          <div v-else class="cover-placeholder">
            <el-icon :size="32"><Document /></el-icon>
          </div>
        </div>
        <div class="card-body">
          <div class="card-top">
            <el-tag size="small" v-if="article.categoryName" :color="getCategoryColor(article.categoryId)" effect="dark" class="cat-tag">{{ article.categoryName }}</el-tag>
            <el-button text type="warning" size="small" icon="StarFilled" @click.stop="handleRemoveBookmark(article.articleId!)">取消收藏</el-button>
          </div>
          <h3>{{ article.title }}</h3>
          <p class="summary">{{ article.summary }}</p>
          <div class="card-footer">
            <div class="meta">
              <span v-if="article.author">{{ article.author }}</span>
              <span>{{ formatDate(article.publishTime) }}</span>
            </div>
            <div class="stats">
              <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
              <span><el-icon><Pointer /></el-icon> {{ article.likeCount }}</span>
            </div>
          </div>
          <el-progress v-if="article.readProgress" :percentage="article.readProgress" :stroke-width="4" :show-text="false" style="margin-top: 10px" />
        </div>
      </div>
    </div>

    <div v-if="!bookmarks.length && !loading" class="empty">
      <el-empty description="暂无收藏文章">
        <el-button type="primary" @click="router.push('/patient/learning')">去浏览文章</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup name="PatientLearningBookmarks" lang="ts">
import { getBookmarks, toggleBookmark } from '@/api/biz/learning'
import type { LearningArticle } from '@/types/api/biz/learning'
import { useRouter } from 'vue-router'
import { Star, View, Pointer, Document } from '@element-plus/icons-vue'

const router = useRouter()
const { proxy } = getCurrentInstance() as any

const bookmarks = ref<LearningArticle[]>([])
const loading = ref(false)

const categoryColors: Record<number, string> = {
  100: '#ef4444', 101: '#22c55e', 102: '#a855f7', 103: '#ec4899',
  104: '#f59e0b', 105: '#ef4444', 106: '#e11d48', 107: '#f97316',
  108: '#16a34a', 109: '#3b82f6', 110: '#7c3aed',
}

const gradients: Record<number, string> = {
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

function getCategoryColor(categoryId?: number): string {
  return categoryColors[categoryId || 0] || '#909399'
}

function getGradient(categoryId?: number): string {
  return gradients[categoryId || 0] || 'linear-gradient(135deg, #f0f9ff, #e0f2fe)'
}

function loadBookmarks() {
  loading.value = true
  getBookmarks().then((response: any) => {
    bookmarks.value = response.data || []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function goToDetail(articleId: number) {
  router.push('/patient/learning/detail/' + articleId)
}

function handleRemoveBookmark(articleId: number) {
  toggleBookmark(articleId).then(() => {
    bookmarks.value = bookmarks.value.filter((a: any) => a.articleId !== articleId)
    proxy.$modal.msgSuccess('已取消收藏')
  })
}

function formatDate(date?: string): string {
  if (!date) return ''
  return date.substring(0, 10)
}

loadBookmarks()
</script>

<style scoped lang="scss">
.bookmarks-page {
  animation: fadeIn 0.3s ease;
  max-width: 800px;
  margin: 0 auto;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;

    h2 {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 0;
      font-size: 1.4rem;
      color: var(--el-text-color-primary);

      .el-icon {
        color: #f59e0b;
      }
    }
  }

  .count {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.bookmark-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bookmark-card {
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  background: var(--el-bg-color);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);

    .card-cover img {
      transform: scale(1.05);
    }
  }

  .card-cover {
    width: 200px;
    min-height: 160px;
    flex-shrink: 0;
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s;
    }

    .cover-placeholder {
      color: rgba(0, 0, 0, 0.15);
    }
  }

  .card-body {
    flex: 1;
    padding: 16px 20px;
    display: flex;
    flex-direction: column;
    min-width: 0;

    .card-top {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;

      .cat-tag {
        border: none;
      }
    }

    h3 {
      margin: 0 0 6px;
      font-size: 1.05rem;
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
      margin: 0 0 10px;
    }

    .card-footer {
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

.empty {
  padding: 60px 0;
}
</style>
