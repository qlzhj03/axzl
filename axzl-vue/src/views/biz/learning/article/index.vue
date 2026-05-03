<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="文章标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入文章标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="queryParams.categoryId" placeholder="选择分类" clearable style="width: 200px">
          <el-option v-for="item in categoryOptions" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="文章状态" clearable style="width: 200px">
          <el-option label="草稿" value="0" />
          <el-option label="已发布" value="1" />
          <el-option label="已下架" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:learning:article:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['biz:learning:article:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="articleList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文章ID" prop="articleId" width="80" align="center" />
      <el-table-column label="标题" prop="title" min-width="200" show-overflow-tooltip />
      <el-table-column label="分类" prop="categoryName" width="120" align="center" />
      <el-table-column label="作者" prop="author" width="100" align="center" />
      <el-table-column label="阅读量" prop="viewCount" width="80" align="center" />
      <el-table-column label="点赞数" prop="likeCount" width="80" align="center" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : scope.row.status === '2' ? 'info' : 'warning'">
            {{ scope.row.status === '0' ? '草稿' : scope.row.status === '1' ? '已发布' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="70" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.isTop === 1" type="danger" size="small">置顶</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="发布时间" prop="publishTime" width="160" align="center" />
      <el-table-column label="操作" width="250" align="center">
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:learning:article:edit']" />
          </el-tooltip>
          <el-tooltip :content="scope.row.status === '1' ? '下架' : '发布'" placement="top">
            <el-button link type="primary" :icon="scope.row.status === '1' ? 'Bottom' : 'Top'" @click="handlePublish(scope.row)" v-hasPermi="['biz:learning:article:edit']" />
          </el-tooltip>
          <el-tooltip content="AI摘要" placement="top">
            <el-button link type="primary" icon="MagicStick" @click="handleAiSummary(scope.row)" v-hasPermi="['biz:learning:article:edit']" />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:learning:article:remove']" />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body>
      <el-form ref="articleRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="文章标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入文章标题" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属分类" prop="categoryId">
              <el-tree-select v-model="form.categoryId" :data="categoryTree" :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }" value-key="categoryId" placeholder="选择分类" check-strictly clearable style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源" prop="source">
              <el-input v-model="form.source" placeholder="请输入来源" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图片" prop="coverImage">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="文章摘要" prop="summary">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入文章摘要" />
        </el-form-item>
        <el-form-item label="文章内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="请输入文章内容（支持HTML富文本）" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input v-model="form.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="置顶" prop="isTop">
              <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="推荐" prop="isFeatured">
              <el-switch v-model="form.isFeatured" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="0">草稿</el-radio>
                <el-radio value="1">发布</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="LearningArticle" lang="ts">
import { listArticle, getArticle, addArticle, updateArticle, delArticle, publishArticle, generateAiSummary, getCategoryTree } from '@/api/biz/learning'
import type { LearningArticle, LearningCategory } from '@/types/api/biz/learning'

const { proxy } = getCurrentInstance() as any

const articleList = ref<LearningArticle[]>([])
const categoryOptions = ref<LearningCategory[]>([])
const categoryTree = ref<LearningCategory[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref<number[]>([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {} as any,
  queryParams: { pageNum: 1, pageSize: 10, title: undefined, categoryId: undefined, status: undefined },
  rules: {
    title: [{ required: true, message: '文章标题不能为空', trigger: 'blur' }],
    categoryId: [{ required: true, message: '所属分类不能为空', trigger: 'change' }],
    content: [{ required: true, message: '文章内容不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询文章列表 */
function getList() {
  loading.value = true
  listArticle(queryParams.value).then((response: any) => {
    articleList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 加载分类选项 */
function loadCategories() {
  getCategoryTree().then((response: any) => {
    categoryTree.value = response.data
    // 扁平化用于搜索下拉
    categoryOptions.value = flattenTree(response.data)
  })
}

function flattenTree(tree: any[]): any[] {
  const result: any[] = []
  function walk(nodes: any[]) {
    for (const node of nodes) {
      result.push({ categoryId: node.categoryId, categoryName: node.categoryName })
      if (node.children && node.children.length) {
        walk(node.children)
      }
    }
  }
  walk(tree)
  return result
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map((item: any) => item.articleId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '添加文章'
}

/** 修改按钮操作 */
function handleUpdate(row: any) {
  reset()
  getArticle(row.articleId).then((response: any) => {
    form.value = response.data
    open.value = true
    title.value = '修改文章'
  })
}

/** 发布/下架操作 */
function handlePublish(row: any) {
  const action = row.status === '1' ? '下架' : '发布'
  proxy.$modal.confirm('确认' + action + '文章"' + row.title + '"？').then(() => {
    return publishArticle(row.articleId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess(action + '成功')
  }).catch(() => {})
}

/** AI生成摘要 */
function handleAiSummary(row: any) {
  proxy.$modal.confirm('确认为文章"' + row.title + '"生成AI摘要？').then(() => {
    return generateAiSummary(row.articleId)
  }).then((response: any) => {
    proxy.$modal.msgSuccess('AI摘要生成成功')
    getList()
  }).catch(() => {})
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs['articleRef'].validate((valid: boolean) => {
    if (valid) {
      if (form.value.articleId) {
        updateArticle(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addArticle(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row?: any) {
  const articleIds = row ? [row.articleId] : ids.value
  proxy.$modal.confirm('是否确认删除选中的文章？').then(() => {
    return delArticle(articleIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 重置表单 */
function reset() {
  form.value = {
    articleId: undefined,
    categoryId: undefined,
    title: undefined,
    summary: undefined,
    content: undefined,
    coverImage: undefined,
    author: undefined,
    source: undefined,
    tags: undefined,
    isTop: 0,
    isFeatured: 0,
    status: '0',
    remark: undefined
  }
  proxy.resetForm('articleRef')
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

getList()
loadCategories()
</script>
