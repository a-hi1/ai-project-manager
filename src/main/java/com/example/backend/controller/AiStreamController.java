package com.example.backend.controller;

import com.example.backend.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/ai")
public class AiStreamController {
    @Autowired
    private AiService aiService;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody java.util.Map<String, Object> data) {
        final SseEmitter emitter = new SseEmitter(600000L);

        final String userMessage = (String) data.get("message");

        emitter.onTimeout(emitter::complete);
        emitter.onError(e -> {
            emitter.complete();
        });
        emitter.onCompletion(() -> {});

        executor.execute(() -> {
            try {
                final String response = aiService.chatStream(userMessage);
                
                // 按字符流式发送，模拟打字效果
                final char[] chars = response.toCharArray();
                final StringBuilder currentText = new StringBuilder();
                
                for (int i = 0; i < chars.length; i++) {
                    currentText.append(chars[i]);
                    // 每3个字符发送一次，或者到结尾时发送
                    if ((i + 1) % 3 == 0 || i == chars.length - 1) {
                        try {
                            emitter.send(SseEmitter.event()
                                .data(currentText.toString()));
                        } catch (IOException e) {
                            // 连接可能已断开
                            break;
                        }
                        Thread.sleep(30);
                    }
                }
                
                // 发送完成信号
                try {
                    emitter.send(SseEmitter.event()
                        .data("[DONE]"));
                } catch (IOException e) {
                    // 忽略
                }
                
                emitter.complete();
            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event()
                        .data("抱歉，处理您的请求时出现了错误，请稍后重试。"));
                } catch (IOException ioException) {
                    // 忽略
                }
                emitter.complete();
            }
        });

        return emitter;
    }
}
