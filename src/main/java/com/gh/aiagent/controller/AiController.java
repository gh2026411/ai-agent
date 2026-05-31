package com.gh.aiagent.controller;

import com.gh.aiagent.agent.GhManus;
import com.gh.aiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

//    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
//        return loveApp.doChatByStream(message, chatId);
//    }


    @GetMapping(value = "/love_app/chat/sse", produces = "text/event-stream;charset=UTF-8")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }




    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        GhManus yuManus = new GhManus(allTools, dashscopeChatModel);
        return yuManus.runStream(message);
    }


//    @GetMapping("/love_app/chat/sse/emitter")
//    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
//
//        SseEmitter emitter = new SseEmitter(180000L);
//
//        loveApp.doChatByStream(message, chatId)
//                .subscribe(
//
//                        chunk -> {
//                            try {
//                                emitter.send(chunk);
//                            } catch (IOException e) {
//                                emitter.completeWithError(e);
//                            }
//                        },
//
//                        emitter::completeWithError,
//
//                        emitter::complete
//                );
//
//        return emitter;
//    }

}