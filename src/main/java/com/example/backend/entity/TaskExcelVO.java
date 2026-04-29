package com.example.backend.entity;

import com.alibaba.excel.annotation.ExcelProperty;

public class TaskExcelVO {
    @ExcelProperty("任务名称")
    private String name;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("负责人")
    private String assignee;

    @ExcelProperty("开始日期")
    private String startDate;

    @ExcelProperty("结束日期")
    private String endDate;

    @ExcelProperty("工期(天)")
    private Integer duration;

    @ExcelProperty("进度(%)")
    private Integer progress;

    @ExcelProperty("优先级")
    private String priority;

    @ExcelProperty("状态")
    private String status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}