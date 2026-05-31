import axios from 'axios'

// 统一使用相对路径，由nginx处理代理
const API_BASE_URL = '/api'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000
})

// SSE连接封装函数
export const createSSEConnection = (url, params, onMessage, onError, onEnd) => {
  const queryString = new URLSearchParams(params).toString()
  const eventSource = new EventSource(`${url}?${queryString}`)

  eventSource.onmessage = (event) => {
    if (event.data) {
      onMessage(event.data)
    }
  }

  eventSource.onerror = (error) => {
    console.error('SSE error:', error)
    if (onError) onError(error)
  }

  eventSource.addEventListener('end', () => {
    eventSource.close()
    if (onEnd) onEnd()
  })

  return eventSource
}

// AI恋爱大师SSE接口
export const loveAppChatSSE = (message, chatId, onMessage, onError, onEnd) => {
  return createSSEConnection(
    '/api/ai/love_app/chat/sse',
    { message, chatId },
    onMessage,
    onError,
    onEnd
  )
}

// AI超级智能体SSE接口
export const manusChatSSE = (message, onMessage, onError, onEnd) => {
  return createSSEConnection(
    '/api/ai/manus/chat',
    { message },
    onMessage,
    onError,
    onEnd
  )
}

export default apiClient
