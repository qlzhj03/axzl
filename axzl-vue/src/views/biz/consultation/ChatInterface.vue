<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3>智能问诊助手</h3>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role]"
      >
        <div class="message-content">
          <div class="role-label">{{ msg.role === 'user' ? '用户' : 'AI助手' }}</div>
          <div v-if="msg.role === 'assistant'" class="text-content bubble-md" v-html="renderMd(msg.content)"></div>
          <div v-else class="text-content">{{ msg.content }}</div>
          <span v-if="isStreaming && index === messages.length - 1" class="stream-cursor"></span>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="请描述您的症状..."
        @keyup.enter.ctrl="sendMessage"
      />
      <el-button 
        type="primary" 
        @click="sendMessage" 
        :loading="isStreaming"
        :disabled="!inputMessage.trim()"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { chatStream } from '@/api/biz/consultation'
import MarkdownIt from 'markdown-it'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const md = new MarkdownIt({
  breaks: true,
  linkify: true,
  html: false,
  highlight(str: string, lang: string) {
    const escaped = md.utils.escapeHtml(str)
    const langLabel = lang ? `<span class="code-lang">${lang}</span>` : ''
    return `<pre class="code-block">${langLabel}<code class="language-${lang || 'text'}">${escaped}</code></pre>`
  }
})

function renderMd(content: string) {
  if (!content) return ''
  return md.render(content)
}

const messages = ref<Message[]>([])
const inputMessage = ref('')
const isStreaming = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

async function sendMessage() {
  if (!inputMessage.value.trim() || isStreaming.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''

  messages.value.push({ role: 'user', content: userMessage })
  scrollToBottom()

  isStreaming.value = true

  const aiIdx = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })

  let acc = ''

  chatStream({
    sessionId: '',
    content: userMessage
  }, {
    onMessage(text: string) {
      acc += text
      messages.value[aiIdx].content = acc
      scrollToBottom()
    },
    onDone() {
      isStreaming.value = false
      scrollToBottom()
    },
    onError(err: Error) {
      console.error('SSE error:', err)
      if (!messages.value[aiIdx].content) {
        messages.value[aiIdx].content = '抱歉，发生了错误，请稍后重试。'
      }
      isStreaming.value = false
      scrollToBottom()
    }
  })
}
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  flex-direction: column;
  height: 600px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  
  h3 {
    margin: 0;
    color: #303133;
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #fafafa;

  .message {
    margin-bottom: 20px;
    display: flex;

    &.user {
      justify-content: flex-end;

      .message-content {
        background-color: #ecf5ff;
        border-radius: 18px 18px 4px 18px;
        max-width: 70%;

        .role-label {
          color: #409eff;
        }
      }
    }

    &.assistant {
      justify-content: flex-start;

      .message-content {
        background-color: #ffffff;
        border-radius: 18px 18px 18px 4px;
        max-width: 85%;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

        .role-label {
          color: #67c23a;
        }
      }
    }

    .message-content {
      padding: 12px 16px;

      .role-label {
        font-size: 12px;
        margin-bottom: 5px;
        font-weight: bold;
      }

      .text-content {
        line-height: 1.6;
        word-break: break-word;
      }
    }
  }
}

.stream-cursor {
  display: inline-block;
  width: 2px;
  height: 16px;
  background: var(--el-color-primary, #409eff);
  animation: blink 1s step-end infinite;
  vertical-align: text-bottom;
  margin-left: 2px;
}

@keyframes blink {
  50% { opacity: 0; }
}

.bubble-md {
  :deep(h1), :deep(h2), :deep(h3) { margin: 0.5em 0 0.3em; }
  :deep(p) { margin: 0.3em 0; }
  :deep(ul), :deep(ol) { padding-left: 1.2em; margin: 0.3em 0; }
  :deep(li) { margin: 0.15em 0; }
  :deep(code) {
    background: #f0f0f0;
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 0.9em;
  }
  :deep(pre) {
    background: #282c34;
    color: #abb2bf;
    padding: 12px;
    border-radius: 6px;
    overflow-x: auto;
    margin: 0.5em 0;
  }
  :deep(blockquote) {
    border-left: 3px solid #dfe2e5;
    padding-left: 10px;
    margin: 0.5em 0;
    color: #6a737d;
  }
  :deep(table) {
    border-collapse: collapse;
    margin: 0.5em 0;
    th, td { border: 1px solid #dfe2e5; padding: 6px 10px; }
    th { background: #f6f8fa; }
  }
  :deep(strong) { font-weight: 600; }
}

.chat-input {
  padding: 15px 20px;
  background-color: #ffffff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 10px;
  
  :deep(.el-textarea) {
    flex: 1;
  }
  
  .el-button {
    align-self: flex-end;
  }
}
</style>
