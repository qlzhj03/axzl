<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="问题内容" prop="question">
        <el-input
          v-model="queryParams.question"
          placeholder="请输入问题内容"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="分类标签" prop="label">
        <el-input
          v-model="queryParams.label"
          placeholder="请输入分类标签"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源类型" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="请选择来源类型" clearable>
          <el-option label="手工录入" value="qa" />
          <el-option label="文档导入" value="doc" />
        </el-select>
      </el-form-item>
      <el-form-item label="向量状态" prop="vectorStatus">
        <el-select v-model="queryParams.vectorStatus" placeholder="请选择向量状态" clearable>
          <el-option label="未导入" value="0" />
          <el-option label="已导入" value="1" />
          <el-option label="失败" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['biz:knowledge:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['biz:knowledge:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Refresh"
          :loading="syncing"
          @click="handleSync"
          v-hasPermi="['biz:knowledge:list']"
        >手动同步向量
          <el-badge v-if="unprocessedCount > 0" :value="unprocessedCount" :max="99" class="sync-badge" />
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="knowledgeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="问题内容" align="center" prop="question" :show-overflow-tooltip="true" min-width="200" />
      <el-table-column label="分类标签" align="center" prop="label" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.label">{{ scope.row.label }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="评分" align="center" prop="score" width="80" />
      <el-table-column label="关联疾病" align="center" prop="relatedDiseases" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="来源类型" align="center" prop="sourceType" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.sourceType === 'doc' ? 'warning' : ''">
            {{ scope.row.sourceType === 'doc' ? '文档导入' : '手工录入' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="向量状态" align="center" prop="vectorStatus" width="100">
        <template #default="scope">
          <el-tag :type="getVectorStatusTag(scope.row.vectorStatus)">
            {{ getVectorStatusName(scope.row.vectorStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['biz:knowledge:query']">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['biz:knowledge:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:knowledge:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogOpen" width="650px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="问题内容" prop="question">
          <el-input v-model="form.question" type="textarea" :rows="3" placeholder="请输入问题或文本块内容" />
        </el-form-item>
        <el-form-item label="答案内容" prop="answer">
          <el-input v-model="form.answer" type="textarea" :rows="5" placeholder="请输入答案或补充内容" />
        </el-form-item>
        <el-form-item label="分类标签" prop="label">
          <el-input v-model="form.label" placeholder="请输入分类标签，如：心血管、糖尿病" maxlength="50" />
        </el-form-item>
        <el-form-item label="评分" prop="score">
          <el-input-number v-model="form.score" :min="0" :max="100" placeholder="评分/重要度" />
        </el-form-item>
        <el-form-item label="关联疾病" prop="relatedDiseases">
          <el-input v-model="form.relatedDiseases" placeholder="请输入关联疾病，多个用逗号分隔" maxlength="500" />
        </el-form-item>
        <el-form-item label="数据来源" prop="source">
          <el-input v-model="form.source" placeholder="请输入数据来源" maxlength="100" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="dialogOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="知识详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="知识ID">{{ detailForm.id }}</el-descriptions-item>
        <el-descriptions-item label="分类标签">
          <el-tag v-if="detailForm.label">{{ detailForm.label }}</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="评分">{{ detailForm.score ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="关联疾病">{{ detailForm.relatedDiseases || '-' }}</el-descriptions-item>
        <el-descriptions-item label="来源类型">
          <el-tag :type="detailForm.sourceType === 'doc' ? 'warning' : ''">
            {{ detailForm.sourceType === 'doc' ? '文档导入' : '手工录入' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="向量状态">
          <el-tag :type="getVectorStatusTag(detailForm.vectorStatus)">
            {{ getVectorStatusName(detailForm.vectorStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="向量ID" :span="2">{{ detailForm.vectorId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="数据来源" :span="2">{{ detailForm.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题内容" :span="2">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">{{ detailForm.question || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="答案内容" :span="2">
          <div style="white-space: pre-wrap; max-height: 300px; overflow-y: auto;">{{ detailForm.answer || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建人" :span="1">{{ detailForm.createBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="1">{{ parseTime(detailForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新人" :span="1">{{ detailForm.updateBy || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="1">{{ parseTime(detailForm.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailForm.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="Knowledge">
import { listKnowledge, getKnowledge, addKnowledge, updateKnowledge, delKnowledge, syncKnowledge, getSyncStatus } from "@/api/biz/knowledge"

const { proxy } = getCurrentInstance()

const knowledgeList = ref<any[]>([])
const loading = ref<boolean>(true)
const showSearch = ref<boolean>(true)
const ids = ref<number[]>([])
const multiple = ref<boolean>(true)
const total = ref<number>(0)
const dialogOpen = ref<boolean>(false)
const dialogTitle = ref<string>("")
const detailOpen = ref<boolean>(false)
const syncing = ref<boolean>(false)
const unprocessedCount = ref<number>(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  question: undefined,
  label: undefined,
  sourceType: undefined,
  vectorStatus: undefined
})

const form = ref<any>({})
const detailForm = ref<any>({})

const rules = {
  question: [{ required: true, message: "问题内容不能为空", trigger: "blur" }]
}

/** 查询知识库列表 */
function getList() {
  loading.value = true
  listKnowledge(queryParams.value).then(response => {
    knowledgeList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy?.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  form.value = {
    question: "",
    answer: "",
    label: "",
    score: 0,
    relatedDiseases: "",
    source: "",
    status: "0",
    remark: ""
  }
  dialogTitle.value = "新增知识"
  dialogOpen.value = true
}

/** 修改按钮操作 */
function handleEdit(row: any) {
  getKnowledge(row.id).then(response => {
    form.value = response.data
    dialogTitle.value = "修改知识"
    dialogOpen.value = true
  })
}

/** 详情按钮操作 */
function handleDetail(row: any) {
  getKnowledge(row.id).then(response => {
    detailForm.value = response.data
    detailOpen.value = true
  })
}

/** 提交表单 */
function submitForm() {
  const formRef = proxy?.$refs.formRef as any
  formRef?.validate((valid: boolean) => {
    if (!valid) return

    if (form.value.id) {
      updateKnowledge(form.value).then(() => {
        proxy?.$modal.msgSuccess("修改成功")
        dialogOpen.value = false
        getList()
        refreshSyncStatus()
      })
    } else {
      addKnowledge(form.value).then(() => {
        proxy?.$modal.msgSuccess("新增成功，正在后台自动向量化")
        dialogOpen.value = false
        getList()
        refreshSyncStatus()
      })
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row?: any) {
  const knowledgeIds = row ? [row.id] : ids.value
  proxy?.$modal.confirm('是否确认删除选中的知识条目？').then(() => {
    return delKnowledge(knowledgeIds)
  }).then(() => {
    getList()
    proxy?.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 向量状态标签类型 */
function getVectorStatusTag(status: string): string {
  const tagMap: Record<string, string> = {
    '0': 'info',
    '1': 'success',
    '2': 'danger'
  }
  return tagMap[status] || ''
}

/** 向量状态名称 */
function getVectorStatusName(status: string): string {
  const nameMap: Record<string, string> = {
    '0': '未导入',
    '1': '已导入',
    '2': '失败'
  }
  return nameMap[status] || status
}

/** 查询未向量化数量 */
function refreshSyncStatus() {
  getSyncStatus().then(response => {
    unprocessedCount.value = response.unprocessed || 0
  })
}

/** 手动同步向量 */
function handleSync() {
  syncing.value = true
  syncKnowledge().then(response => {
    proxy?.$modal.msgSuccess(response.msg || "同步任务已启动")
    setTimeout(() => {
      refreshSyncStatus()
      getList()
      syncing.value = false
    }, 3000)
  }).catch(() => {
    syncing.value = false
  })
}

getList()
refreshSyncStatus()
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}
.sync-badge {
  margin-left: 6px;
  :deep(.el-badge__content) {
    vertical-align: super;
  }
}
</style>
