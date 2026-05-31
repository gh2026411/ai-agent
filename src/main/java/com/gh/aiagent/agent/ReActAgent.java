package com.gh.aiagent.agent;

import com.gh.aiagent.agent.model.AgentState;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@EqualsAndHashCode(callSuper = true)
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    public ReActAgent(ChatClient chatClient) {
        super(chatClient);
    }

    public ReActAgent() {
        super();
    }

    public abstract boolean think();
    public abstract String act();
    public abstract boolean observe();
    public abstract void thinkStream(SseEmitter emitter);
    public abstract void actStream(SseEmitter emitter);
    public abstract void observeStream(SseEmitter emitter);

    @Override
    public String step() {
        try {
            // 1. 思考阶段
            boolean shouldAct = think();
            if (!shouldAct) {
                setState(AgentState.FINISHED);
                return "思考完成 - 无需行动";
            }

            // 2. 行动阶段
            String actResult = act();
            if (getState() == AgentState.FINISHED) {
                return actResult;
            }

            // 3. 观察阶段：根据执行结果判断是否需要继续
            boolean shouldContinue = observe();
            if (!shouldContinue) {
                setState(AgentState.FINISHED);
                return actResult + " - 观察完成 - 任务结束";
            }

            return actResult + " - 观察完成 - 继续下一步";
        } catch (Exception e) {
            log.error("{} 步骤执行失败", getName(), e);
            return "步骤执行失败: " + e.getMessage();
        }
    }

    @Override
    public void stepStream(SseEmitter emitter) {
        try {
            // 1. 思考阶段
            thinkStream(emitter);
            if (getState() == AgentState.FINISHED) {
                return;
            }

            // 2. 行动阶段
            actStream(emitter);
            if (getState() == AgentState.FINISHED) {
                return;
            }

            // 3. 观察阶段：根据执行结果判断是否需要继续
            observeStream(emitter);
        } catch (Exception e) {
            log.error("{} 流式步骤执行失败", getName(), e);
            try {
                emitter.send("步骤执行失败: " + e.getMessage());
            } catch (Exception ex) {
                log.error("发送错误消息失败", ex);
            }
        }
    }
}
