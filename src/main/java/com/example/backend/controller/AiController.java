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
    public Map<String, Object> uploadDocument(HttpServletRequest request, @RequestParam("file") MultipartFile file, @RequestParam(value = "projectId", required = false) Integer projectId, @RequestParam(value = "docType", required = false) String docType) {
        try {
            String authHeader = request.getHeader("Authorization");
            System.out.println("Auth Header: " + (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null"));
            
            Integer userId = jwtUtils.getUserIdFromToken(authHeader);
            System.out.println("User ID from token: " + userId);
            
            if (userId == null) {
                return Map.of("success", false, "message", "无法验证用户身份，请重新登录");
            }
            
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            
            System.out.println("Uploading file: " + fileName + ", size: " + file.getSize() + " bytes, type: " + contentType + ", docType: " + docType);
            
            // 使用文档解析器解析内容
            String rawContent = DocumentParser.parseDocument(file);
            String content = sanitizeContent(rawContent);
            
            System.out.println("Parsed content length: " + content.length() + " chars");

            // 先保存文档
            KnowledgeDocument document = new KnowledgeDocument();
            document.setUserId(userId);
            document.setProjectId(projectId);
            document.setFileName(fileName);
            document.setTitle(fileName);
            document.setDocType(docType != null ? docType : "file");
            document.setContent(content);
            document.setCreatedBy(userId);
            document.setCreatedAt(LocalDateTime.now());
            knowledgeDocumentService.save(document);
            
            System.out.println("Document saved with ID: " + document.getId());

            // 调用AI解析需求文档
            String parseResult = aiService.parseRequirementDocument(content);
            System.out.println("AI parse result length: " + parseResult.length());

            return Map.of("success", true, "message", "文档上传并解析成功", "documentId", document.getId(), "fileName", fileName, "data", parseResult);
        } catch (Exception e) {
            System.out.println("===== 上传文件错误 =====");
            e.printStackTrace();
            return Map.of("success", false, "message", "文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理内容，移除NUL字符和其他无效字符
     */
    private String sanitizeContent(String content) {
        if (content == null) {
            return "";
        }
        // 移除NUL字符
        String sanitized = content.replace("\0", "");
        // 替换其他控制字符（保留换行和制表符）
        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B-\\x0C\\x0E-\\x1F]", "");
        return sanitized;
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
            
            System.out.println("===== AI聊天请求开始 =====");
            System.out.println("用户ID: " + userId);
            System.out.println("用户消息: " + userMessage);
            System.out.println("项目ID: " + projectId);
            
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
            
            System.out.println("历史对话数量: " + history.size());
            
            // 构建上下文
            StringBuilder context = new StringBuilder();
            for (AiConversation conv : history) {
                context.append(conv.getRole()).append(": ").append(conv.getMessage()).append("\n");
            }
            
            // 调用AI服务，带上项目上下文
            String aiResponse;
            try {
                System.out.println("开始调用AI服务...");
                if (context.length() > 0) {
                    aiResponse = aiService.chatWithContext(context.toString() + "\nuser: " + userMessage, projectId);
                } else {
                    aiResponse = aiService.chatWithContext(userMessage, projectId);
                }
                System.out.println("AI服务调用成功，响应长度: " + aiResponse.length());
            } catch (Exception e) {
                System.out.println("===== AI服务调用失败 =====");
                System.out.println("错误类型: " + e.getClass().getName());
                System.out.println("错误信息: " + e.getMessage());
                e.printStackTrace();
                aiResponse = "抱歉，AI服务暂时不可用，请稍后再试。错误详情: " + e.getMessage();
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
    public Map<String, Object> addKnowledge(HttpServletRequest request, @RequestBody KnowledgeDocument document) {
        try {
            Integer userId = jwtUtils.getUserIdFromToken(request.getHeader("Authorization"));
            document.setUserId(userId);
            document.setCreatedBy(userId);
            if (document.getCreatedAt() == null) {
                document.setCreatedAt(LocalDateTime.now());
            }
            // 清理内容
            if (document.getContent() != null) {
                document.setContent(sanitizeContent(document.getContent()));
            }
            knowledgeDocumentService.create(document);
            return Map.of("success", true, "message", "知识添加成功");
        } catch (Exception e) {
            e.printStackTrace();
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

    @DeleteMapping("/knowledge/delete/{id}")
    public Map<String, Object> deleteKnowledgeWithPath(@PathVariable Integer id) {
        return deleteKnowledge(id);
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

    @GetMapping("/knowledge/semantic-search")
    public Map<String, Object> semanticSearchKnowledge(@RequestParam(required = false) Integer projectId, 
                                                       @RequestParam String query,
                                                       @RequestParam(defaultValue = "5") int limit) {
        try {
            List<KnowledgeDocument> documents = knowledgeDocumentService.searchBySemantic(projectId, query, limit);
            return Map.of("success", true, "data", documents);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PostMapping("/knowledge/vectorize-all")
    public Map<String, Object> vectorizeAllDocuments() {
        try {
            knowledgeDocumentService.vectorizeAllDocuments();
            return Map.of("success", true, "message", "所有文档向量化完成");
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