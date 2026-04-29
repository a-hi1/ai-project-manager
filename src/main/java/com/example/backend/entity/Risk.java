package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("risk")
public class Risk {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("project_id")
    private Integer projectId;
    private String name;
    private String description;
    private Integer probability;
    private Integer impact;
    private String category;
    private String status;
    @TableField("mitigation_plan")
    private String mitigationPlan;
    @TableField("contingency_plan")
    private String contingencyPlan;
    @TableField("identified_by")
    private Integer identifiedBy;
    @TableField("identified_at")
    private LocalDate identifiedAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getProbability() { return probability; }
    public void setProbability(Integer probability) { this.probability = probability; }
    public Integer getImpact() { return impact; }
    public void setImpact(Integer impact) { this.impact = impact; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMitigationPlan() { return mitigationPlan; }
    public void setMitigationPlan(String mitigationPlan) { this.mitigationPlan = mitigationPlan; }
    public String getContingencyPlan() { return contingencyPlan; }
    public void setContingencyPlan(String contingencyPlan) { this.contingencyPlan = contingencyPlan; }
    public Integer getIdentifiedBy() { return identifiedBy; }
    public void setIdentifiedBy(Integer identifiedBy) { this.identifiedBy = identifiedBy; }
    public LocalDate getIdentifiedAt() { return identifiedAt; }
    public void setIdentifiedAt(LocalDate identifiedAt) { this.identifiedAt = identifiedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}