package com.example.backend.controller;

import com.example.backend.annotation.RequirePermission;
import com.example.backend.entity.Deliverable;
import com.example.backend.service.DeliverableService;
import com.example.backend.utils.JwtUtils;
import com.example.backend.utils.PermissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliverable")
public class DeliverableController {
    @Autowired
    private DeliverableService deliverableService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    // 使用绝对路径，在项目根目录下创建 uploads 文件夹
    private static final String UPLOAD_DIR;
    
    static {
        // 获取项目根目录
        String projectDir = System.getProperty("user.dir");
        UPLOAD_DIR = projectDir + File.separator + "uploads" + File.separator + "deliverables";
        System.out.println("文件上传目录: " + UPLOAD_DIR);
    }

    @GetMapping("/project/{projectId}")
    public Map<String, Object> getByProjectId(@PathVariable Integer projectId) {
        List<Deliverable> deliverables = deliverableService.getByProjectId(projectId);
        return Map.of("success", true, "data", deliverables);
    }

    @PostMapping("/create")
    @RequirePermission(PermissionUtils.PERM_DOCUMENT_MANAGE)
    public Map<String, Object> create(@RequestBody Deliverable deliverable) {
        try {
            Deliverable created = deliverableService.create(deliverable);
            return Map.of("success", true, "data", created);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
    
    @PostMapping("/upload")
    @RequirePermission(PermissionUtils.PERM_DOCUMENT_MANAGE)
    public Map<String, Object> uploadDeliverable(HttpServletRequest request,
                                                  @RequestParam("file") MultipartFile file,
                                                  @RequestParam("projectId") Integer projectId,
                                                  @RequestParam("name") String name,
                                                  @RequestParam(value = "description", required = false) String description) {
        try {
            System.out.println("=== 开始上传交付物 ===");
            System.out.println("文件名: " + file.getOriginalFilename());
            System.out.println("文件大小: " + file.getSize());
            System.out.println("项目ID: " + projectId);
            
            Integer userId = jwtUtils.getUserIdFromToken(request.getHeader("Authorization"));
            System.out.println("用户ID: " + userId);
            
            if (userId == null) {
                return Map.of("success", false, "message", "无法验证用户身份，请重新登录");
            }
            
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            
            System.out.println("新文件名: " + newFileName);
            
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("创建目录: " + created);
            }
            
            File destFile = new File(uploadDir, newFileName);
            file.transferTo(destFile);
            System.out.println("文件已保存到: " + destFile.getAbsolutePath());
            
            String filePath = UPLOAD_DIR + File.separator + newFileName;
            
            // 先创建并保存基本信息，避免字段问题
            Deliverable deliverable = new Deliverable();
            deliverable.setProjectId(projectId);
            deliverable.setName(name);
            deliverable.setDescription(description);
            deliverable.setFilePath(filePath);
            // 先不设置 fileName，避免数据库字段问题
            deliverable.setSubmittedBy(userId);
            deliverable.setSubmittedAt(LocalDateTime.now());
            deliverable.setStatus("pending");
            
            Deliverable created = deliverableService.create(deliverable);
            System.out.println("交付物已保存，ID: " + created.getId());
            
            // 尝试单独更新 fileName 字段
            try {
                created.setFileName(originalFileName);
                deliverableService.updateById(created);
            } catch (Exception e) {
                System.out.println("更新 fileName 失败（可能字段不存在），继续执行: " + e.getMessage());
            }
            
            return Map.of("success", true, "data", created, "message", "交付物上传成功");
        } catch (Exception e) {
            System.err.println("=== 上传失败 ===");
            e.printStackTrace();
            return Map.of("success", false, "message", "文件上传失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadDeliverable(@RequestParam("filePath") String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            String fileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.toString());
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.length())
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/update")
    @RequirePermission(PermissionUtils.PERM_DOCUMENT_MANAGE)
    public Map<String, Object> update(@RequestBody Deliverable deliverable) {
        try {
            deliverableService.updateById(deliverable);
            return Map.of("success", true, "data", deliverable);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/approve/{id}")
    @RequirePermission(PermissionUtils.PERM_DOCUMENT_MANAGE)
    public Map<String, Object> approve(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        try {
            Deliverable deliverable = deliverableService.getById(id);
            if (deliverable != null) {
                deliverable.setStatus("approved");
                deliverable.setReviewedBy((Integer) data.get("reviewerId"));
                deliverable.setReviewedAt(LocalDateTime.now());
                deliverableService.updateById(deliverable);
                return Map.of("success", true, "data", deliverable);
            }
            return Map.of("success", false, "message", "交付物不存在");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    @PutMapping("/reject/{id}")
    @RequirePermission(PermissionUtils.PERM_DOCUMENT_MANAGE)
    public Map<String, Object> reject(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        try {
            Deliverable deliverable = deliverableService.getById(id);
            if (deliverable != null) {
                deliverable.setStatus("rejected");
                deliverable.setReviewedBy((Integer) data.get("reviewerId"));
                deliverable.setReviewedAt(LocalDateTime.now());
                deliverable.setComments((String) data.get("comments"));
                deliverableService.updateById(deliverable);
                return Map.of("success", true, "data", deliverable);
            }
            return Map.of("success", false, "message", "交付物不存在");
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }
}
