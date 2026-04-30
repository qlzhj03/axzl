# 问诊页面Markdown排版修复说明

## 问题描述
AI生成的回复内容虽然有打字机效果，但文字排版混乱，**所有内容都在一段，换行符丢失**，没有正确显示标题、列表、粗体等Markdown格式。

## 问题原因
1. **SSE协议处理问题**：Spring SseEmitter发送包含`\n`的文本时，会将每个换行符拆分成独立的`data:`行，导致前端接收时换行符丢失
2. **Markdown解析不完善**：前端降级处理函数对Markdown的支持不够全面
3. **marked库未安装**：项目缺少marked库来正确解析Markdown格式

## 解决方案

### 1. 安装marked库

在项目根目录执行以下命令：

```bash
cd axzl-vue
npm install marked
```

### 2. 已完成的代码修改

#### 修改文件：`axzl-vue/src/views/biz/consultation/ConsultationTest.vue`

**主要改进：**

1. **修复SSE换行符丢失问题（核心修复）**
   ```typescript
   let isFirstData = true // 标记是否是第一个data块
   
   // 在每个data块之间添加换行符（除了第一个）
   if (!isFirstData && accumulatedText.length > 0) {
     accumulatedText += '\n'
   }
   isFirstData = false
   ```
   
   **原理说明：**
   - Spring SseEmitter发送文本 `"第一段\n第二段"` 时
   - SSE协议会变成两行：`data: 第一段` 和 `data: 第二段`
   - 前端按行解析后，需要在两个data块之间补回换行符
   - 这样才能还原文本原始的换行结构

2. **将marked改为响应式引用**
   ```typescript
   const marked = ref<any>(null)
   
   import('marked').then((module) => {
     marked.value = module.marked
     marked.value.setOptions({
       breaks: true,
       gfm: true,
     })
   })
   ```

2. **增强降级Markdown解析器**
   - 支持代码块（```）
   - 支持多级标题（# ## ### ####）
   - 支持粗体和斜体（**text** *text*）
   - 支持行内代码（`code`）
   - 支持引用（> text）
   - 支持无序列表（- item）
   - 支持有序列表（1. item）
   - 正确处理换行和段落

3. **实时解析**
   - 每次接收到新的文本块时，都会重新解析完整的累积文本
   - 确保显示的HTML始终是最新且格式正确的

### 3. CSS样式已优化

文件中已包含完整的Markdown样式：
- 标题样式（h1-h6）
- 段落间距
- 列表样式
- 代码块背景色
- 引用块样式
- 表格样式

## 验证步骤

1. 安装marked库后，重启前端开发服务器
2. 打开问诊测试页面
3. 发送一条消息，例如："我最近头痛、发烧"
4. 观察AI回复是否正确显示：
   - ✓ 标题应该加粗变大
   - ✓ 列表项应该有缩进和项目符号
   - ✓ 粗体文本应该加粗显示
   - ✓ 代码应该有背景色
   - ✓ 段落之间应该有适当间距

## 如果仍然有问题

### 检查点1：确认marked已安装
```bash
npm list marked
```

### 检查点2：查看浏览器控制台
- 是否有marked加载错误
- 是否有Markdown解析错误

### 检查点3：检查后端返回内容
在后端日志中查看AI是否真的返回了Markdown格式的文本

### 检查点4：临时测试
可以在formatMessage函数中添加console.log查看原始内容和解析后的HTML：
```typescript
function formatMessage(content: string) {
  console.log('原始内容:', content)
  // ... 解析逻辑
  const result = marked.value.parse(content)
  console.log('解析结果:', result)
  return result
}
```

## 技术细节

### 为什么需要marked库？
虽然我们有降级的正则表达式解析器，但它只能处理简单的Markdown语法。marked库可以：
- 正确嵌套HTML标签
- 处理复杂的Markdown结构
- 支持更多Markdown特性
- 避免正则表达式的边界情况

### 流式渲染的挑战
在SSE流式响应中，文本是逐块到达的，可能导致：
- 不完整的Markdown标记（如只收到 `**粗` 还没收到 `体**`）
- Spring SseEmitter会将文本中的`\n`拆分成多个data行
- marked库会自动处理这种情况
- 我们的实现会在每次更新时重新解析完整文本
- **关键修复**：在每个data块之间补回换行符，还原原始文本结构

## 相关文件
- 前端页面：`axzl-vue/src/views/biz/consultation/ConsultationTest.vue`
- API接口：`axzl-vue/src/api/biz/consultation.ts`
- 后端服务：`biz-ai/src/main/java/com/axzl/biz/ai/service/AiConsultationService.java`
- 后端控制器：`axzl-admin/src/main/java/com/axzl/web/controller/biz/consultation/ConsultationController.java`
