package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_document")
public class KnowledgeDocument {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("project_id")
    private Integer projectId;
    private String title;
    private String content;
    @TableField("doc_type")
    private String docType;
    @TableField("file_path")
    private String filePath;
    @TableField("vector_id")
    private String vectorId;
    @TableField("created_by")
    private Integer createdBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
