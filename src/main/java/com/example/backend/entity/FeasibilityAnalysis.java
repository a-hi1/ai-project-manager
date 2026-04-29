package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feasibility_analysis")
public class FeasibilityAnalysis {
    private Integer id;
    private Integer projectId;
    private String title;
    @TableField("technical_feasibility")
    private String technicalFeasibility;
    @TableField("economic_feasibility")
    private String economicFeasibility;
    @TableField("market_feasibility")
    private String marketFeasibility;
    @TableField("risk_assessment")
    private String riskAssessment;
    @TableField("conclusion")
    private String conclusion;
    @TableField("creator_id")
    private Integer creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTechnicalFeasibility() {
        return technicalFeasibility;
    }

    public void setTechnicalFeasibility(String technicalFeasibility) {
        this.technicalFeasibility = technicalFeasibility;
    }

    public String getEconomicFeasibility() {
        return economicFeasibility;
    }

    public void setEconomicFeasibility(String economicFeasibility) {
        this.economicFeasibility = economicFeasibility;
    }

    public String getMarketFeasibility() {
        return marketFeasibility;
    }

    public void setMarketFeasibility(String marketFeasibility) {
        this.marketFeasibility = marketFeasibility;
    }

    public String getRiskAssessment() {
        return riskAssessment;
    }

    public void setRiskAssessment(String riskAssessment) {
        this.riskAssessment = riskAssessment;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
