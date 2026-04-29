package com.example.backend.service;

import com.example.backend.entity.Task;
import com.example.backend.mapper.TaskMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiService {
    private final ChatLanguageModel chatModel;
    private final TaskMapper taskMapper;

    @Autowired
    public AiService(ChatLanguageModel chatModel, TaskMapper taskMapper) {
        this.chatModel = chatModel;
        this.taskMapper = taskMapper;
    }

    public String chat(String message) {
        return chatModel.generate(message);
    }

    public String chatStream(String message) {
        return chatModel.generate(message);
    }

    public String parseRequirementDocument(String content) {
        String prompt = """
            你是一个需求分析专家。请分析以下需求文档，提取功能点、业务规则，并检测潜在的需求冲突。

            需求文档内容：
            %s

            请以JSON格式返回，格式如下：
            {
                "功能点": ["功能点1", "功能点2", ...],
                "业务规则": ["规则1", "规则2", ...],
                "需求冲突": ["冲突1", "冲突2", ...],
                "优化建议": ["建议1", "建议2", ...]
            }
            """.formatted(content);

        return chatModel.generate(prompt);
    }

    public String splitTask(String requirement) {
        String prompt = """
            你是一个项目经理。请根据以下需求，自动拆分成WBS任务结构，识别任务间的依赖关系。

            需求内容：
            %s

            请以JSON格式返回，格式如下：
            {
                "任务列表": [
                    {
                        "任务名称": "任务1",
                        "描述": "任务描述",
                        "工期": 3,
                        "前置任务": []
                    },
                    {
                        "任务名称": "任务2",
                        "描述": "任务描述",
                        "工期": 2,
                        "前置任务": ["任务1"]
                    }
                ]
            }
            """.formatted(requirement);

        return chatModel.generate(prompt);
    }

    public String analyzeProjectStatus(Integer projectId) {
        String prompt = """
            请分析以下项目的状态数据，包括任务进度、风险情况、工时统计等。

            项目ID：%d

            请给出：
            1. 项目整体健康状况评估
            2. 当前面临的主要风险
            3. 建议的改进措施
            """.formatted(projectId);

        return chatModel.generate(prompt);
    }

    public String answerQuestion(String question, String context) {
        String prompt = """
            基于以下上下文信息，回答用户的问题。如果上下文中没有相关信息，请说明无法回答。

            上下文信息：
            %s

            用户问题：
            %s
            """.formatted(context, question);

        return chatModel.generate(prompt);
    }

    public ProgressPrediction predictProgress(Integer projectId) {
        List<Task> tasks = taskMapper.findByProjectId(projectId);
        if (tasks.isEmpty()) {
            return new ProgressPrediction(0, 0, 0.0, LocalDate.now(), LocalDate.now(), "无任务数据", 0.0);
        }

        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(t -> "done".equals(t.getStatus())).count();
        int inProgressTasks = (int) tasks.stream().filter(t -> "in_progress".equals(t.getStatus())).count();

        long totalDuration = tasks.stream()
            .filter(t -> t.getDuration() != null)
            .mapToLong(Task::getDuration)
            .sum();

        long completedDuration = tasks.stream()
            .filter(t -> "done".equals(t.getStatus()) && t.getDuration() != null)
            .mapToLong(Task::getDuration)
            .sum();

        double avgProgress = totalDuration > 0 ? (double) completedDuration / totalDuration * 100 : 0;

        LocalDate now = LocalDate.now();
        long avgDaysPerTask = totalTasks > 0 ? totalDuration / totalTasks : 1;
        long remainingDays = (long) ((totalDuration - completedDuration) / (completedTasks > 0 ? (double) completedDuration / completedTasks : 1) * avgDaysPerTask);
        LocalDate predictedEndDate = now.plusDays(Math.max(0, remainingDays));

        Task latestTask = tasks.stream()
            .filter(t -> t.getEndDate() != null && !"done".equals(t.getStatus()))
            .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
            .findFirst()
            .orElse(null);

        LocalDate originalEndDate = latestTask != null ? latestTask.getEndDate() : now.plusDays(totalDuration);

        long daysUntilDeadline = ChronoUnit.DAYS.between(now, originalEndDate);
        long daysUntilPredicted = ChronoUnit.DAYS.between(now, predictedEndDate);

        double onTimeProbability;
        String riskAssessment;
        if (daysUntilPredicted <= daysUntilDeadline) {
            onTimeProbability = 0.85 + (daysUntilDeadline - daysUntilPredicted) * 0.01;
            riskAssessment = "项目进度正常，预计能按时完成";
        } else {
            onTimeProbability = Math.max(0.1, 0.5 - (daysUntilPredicted - daysUntilDeadline) * 0.05);
            riskAssessment = "项目存在延期风险，预计延期 " + (daysUntilPredicted - daysUntilDeadline) + " 天";
        }
        onTimeProbability = Math.min(0.99, onTimeProbability);

        return new ProgressPrediction(
            totalTasks,
            completedTasks,
            avgProgress,
            originalEndDate,
            predictedEndDate,
            riskAssessment,
            onTimeProbability
        );
    }

    public SmartFormFill suggestProjectPlan(String projectName, String coreGoal) {
        String prompt = """
            你是一个资深项目经理。根据以下项目信息，推荐合适的项目计划。

            项目名称：%s
            核心目标：%s

            请以JSON格式返回，格式如下：
            {
                "建议周期": {
                    "开始日期": "建议的开始日期（YYYY-MM-DD格式，距今7天后）",
                    "结束日期": "建议的结束日期（YYYY-MM-DD格式）",
                    "总工期": 月数
                },
                "团队配置": {
                    "项目经理": 1,
                    "开发人员": 数量,
                    "测试人员": 数量,
                    "其他角色": ["角色1:数量", ...]
                },
                "初始任务": [
                    {
                        "任务名称": "任务1",
                        "工期": 天数,
                        "描述": "任务描述"
                    }
                ]
            }
            项目周期建议：
            - 小型项目（核心功能单一）：2-3个月
            - 中型项目（多功能系统）：3-6个月
            - 大型项目（复杂企业系统）：6-12个月

            团队配置建议：
            - 每3个开发人员配1个测试人员
            - 项目经理1人
            - 根据工期调整人数

            请直接返回JSON，不要有其他文字。
            """.formatted(projectName, coreGoal);

        String aiResponse = chatModel.generate(prompt);
        return new SmartFormFill(aiResponse);
    }

    public record ProgressPrediction(
        int totalTasks,
        int completedTasks,
        double overallProgress,
        LocalDate originalEndDate,
        LocalDate predictedEndDate,
        String riskAssessment,
        double onTimeProbability
    ) {}

    public record SmartFormFill(String aiSuggestion) {}
}
