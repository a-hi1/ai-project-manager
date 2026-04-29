package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.KnowledgeDocument;
import com.example.backend.mapper.KnowledgeDocumentMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeDocumentService extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument> {
    public List<KnowledgeDocument> getByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public KnowledgeDocument create(KnowledgeDocument document) {
        save(document);
        return document;
    }

    public List<KnowledgeDocument> searchByKeyword(Integer projectId, String keyword) {
        return lambdaQuery()
                .eq(KnowledgeDocument::getProjectId, projectId)
                .like(KnowledgeDocument::getTitle, keyword)
                .or()
                .like(KnowledgeDocument::getContent, keyword)
                .list();
    }
}