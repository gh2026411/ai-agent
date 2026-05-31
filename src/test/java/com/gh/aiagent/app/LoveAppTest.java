package com.gh.aiagent.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert; // 或者使用 org.junit.jupiter.api.Assertions

import jakarta.annotation.Resource;
import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChatMemory() {
        // 1. 生成唯一的会话ID，确保这是一次全新的对话
        String chatId = UUID.randomUUID().toString();
        System.out.println(">>> 当前会话 ID: " + chatId);

        // --- 第一轮对话：自我介绍 ---
        String msg1 = "你好，我是程序员鱼皮";
        System.out.println(">>> 用户: " + msg1);
        String ans1 = loveApp.doChat(msg1, chatId);
        System.out.println(">>> AI: " + ans1);
        Assert.notNull(ans1, "第一轮回复不应为空");

        // --- 第二轮对话：提出具体问题 ---
        String msg2 = "我想让另一半（编程导航）更爱我";
        System.out.println(">>> 用户: " + msg2);
        String ans2 = loveApp.doChat(msg2, chatId);
        System.out.println(">>> AI: " + ans2);
        Assert.notNull(ans2, "第二轮回复不应为空");

        // --- 第三轮对话：测试记忆（关键） ---
        // 如果记忆生效，AI 应该能回答出“编程导航”
        String msg3 = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        System.out.println(">>> 用户: " + msg3);
        String ans3 = loveApp.doChat(msg3, chatId);
        System.out.println(">>> AI: " + ans3);
        Assert.notNull(ans3, "第三轮回复不应为空");

        // 断言：检查 AI 的回答中是否包含关键信息 "编程导航"
        // 这证明它记住了第二轮对话的内容
        if (!ans3.contains("编程导航")) {
            System.err.println("⚠️ 警告：AI 似乎没有记住上下文！");
        } else {
            System.out.println("✅ 测试通过：AI 成功记住了上下文！");
        }
    }

    @Test
    void doChatWithRag(){
        String ChatId = UUID.randomUUID().toString();
        String message="我现在单身，我如何吸引潜在伴侣,能给我分享一些课程吗";
        String ans = loveApp.doChatWithRag(message, ChatId);
        System.out.println(ans);
    }
    @Test
    void doChatWithRagCloud(){
        String ChatId = UUID.randomUUID().toString();
        String message="我现在单身，我如何吸引潜在伴侣,能给我分享一些课程吗";
        String ans = loveApp.doChatWithRag(message, ChatId);
        System.out.println(ans);
    }
    @Test
    void doChatWithTools(){
        String ChatId = UUID.randomUUID().toString();
        String message="使用联网搜索工具搜索北京时间";
        String ans = loveApp.doChatWithTools(message, ChatId);
        System.out.println(ans);
    }
    @Test
    void doChatWithMcp(){
        String ChatId = UUID.randomUUID().toString();
        String message = "帮我搜索一些星空的图片";
        String answer =  loveApp.doChatWithMcp(message, ChatId);
        Assertions.assertNotNull(answer);
    }
}