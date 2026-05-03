<template>
  <div class="learning-page">
    <div class="page-toolbar">
      <h2>健康知识</h2>
      <el-input v-model="searchTitle" placeholder="搜索文章..." clearable style="width: 280px" @keyup.enter="handleSearch">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <el-tag
        :type="!queryParams.categoryId ? '' : 'info'"
        class="category-tag"
        @click="filterByCategory(undefined)"
      >全部</el-tag>
      <el-tag
        v-for="cat in categories"
        :key="cat.categoryId"
        :type="queryParams.categoryId === cat.categoryId ? '' : 'info'"
        class="category-tag"
        @click="filterByCategory(cat.categoryId)"
      >{{ cat.categoryName }}</el-tag>
    </div>

    <!-- 推荐文章 -->
    <div v-if="recommended.length && !queryParams.categoryId" class="section">
      <h3 class="section-title">推荐阅读</h3>
      <el-row :gutter="16">
        <el-col :span="8" v-for="article in recommended" :key="article.articleId">
          <el-card shadow="hover" class="featured-card" @click="goToDetail(article.articleId!)">
            <div class="featured-cover" v-if="article.coverImage">
              <img :src="article.coverImage" :alt="article.title" />
            </div>
            <div class="featured-info">
              <el-tag size="small" type="success" v-if="article.isTop">置顶</el-tag>
              <el-tag size="small" type="warning" v-if="article.isFeatured">推荐</el-tag>
              <h4>{{ article.title }}</h4>
              <p>{{ article.summary }}</p>
              <div class="meta">
                <span>{{ article.author }}</span>
                <span>{{ article.viewCount }} 阅读</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 文章列表 -->
    <div class="section">
      <h3 class="section-title" v-if="!queryParams.categoryId">最新文章</h3>
      <el-row :gutter="16">
        <el-col :span="12" v-for="article in articleList" :key="article.articleId">
          <el-card shadow="hover" class="article-card" @click="goToDetail(article.articleId!)">
            <div class="article-content">
              <div class="article-text">
                <div class="article-tags">
                  <el-tag size="small" v-if="article.categoryName">{{ article.categoryName }}</el-tag>
                  <el-tag size="small" type="info" v-for="tag in parseTags(article.tags)" :key="tag">{{ tag }}</el-tag>
                </div>
                <h3>{{ article.title }}</h3>
                <p class="summary">{{ article.summary }}</p>
                <div class="meta">
                  <span>{{ article.author }}</span>
                  <span>{{ article.viewCount }} 阅读</span>
                  <span>{{ article.likeCount }} 点赞</span>
                  <span>{{ formatDate(article.publishTime) }}</span>
                </div>
              </div>
              <div class="article-cover" v-if="article.coverImage">
                <img :src="article.coverImage" :alt="article.title" />
              </div>
            </div>
          </el-card>
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
import { Search } from '@element-plus/icons-vue'
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
    // 只取顶级分类
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

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    font-size: 1.5rem;
    color: var(--el-text-color-primary);
  }
}

.category-filter {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;

  .category-tag {
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      transform: translateY(-1px);
    }
  }
}

.section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--el-text-color-primary);
}

.featured-card {
  cursor: pointer;
  margin-bottom: 16px;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  .featured-cover {
    height: 140px;
    overflow: hidden;
    border-radius: 4px;
    margin-bottom: 12px;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .featured-info {
    h4 {
      margin: 8px 0;
      font-size: 1rem;
      color: var(--el-text-color-primary);
    }

    p {
      color: var(--el-text-color-secondary);
      font-size: 0.85rem;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .meta {
      display: flex;
      gap: 12px;
      margin-top: 8px;
      font-size: 0.75rem;
      color: var(--el-text-color-placeholder);
    }
  }
}

.article-card {
  cursor: pointer;
  margin-bottom: 16px;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  .article-content {
    display: flex;
    gap: 16px;
  }

  .article-text {
    flex: 1;
    min-width: 0;

    .article-tags {
      display: flex;
      gap: 6px;
      margin-bottom: 8px;
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
      color: var(--el-text-color-secondary);
      font-size: 0.85rem;
      line-height: 1.6;
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

  .article-cover {
    width: 120px;
    height: 90px;
    flex-shrink: 0;
    border-radius: 4px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
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
