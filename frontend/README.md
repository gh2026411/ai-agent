# AI Chat App

基于Vue3的AI聊天应用前端项目，包含AI恋爱大师和AI超级智能体两个应用。

## 功能特性

- **主页**: 应用选择页面，可切换不同的AI应用
- **AI恋爱大师**: 聊天室风格界面，通过SSE实时获取恋爱建议
- **AI超级智能体**: 聊天室风格界面，通过SSE实时获取智能回复

## 技术栈

- Vue 3
- Vue Router
- Vite
- Axios
- SSE (Server-Sent Events)

## 项目结构

```
frontend/
├── src/
│   ├── api/
│   │   └── ai.js          # API服务封装
│   ├── router/
│   │   └── index.js       # 路由配置
│   ├── views/
│   │   ├── Home.vue       # 主页
│   │   ├── LoveApp.vue    # AI恋爱大师页面
│   │   └── Manus.vue      # AI超级智能体页面
│   ├── App.vue            # 根组件
│   └── main.js            # 入口文件
├── index.html             # HTML模板
├── package.json           # 项目依赖
├── vite.config.js         # Vite配置
└── README.md              # 项目说明
```

## 安装依赖

```bash
cd frontend
npm install
```

## 运行项目

```bash
npm run dev
```

项目将在 http://localhost:3000 启动

## 构建项目

```bash
npm run build
```

## 后端接口

项目已配置代理，前端请求会自动转发到后端：

- AI恋爱大师SSE接口: `/api/ai/love_app/chat/sse`
- AI超级智能体SSE接口: `/api/ai/manus/chat`

确保后端服务运行在 http://localhost:8123

## 使用说明

1. 启动后端服务（SpringBoot应用）
2. 启动前端开发服务器
3. 访问 http://localhost:3000
4. 在主页选择要使用的AI应用
5. 在聊天页面输入消息，实时获取AI回复
