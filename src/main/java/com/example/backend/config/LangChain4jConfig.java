package com.example.backend.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {
    @Value("${langchain4j.zhipu.api-key:dummy-key}")
    private String apiKey;

    @Value("${langchain4j.zhipu.model:glm-4-plus}")
    private String modelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .temperature(0.7)
                .build();
    }
}