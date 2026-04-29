package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project_member")
public class ProjectMember {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("project_id")
    private Integer projectId;
    @TableField("user_id")
    private Integer userId;
    private String role;
    @TableField("joined_at")
    private LocalDateTime joinedAt;
}
