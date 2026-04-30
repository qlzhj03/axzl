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
          <div class="text-content" v-html="formatMessage(msg.content)"></div>
        </div>
      </div>
      
      <!-- 正在输入的提示 -->
      <div v-if="isStreaming" class="message assistant streaming">
        <div class="message-content">
          <div class="role-label">AI助手</div>
          <div class="text-content typing-indicator">
            <span></span><span></span><span></span>
          </div>
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

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const messages = ref<Message[]>([])
const inputMessage = ref('')
const isStreaming = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

// 格式化消息内容（支持简单的Markdown）
function formatMessage(content: string) {
  if (!content) return ''
  
  // 简单的Markdown格式化处理
  let formatted = content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // 粗体
    .replace(/\*(.*?)\*/g, '<em>$1</em>') // 斜体
    .replace(/`(.*?)`/g, '<code>$1</code>') // 代码
    .replace(/\n/g, '<br>') // 换行
  
  return formatted
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 发送消息
async function sendMessage() {
  if (!inputMessage.value.trim() || isStreaming.value) return
  
  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage
  })
  
  scrollToBottom()
  
  // 开始流式响应
  isStreaming.value = true
  
  try {
    const response = await chatStream({
      sessionId: '', // 空字符串表示创建新会话
      content: userMessage
    })
    
    if (!response.body) {
      throw new Error('Response body is null')
    }
    
    // 创建AI消息占位
    const aiMessageIndex = messages.value.length
    messages.value.push({
      role: 'assistant',
      content: ''
    })
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let accumulatedText = ''
    
    while (true) {
      const { done, value } = await reader.read()
      
      if (done) {
        break
      }
      
      const chunk = decoder.decode(value, { stream: true })
      
      // 解析SSE格式的数据
      const lines = chunk.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (data === '[DONE]') {
            continue
          }
          
          // 将接收到的文本添加到累积文本中
          accumulatedText += data
          
          // 更新AI消息内容（实现打字机效果）
          messages.value[aiMessageIndex].content = accumulatedText
          scrollToBottom()
        }
      }
    }
  } catch (error) {
    console.error('Error in stream chat:', error)
    messages.value.push({
      role: 'assistant',
      content: '抱歉，发生了错误，请稍后重试。'
    })
  } finally {
    isStreaming.value = false
    scrollToBottom()
  }
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
        max-width: 70%;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        
        .role-label {
          color: #67c23a;
        }
      }
    }
    
    &.streaming .text-content {
      min-height: 20px;
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

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  
  span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: #909399;
    animation: typing 1.4s infinite ease-in-out both;
    
    &:nth-child(1) {
      animation-delay: -0.32s;
    }
    
    &:nth-child(2) {
      animation-delay: -0.16s;
    }
  }
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
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
