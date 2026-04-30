package com.example.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.AiConversation;
import com.example.backend.entity.KnowledgeDocument;
import com.example.backend.entity.Task;
import com.example.backend.entity.Risk;
import com.example.backend.entity.Bug;
import com.example.backend.service.*;
import com.example.backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    @Autowired
    private AiService aiService;

    @Autowired
    private KnowledgeDocumentService knowledgeDocumentService;

    @Autowired
    private AiConversationService aiConversationService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RiskService riskService;

    @Autowired
    private BugService bugService;

    @Autowired
    private WorkLogService workLogService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/upload-document")
    public Map<String, Object> uploadDocument(HttpServletRequest request, @RequestParam("file") MultipartFile file, @RequestParam(value = "projectId", required = false) Integer projectId) {
        try {
            Integer userId = jwtUtils.getUserIdFromToken(request.getHeader("Authorization"));
            String fileName = file.getOriginalFilename();
            String content = new String(file.getBytes());

            // 保存文档到知识库
            KnowledgeDocument document = new KnowledgeDocument();
            document.setUserId(userId);
            document.setProjectId(projectId);
            document.setFileName(fileName);
            document.setContent(content);
            document.setCreatedAt(LocalDateTime.now());
            knowledgeDocumentService.create(document);

            return Map.of("success", true, "message", "文档上传成功", "documentId", document.getId(), "fileName", fileName);
        } catch (Exception e) {
            return Map.of("success", false, "message", "文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/parse-document")
    public Map<String, Object> parseDocument(@RequestBody Map<String, Object> data) {
        try {
            String content = (String) data.get("content");
            String result = aiService.parseRequirementDocument(content);
            return Map.of("success", true, "data", result);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/split-task")
    public Map<String, Object> splitTask(@RequestBody Map<String, Object> data) {
        try {
            String requirement = (String) data.get("requirement");
            String result = aiService.splitTask(requirement);
            return Map.of("success", true, "data", result);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/chat")
    public Map<String, Object> chat(HttpServletRequest request, @RequestBody Map<String, Object> data) {
        try {
            Integer userId = jwtUtils.getUserIdFromToken(request.getHeader("Authorization"));
            String userMessage = (String) data.get("message");
            Integer projectId = data.get("projectId") != null ? Integer.valueOf(data.get("projectId").toString()) : null;
            
            // 保存用户消息
            AiConversation userConversation = new AiConversation();
            userConversation.setUserId(userId);
            userConversation.setProjectId(projectId);
            userConversation.setRole("user");
            userConversation.setMessage(userMessage);
            userConversation.setCreatedAt(LocalDateTime.now());
            aiConversationService.saveMessage(userConversation);
            
            // 获取历史对话作为上下文
            LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiConversation::getUserId, userId);
            if (projectId != null) {
                wrapper.eq(AiConversation::getProjectId, projectId);
            }
            wrapper.orderByAsc(AiConversation::getCreatedAt);
            wrapper.last("LIMIT 20");
            List<AiConversation> history = aiConversationService.list(wrapper);
            
            // 构建上下文
            StringBuilder context = new StringBuilder();
            for (AiConversation conv : history) {
                context.append(conv.getRole()).append(": ").append(conv.getMessage()).append("\n");
            }
            
            // 调用AI服务，带上项目上下文
            String aiResponse;
            try {
                if (context.length() > 0) {
                    aiResponse = aiService.chatWithContext(context.toString() + "\nuser: " + userMessage, projectId);
                } else {
                    aiResponse = aiService.chatWithContext(userMessage, projectId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                aiResponse = "抱歉，AI服务暂时不可用，请稍后再试。";
            }

            // 保存AI消息
            AiConversation aiConversation = new AiConversation();
            aiConversation.setUserId(userId);
            aiConversation.setProjectId(projectId);
            aiConversation.setRole("ai");
            aiConversation.setMessage(aiResponse);
            aiConversation.setCreatedAt(LocalDateTime.now());
            aiConversationService.saveMessage(aiConversation);

            return Map.of("success", true, "data", aiResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/conversation/history")
    public Map<String, Object> getConversationHistory(HttpServletRequest request, @RequestParam(value = "projectId", required = false) Integer projectId) {
        try {
            Integer userId = jwtUtils.getUserIdFromToken(request.getHeader("Authorization"));
            LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiConversation::getUserId, userId);
            if (projectId != null) {
                wrapper.eq(AiConversation::getProjectId, projectId);
            }
            wrapper.orderByAsc(AiConversation::getCreatedAt);
            List<AiConversation> conversations = aiConversationService.list(wrapper);
            return Map.of("success", true, "data", conversations);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/conversation/{projectId}")
    public Map<String, Object> getConversation(@PathVariable Integer projectId) {
        try {
            List<AiConversation> conversations = aiConversationService.getByProjectId(projectId);
            return Map.of("success", true, "data", conversations);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/conversation/clear")
    public Map<String, Object> clearConversation(HttpServletRequest request, @RequestParam(value = "projectId", required = false) Integer projectId) {
        try {
            Integer userId = jwtUtils.getUserIdFromToken(request.getHeader("Authorization"));
            LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiConversation::getUserId, userId);
            if (projectId != null) {
                wrapper.eq(AiConversation::getProjectId, projectId);
            }
            aiConversationService.remove(wrapper);
            return Map.of("success", true, "message", "对话历史已清空");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/knowledge/{projectId}")
    public Map<String, Object> getKnowledge(@PathVariable Integer projectId) {
        try {
            List<KnowledgeDocument> documents = knowledgeDocumentService.getByProjectId(projectId);
            return Map.of("success", true, "data", documents);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/knowledge/list")
    public Map<String, Object> getKnowledgeList(HttpServletRequest request) {
        try {
            Integer userId = jwtUtils.getUserIdFromToken(request.getHeader("Authorization"));
            LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KnowledgeDocument::getUserId, userId);
            wrapper.orderByDesc(KnowledgeDocument::getCreatedAt);
            List<KnowledgeDocument> documents = knowledgeDocumentService.list(wrapper);
            return Map.of("success", true, "data", documents);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/knowledge/add")
    public Map<String, Object> addKnowledge(@RequestBody KnowledgeDocument document) {
        try {
            knowledgeDocumentService.create(document);
            return Map.of("success", true, "message", "知识添加成功");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @DeleteMapping("/knowledge/{id}")
    public Map<String, Object> deleteKnowledge(@PathVariable Integer id) {
        try {
            knowledgeDocumentService.removeById(id);
            return Map.of("success", true, "message", "文档删除成功");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/knowledge/search/{projectId}")
    public Map<String, Object> searchKnowledge(@PathVariable Integer projectId, @RequestParam String keyword) {
        try {
            List<KnowledgeDocument> documents = knowledgeDocumentService.searchByKeyword(projectId, keyword);
            return Map.of("success", true, "data", documents);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/generate-retrospective-summary")
    public Map<String, Object> generateRetrospectiveSummary(@RequestBody Map<String, Object> data) {
        try {
            Integer projectId = (Integer) data.get("projectId");

            List<Task> tasks = taskService.getTasksByProjectIdList(projectId);
            List<Risk> risks = riskService.getRisksByProjectId(projectId);
            List<Bug> bugs = bugService.getByProjectId(projectId);

            int totalTasks = tasks.size();
            int completedTasks = (int) tasks.stream().filter(t -> "done".equals(t.getStatus())).count();
            int totalRisks = risks.size();
            int resolvedRisks = (int) risks.stream().filter(r -> "resolved".equals(r.getStatus())).count();
            int totalBugs = bugs.size();
            int resolvedBugs = (int) bugs.stream().filter(b -> "resolved".equals(b.getStatus())).count();

            String prompt = """
                请基于以下项目数据，生成一份项目复盘总结：

                任务统计：
                - 总任务数：%d
                - 已完成任务：%d
                - 任务完成率：%.2f%%

                风险统计：
                - 总风险数：%d
                - 已解决风险：%d

                缺陷统计：
                - 总缺陷数：%d
                - 已修复缺陷：%d

                请生成一份简洁的项目复盘总结，包括：主要成就、遇到的问题、经验教训。
                """.formatted(
                    totalTasks, completedTasks,
                    totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0,
                    totalRisks, resolvedRisks,
                    totalBugs, resolvedBugs
            );

            String summary = aiService.chat(prompt);
            return Map.of("success", true, "data", summary);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/predict-progress/{projectId}")
    public Map<String, Object> predictProgress(@PathVariable Integer projectId) {
        try {
            AiService.ProgressPrediction prediction = aiService.predictProgress(projectId);
            return Map.of("success", true, "data", prediction);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/suggest-plan")
    public Map<String, Object> suggestProjectPlan(@RequestBody Map<String, Object> data) {
        try {
            String projectName = (String) data.get("projectName");
            String coreGoal = (String) data.get("coreGoal");
            AiService.SmartFormFill suggestion = aiService.suggestProjectPlan(projectName, coreGoal);
            return Map.of("success", true, "data", suggestion);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @GetMapping("/project-context/{projectId}")
    public Map<String, Object> getProjectContext(@PathVariable Integer projectId) {
        try {
            String context = aiService.getProjectContext(projectId);
            return Map.of("success", true, "data", context);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/analyze-health/{projectId}")
    public Map<String, Object> analyzeProjectHealth(@PathVariable Integer projectId) {
        try {
            String analysis = aiService.analyzeProjectHealth(projectId);
            return Map.of("success", true, "data", analysis);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/daily-report/{projectId}")
    public Map<String, Object> generateDailyReport(@PathVariable Integer projectId) {
        try {
            String report = aiService.generateDailyReport(projectId);
            return Map.of("success", true, "data", report);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/suggest-tasks/{projectId}")
    public Map<String, Object> suggestTasks(@PathVariable Integer projectId) {
        try {
            String suggestions = aiService.suggestTasks(projectId);
            return Map.of("success", true, "data", suggestions);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/summarize/{projectId}")
    public Map<String, Object> summarizeProject(@PathVariable Integer projectId) {
        try {
            String summary = aiService.summarizeProject(projectId);
            return Map.of("success", true, "data", summary);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}