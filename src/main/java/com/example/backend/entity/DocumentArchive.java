package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("document_archive")
public class DocumentArchive {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("project_id")
    private Integer projectId;
    private String title;
    private String content;
    @TableField("doc_type")
    private String docType;
    @TableField("source_table")
    private String sourceTable;
    @TableField("source_id")
    private Integer sourceId;
    @TableField("archived_at")
    private LocalDateTime archivedAt;
    @TableField("archived_by")
    private Integer archivedBy;
}
