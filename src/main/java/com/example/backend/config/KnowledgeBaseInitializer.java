package com.example.backend.config;

import com.example.backend.service.KnowledgeDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeBaseInitializer implements CommandLineRunner {

    @Autowired
    private KnowledgeDocumentService knowledgeDocumentService;

    @Override
    public void run(String... args) {
        try {
            System.out.println("正在检查并向量化知识库文档...");
            knowledgeDocumentService.vectorizeAllDocuments();
            System.out.println("知识库文档向量化完成！");
        } catch (Exception e) {
            System.err.println("知识库初始化失败: " + e.getMessage());
        }
    }
}
