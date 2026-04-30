<template>
  <div class="consultation-test-container">
    <div class="chat-header">
      <h3>问诊测试 - 流式响应演示</h3>
      <p class="subtitle">体验AI助手的实时打字机效果</p>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        :class="['message', msg.role]"
      >
        <div class="message-content">
          <div class="role-label">{{ msg.role === 'user' ? '用户' : 'AI助手' }}</div>
          <div class="text-content" v-html="renderMarkdown(msg.content, msg.role)"></div>
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
      <div class="quick-questions">
        <el-tag 
          v-for="(question, index) in quickQuestions" 
          :key="index"
          size="small"
          class="quick-question-tag"
          @click="selectQuickQuestion(question)"
        >
          {{ question }}
        </el-tag>
      </div>
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="3"
        placeholder="请描述您的症状，例如：我最近头痛、发烧..."
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
import { ref, nextTick, computed } from 'vue'
import { chatStream } from '@/api/biz/consultation'
import MarkdownIt from 'markdown-it'

// 创建 markdown-it 实例
const md = new MarkdownIt({
  breaks: true,  // 支持换行符转换为<br>
  linkify: true, // 自动识别链接
  html: true     // 允许HTML标签
})

const messages = ref<any[]>([])
const inputMessage = ref('')
const isStreaming = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

// 快速问题列表
const quickQuestions = ref([
  '我最近头痛、发烧',
  '胃痛怎么办？',
  '咳嗽一周了',
  '血压偏高需要注意什么？'
])

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 选择快速问题
function selectQuickQuestion(question: string) {
  inputMessage.value = question
}

// 渲染Markdown
function renderMarkdown(content: string, role: string) {
  if (!content) return ''
  // 只对AI消息进行Markdown渲染
  if (role === 'assistant') {
    return md.render(content)
  }
  // 用户消息直接返回纯文本
  return content
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
    
    // 检查HTTP响应状态
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status} - ${response.statusText}`)
    }
    
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
      
      // 解码接收到的数据
      const chunk = decoder.decode(value, { stream: true })
      
      // 按行分割SSE协议数据
      const lines = chunk.split('\n')
      
      for (const line of lines) {
        const trimmedLine = line.trim()
        
        // 跳过空行和注释
        if (!trimmedLine || trimmedLine.startsWith(':')) {
          continue
        }
        
        // 处理data行
        if (trimmedLine.startsWith('data:')) {
          // 提取data后面的内容（保留原始格式）
          const data = trimmedLine.substring(5)
          
          // 如果是结束标记，跳过
          if (data.trim() === '[DONE]') {
            continue
          }
          
          // 直接拼接原始数据，不做任何修改
          accumulatedText += data
          
          // 实时更新显示
          messages.value[aiMessageIndex].content = accumulatedText
          scrollToBottom()
        }
      }
    }
  } catch (error) {
    console.error('Error in stream chat:', error)
    const errorMessage = error instanceof Error ? error.message : '未知错误'
    messages.value.push({
      role: 'assistant',
      content: `抱歉，发生了错误：${errorMessage}。请稍后重试。`
    })
  } finally {
    isStreaming.value = false
    scrollToBottom()
  }
}
</script>

<style scoped lang="scss">
.consultation-test-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f5f7fa;
}

.chat-header {
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  
  h3 {
    margin: 0 0 8px 0;
    font-size: 20px;
    font-weight: 600;
  }
  
  .subtitle {
    margin: 0;
    font-size: 14px;
    opacity: 0.9;
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
        
        // Markdown样式
        :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
          margin-top: 16px;
          margin-bottom: 8px;
          font-weight: 600;
        }
        
        :deep(p) {
          margin: 8px 0;
        }
        
        :deep(ul), :deep(ol) {
          margin: 8px 0;
          padding-left: 20px;
        }
        
        :deep(li) {
          margin: 4px 0;
        }
        
        :deep(code) {
          background-color: #f0f0f0;
          padding: 2px 6px;
          border-radius: 3px;
          font-family: 'Courier New', monospace;
          font-size: 0.9em;
        }
        
        :deep(pre) {
          background-color: #f0f0f0;
          padding: 12px;
          border-radius: 6px;
          overflow-x: auto;
          margin: 8px 0;
          
          code {
            background: none;
            padding: 0;
          }
        }
        
        :deep(blockquote) {
          border-left: 4px solid #ddd;
          padding-left: 12px;
          margin: 8px 0;
          color: #666;
        }
        
        :deep(table) {
          border-collapse: collapse;
          width: 100%;
          margin: 8px 0;
          
          th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
          }
          
          th {
            background-color: #f5f5f5;
          }
        }
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
  padding: 16px 20px;
  background-color: #ffffff;
  border-top: 1px solid #e4e7ed;
  
  .quick-questions {
    margin-bottom: 12px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    
    .quick-question-tag {
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }
  }
  
  :deep(.el-textarea) {
    margin-bottom: 12px;
  }
  
  :deep(.el-button) {
    float: right;
  }
}
</style>
