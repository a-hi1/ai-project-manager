package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("deliverable")
public class Deliverable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("project_id")
    private Integer projectId;
    private String name;
    private String description;
    @TableField("file_path")
    private String filePath;
    
    // 文件名字段 - 从 file_path 中提取
    @TableField(exist = false)
    private String fileName;
    
    @TableField("submitted_by")
    private Integer submittedBy;
    @TableField("submitted_at")
    private LocalDateTime submittedAt;
    private String status;
    @TableField("reviewed_by")
    private Integer reviewedBy;
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;
    private String comments;
    
    // 辅助方法：从文件路径中获取文件名
    public String getFileName() {
        if (fileName != null) {
            return fileName;
        }
        if (filePath != null) {
            String[] parts = filePath.split("[\\\\/]");
            return parts[parts.length - 1];
        }
        return null;
    }
}
