package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.DocumentArchive;
import com.example.backend.mapper.DocumentArchiveMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentArchiveService extends ServiceImpl<DocumentArchiveMapper, DocumentArchive> {
    public List<DocumentArchive> getByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public DocumentArchive create(DocumentArchive documentArchive) {
        save(documentArchive);
        return documentArchive;
    }

    public void archiveProjectDocuments(Integer projectId, Integer archivedBy) {
        DocumentArchive archive = new DocumentArchive();
        archive.setProjectId(projectId);
        archive.setTitle("项目文档归档");
        archive.setDocType("project_archive");
        archive.setArchivedBy(archivedBy);
        archive.setContent("项目结束阶段自动归档");
        create(archive);
    }

    public void deleteByProjectId(Integer projectId) {
        List<DocumentArchive> archives = getByProjectId(projectId);
        for (DocumentArchive archive : archives) {
            removeById(archive.getId());
        }
    }
}