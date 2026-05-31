<template>
  <div class="home">
    <div class="container">
      <h1 class="title">AI 应用中心</h1>
      <div class="app-grid">
        <div class="app-card" @click="navigateTo('/love-app')">
          <div class="app-icon">💕</div>
          <h2 class="app-name">AI 恋爱大师</h2>
          <p class="app-desc">专业的恋爱建议与情感交流</p>
        </div>
        <div class="app-card" @click="navigateTo('/manus')">
          <div class="app-icon">🤖</div>
          <h2 class="app-name">AI 超级智能体</h2>
          <p class="app-desc">全能型AI助手，解决各种问题</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()

    const navigateTo = (path) => {
      router.push(path)
    }

    return {
      navigateTo
    }
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.container {
  max-width: 1200px;
  width: 100%;
}

.title {
  text-align: center;
  color: white;
  font-size: 48px;
  margin-bottom: 60px;
  font-weight: 700;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  padding: 0 20px;
}

.app-card {
  background: white;
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.app-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.app-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.app-name {
  font-size: 24px;
  color: #333;
  margin-bottom: 15px;
  font-weight: 600;
}

.app-desc {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
}
</style>
