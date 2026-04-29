package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_dependency")
public class TaskDependency {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer taskId;
    private Integer dependsOnId;
    private String dependencyType;
    private LocalDateTime createdAt;
}
