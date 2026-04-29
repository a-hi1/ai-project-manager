package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;

public class ProjectExcelVO {
    @ExcelProperty("项目名称")
    private String name;

    @ExcelProperty("项目描述")
    private String description;

    @ExcelProperty("开始日期")
    private String startDate;

    @ExcelProperty("结束日期")
    private String endDate;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("总任务数")
    private Integer totalTasks;

    @ExcelProperty("已完成任务")
    private Integer completedTasks;

    @ExcelProperty("完成率(%)")
    private Double completionRate;

    @ExcelProperty("风险数量")
    private Integer riskCount;

    @ExcelProperty("缺陷数量")
    private Integer bugCount;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTotalTasks() { return totalTasks; }
    public void setTotalTasks(Integer totalTasks) { this.totalTasks = totalTasks; }
    public Integer getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(Integer completedTasks) { this.completedTasks = completedTasks; }
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
    public Integer getRiskCount() { return riskCount; }
    public void setRiskCount(Integer riskCount) { this.riskCount = riskCount; }
    public Integer getBugCount() { return bugCount; }
    public void setBugCount(Integer bugCount) { this.bugCount = bugCount; }
}