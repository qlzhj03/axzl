<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="分类名称" prop="categoryName">
        <el-input v-model="queryParams.categoryName" placeholder="请输入分类名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="分类状态" clearable style="width: 200px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
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
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:learning:category:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 树形表格 -->
    <el-table v-if="refreshTable" :data="categoryList" row-key="categoryId" :default-expand-all="isExpandAll" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
      <el-table-column label="分类名称" prop="categoryName" min-width="200" />
      <el-table-column label="图标" prop="icon" width="100" align="center">
        <template #default="scope">
          <span>{{ scope.row.icon }}</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sortOrder" width="80" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">{{ scope.row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="180" align="center" />
      <el-table-column label="操作" width="220" align="center">
        <template #default="scope">
          <el-tooltip content="修改" placement="top">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:learning:category:edit']" />
          </el-tooltip>
          <el-tooltip content="新增子分类" placement="top">
            <el-button link type="primary" icon="Plus" @click="handleAdd(scope.row)" v-hasPermi="['biz:learning:category:add']" />
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:learning:category:remove']" />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="categoryRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select v-model="form.parentId" :data="categoryOptions" :props="{ value: 'categoryId', label: 'categoryName', children: 'children' }" value-key="categoryId" placeholder="选择上级分类" check-strictly clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
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

<script setup name="LearningCategory" lang="ts">
import { listCategory, getCategory, addCategory, updateCategory, delCategory, getCategoryTree } from '@/api/biz/learning'
import type { LearningCategory } from '@/types/api/biz/learning'

const { proxy } = getCurrentInstance() as any

const categoryList = ref<LearningCategory[]>([])
const categoryOptions = ref<LearningCategory[]>([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const title = ref('')
const isExpandAll = ref(true)
const refreshTable = ref(true)

const data = reactive({
  form: {} as any,
  queryParams: { categoryName: undefined, status: undefined },
  rules: {
    categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
    sortOrder: [{ required: true, message: '排序不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询分类列表 */
function getList() {
  loading.value = true
  listCategory(queryParams.value).then((response: any) => {
    categoryList.value = proxy.handleTree(response.rows, 'categoryId', 'parentId')
    loading.value = false
  })
}

/** 查询分类下拉树 */
function getTree() {
  getCategoryTree().then((response: any) => {
    categoryOptions.value = response.data
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 展开/折叠操作 */
function toggleExpandAll() {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

/** 新增按钮操作 */
function handleAdd(row?: any) {
  reset()
  getTree()
  if (row && row.categoryId) {
    form.value.parentId = row.categoryId
  }
  open.value = true
  title.value = '添加分类'
}

/** 修改按钮操作 */
function handleUpdate(row: any) {
  reset()
  getTree()
  getCategory(row.categoryId).then((response: any) => {
    form.value = response.data
    open.value = true
    title.value = '修改分类'
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs['categoryRef'].validate((valid: boolean) => {
    if (valid) {
      if (form.value.categoryId) {
        updateCategory(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addCategory(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row: any) {
  proxy.$modal.confirm('是否确认删除分类"' + row.categoryName + '"？').then(() => {
    return delCategory(row.categoryId)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 重置表单 */
function reset() {
  form.value = {
    categoryId: undefined,
    parentId: 0,
    categoryName: undefined,
    icon: undefined,
    sortOrder: 0,
    status: '0',
    remark: undefined
  }
  proxy.resetForm('categoryRef')
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

getList()
</script>
