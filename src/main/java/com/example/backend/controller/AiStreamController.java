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
        System.out.println("收到用户消息: " + userMessage);

        emitter.onTimeout(() -> {
            System.out.println("SSE连接超时");
            emitter.complete();
        });
        emitter.onError(e -> {
            System.err.println("SSE连接错误: " + e.getMessage());
            e.printStackTrace();
            emitter.complete();
        });
        emitter.onCompletion(() -> {
            System.out.println("SSE连接完成");
        });

        executor.execute(() -> {
            try {
                System.out.println("开始调用AI服务...");
                final String response = aiService.chatStream(userMessage);
                System.out.println("AI返回结果长度: " + response.length());
                System.out.println("AI返回结果: " + response);
                
                // 模拟思考时间（AI正在思考）
                Thread.sleep(500);
                
                // 逐字发送，模拟打字机效果
                final char[] chars = response.toCharArray();
                final StringBuilder currentText = new StringBuilder();
                
                for (int i = 0; i < chars.length; i++) {
                    currentText.append(chars[i]);
                    // 每2-3个字符发送一次
                    if ((i + 1) % 2 == 0 || i == chars.length - 1) {
                        try {
                            emitter.send(SseEmitter.event()
                                .data(currentText.toString()));
                        } catch (IOException e) {
                            System.err.println("发送SSE数据失败: " + e.getMessage());
                            // 连接可能已断开
                            break;
                        }
                        // 随机延迟，让打字更自然
                        Thread.sleep(20 + (int)(Math.random() * 20));
                    }
                }
                
                // 发送完成信号
                try {
                    emitter.send(SseEmitter.event()
                        .data("[DONE]"));
                } catch (IOException e) {
                    System.err.println("发送完成信号失败: " + e.getMessage());
                }
                
                emitter.complete();
            } catch (Exception e) {
                System.err.println("处理流式聊天时出错: " + e.getMessage());
                e.printStackTrace();
                try {
                    emitter.send(SseEmitter.event()
                        .data("抱歉，处理您的请求时出现了错误，请稍后重试。"));
                    emitter.send(SseEmitter.event()
                        .data("[DONE]"));
                } catch (IOException ioException) {
                    System.err.println("发送错误消息失败: " + ioException.getMessage());
                }
                emitter.complete();
            }
        });

        return emitter;
    }
}
