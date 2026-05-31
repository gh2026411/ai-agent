package com.gh.aiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;


public class LangChain4jAiInvoke {
    public static void main(String[] args){
        ChatModel qwenModel=QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String result=qwenModel.chat("你好");
System.out.println(result);    }
}
