package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("project_retrospective")
public class ProjectRetrospective {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("project_id")
    private Integer projectId;
    private String summary;
    @TableField("duration_days")
    private Integer durationDays;
    @TableField("total_tasks")
    private Integer totalTasks;
    @TableField("completed_tasks")
    private Integer completedTasks;
    @TableField("task_completion_rate")
    private BigDecimal taskCompletionRate;
    @TableField("total_risks")
    private Integer totalRisks;
    @TableField("resolved_risks")
    private Integer resolvedRisks;
    @TableField("total_bugs")
    private Integer totalBugs;
    @TableField("resolved_bugs")
    private Integer resolvedBugs;
    @TableField("total_hours")
    private BigDecimal totalHours;
    @TableField("key_achievements")
    private String keyAchievements;
    @TableField("key_challenges")
    private String keyChallenges;
    @TableField("lessons_learned")
    private String lessonsLearned;
    private String recommendations;
    @TableField("created_by")
    private Integer createdBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
