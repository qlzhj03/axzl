<template>
  <div class="patient-consultation">
    <!-- 左侧：会话历史 -->
    <aside :class="['session-sidebar', { collapsed: sidebarCollapsed }]">
      <template v-if="!sidebarCollapsed">
        <!-- 顶部操作区 -->
        <div class="sb-top">
          <button class="new-chat-btn" @click="startNewChat">
            <el-icon :size="18"><EditPen /></el-icon>
            <span>新建问诊</span>
          </button>
          <button class="icon-btn" @click="sidebarCollapsed = true" title="收起侧栏">
            <el-icon :size="16"><Fold /></el-icon>
          </button>
        </div>

        <!-- 搜索 -->
        <div class="sb-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索问诊记录..."
            clearable
            :prefix-icon="Search"
          />
        </div>

        <!-- 会话列表 -->
        <div class="sb-list" ref="sessionListRef" @scroll="onSessionScroll">
          <div
            v-for="session in filteredSessions"
            :key="session.sessionId"
            :class="['sb-item', { active: currentSessionId === session.sessionId }]"
            @click="loadSession(session)"
          >
            <div class="sb-item-icon">
              <el-icon :size="16"><ChatDotRound /></el-icon>
            </div>
            <div class="sb-item-body">
              <div class="sb-item-top">
                <span class="sb-item-title">{{ session.title || '新对话' }}</span>
                <el-icon v-if="session.isPinned" :size="12" class="pin-icon"><Top /></el-icon>
              </div>
              <div class="sb-item-desc" v-if="session.symptomSummary">{{ session.symptomSummary }}</div>
              <div class="sb-item-foot">
                <span v-if="session.recommendedLabel" class="dept-tag">{{ session.recommendedLabel }}</span>
                <span class="sb-item-time">{{ formatTime(session.lastMessageTime) }}</span>
              </div>
            </div>
            <div class="sb-item-actions" @click.stop>
              <el-dropdown trigger="click" @command="(cmd: string) => handleSessionCommand(cmd, session)" size="small">
                <button class="more-btn"><el-icon :size="14"><MoreFilled /></el-icon></button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="pin">
                      <el-icon><Top /></el-icon>{{ session.isPinned ? '取消置顶' : '置顶' }}
                    </el-dropdown-item>
                    <el-dropdown-item command="rename">
                      <el-icon><EditPen /></el-icon>重命名
                    </el-dropdown-item>
                    <el-dropdown-item command="rate" v-if="!session.satisfaction">
                      <el-icon><Star /></el-icon>评价
                    </el-dropdown-item>
                    <el-dropdown-item command="delete" divided>
                      <el-icon><Delete /></el-icon>删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 状态提示 -->
          <div v-if="loadingMore" class="sb-status"><el-icon class="is-loading"><Loading /></el-icon> 加载中...</div>
          <div v-if="!loadingSessions && filteredSessions.length === 0" class="sb-empty">
            <el-icon :size="40"><ChatLineSquare /></el-icon>
            <p>{{ searchKeyword ? '未找到匹配记录' : '暂无问诊记录' }}</p>
            <span>点击上方按钮开始问诊</span>
          </div>
          <div v-if="noMore && filteredSessions.length > 0" class="sb-status">— 没有更多了 —</div>
        </div>
      </template>

      <!-- 收缩状态 -->
      <div v-else class="sb-collapsed">
        <button class="icon-btn active" @click="sidebarCollapsed = false" title="展开侧栏">
          <el-icon :size="18"><Expand /></el-icon>
        </button>
        <el-tooltip content="新建问诊" placement="right">
          <button class="icon-btn" @click="startNewChat">
            <el-icon :size="18"><EditPen /></el-icon>
          </button>
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
        <div v-if="!currentSessionId && !loadingMessages && messages.length === 0" class="welcome-area">
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
        <div v-for="(msg, index) in messages" :key="index" :class="['bubble-row', msg.role]">
          <div v-if="msg.role === 'assistant'" class="avatar-ai">AI</div>
          <div :class="['bubble', msg.role]">
            <!-- AI 消息：渲染 markdown -->
            <div v-if="msg.role === 'assistant'" class="bubble-md" v-html="renderMd(msg.content)"></div>
            <!-- 用户消息：纯文本 -->
            <div v-else class="bubble-content">{{ msg.content }}</div>
            <!-- 流式光标 -->
            <span v-if="isStreaming && index === messages.length - 1" class="stream-cursor"></span>
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
import {
  Plus, MoreFilled, Search, Loading, Promotion, Collection,
  EditPen, Fold, Expand, ChatDotRound, ChatLineSquare, Top,
  Star, Delete
} from '@element-plus/icons-vue'
import { chatStream, getMyHistory, getSessionDetail, deleteMySession, rateSession, updateConsultationTitle, updatePinStatus } from '@/api/biz/consultation'
import MarkdownIt from 'markdown-it'

defineOptions({ name: 'PatientConsultation' })

// --- markdown-it 实例，禁用 raw HTML 避免 XSS ---
const md = new MarkdownIt({
  breaks: true,
  linkify: true,
  html: false,
  highlight(str: string, lang: string) {
    // 代码高亮：添加语言标签 + escaped 内容
    const escaped = md.utils.escapeHtml(str)
    const langLabel = lang ? `<span class="code-lang">${lang}</span>` : ''
    return `<pre class="code-block">${langLabel}<code class="language-${lang || 'text'}">${escaped}</code></pre>`
  }
})

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

function renderMd(content: string) {
  if (!content) return ''
  return md.render(content)
}

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

// --- SSE 流式发送 ---
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
    const res = await chatStream({
      sessionId: currentSessionId.value,
      content,
      useKnowledgeBase: useKnowledgeBase.value
    })

    if (!res.ok || !res.body) throw new Error(`HTTP ${res.status}`)

    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let acc = ''          // 累积的完整文本
    let newSid = ''       // 新会话ID
    let buf = ''          // 未处理完的行缓冲

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buf += decoder.decode(value, { stream: true })
      const lines = buf.split('\n')
      buf = lines.pop() || '' // 最后一行可能不完整

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || trimmed.startsWith(':')) continue // 空行或注释

        // 处理命名事件: event:end / event:error
        if (trimmed.startsWith('event:')) {
          const eventName = trimmed.substring(6).trim()
          if (eventName === 'end' || eventName === 'error') {
            // 下一行 data: 会携带具体内容，但在循环中会被跳过
            continue
          }
          continue
        }

        // 处理 data: 行
        if (trimmed.startsWith('data:')) {
          const payload = trimmed.substring(5).trim()

          // 结束标记
          if (payload === '[DONE]') continue

          // 尝试解析 JSON（sessionId 等元数据）
          try {
            const obj = JSON.parse(payload)
            if (obj.sessionId) {
              newSid = obj.sessionId
              continue
            }
            // error 事件的 JSON
            if (obj.error || obj.message) continue
            continue
          } catch {
            // 非 JSON = 纯文本 chunk，累加
          }

          acc += payload
          messages.value[aiIdx].content = acc
          scrollToBottom()
        }
      }
    }

    // 处理缓冲区中剩余内容
    if (buf.trim()) {
      const leftover = buf.trim()
      if (leftover.startsWith('data:')) {
        const payload = leftover.substring(5).trim()
        if (payload && payload !== '[DONE]') {
          try { JSON.parse(payload) } catch {
            acc += payload
            messages.value[aiIdx].content = acc
          }
        }
      }
    }

    // 绑定新会话ID
    if (!currentSessionId.value && newSid) {
      currentSessionId.value = newSid
    }

    // 刷新会话列表
    loadSessions(true)
  } catch (e) {
    console.error('SSE error:', e)
    if (!messages.value[aiIdx].content) {
      messages.value[aiIdx].content = '抱歉，发生了错误，请稍后重试。'
    }
  } finally {
    isStreaming.value = false
    scrollToBottom()
  }
}

onMounted(() => { loadSessions() })
</script>

<style scoped lang="scss">
// ===== 整体布局 =====
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
  background: var(--patient-card-bg);
  border-right: 1px solid var(--patient-border);
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &.collapsed {
    width: 56px;
    align-items: center;
  }
}

// --- 顶部操作区 ---
.sb-top {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 14px 12px;
  flex-shrink: 0;
}

.new-chat-btn {
  flex: 1;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #2b7de9 0%, #66b1ff 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(43, 125, 233, 0.3);

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(43, 125, 233, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

.icon-btn {
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--patient-border);
  border-radius: 10px;
  background: var(--patient-bg);
  color: var(--patient-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover {
    border-color: var(--el-color-primary-light-5);
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }

  &.active {
    background: var(--el-color-primary-light-9);
    border-color: var(--el-color-primary-light-5);
    color: var(--el-color-primary);
  }
}

// --- 搜索 ---
.sb-search {
  padding: 0 14px 12px;
  flex-shrink: 0;

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    background: var(--patient-bg);
    box-shadow: none;
    border: 1px solid var(--patient-border);

    &:hover, &.is-focus {
      border-color: var(--el-color-primary-light-5);
    }
  }
}

// --- 列表 ---
.sb-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 12px;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: var(--el-border-color-lighter); border-radius: 4px; }
  &::-webkit-scrollbar-track { background: transparent; }
}

.sb-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 10px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 2px;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: var(--patient-bg);
  }

  &.active {
    background: var(--el-color-primary-light-9);

    .sb-item-icon {
      background: var(--el-color-primary);
      color: #fff;
    }
  }

  .sb-item-actions { opacity: 0; }
  &:hover .sb-item-actions { opacity: 1; }
}

.sb-item-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: var(--patient-bg);
  border: 1px solid var(--patient-border);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  flex-shrink: 0;
  margin-top: 1px;
  transition: all 0.2s;
}

.sb-item-body {
  flex: 1;
  min-width: 0;
}

.sb-item-top {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 3px;
}

.sb-item-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--patient-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.pin-icon {
  color: var(--el-color-warning);
  flex-shrink: 0;
}

.sb-item-desc {
  font-size: 12px;
  color: var(--patient-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 5px;
  line-height: 1.4;
}

.sb-item-foot {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dept-tag {
  font-size: 10px;
  font-weight: 500;
  color: var(--el-color-primary);
  background: rgba(64, 158, 255, 0.08);
  padding: 1px 6px;
  border-radius: 4px;
  line-height: 16px;
  flex-shrink: 0;
}

.sb-item-time {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
}

.sb-item-actions {
  position: absolute;
  top: 10px;
  right: 8px;
  flex-shrink: 0;
}

.more-btn {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--patient-text-secondary);
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    background: var(--el-fill-color-light);
    color: var(--patient-text);
  }
}

// --- 状态 ---
.sb-status {
  text-align: center;
  padding: 16px 12px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.sb-empty {
  text-align: center;
  padding: 48px 20px;
  color: var(--el-text-color-placeholder);

  .el-icon { margin-bottom: 12px; }

  p {
    font-size: 14px;
    font-weight: 500;
    color: var(--patient-text-secondary);
    margin: 0 0 4px;
  }

  span {
    font-size: 12px;
  }
}

// --- 收缩状态 ---
.sb-collapsed {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 16px;
  gap: 8px;
  width: 100%;
}

// ===== 右侧聊天区 =====
.chat-view {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--patient-border);
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  background: var(--patient-card-bg);
  min-height: 52px;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--patient-text);
  }
}

.header-meta { display: flex; gap: 6px; margin-left: 8px; }

// 消息区
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: var(--patient-chat-bg);

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

.welcome-icon {
  font-size: 56px;
  margin-bottom: 16px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.welcome-area h2 {
  color: var(--patient-text);
  font-size: 24px;
  margin: 0 0 8px;
  font-weight: 600;
}

.welcome-area p {
  color: var(--patient-text-secondary);
  font-size: 15px;
  margin: 0 0 32px;
}

.quick-questions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  max-width: 520px;
  width: 100%;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: var(--patient-card-bg);
  border: 1px solid var(--patient-border);
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  color: var(--patient-text);
  text-align: left;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    border-color: var(--el-color-primary);
    color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.15);
  }
}

.quick-icon { font-size: 18px; flex-shrink: 0; }

// 消息气泡
.bubble-row {
  display: flex;
  gap: 12px;
  max-width: 85%;
  animation: messageIn 0.3s ease-out;

  &.user {
    align-self: flex-end;
    flex-direction: row-reverse;
  }

  &.assistant {
    align-self: flex-start;
  }
}

@keyframes messageIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.avatar-ai {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2b7de9 0%, #66b1ff 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
  margin-top: 2px;
}

.bubble {
  padding: 14px 18px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
  position: relative;

  &.assistant {
    background: var(--patient-card-bg);
    color: var(--patient-text);
    border-bottom-left-radius: 4px;
    border: 1px solid var(--patient-border);
    max-width: 100%;
  }

  &.user {
    background: linear-gradient(135deg, #2b7de9 0%, #66b1ff 100%);
    color: #fff;
    border-bottom-right-radius: 4px;
  }
}

.bubble-content {
  white-space: pre-wrap;
}

// ===== Markdown 渲染样式 =====
.bubble-md {
  :deep(*) {
    margin: 0;
    line-height: 1.7;
  }

  // 段落
  :deep(p) {
    margin: 6px 0;
    &:first-child { margin-top: 0; }
    &:last-child { margin-bottom: 0; }
  }

  // 标题
  :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
    margin: 16px 0 8px;
    font-weight: 600;
    color: var(--patient-text);
    &:first-child { margin-top: 0; }
  }
  :deep(h1) { font-size: 1.3em; }
  :deep(h2) { font-size: 1.2em; }
  :deep(h3) { font-size: 1.1em; }

  // 加粗 / 斜体
  :deep(strong) { font-weight: 600; color: var(--patient-text); }
  :deep(em) { font-style: italic; }

  // 列表
  :deep(ul), :deep(ol) {
    padding-left: 20px;
    margin: 6px 0;
  }
  :deep(li) {
    margin: 3px 0;
    &::marker { color: var(--el-color-primary); }
  }

  // 代码块
  :deep(.code-block) {
    position: relative;
    background: #1e1e2e;
    color: #cdd6f4;
    border-radius: 10px;
    padding: 14px 16px;
    margin: 10px 0;
    overflow-x: auto;
    font-size: 13px;
    line-height: 1.6;

    code {
      background: none;
      padding: 0;
      border-radius: 0;
      font-size: inherit;
      color: inherit;
    }
  }

  :deep(.code-lang) {
    position: absolute;
    top: 6px;
    right: 10px;
    font-size: 11px;
    color: #6c7086;
    text-transform: uppercase;
    font-weight: 500;
    pointer-events: none;
  }

  // 行内代码
  :deep(code):not(.code-block code) {
    background: rgba(64, 158, 255, 0.08);
    color: var(--el-color-primary);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 0.9em;
    font-family: 'SF Mono', 'Fira Code', 'Cascadia Code', Consolas, monospace;
  }

  // 引用
  :deep(blockquote) {
    margin: 10px 0;
    padding: 10px 16px;
    border-left: 3px solid var(--el-color-primary);
    background: rgba(64, 158, 255, 0.04);
    border-radius: 0 8px 8px 0;
    color: var(--patient-text-secondary);

    p { margin: 4px 0; }
  }

  // 表格
  :deep(table) {
    border-collapse: collapse;
    margin: 10px 0;
    width: 100%;
    font-size: 13px;
  }
  :deep(th), :deep(td) {
    border: 1px solid var(--patient-border);
    padding: 8px 12px;
    text-align: left;
  }
  :deep(th) {
    background: var(--patient-bg);
    font-weight: 600;
    color: var(--patient-text);
  }
  :deep(tr:nth-child(even)) {
    background: rgba(0, 0, 0, 0.02);
  }

  // 水平线
  :deep(hr) {
    border: none;
    border-top: 1px solid var(--patient-border);
    margin: 16px 0;
  }

  // 链接
  :deep(a) {
    color: var(--el-color-primary);
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }

  // 图片
  :deep(img) {
    max-width: 100%;
    border-radius: 8px;
    margin: 8px 0;
  }
}

// 用户气泡中的 markdown 样式覆盖
.user .bubble-content {
  white-space: pre-wrap;
}

// 流式光标
.stream-cursor {
  display: inline-block;
  width: 2px;
  height: 16px;
  background: var(--el-color-primary);
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

// ===== 输入区域 =====
.chat-input-area {
  padding: 16px 24px 20px;
  border-top: 1px solid var(--patient-border);
  background: var(--patient-card-bg);
  flex-shrink: 0;
}

.input-toolbar {
  margin-bottom: 10px;
}

.kb-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  color: var(--patient-text-secondary);
  background: var(--patient-bg);
  border: 1px solid var(--patient-border);
  transition: all 0.25s;
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
  gap: 12px;

  :deep(.el-textarea__inner) {
    border-radius: 12px;
    padding: 10px 16px;
    line-height: 1.5;
    resize: none;
    box-shadow: none !important;
    font-size: 14px;
  }
}

.send-btn {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  padding: 0;
  background: linear-gradient(135deg, #2b7de9 0%, #66b1ff 100%);
  border: none;

  &:hover {
    transform: scale(1.05);
  }
}

.input-hint {
  font-size: 12px;
  color: var(--patient-text-secondary);
  margin-top: 8px;
  opacity: 0.6;
}

.rate-content {
  text-align: center;
  padding: 16px 0;
}

@media (max-width: 768px) {
  .session-sidebar { display: none; }
  .bubble-row { max-width: 92%; }
}
</style>
