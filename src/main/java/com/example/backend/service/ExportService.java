package com.example.backend.service;

import com.alibaba.excel.EasyExcel;
import com.example.backend.entity.ProjectExcelVO;
import com.example.backend.entity.ProjectRetrospective;
import com.example.backend.entity.TaskExcelVO;
import com.example.backend.mapper.ProjectRetrospectiveMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExportService {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectRetrospectiveMapper retrospectiveMapper;

    public void exportTasks(Integer projectId, HttpServletResponse response) throws IOException {
        List<TaskExcelVO> taskVOList = new ArrayList<>();
        var tasks = taskService.getTasksByProjectIdList(projectId);

        for (var task : tasks) {
            TaskExcelVO vo = new TaskExcelVO();
            vo.setName(task.getName());
            vo.setDescription(task.getDescription());
            vo.setStartDate(task.getStartDate() != null ? task.getStartDate().toString() : "");
            vo.setEndDate(task.getEndDate() != null ? task.getEndDate().toString() : "");
            vo.setDuration(task.getDuration());
            vo.setProgress(task.getProgress());
            vo.setPriority(task.getPriority());
            vo.setStatus(getStatusName(task.getStatus()));
            taskVOList.add(vo);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("任务列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), TaskExcelVO.class)
            .sheet("任务列表")
            .doWrite(taskVOList);
    }

    public void exportProjects(HttpServletResponse response) throws IOException {
        List<ProjectExcelVO> projectVOList = new ArrayList<>();
        var projects = projectService.findAll();

        for (var project : projects) {
            ProjectExcelVO vo = new ProjectExcelVO();
            vo.setName(project.getName());
            vo.setDescription(project.getDescription());
            vo.setStartDate(project.getStartDate() != null ? project.getStartDate().toString() : "");
            vo.setEndDate(project.getEndDate() != null ? project.getEndDate().toString() : "");
            vo.setStatus(getStatusName(project.getStatus()));

            ProjectService.ProjectStats stats = projectService.getProjectStats(project.getId());
            vo.setTotalTasks(stats.getTotalTasks());
            vo.setCompletedTasks(stats.getCompletedTasks());
            vo.setCompletionRate(stats.getCompletionRate());
            vo.setRiskCount(stats.getTotalRisks());
            vo.setBugCount(stats.getTotalBugs());
            projectVOList.add(vo);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("项目报表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), ProjectExcelVO.class)
            .sheet("项目报表")
            .doWrite(projectVOList);
    }

    public void exportRetrospective(Integer projectId, HttpServletResponse response) throws IOException {
        List<ProjectRetrospective> retrospectives = retrospectiveMapper.findByProjectId(projectId);
        if (retrospectives == null || retrospectives.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"复盘报告不存在\"}");
            return;
        }

        ProjectRetrospective retrospective = retrospectives.get(0);
        List<ProjectRetrospective> data = List.of(retrospective);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("复盘报告", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), ProjectRetrospective.class)
            .sheet("复盘报告")
            .doWrite(data);
    }

    private String getStatusName(String status) {
        if (status == null) return "";
        return switch (status) {
            case "pending" -> "待启动";
            case "in_progress" -> "进行中";
            case "completed" -> "已完成";
            case "todo" -> "待处理";
            case "done" -> "已完成";
            case "review" -> "待审核";
            default -> status;
        };
    }
}
