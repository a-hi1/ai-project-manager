package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("milestone")
public class Milestone {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer projectId;
    private String name;
    private String description;
    private LocalDate dueDate;
    private String status;
    private Integer createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
