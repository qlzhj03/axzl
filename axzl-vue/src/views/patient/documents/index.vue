<template>
  <div class="patient-documents">
    <div class="page-toolbar">
      <h2 class="page-title">我的文档</h2>
      <el-upload
        :show-file-list="false"
        :before-upload="beforeUpload"
        :http-request="handleUpload"
        accept=".txt,.doc,.docx,.pdf,.md,.jsonl"
      >
        <el-button type="primary">
          <el-icon><Upload /></el-icon>上传文档
        </el-button>
      </el-upload>
    </div>

    <el-card shadow="never">
      <el-table :data="documents" v-loading="loading" style="width: 100%;">
        <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip />
        <el-table-column prop="fileType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ (row.fileType || '').toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="大小" width="100">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="chunkCount" label="分块数" width="80" />
        <el-table-column label="向量化状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.vectorStatus)" size="small">
              {{ statusText(row.vectorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.vectorStatus === '0' || row.vectorStatus === '3'"
              type="primary"
              link
              size="small"
              @click="reprocess(row)"
            >
              重新处理
            </el-button>
            <el-button type="danger" link size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadDocuments"
          @current-change="loadDocuments"
        />
      </div>
    </el-card>

    <el-alert
      title="文档上传后将自动进行向量化处理，用于 AI 问诊时的知识检索。支持格式：TXT、Word、PDF、Markdown、JSONL。"
      type="info"
      show-icon
      :closable="false"
      style="margin-top: 16px;"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { listDocument, uploadDocument, delDocument, reprocessDocument } from '@/api/biz/document'

defineOptions({ name: 'PatientDocuments' })

const loading = ref(false)
const documents = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })

function formatSize(bytes: number | undefined) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function formatTime(time: string | undefined) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

function statusText(s: string) {
  const map: Record<string, string> = { '0': '待处理', '1': '处理中', '2': '已完成', '3': '失败' }
  return map[s] || '未知'
}

function statusType(s: string) {
  const map: Record<string, string> = { '0': 'info', '1': 'warning', '2': 'success', '3': 'danger' }
  return (map[s] || 'info') as any
}

async function loadDocuments() {
  loading.value = true
  try {
    const res = await listDocument(queryParams)
    documents.value = res.rows || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function beforeUpload(file: File) {
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

async function handleUpload(options: any) {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    await uploadDocument(formData)
    ElMessage.success('上传成功，正在后台进行向量化处理')
    loadDocuments()
  } catch (e) {
    console.error(e)
    ElMessage.error('上传失败')
  }
}

async function reprocess(row: any) {
  try {
    await reprocessDocument(row.id)
    ElMessage.success('已重新提交处理')
    loadDocuments()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

async function remove(row: any) {
  try {
    await ElMessageBox.confirm(`确定删除文档「${row.fileName}」吗？`, '提示', { type: 'warning' })
    await delDocument(row.id)
    ElMessage.success('已删除')
    loadDocuments()
  } catch {}
}

onMounted(() => { loadDocuments() })
</script>

<style scoped lang="scss">
.patient-documents {
  animation: fadeIn 0.3s ease;
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--patient-text);
  margin: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
