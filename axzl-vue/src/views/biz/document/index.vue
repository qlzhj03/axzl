<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文件名" prop="fileName">
        <el-input
          v-model="queryParams.fileName"
          placeholder="请输入文件名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="文件类型" prop="fileType">
        <el-select v-model="queryParams.fileType" placeholder="请选择文件类型" clearable>
          <el-option label="TXT" value="txt" />
          <el-option label="Word" value="word" />
          <el-option label="PDF" value="pdf" />
          <el-option label="Markdown" value="md" />
          <el-option label="JSONL" value="jsonl" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态" prop="vectorStatus">
        <el-select v-model="queryParams.vectorStatus" placeholder="请选择处理状态" clearable>
          <el-option label="未处理" value="0" />
          <el-option label="处理中" value="1" />
          <el-option label="已完成" value="2" />
          <el-option label="失败" value="3" />
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
          icon="Upload"
          @click="handleUpload"
          v-hasPermi="['biz:document:upload']"
        >上传文档</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['biz:document:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="documentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="文档ID" align="center" prop="id" width="80" />
      <el-table-column label="文件名" align="center" prop="fileName" :show-overflow-tooltip="true" />
      <el-table-column label="文件类型" align="center" prop="fileType" width="100">
        <template #default="scope">
          <el-tag :type="getFileTypeTag(scope.row.fileType)">
            {{ getFileTypeName(scope.row.fileType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文件大小" align="center" prop="fileSize" width="120">
        <template #default="scope">
          {{ formatFileSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="切分块数" align="center" prop="chunkCount" width="100" />
      <el-table-column label="处理状态" align="center" prop="vectorStatus" width="100">
        <template #default="scope">
          <el-tag :type="getStatusTag(scope.row.vectorStatus)">
            {{ getStatusName(scope.row.vectorStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上传时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)" v-hasPermi="['biz:document:query']">详情</el-button>
          <el-button 
            link 
            type="primary" 
            icon="Refresh" 
            @click="handleReprocess(scope.row)" 
            v-hasPermi="['biz:document:edit']"
            v-if="scope.row.vectorStatus === '3'"
          >重试</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:document:remove']">删除</el-button>
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

    <!-- 上传文档对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="500px" append-to-body>
      <el-upload
        ref="uploadRef"
        :action="upload.url"
        :headers="upload.headers"
        :before-upload="handleBeforeUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :file-list="upload.fileList"
        :limit="1"
        :auto-upload="false"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">
            支持 txt/word/pdf/md/jsonl 格式，文件大小不超过 10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitUpload">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 文档详情对话框 -->
    <el-dialog title="文档详情" v-model="viewOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="文档ID">{{ viewForm.id }}</el-descriptions-item>
        <el-descriptions-item label="文件名">{{ viewForm.fileName }}</el-descriptions-item>
        <el-descriptions-item label="文件类型">
          <el-tag :type="getFileTypeTag(viewForm.fileType)">
            {{ getFileTypeName(viewForm.fileType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ formatFileSize(viewForm.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="切分块数">{{ viewForm.chunkCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getStatusTag(viewForm.vectorStatus)">
            {{ getStatusName(viewForm.vectorStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="文件路径" :span="2">{{ viewForm.milvusFilePath || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上传时间" :span="2">{{ parseTime(viewForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ parseTime(viewForm.updateTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewForm.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="Document">
import { listDocument, getDocument, delDocument, uploadDocument, reprocessDocument } from "@/api/biz/document"
import { getToken } from "@/utils/auth"
import type { UploadFileResult } from '@/types/api/common'

const { proxy } = getCurrentInstance()

const documentList = ref<any[]>([])
const loading = ref<boolean>(true)
const showSearch = ref<boolean>(true)
const ids = ref<number[]>([])
const multiple = ref<boolean>(true)
const total = ref<number>(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  fileName: undefined,
  fileType: undefined,
  vectorStatus: undefined
})

const upload = reactive({
  open: false,
  title: "上传文档",
  url: import.meta.env.VITE_APP_BASE_API + "/biz/knowledge/document/upload",
  headers: { Authorization: "Bearer " + getToken() },
  fileList: [] as any[]
})

const viewOpen = ref<boolean>(false)
const viewForm = ref<any>({})

/** 查询文档列表 */
function getList() {
  loading.value = true
  listDocument(queryParams.value).then(response => {
    documentList.value = response.rows
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

/** 上传按钮操作 */
function handleUpload() {
  upload.fileList = []
  upload.open = true
}

/** 上传前校检 */
function handleBeforeUpload(file: File): boolean {
  const allowedTypes = ['txt', 'doc', 'docx', 'pdf', 'md', 'jsonl']
  const ext = file.name.split('.').pop()?.toLowerCase()
  
  if (!ext || !allowedTypes.includes(ext)) {
    proxy?.$modal.msgError("文件格式不正确，请上传 txt/word/pdf/md/jsonl 格式文件!")
    return false
  }
  
  if (file.size > 10 * 1024 * 1024) {
    proxy?.$modal.msgError("上传文件大小不能超过 10MB!")
    return false
  }
  
  return true
}

/** 文件上传成功 */
function handleUploadSuccess(response: UploadFileResult, file: any) {
  if (response.code === 200) {
    proxy?.$modal.msgSuccess(response.message || "上传成功，正在后台进行向量化处理")
    upload.open = false
    getList()
  } else {
    proxy?.$modal.msgError(response.msg || "上传失败")
  }
}

/** 文件上传失败 */
function handleUploadError(err: Error) {
  proxy?.$modal.msgError("上传失败")
}

/** 提交上传文件 */
function submitUpload() {
  const uploadRef = proxy?.$refs.uploadRef
  if (uploadRef) {
    uploadRef.submit()
  }
}

/** 查看详情 */
function handleView(row: any) {
  getDocument(row.id).then(response => {
    viewForm.value = response.data
    viewOpen.value = true
  })
}

/** 重新处理文档 */
function handleReprocess(row: any) {
  proxy?.$modal.confirm('是否确认重新处理该文档？').then(() => {
    return reprocessDocument(row.id)
  }).then(() => {
    getList()
    proxy?.$modal.msgSuccess("已启动重新处理")
  }).catch(() => {})
}

/** 删除按钮操作 */
function handleDelete(row?: any) {
  const docIds = row ? [row.id] : ids.value
  proxy?.$modal.confirm('是否确认删除选中的文档？').then(() => {
    return delDocument(docIds)
  }).then(() => {
    getList()
    proxy?.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 获取文件类型标签 */
function getFileTypeTag(type: string): string {
  const tagMap: Record<string, string> = {
    'txt': '',
    'word': 'success',
    'pdf': 'danger',
    'md': 'warning',
    'jsonl': 'info'
  }
  return tagMap[type] || ''
}

/** 获取文件类型名称 */
function getFileTypeName(type: string): string {
  const nameMap: Record<string, string> = {
    'txt': 'TXT',
    'word': 'Word',
    'pdf': 'PDF',
    'md': 'Markdown',
    'jsonl': 'JSONL'
  }
  return nameMap[type] || type
}

/** 获取状态标签 */
function getStatusTag(status: string): string {
  const tagMap: Record<string, string> = {
    '0': 'info',
    '1': 'warning',
    '2': 'success',
    '3': 'danger'
  }
  return tagMap[status] || ''
}

/** 获取状态名称 */
function getStatusName(status: string): string {
  const nameMap: Record<string, string> = {
    '0': '未处理',
    '1': '处理中',
    '2': '已完成',
    '3': '失败'
  }
  return nameMap[status] || status
}

/** 格式化文件大小 */
function formatFileSize(size: number): string {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let fileSize = size
  
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }
  
  return fileSize.toFixed(2) + ' ' + units[index]
}

getList()
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}
</style>
