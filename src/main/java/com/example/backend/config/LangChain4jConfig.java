package com.example.backend.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LangChain4jConfig {
    @Value("${langchain4j.zhipu.api-key:dummy-key}")
    private String apiKey;

    @Value("${langchain4j.zhipu.model:glm-4-plus}")
    private String modelName;

    @Value("${langchain4j.zhipu.embedding-model:embedding-2}")
    private String embeddingModelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .temperature(0.7)
                .timeout(java.time.Duration.ofSeconds(120))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(embeddingModelName)
                .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        // 暂时使用内存存储，避免 PgVector 兼容性问题
        return new InMemoryEmbeddingStore<>();
    }
}