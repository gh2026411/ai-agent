package com.gh.aiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.prompt.PromptTemplate;

@Configuration
public class LoveAppRagRewriteQueryTransformerConfig {



    @Bean
    public QueryTransformer LoveAppRagRewriteQueryTransformer(ChatModel dashscopeChatModel){
        ChatClient.Builder chatClientBuilder = ChatClient.builder(dashscopeChatModel);
        PromptTemplate template= new PromptTemplate("请优化以下用户提问，去掉无关信息，使其更简洁精准，以便在 {target} 中搜索出最佳结果。\n" +
                "原问题：{query}\n" +
                "优化后：");
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .promptTemplate(template)
                .build();
    }
    
}
