package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task_status_log")
public class TaskStatusLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer taskId;
    private String fromStatus;
    private String toStatus;
    private Integer changedBy;
    private String comment;
    private LocalDateTime changedAt;
}
