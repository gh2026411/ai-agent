<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>AI 超级智能体</h1>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']"
      >
        <div class="message-content" v-if="msg.role === 'user'">{{ msg.content }}</div>
        <div class="message-content markdown-content" v-else v-html="renderMarkdown(msg.content)"></div>
        <div v-if="msg.fileDownload" class="file-download">
          <button @click="downloadFile(msg.fileDownload.filename, msg.fileDownload.content)" class="download-btn">
            📎 下载文件: {{ msg.fileDownload.filename }}
          </button>
        </div>
      </div>
      <div v-if="isTyping" class="message ai-message">
        <div class="message-content typing">正在输入...</div>
      </div>
    </div>
    
    <div class="chat-input">
      <input 
        v-model="inputMessage" 
        @keyup.enter="sendMessage"
        placeholder="输入你的消息..." 
        :disabled="isTyping"
      />
      <button @click="sendMessage" :disabled="isTyping || !inputMessage.trim()">发送</button>
    </div>
  </div>
</template>

<script>
import { ref, nextTick, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { marked } from 'marked'

export default {
  name: 'Manus',
  setup() {
    const router = useRouter()
    const messages = ref([])
    const inputMessage = ref('')
    const isTyping = ref(false)
    const messagesContainer = ref(null)
    let eventSource = null

    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }

    const renderMarkdown = (content) => {
      // 配置marked以支持图片渲染
      marked.setOptions({
        gfm: true,
        breaks: true,
        sanitize: false
      })
      return marked(content)
    }

    const downloadFile = (filename, content) => {
      try {
        const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
        const url = URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = filename
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        URL.revokeObjectURL(url)
        console.log('文件下载成功:', filename)
      } catch (error) {
        console.error('文件下载失败:', error)
      }
    }

    const sendMessage = () => {
      if (!inputMessage.value.trim() || isTyping.value) return

      const userMessage = inputMessage.value.trim()
      messages.value.push({ role: 'user', content: userMessage })
      inputMessage.value = ''
      scrollToBottom()

      isTyping.value = true

      const params = new URLSearchParams({
        message: userMessage
      })

      eventSource = new EventSource(`/api/ai/manus/chat?${params.toString()}`)

      eventSource.onmessage = (event) => {
        if (event.data) {
          // 检测文件下载标记
          if (event.data.startsWith('[FILE_DOWNLOAD:')) {
            const match = event.data.match(/\[FILE_DOWNLOAD:([^:]+):(.+)\]/)
            if (match) {
              const filename = match[1]
              const content = match[2]
              // 将文件信息存储到消息对象中
              const lastMessage = messages.value[messages.value.length - 1]
              if (lastMessage && lastMessage.role === 'ai') {
                lastMessage.fileDownload = { filename, content }
                lastMessage.content += `\n\n📎 文件已生成: ${filename}`
              } else {
                messages.value.push({
                  role: 'ai',
                  content: `📎 文件已生成: ${filename}`,
                  fileDownload: { filename, content }
                })
              }
              scrollToBottom()
            }
          } else {
            const lastMessage = messages.value[messages.value.length - 1]
            if (lastMessage && lastMessage.role === 'ai') {
              lastMessage.content += event.data
            } else {
              messages.value.push({ role: 'ai', content: event.data })
            }
            scrollToBottom()
          }
        }
      }

      eventSource.onerror = (error) => {
        console.error('SSE error:', error)
        eventSource.close()
        isTyping.value = false
      }

      eventSource.addEventListener('end', () => {
        eventSource.close()
        isTyping.value = false
      })
    }

    const goBack = () => {
      if (eventSource) {
        eventSource.close()
      }
      router.push('/')
    }

    onUnmounted(() => {
      if (eventSource) {
        eventSource.close()
      }
    })

    return {
      messages,
      inputMessage,
      isTyping,
      messagesContainer,
      sendMessage,
      goBack,
      renderMarkdown
    }
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.chat-header {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.back-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.chat-header h1 {
  font-size: 20px;
  margin: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message {
  display: flex;
  max-width: 70%;
}

.user-message {
  align-self: flex-end;
}

.ai-message {
  align-self: flex-start;
}

.message-content {
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.5;
  word-wrap: break-word;
}

.user-message .message-content {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.ai-message .message-content {
  background: white;
  color: #333;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.typing {
  color: #999;
  font-style: italic;
}

.chat-input {
  background: white;
  padding: 20px;
  display: flex;
  gap: 10px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.chat-input input {
  flex: 1;
  padding: 12px 20px;
  border: 2px solid #e0e0e0;
  border-radius: 25px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
}

.chat-input input:focus {
  border-color: #f5576c;
}

.chat-input input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.chat-input button {
  padding: 12px 30px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.2s, opacity 0.3s;
}

.chat-input button:hover:not(:disabled) {
  transform: scale(1.05);
}

.chat-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Markdown样式 */
.markdown-content {
  line-height: 1.6;
}

.markdown-content h1,
.markdown-content h2,
.markdown-content h3,
.markdown-content h4,
.markdown-content h5,
.markdown-content h6 {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-content h1 {
  font-size: 1.5em;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 8px;
}

.markdown-content h2 {
  font-size: 1.3em;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 6px;
}

.markdown-content h3 {
  font-size: 1.15em;
}

.markdown-content p {
  margin: 8px 0;
}

.markdown-content ul,
.markdown-content ol {
  margin: 8px 0;
  padding-left: 20px;
}

.markdown-content li {
  margin: 4px 0;
}

.markdown-content code {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.markdown-content pre {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-content pre code {
  background: none;
  padding: 0;
}

.markdown-content blockquote {
  border-left: 4px solid #f5576c;
  padding-left: 12px;
  margin: 8px 0;
  color: #666;
  font-style: italic;
}

.markdown-content strong {
  font-weight: 600;
}

.markdown-content em {
  font-style: italic;
}

.markdown-content a {
  color: #f5576c;
  text-decoration: none;
}

.markdown-content a:hover {
  text-decoration: underline;
}

.markdown-content table {
  border-collapse: collapse;
  margin: 8px 0;
  width: 100%;
}

.markdown-content th,
.markdown-content td {
  border: 1px solid #e0e0e0;
  padding: 8px;
  text-align: left;
}

.markdown-content th {
  background: #f5f5f5;
  font-weight: 600;
}

.markdown-content hr {
  border: none;
  border-top: 1px solid #e0e0e0;
  margin: 16px 0;
}

.markdown-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 8px 0;
  display: block;
}

.file-download {
  margin-top: 8px;
}

.download-btn {
  background: #f5576c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.download-btn:hover {
  background: #e04a5e;
}
</style>
