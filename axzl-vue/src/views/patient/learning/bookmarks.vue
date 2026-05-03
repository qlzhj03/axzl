<template>
  <div class="bookmarks-page">
    <div class="page-toolbar">
      <h2>我的收藏</h2>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :span="12" v-for="article in bookmarks" :key="article.articleId">
        <el-card shadow="hover" class="bookmark-card">
          <div class="card-content" @click="goToDetail(article.articleId!)">
            <div class="card-text">
              <el-tag size="small" v-if="article.categoryName">{{ article.categoryName }}</el-tag>
              <h3>{{ article.title }}</h3>
              <p>{{ article.summary }}</p>
              <div class="meta">
                <span>{{ article.author }}</span>
                <span>{{ article.viewCount }} 阅读</span>
                <span>{{ formatDate(article.publishTime) }}</span>
              </div>
              <el-progress v-if="article.readProgress" :percentage="article.readProgress" :stroke-width="4" style="margin-top: 8px" />
            </div>
            <div class="card-cover" v-if="article.coverImage">
              <img :src="article.coverImage" :alt="article.title" />
            </div>
          </div>
          <div class="card-actions">
            <el-button text type="warning" icon="StarFilled" @click.stop="handleRemoveBookmark(article.articleId!)">取消收藏</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

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

const router = useRouter()
const { proxy } = getCurrentInstance() as any

const bookmarks = ref<LearningArticle[]>([])
const loading = ref(false)

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
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-toolbar {
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 1.5rem;
    color: var(--el-text-color-primary);
  }
}

.bookmark-card {
  margin-bottom: 16px;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  .card-content {
    display: flex;
    gap: 16px;
    cursor: pointer;
  }

  .card-text {
    flex: 1;
    min-width: 0;

    h3 {
      margin: 8px 0;
      font-size: 1rem;
      color: var(--el-text-color-primary);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    p {
      color: var(--el-text-color-secondary);
      font-size: 0.85rem;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      margin-bottom: 8px;
    }

    .meta {
      display: flex;
      gap: 12px;
      font-size: 0.75rem;
      color: var(--el-text-color-placeholder);
    }
  }

  .card-cover {
    width: 100px;
    height: 75px;
    flex-shrink: 0;
    border-radius: 4px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .card-actions {
    margin-top: 8px;
    border-top: 1px solid var(--el-border-color-lighter);
    padding-top: 8px;
  }
}

.empty {
  padding: 60px 0;
}
</style>
