package com.gh.aiagent.app;


//import com.alibaba.cloud.ai.agent.python.tool.PythonTool;
//import com.alibaba.cloud.ai.toolcalling.baidusearch.BaiduAiSearchProperties;
//import com.alibaba.cloud.ai.toolcalling.baidusearch.BaiduAiSearchService;
//import com.alibaba.cloud.ai.toolcalling.baidusearch.BaiduSearchService;
//import com.alibaba.cloud.ai.toolcalling.baidutranslate.BaiduTranslateService;
import com.gh.aiagent.rag.LoveAppRagCustomAdvisorFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    public LoveApp(ChatModel dashscopeChatModel, ChatMemory chatMemory) {
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        // 这里使用 builder 模式创建 Advisor，这是新版本的标准写法
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }



    @Resource
    private VectorStore vectorStore;
    //pgsql的引入
//    @Resource
//    private LoveAppVectorStoreConfig loveAppVectorStore;
//    @Resource
//    private EmbeddingModel dashscopeEmbeddingModel;
    public String doChatWithRag(String message, String chatId) {

        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                //使用pgVector的advisor
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore.loveAppVectorStoreWithPg( dashscopeEmbeddingModel)).build())
                //使用正常QuestionAnswerAdvisor
//                .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .advisors(LoveAppRagCustomAdvisorFactory.creatCustomAdvisor(vectorStore,"已婚"))
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private Advisor loveAppRagCloudAdvisor;


    @Resource
    private QueryTransformer queryTransformer;

    public String doChatWithRagCloud(String message, String chatId) {

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

//    @Autowired
//    private BaiduSearchTool baiduSearchTool;





    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId){


        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId){


        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(toolCallbackProvider)
                .advisors(loveAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .toolCallbacks(toolCallbackProvider)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}
