package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("change_request")
public class ChangeRequest {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("project_id")
    private Integer projectId;
    private String title;
    private String description;
    private String reason;
    private String impact;
    private String status;
    @TableField("requester_id")
    private Integer requesterId;
    @TableField("reviewer_id")
    private Integer reviewerId;
    @TableField("reviewed_at")
    private LocalDateTime reviewedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
