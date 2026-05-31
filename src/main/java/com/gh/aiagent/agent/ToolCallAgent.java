package com.gh.aiagent.agent;

import com.gh.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    private final ToolCallback[] availableTools;
    private ChatResponse toolCallChatResponse;
    private final ChatOptions chatOptions;

    public ToolCallAgent(ChatClient chatClient, ToolCallback[] availableTools) {
        super(chatClient);
        this.availableTools = availableTools;
        this.chatOptions = DashScopeChatOptions.builder().withToolChoice("auto").build();
    }

    @Override
    public boolean think() {
        List<Message> messagesForPrompt = new ArrayList<>(getMessageList());

        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            // 将其作为临时的 UserMessage 加入本次推理，但不存入历史
            messagesForPrompt.add(new UserMessage(getNextStepPrompt()));
        }

        Prompt prompt = new Prompt(messagesForPrompt, chatOptions);

        try {
            // ✅ 1.1.2 标准链式调用
            this.toolCallChatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();

            AssistantMessage assistantMsg = toolCallChatResponse.getResult().getOutput();
            log.debug("🤖 模型原始文本回复: {}", assistantMsg.getText());
            log.debug("️ 解析到的工具调用列表: {}", assistantMsg.getToolCalls());
            String text = assistantMsg.getText();
            List<AssistantMessage.ToolCall> toolCalls = assistantMsg.getToolCalls();

            log.info("{} 的思考: {}", getName(), text);
            log.info("{} 选择了 {} 个工具", getName(), CollectionUtils.isEmpty(toolCalls) ? 0 : toolCalls.size());

            if (!CollectionUtils.isEmpty(toolCalls)) {
                String toolInfo = toolCalls.stream()
                        .map(tc -> String.format("工具名称：%s，参数：%s", tc.name(), tc.arguments()))
                        .collect(Collectors.joining("\n"));
                log.info(toolInfo);
            }

            getMessageList().add(assistantMsg);
            return !CollectionUtils.isEmpty(toolCalls);

        } catch (Exception e) {
            log.error("{} 思考过程异常: {}", getName(), e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public void thinkStream(SseEmitter emitter) {
        List<Message> messagesForPrompt = new ArrayList<>(getMessageList());

        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            messagesForPrompt.add(new UserMessage(getNextStepPrompt()));
        }

        Prompt prompt = new Prompt(messagesForPrompt, chatOptions);

        try {
            Flux<ChatResponse> stream = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .stream()
                    .chatResponse();

            StringBuilder fullText = new StringBuilder();
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.atomic.AtomicBoolean emitterCompleted = new java.util.concurrent.atomic.AtomicBoolean(false);
            
            // 监听emitter完成事件
            emitter.onCompletion(() -> {
                emitterCompleted.set(true);
                latch.countDown();
            });
            
            emitter.onTimeout(() -> {
                emitterCompleted.set(true);
                latch.countDown();
            });

            stream.subscribe(
                    chatResponse -> {
                        try {
                            if (emitterCompleted.get()) {
                                return;
                            }
                            AssistantMessage assistantMsg = chatResponse.getResult().getOutput();
                            String text = assistantMsg.getText();
                            if (text != null && !text.isEmpty()) {
                                fullText.append(text);
                                emitter.send(text);
                            }
                        } catch (IllegalStateException e) {
                            log.warn("Emitter已完成，停止发送消息");
                            emitterCompleted.set(true);
                        } catch (org.apache.catalina.connector.ClientAbortException e) {
                            log.warn("客户端断开连接，停止发送消息");
                            emitterCompleted.set(true);
                        } catch (Exception e) {
                            log.error("发送流式消息失败", e);
                        }
                    },
                    error -> {
                        log.error("{} 思考过程异常: {}", getName(), error.getMessage());
                        if (!emitterCompleted.get()) {
                            try {
                                emitter.send("\n[错误: " + error.getMessage() + "]");
                            } catch (Exception e) {
                                log.error("发送错误消息失败", e);
                            }
                        }
                        latch.countDown();
                    },
                    () -> {
                        try {
                            if (emitterCompleted.get()) {
                                latch.countDown();
                                return;
                            }
                            // 流结束后，构建完整的AssistantMessage
                            AssistantMessage assistantMsg = new AssistantMessage(fullText.toString());
                            List<AssistantMessage.ToolCall> toolCalls = new ArrayList<>();
                            
                            // 这里需要重新获取工具调用信息，因为流式响应可能不包含
                            // 为了简化，我们使用非流式方式获取工具调用
                            this.toolCallChatResponse = getChatClient().prompt(prompt)
                                    .system(getSystemPrompt())
                                    .toolCallbacks(availableTools)
                                    .call()
                                    .chatResponse();
                            
                            toolCalls = this.toolCallChatResponse.getResult().getOutput().getToolCalls();
                            
                            log.info("{} 的思考: {}", getName(), fullText.toString());
                            log.info("{} 选择了 {} 个工具", getName(), CollectionUtils.isEmpty(toolCalls) ? 0 : toolCalls.size());

                            if (!CollectionUtils.isEmpty(toolCalls)) {
                                String toolInfo = toolCalls.stream()
                                        .map(tc -> String.format("\n[调用工具: %s, 参数: %s]", tc.name(), tc.arguments()))
                                        .collect(Collectors.joining(""));
                                if (!emitterCompleted.get()) {
                                    emitter.send(toolInfo);
                                }
                            }

                            // 更新assistantMsg以包含工具调用
                            if (!CollectionUtils.isEmpty(toolCalls)) {
                                getMessageList().add(this.toolCallChatResponse.getResult().getOutput());
                            } else {
                                getMessageList().add(assistantMsg);
                                // 没有工具调用时，设置状态为FINISHED
                                setState(AgentState.FINISHED);
                                log.info("{} 无工具调用，设置状态为FINISHED", getName());
                            }
                        } catch (Exception e) {
                            log.error("处理流式结束失败", e);
                        } finally {
                            latch.countDown();
                        }
                    }
            );
            
            // 等待流式输出完成
            latch.await(60, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("{} 思考过程异常: {}", getName(), e.getMessage());
            try {
                emitter.send("\n[错误: " + e.getMessage() + "]");
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }

    @Override
    public void actStream(SseEmitter emitter) {
        if (toolCallChatResponse == null || CollectionUtils.isEmpty(toolCallChatResponse.getResult().getOutput().getToolCalls())) {
            try {
                emitter.send("\n[无需执行工具]");
            } catch (Exception e) {
                log.error("发送消息失败", e);
            }
            return;
        }

        List<AssistantMessage.ToolCall> toolCalls = toolCallChatResponse.getResult().getOutput().getToolCalls();
        List<ToolResponseMessage.ToolResponse> responses = new ArrayList<>();

        for (AssistantMessage.ToolCall tc : toolCalls) {
            String toolName = tc.name();
            String toolArgs = tc.arguments();

            try {
                emitter.send("\n[执行工具: " + toolName + "]");
                log.info("正在执行工具: {}, 参数: {}", toolName, toolArgs);

                String execResult = executeToolByName(toolName, toolArgs);
                responses.add(new ToolResponseMessage.ToolResponse(tc.id(), toolName, execResult));

                // 检测文件下载请求
                if (execResult != null && execResult.startsWith("FILE_DOWNLOAD:")) {
                    // 解析文件名和内容
                    String[] parts = execResult.split(":", 3);
                    if (parts.length == 3) {
                        String filename = parts[1];
                        String content = parts[2];
                        // 发送文件下载标记
                        emitter.send("\n[FILE_DOWNLOAD:" + filename + ":" + content + "]");
                        log.info("发送文件下载请求: {}", filename);
                    } else {
                        emitter.send("\n[工具结果: " + execResult + "]");
                    }
                } else {
                    emitter.send("\n[工具结果: " + execResult + "]");
                }
            } catch (org.apache.catalina.connector.ClientAbortException e) {
                log.warn("客户端断开连接，停止发送工具执行结果");
            } catch (Exception e) {
                log.error("工具 {} 执行失败: {}", toolName, e.getMessage());
                String errorMsg = "执行失败: " + e.getMessage();
                responses.add(new ToolResponseMessage.ToolResponse(tc.id(), toolName, errorMsg));
                try {
                    emitter.send("\n[工具错误: " + errorMsg + "]");
                } catch (Exception ex) {
                    log.error("发送错误消息失败", ex);
                }
            }
        }

        if (!responses.isEmpty()) {
            ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                    .responses(responses)
                    .build();

            getMessageList().add(toolResponseMessage);

            boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                    .anyMatch(response -> "doTerminate".equals(response.name()));

            if (terminateToolCalled) {
                setState(AgentState.FINISHED);
                log.info("检测到终止工具调用，Agent 状态已更新为 FINISHED");
                try {
                    emitter.send("\n[任务完成]");
                } catch (Exception e) {
                    log.error("发送消息失败", e);
                }
            }
        }
    }

    @Override
    public boolean observe() {
        try {
            // 观察阶段：让AI根据工具执行结果判断是否需要继续
            List<Message> messagesForPrompt = new ArrayList<>(getMessageList());

            Prompt prompt = new Prompt(messagesForPrompt, chatOptions);

            // 构建观察提示词
            String observePrompt = """
                请观察刚才的工具执行结果，判断用户的任务是否已经完成。
                如果任务已完成，请调用 doTerminate 工具终止交互。
                如果任务未完成，请说明还需要做什么，但不要调用任何工具。
                """;

            ChatResponse observeResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt() + "\n" + observePrompt)
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();

            List<AssistantMessage.ToolCall> toolCalls = observeResponse.getResult().getOutput().getToolCalls();

            log.info("{} 的观察: {}", getName(), observeResponse.getResult().getOutput().getText());
            log.info("{} 观察后选择了 {} 个工具", getName(), CollectionUtils.isEmpty(toolCalls) ? 0 : toolCalls.size());

            // 检查是否调用了doTerminate
            boolean terminateToolCalled = !CollectionUtils.isEmpty(toolCalls) &&
                    toolCalls.stream().anyMatch(tc -> "doTerminate".equals(tc.name()));

            if (terminateToolCalled) {
                setState(AgentState.FINISHED);
                log.info("{} 观察后检测到终止工具调用，设置状态为FINISHED", getName());
                return false; // 任务完成，不需要继续
            } else {
                // 没有调用doTerminate，任务未完成，继续下一个step
                getMessageList().add(observeResponse.getResult().getOutput());
                return true; // 任务未完成，继续
            }
        } catch (Exception e) {
            log.error("{} 观察过程异常: {}", getName(), e.getMessage());
            return false; // 出错时结束，避免无限循环
        }
    }

    @Override
    public void observeStream(SseEmitter emitter) {
        try {
            // 观察阶段：让AI根据工具执行结果判断是否需要继续
            List<Message> messagesForPrompt = new ArrayList<>(getMessageList());

            Prompt prompt = new Prompt(messagesForPrompt, chatOptions);

            // 构建观察提示词
            String observePrompt = """
                请观察刚才的工具执行结果，判断用户的任务是否已经完成。
                如果任务已完成，请调用 doTerminate 工具终止交互。
                如果任务未完成，请说明还需要做什么，但不要调用任何工具。
                """;

            Flux<ChatResponse> stream = getChatClient().prompt(prompt)
                    .system(getSystemPrompt() + "\n" + observePrompt)
                    .toolCallbacks(availableTools)
                    .stream()
                    .chatResponse();

            StringBuilder fullText = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);

            AtomicBoolean emitterCompleted = new AtomicBoolean(false);

            emitter.onCompletion(() -> emitterCompleted.set(true));
            emitter.onTimeout(() -> emitterCompleted.set(true));

            stream.subscribe(
                    chatResponse -> {
                        try {
                            if (emitterCompleted.get()) return;
                            AssistantMessage assistantMsg = chatResponse.getResult().getOutput();
                            String text = assistantMsg.getText();
                            if (text != null && !text.isEmpty()) {
                                fullText.append(text);
                                emitter.send(text);
                            }
                        } catch (IllegalStateException e) {
                            log.warn("Emitter已完成，停止发送消息");
                            emitterCompleted.set(true);
                        } catch (org.apache.catalina.connector.ClientAbortException e) {
                            log.warn("客户端断开连接，停止发送观察消息");
                            emitterCompleted.set(true);
                        } catch (Exception e) {
                            log.error("发送观察消息失败", e);
                        }
                    },
                    error -> {
                        log.error("{} 观察过程异常: {}", getName(), error.getMessage());
                        if (!emitterCompleted.get()) {
                            try {
                                emitter.send("\n[观察错误: " + error.getMessage() + "]");
                            } catch (Exception e) {
                                log.error("发送错误消息失败", e);
                            }
                        }
                        latch.countDown();
                    },
                    () -> {
                        try {
                            if (emitterCompleted.get()) {
                                latch.countDown();
                                return;
                            }

                            // 重新获取工具调用信息
                            ChatResponse observeResponse = getChatClient().prompt(prompt)
                                    .system(getSystemPrompt() + "\n" + observePrompt)
                                    .toolCallbacks(availableTools)
                                    .call()
                                    .chatResponse();

                            List<AssistantMessage.ToolCall> toolCalls = observeResponse.getResult().getOutput().getToolCalls();

                            log.info("{} 的观察: {}", getName(), fullText.toString());
                            log.info("{} 观察后选择了 {} 个工具", getName(), CollectionUtils.isEmpty(toolCalls) ? 0 : toolCalls.size());

                            // 检查是否调用了doTerminate
                            boolean terminateToolCalled = !CollectionUtils.isEmpty(toolCalls) &&
                                    toolCalls.stream().anyMatch(tc -> "doTerminate".equals(tc.name()));

                            if (terminateToolCalled) {
                                setState(AgentState.FINISHED);
                                log.info("{} 观察后检测到终止工具调用，设置状态为FINISHED", getName());
                                try {
                                    emitter.send("\n[任务完成]");
                                } catch (Exception e) {
                                    log.error("发送消息失败", e);
                                }
                            } else {
                                // 没有调用doTerminate，任务未完成，继续下一个step
                                getMessageList().add(observeResponse.getResult().getOutput());
                            }
                        } catch (Exception e) {
                            log.error("处理观察流式结束失败", e);
                        } finally {
                            latch.countDown();
                        }
                    }
            );

            latch.await(60, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("{} 观察过程异常: {}", getName(), e.getMessage());
            try {
                emitter.send("\n[观察错误: " + e.getMessage() + "]");
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }

    @Override
    public String act() {
        // 1. 检查是否有工具需要调用
        if (toolCallChatResponse == null || CollectionUtils.isEmpty(toolCallChatResponse.getResult().getOutput().getToolCalls())) {
            return "没有工具调用";
        }

        List<AssistantMessage.ToolCall> toolCalls = toolCallChatResponse.getResult().getOutput().getToolCalls();
        List<ToolResponseMessage.ToolResponse> responses = new ArrayList<>();

        // 2. 循环执行工具 (保留原有的执行逻辑)
        for (AssistantMessage.ToolCall tc : toolCalls) {
            String toolName = tc.name();
            String toolArgs = tc.arguments();
            log.info("正在执行工具: {}, 参数: {}", toolName, toolArgs);

            try {
                // 执行工具并获取结果字符串
                String execResult = executeToolByName(toolName, toolArgs);

                // ✅ 使用 Builder 模式或公共构造函数构建 ToolResponse
                // 注意：ToolResponse 通常是静态内部类，可以直接 new
                responses.add(new ToolResponseMessage.ToolResponse(tc.id(), toolName, execResult));

            } catch (Exception e) {
                log.error("工具 {} 执行失败: {}", toolName, e.getMessage());
                responses.add(new ToolResponseMessage.ToolResponse(tc.id(), toolName, "执行失败: " + e.getMessage()));
            }
        }

        // 3. 如果有执行结果，进行处理 (融合你提供的逻辑)
        if (!responses.isEmpty()) {
            // A. 构建 ToolResponseMessage (使用 Builder 修复 protected 报错)
            ToolResponseMessage toolResponseMessage = ToolResponseMessage.builder()
                    .responses(responses)
                    .build();

            // B. 将消息添加到会话历史中 (模拟 toolExecutionResult 的上下文)
            getMessageList().add(toolResponseMessage);

            // C. === 以下是你提供的核心逻辑 ===

            // 1. 生成结果字符串
            String results = toolResponseMessage.getResponses().stream()
                    .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                    .collect(Collectors.joining("\n"));

            // 2. 检查是否调用了终止工具
            boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                    .anyMatch(response -> "doTerminate".equals(response.name()));

            if (terminateToolCalled) {
                setState(AgentState.FINISHED);
                log.info("检测到终止工具调用，Agent 状态已更新为 FINISHED");
            }

            // 3. 日志与返回
            log.info(results);
            return results;
        }

        return "工具执行完毕但无有效响应";
    }

    private String executeToolByName(String toolName, String toolArgs) {
        for (ToolCallback tool : availableTools) {
            if (tool.getToolDefinition().name().equals(toolName)) {
                // ✅ 修正：直接返回 String 结果
                return tool.call(toolArgs);
            }
        }
        throw new IllegalArgumentException("未注册的工具: " + toolName);
    }
}
