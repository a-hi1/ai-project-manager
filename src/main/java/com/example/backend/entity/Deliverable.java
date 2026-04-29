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
}
