<template>
  <div class="patient-consultation">
    <!-- 左侧：会话历史 -->
    <aside :class="['session-sidebar', { collapsed: sidebarCollapsed }]">
      <template v-if="!sidebarCollapsed">
        <div class="sidebar-header">
          <span class="sidebar-title">问诊记录</span>
          <div class="header-actions">
            <el-button type="primary" link @click="startNewChat">
              <el-icon><Plus /></el-icon> 新对话
            </el-button>
            <el-icon class="collapse-btn" @click="sidebarCollapsed = true"><DArrowLeft /></el-icon>
          </div>
        </div>

        <div class="sidebar-search">
          <el-input
            v-model="searchKeyword"
            size="small"
            placeholder="搜索..."
            clearable
            :prefix-icon="Search"
          />
        </div>

        <div class="session-list" ref="sessionListRef" @scroll="onSessionScroll">
          <div
            v-for="session in filteredSessions"
            :key="session.sessionId"
            :class="['session-item', { active: currentSessionId === session.sessionId }]"
            @click="loadSession(session)"
          >
            <div class="session-title-row">
              <span v-if="session.isPinned" class="pin-icon">📌</span>
              <span class="session-title">{{ session.title || '新对话' }}</span>
            </div>
            <div class="session-preview" v-if="session.symptomSummary">{{ session.symptomSummary }}</div>
            <div class="session-footer">
              <span v-if="session.recommendedLabel" class="session-tag">{{ session.recommendedLabel }}</span>
              <span class="session-count">{{ session.messageCount || 0 }}条</span>
              <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
              <div class="session-actions" @click.stop>
                <el-dropdown trigger="click" @command="(cmd: string) => handleSessionCommand(cmd, session)">
                  <el-icon class="more-btn"><MoreFilled /></el-icon>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="pin">{{ session.isPinned ? '取消置顶' : '置顶' }}</el-dropdown-item>
                      <el-dropdown-item command="rename">重命名</el-dropdown-item>
                      <el-dropdown-item command="rate" v-if="!session.satisfaction">评价</el-dropdown-item>
                      <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>

          <div v-if="loadingMore" class="list-status"><el-icon class="is-loading"><Loading /></el-icon> 加载中...</div>
          <div v-if="!loadingSessions && filteredSessions.length === 0" class="list-status empty">
            {{ searchKeyword ? '未找到匹配记录' : '暂无问诊记录' }}
          </div>
          <div v-if="noMore && filteredSessions.length > 0" class="list-status">没有更多了</div>
        </div>
      </template>

      <!-- 收缩状态 -->
      <div v-else class="sidebar-collapsed-bar">
        <el-tooltip content="展开侧栏" placement="right">
          <el-icon class="expand-btn" @click="sidebarCollapsed = false"><DArrowRight /></el-icon>
        </el-tooltip>
        <el-tooltip content="新对话" placement="right">
          <el-icon class="expand-btn" @click="startNewChat"><Plus /></el-icon>
        </el-tooltip>
      </div>
    </aside>

    <!-- 右侧：聊天区域 -->
    <section class="chat-view">
      <!-- 头部 -->
      <div class="chat-header">
        <template v-if="currentSession">
          <h3>{{ currentSession.title || 'AI 智能问诊' }}</h3>
          <div class="header-meta">
            <el-tag v-if="currentSession.recommendedLabel" size="small" effect="plain">{{ currentSession.recommendedLabel }}</el-tag>
            <el-tag v-if="currentSession.status === '1'" type="info" size="small">已结束</el-tag>
            <el-tag v-if="currentSession.satisfaction" type="warning" size="small" effect="plain">
              {{ '★'.repeat(currentSession.satisfaction) }}
            </el-tag>
          </div>
        </template>
        <template v-else>
          <h3>AI 智能问诊</h3>
        </template>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesRef" v-loading="loadingMessages">
        <!-- 欢迎页 -->
        <div v-if="!currentSessionId && !loadingMessages" class="welcome-area">
          <div class="welcome-icon">🩺</div>
          <h2>AI 智能全科预诊</h2>
          <p>描述您的症状，AI 助手将结合医学知识库为您提供专业建议</p>
          <div class="quick-questions">
            <div v-for="q in quickQuestions" :key="q" class="quick-card" @click="inputMessage = q">
              <span class="quick-icon">💬</span>{{ q }}
            </div>
          </div>
        </div>

        <!-- 消息气泡 -->
        <template v-for="(msg, index) in messages" :key="index">
          <div :class="['bubble-row', msg.role]">
            <div v-if="msg.role === 'assistant'" class="avatar-ai">AI</div>
            <div :class="['bubble', msg.role]">
              <div v-if="msg.role === 'assistant'" class="bubble-content" v-html="renderMarkdown(msg.content)"></div>
              <div v-else class="bubble-content">{{ msg.content }}</div>
            </div>
          </div>
        </template>

        <!-- 流式输入中 -->
        <div v-if="isStreaming" class="bubble-row assistant">
          <div class="avatar-ai">AI</div>
          <div class="bubble assistant">
            <div class="typing-indicator"><span></span><span></span><span></span></div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-toolbar">
          <el-tooltip :content="useKnowledgeBase ? '知识库增强：开启' : '知识库增强：关闭'" placement="top">
            <div :class="['kb-toggle', { active: useKnowledgeBase }]" @click="useKnowledgeBase = !useKnowledgeBase">
              <el-icon><Collection /></el-icon>
              <span>知识库</span>
            </div>
          </el-tooltip>
        </div>
        <div class="input-row">
          <el-input
            ref="inputRef"
            v-model="inputMessage"
            type="textarea"
            :rows="1"
            :autosize="{ minRows: 1, maxRows: 4 }"
            :placeholder="useKnowledgeBase ? '描述您的症状，将结合知识库为您分析...' : '描述您的症状...'"
            @keydown.enter.ctrl.exact.prevent="sendMessage"
            @keydown.enter.meta.exact.prevent="sendMessage"
            resize="none"
          />
          <el-button
            type="primary"
            :icon="Promotion"
            :disabled="!inputMessage.trim() || isStreaming"
            :loading="isStreaming"
            @click="sendMessage"
            class="send-btn"
          />
        </div>
        <div class="input-hint">Enter 换行 · Ctrl+Enter 发送</div>
      </div>
    </section>

    <!-- 评价对话框 -->
    <el-dialog v-model="rateDialogVisible" title="评价问诊" width="400px" append-to-body>
      <div class="rate-content">
        <p>请为本次问诊服务评分：</p>
        <el-rate v-model="ratingScore" :max="5" size="large" />
      </div>
      <template #footer>
        <el-button @click="rateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRating">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MoreFilled, Search, Loading, Promotion, DArrowLeft, DArrowRight, Collection } from '@element-plus/icons-vue'
import { chatStream, getMyHistory, getSessionDetail, deleteMySession, rateSession, updateConsultationTitle, updatePinStatus } from '@/api/biz/consultation'
import MarkdownIt from 'markdown-it'

defineOptions({ name: 'PatientConsultation' })

const md = new MarkdownIt({ breaks: true, linkify: true, html: true })

// --- state ---
const sessions = ref<any[]>([])
const messages = ref<any[]>([])
const currentSession = ref<any>(null)
const currentSessionId = ref('')
const inputMessage = ref('')
const isStreaming = ref(false)
const loadingSessions = ref(false)
const loadingMessages = ref(false)
const messagesRef = ref<HTMLElement | null>(null)
const sessionListRef = ref<HTMLElement | null>(null)
const inputRef = ref()
const rateDialogVisible = ref(false)
const ratingScore = ref(5)
const ratingSessionId = ref('')
const sidebarCollapsed = ref(false)
const useKnowledgeBase = ref(true)

// --- search ---
const searchKeyword = ref('')
const filteredSessions = computed(() => {
  const kw = searchKeyword.value.trim().toLowerCase()
  if (!kw) return sessions.value
  return sessions.value.filter(s =>
    (s.title || '').toLowerCase().includes(kw) ||
    (s.symptomSummary || '').toLowerCase().includes(kw) ||
    (s.recommendedLabel || '').toLowerCase().includes(kw)
  )
})

// --- pagination ---
const pageNum = ref(1)
const pageSize = 10
const noMore = ref(false)
const loadingMore = ref(false)

async function loadSessions(reset = false) {
  if (reset) { pageNum.value = 1; noMore.value = false; sessions.value = [] }
  if (loadingSessions.value || loadingMore.value) return
  pageNum.value === 1 ? (loadingSessions.value = true) : (loadingMore.value = true)
  try {
    const res = await getMyHistory({ pageNum: pageNum.value, pageSize })
    const list = res.rows || []
    sessions.value = reset ? list : [...sessions.value, ...list]
    sessions.value.sort((a: any, b: any) => {
      if (a.isPinned !== b.isPinned) return b.isPinned - a.isPinned
      return new Date(b.lastMessageTime || 0).getTime() - new Date(a.lastMessageTime || 0).getTime()
    })
    list.length < pageSize ? (noMore.value = true) : pageNum.value++
  } catch (e) { console.error(e) }
  finally { loadingSessions.value = false; loadingMore.value = false }
}

function onSessionScroll() {
  const el = sessionListRef.value
  if (!el || noMore.value || loadingMore.value) return
  if (el.scrollTop + el.clientHeight >= el.scrollHeight - 60) loadSessions()
}

// --- session actions ---
async function loadSession(session: any) {
  if (currentSessionId.value === session.sessionId) return
  currentSessionId.value = session.sessionId
  currentSession.value = session
  loadingMessages.value = true
  messages.value = []
  try {
    const res = await getSessionDetail(session.sessionId)
    messages.value = res.data?.messages || []
    scrollToBottom()
  } catch { ElMessage.error('加载会话失败') }
  finally { loadingMessages.value = false }
}

function startNewChat() {
  currentSessionId.value = ''
  currentSession.value = null
  messages.value = []
  nextTick(() => inputRef.value?.focus())
}

function handleSessionCommand(cmd: string, session: any) {
  const actions: Record<string, () => void> = {
    pin: () => togglePin(session),
    rename: () => renameSession(session),
    rate: () => { ratingSessionId.value = session.sessionId; ratingScore.value = 5; rateDialogVisible.value = true },
    delete: () => removeSession(session)
  }
  actions[cmd]?.()
}

async function togglePin(session: any) {
  try {
    await updatePinStatus(session.sessionId, session.isPinned ? 0 : 1)
    ElMessage.success(session.isPinned ? '已取消置顶' : '已置顶')
    loadSessions(true)
  } catch { ElMessage.error('操作失败') }
}

async function renameSession(session: any) {
  try {
    const { value } = await ElMessageBox.prompt('请输入新名称', '重命名', { inputValue: session.title || '' })
    if (value !== null) {
      await updateConsultationTitle(session.sessionId, value)
      session.title = value
      ElMessage.success('重命名成功')
    }
  } catch {}
}

async function removeSession(session: any) {
  try {
    await ElMessageBox.confirm('确定删除该问诊记录吗？', '提示', { type: 'warning' })
    await deleteMySession(session.sessionId)
    if (currentSessionId.value === session.sessionId) startNewChat()
    loadSessions(true)
    ElMessage.success('已删除')
  } catch {}
}

async function submitRating() {
  try {
    await rateSession(ratingSessionId.value, ratingScore.value)
    rateDialogVisible.value = false
    ElMessage.success('评价成功')
    loadSessions(true)
  } catch { ElMessage.error('评价失败') }
}

// --- chat ---
const quickQuestions = [
  '我最近经常头痛，怎么办？',
  '血压偏高需要注意什么？',
  '咳嗽一周了还没好',
  '帮我解读一下检验报告'
]

function renderMarkdown(content: string) { return content ? md.render(content) : '' }

function formatTime(time: string | Date | undefined) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  if (d.toDateString() === now.toDateString()) return `${pad(d.getHours())}:${pad(d.getMinutes())}`
  return `${d.getMonth() + 1}/${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function scrollToBottom() {
  nextTick(() => { if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight })
}

async function sendMessage() {
  const content = inputMessage.value.trim()
  if (!content || isStreaming.value) return
  messages.value.push({ role: 'user', content })
  inputMessage.value = ''
  scrollToBottom()
  isStreaming.value = true
  const aiIdx = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })
  try {
    const res = await chatStream({ sessionId: currentSessionId.value, content, useKnowledgeBase: useKnowledgeBase.value })
    if (!res.ok || !res.body) throw new Error(`HTTP ${res.status}`)
    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let acc = '', newSid = '', lineBuf = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      lineBuf += decoder.decode(value, { stream: true })
      const lines = lineBuf.split('\n')
      lineBuf = lines.pop() || ''  // 最后一个可能不完整，留到下次
      for (const line of lines) {
        const t = line.trim()
        if (!t || t.startsWith(':')) continue
        if (t.startsWith('event:')) continue  // 忽略事件名行
        if (t.startsWith('data:')) {
          const d = t.substring(5).trim()
          if (d === '[DONE]') continue
          // 尝试解析 JSON（sessionId 等元数据）
          try {
            const p = JSON.parse(d)
            if (p.sessionId) { newSid = p.sessionId; continue }
            // 其他 JSON 对象也跳过，不拼接到正文
            continue
          } catch {}
          // 纯文本 chunk，直接累加
          acc += d
          messages.value[aiIdx].content = acc
          scrollToBottom()
        }
      }
    }
    if (!currentSessionId.value && newSid) currentSessionId.value = newSid
    loadSessions(true)
  } catch (e) {
    console.error(e)
    messages.value[aiIdx].content = '抱歉，发生了错误，请稍后重试。'
  } finally { isStreaming.value = false; scrollToBottom() }
}

onMounted(() => { loadSessions() })
</script>

<style scoped lang="scss">
// ===== 整体布局：左右两栏，铺满视口 =====
.patient-consultation {
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  background: var(--patient-bg);
  z-index: 1;
}

// ===== 左侧会话栏 =====
.session-sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--patient-border);
  background: var(--patient-bg);
  transition: width 0.25s ease;

  &.collapsed {
    width: 56px;
    align-items: center;
  }
}

.sidebar-collapsed-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 16px;
  gap: 8px;
  width: 100%;
}

.expand-btn {
  font-size: 20px;
  cursor: pointer;
  padding: 10px;
  border-radius: 8px;
  color: var(--patient-text-secondary);
  transition: all 0.2s;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  &:hover {
    background: var(--el-color-primary-light-9);
    color: var(--el-color-primary);
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.collapse-btn {
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  color: var(--patient-text-secondary);
  transition: all 0.2s;
  &:hover {
    background: var(--el-fill-color-light);
    color: var(--el-color-primary);
  }
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 0;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--patient-text);
}

.sidebar-search {
  padding: 12px 12px 8px;
  flex-shrink: 0;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 8px;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: var(--patient-border); border-radius: 2px; }
  &::-webkit-scrollbar-track { background: transparent; }
}

.session-item {
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 2px;
  transition: background 0.15s;

  &:hover { background: var(--patient-card-bg); }
  &.active { background: var(--el-color-primary-light-9); }

  .session-actions { opacity: 0; }
  &:hover .session-actions { opacity: 1; }
}

.session-title-row {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 2px;
}

.pin-icon { font-size: 12px; flex-shrink: 0; }

.session-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--patient-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.session-preview {
  font-size: 12px;
  color: var(--patient-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
  padding-left: 16px;
}

.session-footer {
  display: flex;
  align-items: center;
  gap: 6px;
  padding-left: 16px;
}

.session-tag {
  font-size: 11px;
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  padding: 0 5px;
  border-radius: 3px;
  line-height: 16px;
  flex-shrink: 0;
}

.session-count {
  font-size: 11px;
  color: var(--patient-text-secondary);
  flex-shrink: 0;
}

.session-time {
  font-size: 11px;
  color: var(--patient-text-secondary);
  margin-left: auto;
}

.session-actions { flex-shrink: 0; }

.more-btn {
  padding: 2px;
  border-radius: 4px;
  cursor: pointer;
  color: var(--patient-text-secondary);
  font-size: 14px;
  &:hover { background: var(--el-fill-color-light); }
}

.list-status {
  text-align: center;
  padding: 12px;
  font-size: 12px;
  color: var(--patient-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  &.empty { padding: 40px 16px; }
}

// ===== 右侧聊天区 =====
.chat-view {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 14px 24px;
  border-bottom: 1px solid var(--patient-border);
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  background: var(--patient-card-bg);
  min-height: 48px;

  h3 { margin: 0; font-size: 15px; font-weight: 600; color: var(--patient-text); }
}

.header-meta { display: flex; gap: 6px; margin-left: 8px; }

// 消息区：flex:1 + overflow-y:auto 是唯一滚动区域
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--patient-chat-bg);

  // 隐藏滚动条但保留滚动能力
  &::-webkit-scrollbar { width: 0; height: 0; }
  scrollbar-width: none;
}

// 欢迎页
.welcome-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.welcome-icon { font-size: 48px; margin-bottom: 12px; }

.welcome-area h2 {
  color: var(--patient-text);
  font-size: 22px;
  margin: 0 0 8px;
}

.welcome-area p {
  color: var(--patient-text-secondary);
  font-size: 14px;
  margin: 0 0 28px;
}

.quick-questions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  max-width: 480px;
  width: 100%;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  background: var(--patient-card-bg);
  border: 1px solid var(--patient-border);
  border-radius: 10px;
  cursor: pointer;
  font-size: 13px;
  color: var(--patient-text);
  text-align: left;
  transition: all 0.15s;

  &:hover {
    border-color: var(--el-color-primary);
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }
}

.quick-icon { font-size: 16px; flex-shrink: 0; }

// 消息气泡
.bubble-row {
  display: flex;
  gap: 10px;
  max-width: 80%;

  &.user {
    align-self: flex-end;
    flex-direction: row-reverse;
  }

  &.assistant {
    align-self: flex-start;
  }
}

.avatar-ai {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--el-color-primary-light-7);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  margin-top: 2px;
}

.bubble {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;

  &.assistant {
    background: var(--patient-bubble-ai-bg);
    color: var(--patient-text);
    border-bottom-left-radius: 4px;
  }

  &.user {
    background: var(--el-color-primary);
    color: #fff;
    border-bottom-right-radius: 4px;
  }
}

.bubble-content {
  :deep(p) { margin: 4px 0; }
  :deep(ul), :deep(ol) { padding-left: 20px; margin: 4px 0; }
  :deep(code) { background: rgba(0,0,0,0.06); padding: 2px 5px; border-radius: 3px; font-size: 0.9em; }
  :deep(pre) { background: rgba(0,0,0,0.06); padding: 10px; border-radius: 6px; overflow-x: auto; code { background: none; padding: 0; } }
  :deep(strong) { font-weight: 600; }
  :deep(table) { border-collapse: collapse; margin: 8px 0; th, td { border: 1px solid var(--patient-border); padding: 6px 10px; } th { background: var(--patient-bg); } }
  :deep(h1), :deep(h2), :deep(h3), :deep(h4) { margin: 8px 0 4px; }
}

.user .bubble-content {
  :deep(code) { background: rgba(255,255,255,0.2); }
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 2px 0;

  span {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: var(--el-text-color-placeholder);
    animation: typing 1.4s infinite ease-in-out both;
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes typing {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

// ===== 输入区域 =====
.chat-input-area {
  padding: 10px 24px 12px;
  border-top: 1px solid var(--patient-border);
  background: var(--patient-card-bg);
  flex-shrink: 0;
}

.input-toolbar {
  margin-bottom: 8px;
}

.kb-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  color: var(--patient-text-secondary);
  background: var(--patient-bg);
  border: 1px solid var(--patient-border);
  transition: all 0.2s;
  user-select: none;

  &:hover {
    border-color: var(--el-color-primary-light-5);
    color: var(--el-color-primary);
  }

  &.active {
    background: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary-light-5);
    color: var(--el-color-primary);
    font-weight: 500;
  }
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;

  :deep(.el-textarea__inner) {
    border-radius: 10px;
    padding: 8px 14px;
    line-height: 1.5;
    resize: none;
    box-shadow: none !important;
  }
}

.send-btn {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  padding: 0;
}

.input-hint {
  font-size: 11px;
  color: var(--patient-text-secondary);
  margin-top: 6px;
  opacity: 0.7;
}

.rate-content {
  text-align: center;
  padding: 16px 0;
}

@media (max-width: 768px) {
  .session-sidebar { display: none; }
}
</style>
