<template>
  <div class="chat-container">
    <div class="chat-header">
      <button class="back-btn" @click="goBack">← 返回</button>
      <h1>AI 恋爱大师</h1>
      <span class="chat-id">会话ID: {{ chatId }}</span>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="(msg, index) in messages" 
        :key="index" 
        :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']"
      >
        <div class="message-content">{{ msg.content }}</div>
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
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'LoveApp',
  setup() {
    const router = useRouter()
    const messages = ref([])
    const inputMessage = ref('')
    const chatId = ref('')
    const isTyping = ref(false)
    const messagesContainer = ref(null)
    let eventSource = null

    const generateChatId = () => {
      return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
    }

    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }

    const sendMessage = () => {
      if (!inputMessage.value.trim() || isTyping.value) return

      const userMessage = inputMessage.value.trim()
      messages.value.push({ role: 'user', content: userMessage })
      inputMessage.value = ''
      scrollToBottom()

      isTyping.value = true

      const params = new URLSearchParams({
        message: userMessage,
        chatId: chatId.value
      })

      eventSource = new EventSource(`/api/ai/love_app/chat/sse?${params.toString()}`)

      eventSource.onmessage = (event) => {
        if (event.data) {
          const lastMessage = messages.value[messages.value.length - 1]
          if (lastMessage && lastMessage.role === 'ai') {
            lastMessage.content += event.data
          } else {
            messages.value.push({ role: 'ai', content: event.data })
          }
          scrollToBottom()
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

    onMounted(() => {
      chatId.value = generateChatId()
    })

    onUnmounted(() => {
      if (eventSource) {
        eventSource.close()
      }
    })

    return {
      messages,
      inputMessage,
      chatId,
      isTyping,
      messagesContainer,
      sendMessage,
      goBack
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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

.chat-id {
  font-size: 12px;
  opacity: 0.8;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
  border-color: #667eea;
}

.chat-input input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.chat-input button {
  padding: 12px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
</style>
