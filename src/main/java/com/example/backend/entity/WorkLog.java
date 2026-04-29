package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("work_log")
public class WorkLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("task_id")
    private Integer taskId;
    @TableField("user_id")
    private Integer userId;
    private BigDecimal hours;
    @TableField("work_date")
    private LocalDate workDate;
    private String description;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
