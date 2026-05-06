package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.KnowledgeDocument;
import com.example.backend.mapper.KnowledgeDocumentMapper;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KnowledgeDocumentService extends ServiceImpl<KnowledgeDocumentMapper, KnowledgeDocument> {
    
    @Autowired
    private EmbeddingModel embeddingModel;
    
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    public List<KnowledgeDocument> getByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public KnowledgeDocument create(KnowledgeDocument document) {
        save(document);
        
        if (document.getContent() != null && !document.getContent().isEmpty()) {
            vectorizeDocument(document);
        }
        
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

    public void vectorizeDocument(KnowledgeDocument document) {
        try {
            String text = (document.getTitle() != null ? document.getTitle() + "\n" : "") + 
                         (document.getContent() != null ? document.getContent() : "");
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("documentId", String.valueOf(document.getId()));
            metadata.put("projectId", String.valueOf(document.getProjectId()));
            if (document.getTitle() != null) {
                metadata.put("title", document.getTitle());
            }
            if (document.getDocType() != null) {
                metadata.put("docType", document.getDocType());
            }
            
            // 创建 Metadata 对象
            Metadata metadataObj = new Metadata();
            metadata.forEach(metadataObj::put);
            
            TextSegment segment = TextSegment.from(text, metadataObj);
            Response<Embedding> embeddingResponse = embeddingModel.embed(segment);
            String vectorId = embeddingStore.add(embeddingResponse.content(), segment);
            
            document.setVectorId(vectorId);
            updateById(document);
        } catch (Exception e) {
            System.err.println("向量化文档失败: " + e.getMessage());
        }
    }

    public List<KnowledgeDocument> searchBySemantic(Integer projectId, String query, int limit) {
        List<KnowledgeDocument> results = new ArrayList<>();
        try {
            Response<Embedding> queryEmbedding = embeddingModel.embed(query);
            
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding.content())
                    .maxResults(limit)
                    .minScore(0.5)
                    .build();
            
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(request);
            
            for (EmbeddingMatch<TextSegment> match : searchResult.matches()) {
                TextSegment segment = match.embedded();
                String docIdStr = segment.metadata().getString("documentId");
                String projectIdStr = segment.metadata().getString("projectId");
                
                if (docIdStr != null && (projectId == null || projectIdStr.equals(String.valueOf(projectId)))) {
                    KnowledgeDocument doc = getById(Integer.parseInt(docIdStr));
                    if (doc != null) {
                        results.add(doc);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("语义搜索失败: " + e.getMessage());
        }
        return results;
    }

    public void vectorizeAllDocuments() {
        List<KnowledgeDocument> docs = list();
        for (KnowledgeDocument doc : docs) {
            if (doc.getVectorId() == null || doc.getVectorId().isEmpty()) {
                vectorizeDocument(doc);
            }
        }
    }
}