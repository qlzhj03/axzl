<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" class="demo-tabs">
      <el-tab-pane label="会话列表" name="list">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="用户昵称" prop="nickName">
            <el-input v-model="queryParams.nickName" placeholder="请输入用户昵称" clearable style="width: 240px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="会话标题" prop="title">
            <el-input v-model="queryParams.title" placeholder="请输入会话标题" clearable style="width: 240px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="会话状态" clearable style="width: 240px">
              <el-option label="进行中" value="0" />
              <el-option label="已结束" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间" style="width: 308px">
            <el-date-picker
              v-model="dateRange"
              value-format="YYYY-MM-DD"
              type="daterange"
              range-separator="-"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            ></el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['biz:consultation:remove']">删除</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="sessionList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="用户昵称" align="center" prop="nickName" width="120" :show-overflow-tooltip="true" />
          <el-table-column label="会话标题" align="center" prop="title" min-width="200" :show-overflow-tooltip="true">
            <template #default="scope">
              <el-tag v-if="scope.row.isPinned === 1" type="warning" size="small" style="margin-right: 5px">置顶</el-tag>
              <span>{{ scope.row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column label="症状摘要" align="center" prop="symptomSummary" min-width="200" :show-overflow-tooltip="true" />
          <el-table-column label="推荐科室" align="center" prop="recommendedLabel" width="120" />
          <el-table-column label="消息数" align="center" prop="messageCount" width="80" />
          <el-table-column label="满意度" align="center" prop="satisfaction" width="100">
            <template #default="scope">
              <el-rate v-if="scope.row.satisfaction" v-model="scope.row.satisfaction" disabled show-score text-color="#ff9900" score-template="{value}星" />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" prop="status" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
                {{ scope.row.status === '0' ? '进行中' : '已结束' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最后消息时间" align="center" prop="lastMessageTime" width="160">
            <template #default="scope">
              <span>{{ parseTime(scope.row.lastMessageTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="250" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-tooltip content="查看" placement="top">
                <el-button link type="primary" icon="View" @click="handleView(scope.row)"></el-button>
              </el-tooltip>
              <el-tooltip content="修改标题" placement="top">
                <el-button link type="primary" icon="Edit" @click="handleEditTitle(scope.row)" v-hasPermi="['biz:consultation:edit']"></el-button>
              </el-tooltip>
              <el-tooltip :content="scope.row.isPinned === 1 ? '取消置顶' : '置顶'" placement="top">
                <el-button link :type="scope.row.isPinned === 1 ? 'warning' : 'primary'" :icon="scope.row.isPinned === 1 ? 'Star' : 'StarFilled'" @click="handlePin(scope.row)" v-hasPermi="['biz:consultation:pin']"></el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:consultation:remove']"></el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-tab-pane>
      
      <el-tab-pane label="智能问诊" name="chat">
        <ChatInterface />
      </el-tab-pane>
    </el-tabs>

    <!-- 查看会话详情对话框 -->
    <el-dialog title="会话详情" v-model="viewOpen" width="900px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户昵称">{{ viewForm.nickName }}</el-descriptions-item>
        <el-descriptions-item label="会话标题">{{ viewForm.title }}</el-descriptions-item>
        <el-descriptions-item label="推荐科室">{{ viewForm.recommendedLabel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="消息数量">{{ viewForm.messageCount }}</el-descriptions-item>
        <el-descriptions-item label="满意度">
          <el-rate v-if="viewForm.satisfaction" v-model="viewForm.satisfaction" disabled show-score text-color="#ff9900" score-template="{value}星" />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewForm.status === '0' ? 'success' : 'info'">
            {{ viewForm.status === '0' ? '进行中' : '已结束' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="症状摘要" :span="2">
          <div style="white-space: pre-wrap;">{{ viewForm.symptomSummary || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="AI诊断" :span="2">
          <div style="white-space: pre-wrap;">{{ viewForm.aiDiagnosis || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(viewForm.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="最后消息时间">{{ parseTime(viewForm.lastMessageTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewForm.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">对话记录</el-divider>
      <div class="message-list" v-if="messageList.length > 0">
        <div v-for="(msg, index) in messageList" :key="index" :class="['message-item', msg.role]">
          <div class="message-header">
            <span class="message-role">{{ msg.role === 'user' ? '用户' : 'AI助手' }}</span>
            <span class="message-time">{{ parseTime(msg.createTime) }}</span>
          </div>
          <div class="message-content">{{ msg.content }}</div>
        </div>
      </div>
      <el-empty v-else description="暂无对话记录" />

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑标题对话框 -->
    <el-dialog title="修改会话标题" v-model="editDialogVisible" width="500px" append-to-body>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="会话标题">
          <el-input v-model="editForm.title" placeholder="请输入会话标题" maxlength="100" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitEditTitle">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts" name="Consultation">
import { listConsultation, getConsultation, delConsultation, updateConsultationTitle, updatePinStatus } from "@/api/biz/consultation"
import ChatInterface from './ChatInterface.vue'

const { proxy } = getCurrentInstance()

const activeTab = ref('list') // 默认显示会话列表
const sessionList = ref<any[]>([])
const messageList = ref<any[]>([])
const viewOpen = ref<boolean>(false)
const loading = ref<boolean>(true)
const showSearch = ref<boolean>(true)
const ids = ref<string[]>([])
const multiple = ref<boolean>(true)
const total = ref<number>(0)
const dateRange = ref<string[]>([])

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  nickName: undefined,
  title: undefined,
  status: undefined
})

const viewForm = ref<any>({})

// 编辑标题相关
const editDialogVisible = ref<boolean>(false)
const editForm = ref({
  sessionId: '',
  title: ''
})

/** 查询问诊会话列表 */
function getList() {
  loading.value = true
  listConsultation(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    sessionList.value = response.rows
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
  dateRange.value = []
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection: any[]) {
  ids.value = selection.map(item => item.sessionId)
  multiple.value = !selection.length
}

/** 查看按钮操作 */
function handleView(row: any) {
  const sessionId = row.sessionId
  getConsultation(sessionId).then(response => {
    viewForm.value = response.data
    // TODO: 获取消息列表，需要后端提供接口
    messageList.value = []
    viewOpen.value = true
  })
}

/** 删除按钮操作 */
function handleDelete(row?: any) {
  const sessionIds = row?.sessionId || ids.value
  proxy.$modal.confirm('是否确认删除选中的问诊会话？').then(function () {
    return delConsultation(sessionIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 编辑标题按钮操作 */
function handleEditTitle(row: any) {
  editForm.value.sessionId = row.sessionId
  editForm.value.title = row.title
  editDialogVisible.value = true
}

/** 提交编辑标题 */
function submitEditTitle() {
  if (!editForm.value.title.trim()) {
    proxy.$modal.msgError("标题不能为空")
    return
  }
  updateConsultationTitle(editForm.value.sessionId, editForm.value.title).then(() => {
    proxy.$modal.msgSuccess("修改成功")
    editDialogVisible.value = false
    getList()
  })
}

/** 置顶/取消置顶按钮操作 */
function handlePin(row: any) {
  const newPinStatus = row.isPinned === 1 ? 0 : 1
  const actionText = newPinStatus === 1 ? '置顶' : '取消置顶'
  proxy.$modal.confirm(`确认${actionText}该会话吗？`).then(() => {
    return updatePinStatus(row.sessionId, newPinStatus)
  }).then(() => {
    proxy.$modal.msgSuccess(`${actionText}成功`)
    getList()
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.message-list {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px;

  .message-item {
    margin-bottom: 15px;
    padding: 10px;
    border-radius: 8px;

    &.user {
      background-color: #f0f9ff;
      border-left: 4px solid #409eff;
    }

    &.assistant {
      background-color: #f4f4f5;
      border-left: 4px solid #67c23a;
    }

    .message-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 12px;
      color: #909399;

      .message-role {
        font-weight: bold;
        color: #606266;
      }
    }

    .message-content {
      line-height: 1.6;
      white-space: pre-wrap;
      word-break: break-word;
    }
  }
}
</style>
