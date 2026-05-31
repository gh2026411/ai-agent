# AI Agent

一个基于 Spring AI 和阿里云通义千问的智能 AI Agent 项目，包含前端界面、后端服务和 MCP 服务器。

## 项目简介

本项目是一个功能完整的 AI Agent 系统，集成了多种 AI 能力和工具调用功能，支持对话交互、RAG 检索增强生成、工具调用等特性。

## 功能特性

### 核心功能
- **AI Agent 框架**：基于 Spring AI 和阿里云通义千问的智能 Agent 系统
- **工具调用**：支持多种工具的自动调用和编排
- **RAG 检索增强**：基于向量存储的知识检索和增强生成
- **SSE 流式响应**：支持 Server-Sent Events 实时流式输出
- **MCP 协议支持**：集成 Model Context Protocol 客户端和服务器

### 应用场景
- **恋爱顾问应用**：基于 RAG 的恋爱问题咨询系统
- **Manus Agent**：通用任务执行 Agent
- **图像搜索 MCP 服务器**：提供图像搜索能力的 MCP 服务

### 工具集成
- 文件操作工具
- PDF 生成工具
- 网页抓取工具
- 网络搜索工具
- 终端操作工具
- 资源下载工具

## 技术栈

### 后端
- **框架**：Spring Boot 3.4.4
- **AI 框架**：Spring AI 1.1.2, Spring AI Alibaba 1.1.2.0
- **LLM**：阿里云通义千问（DashScope）
- **向量存储**：Spring AI Vector Store
- **MCP**：Spring AI MCP Client/Server
- **工具库**：Hutool, Jsoup, iText
- **文档**：Knife4j (OpenAPI 3)
- **Java 版本**：21

### 前端
- **框架**：Vue 3.4.21
- **路由**：Vue Router 4.3.0
- **HTTP 客户端**：Axios 1.6.8
- **构建工具**：Vite 5.2.0
- **Markdown**：Marked 12.0.0

### MCP 服务器
- **框架**：Spring Boot 3.5.14
- **MCP**：Spring AI MCP Server 1.1.6
- **Java 版本**：21

## 项目结构

```
ai-agent/
├── src/                          # 后端源代码
│   ├── main/
│   │   ├── java/com/gh/aiagent/
│   │   │   ├── agent/           # Agent 实现
│   │   │   ├── app/             # 应用层
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   ├── rag/             # RAG 相关
│   │   │   ├── tools/           # 工具实现
│   │   │   └── constant/        # 常量定义
│   │   └── resources/
│   │       ├── application.yml.example  # 配置示例
│   │       └── md/              # Markdown 文档
│   └── test/                    # 测试代码
├── frontend/                    # 前端项目
│   ├── src/
│   │   ├── api/                # API 调用
│   │   ├── views/              # 页面组件
│   │   ├── router/             # 路由配置
│   │   └── App.vue             # 根组件
│   ├── nginx.conf              # Nginx 配置
│   ├── Dockerfile              # 前端 Dockerfile
│   └── package.json            # 前端依赖
├── gh-image-search-mcp-server/ # MCP 服务器
│   └── src/main/java/com/gh/ghimagesearchmcpserver/
│       └── tools/              # MCP 工具
├── Dockerfile                  # 后端 Dockerfile
├── .dockerignore              # Docker 忽略文件
└── pom.xml                    # Maven 配置
```

## 快速开始

### 前置要求

- JDK 21
- Maven 3.9+
- Node.js 18+
- 阿里云通义千问 API Key

### 配置

1. 复制配置文件示例：
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

2. 在 `application.yml` 中配置阿里云 API Key：
```yaml
spring:
  ai:
    dashscope:
      api-key: your-api-key-here
      chat:
        options:
          model: qwen-plus
```

### 本地开发

#### 启动后端
```bash
mvn spring-boot:run
```

后端服务将在 `http://localhost:8123` 启动

#### 启动前端
```bash
cd frontend
npm install
npm run dev
```

前端开发服务器将在 `http://localhost:5173` 启动

#### 启动 MCP 服务器
```bash
cd gh-image-search-mcp-server
mvn spring-boot:run
```

MCP 服务器将在 SSE 模式下运行

### Docker 部署

#### 构建后端镜像
```bash
docker build -t ai-agent-backend:latest .
```

#### 构建前端镜像
```bash
cd frontend
docker build -t ai-agent-frontend:latest .
```

#### 运行容器
```bash
docker run -p 8123:8123 ai-agent-backend:latest
docker run -p 80:80 ai-agent-frontend:latest
```

## API 文档

启动后端服务后，访问以下地址查看 API 文档：

- Swagger UI: `http://localhost:8123/doc.html`

## 配置说明

### 后端配置

主要配置项在 `application.yml` 中：

- `spring.ai.dashscope.api-key`: 阿里云通义千问 API Key
- `server.port`: 服务端口（默认 8123）
- `spring.ai.mcp`: MCP 客户端配置

### 前端配置

前端使用相对路径 `/api` 访问后端 API，由 Nginx 或 Vite 开发服务器代理到后端。

## 环境变量

可以通过环境变量覆盖配置：

- `DASHSCOPE_API_KEY`: 阿里云 API Key
- `SERVER_PORT`: 服务端口
- `JAVA_OPTS`: JVM 参数

## 常见问题

### 1. 如何获取阿里云 API Key？

访问 [阿里云百炼平台](https://bailian.console.aliyun.com/) 注册并获取 API Key。

### 2. 前端无法连接后端？

确保后端服务已启动，检查 Nginx 配置或 Vite 代理设置。

### 3. MCP 服务器如何连接？

在 `application.yml` 中配置 MCP 服务器地址，或使用 `mcp-servers.json` 配置文件。

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

本项目采用 MIT 许可证。

## 联系方式

如有问题，请通过 GitHub Issues 联系。
