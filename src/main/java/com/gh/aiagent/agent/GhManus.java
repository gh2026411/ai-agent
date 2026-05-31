package com.gh.aiagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class GhManus extends ToolCallAgent {

    public GhManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        // 1️⃣ 先构建 ChatClient（Spring AI 1.1.2 标准写法）


        // 2️⃣ 调用父类构造函数（Java 语法要求 super() 必须在第一行）
        super(ChatClient.builder(dashscopeChatModel)
                .build(), allTools);

        // 3️⃣ 配置 Agent 业务属性
        this.setName("Ghagent");
        this.setSystemPrompt("""
    你是 Ghagent，一个全能 AI 助手。
    【工作流程】
    你将按照"思考-行动-观察"的三阶段流程工作：
    1. 思考阶段：分析用户需求，决定是否需要调用工具
    2. 行动阶段：执行工具调用，完成具体任务
    3. 观察阶段：根据工具执行结果，判断任务是否完成

    【核心规则】
    1. 必须使用工具完成任务，严禁仅用文本敷衍。
    2. 不要说"我将调用工具"，必须直接调用工具函数。
    3. 工具调用参数必须是有效的JSON格式。
    4. 生成文件时必须调用 FileOperationTool 工具。
    5. 在观察阶段，如果任务已完成，必须调用 doTerminate 工具终止交互。
    6. 在观察阶段，如果任务未完成，说明还需要做什么，但不要调用任何工具。
    """);
        this.setNextStepPrompt("""
            在思考阶段，根据用户需求和当前状态，主动选择最合适的工具或工具组合。
            对于复杂的任务，您可以分解问题并逐步使用不同的工具来解决它。
            在行动阶段，执行选定的工具，并清楚地说明执行目的。
            在观察阶段，根据工具执行结果判断任务是否完成，如果完成则调用doTerminate终止。
            """);
        this.setMaxSteps(20);
    }
}
