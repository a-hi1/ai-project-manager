package com.example.backend.utils;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class PermissionUtils {
    
    @Autowired
    private UserService userService;
    
    // 权限代码常量
    public static final String PERM_USER_MANAGE = "user:manage";
    public static final String PERM_USER_VIEW = "user:view";
    public static final String PERM_PROJECT_CREATE = "project:create";
    public static final String PERM_PROJECT_MANAGE = "project:manage";
    public static final String PERM_TASK_CREATE = "task:create";
    public static final String PERM_TASK_MANAGE = "task:manage";
    public static final String PERM_REPORT_VIEW = "report:view";
    public static final String PERM_REPORT_GENERATE = "report:generate";
    public static final String PERM_RISK_MANAGE = "risk:manage";
    public static final String PERM_BUG_MANAGE = "bug:manage";
    public static final String PERM_CHANGE_MANAGE = "change:manage";
    public static final String PERM_DOCUMENT_MANAGE = "document:manage";
    public static final String PERM_AI_USE = "ai:use";
    public static final String PERM_LOG_VIEW = "log:view";
    
    // 验证用户是否有指定权限
    public boolean hasPermission(HttpServletRequest request, String permission) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            return false;
        }
        
        User user = userService.findByUsername(username);
        if (user == null) {
            return false;
        }
        
        // 管理员拥有所有权限
        if (user.getRoleId() == 1) {
            return true;
        }
        
        // 根据角色验证权限
        return checkRolePermission(user.getRoleId(), permission);
    }
    
    // 根据角色ID验证权限
    public boolean checkRolePermission(Integer roleId, String permission) {
        return switch (roleId) {
            case 1 -> true; // admin - 管理员拥有所有权限
            case 2 -> checkPmPermission(permission); // pm - 项目经理
            case 3 -> checkDeveloperPermission(permission); // developer - 开发者
            case 4 -> checkTesterPermission(permission); // tester - 测试工程师
            case 5 -> checkProductPermission(permission); // product - 产品经理
            case 6 -> checkDesignerPermission(permission); // designer - UI设计师
            case 7 -> checkGuestPermission(permission); // guest - 访客
            default -> false;
        };
    }
    
    // 项目经理权限检查
    private boolean checkPmPermission(String permission) {
        return switch (permission) {
            case PERM_USER_VIEW, PERM_PROJECT_CREATE, PERM_PROJECT_MANAGE, PERM_TASK_CREATE, PERM_TASK_MANAGE,
                PERM_REPORT_VIEW, PERM_REPORT_GENERATE, PERM_RISK_MANAGE, PERM_BUG_MANAGE,
                PERM_CHANGE_MANAGE, PERM_DOCUMENT_MANAGE, PERM_AI_USE, PERM_LOG_VIEW -> true;
            default -> false;
        };
    }
    
    // 开发者权限检查
    private boolean checkDeveloperPermission(String permission) {
        return switch (permission) {
            case PERM_USER_MANAGE, PERM_PROJECT_CREATE, PERM_PROJECT_MANAGE, PERM_TASK_CREATE,
                PERM_REPORT_GENERATE, PERM_RISK_MANAGE, PERM_CHANGE_MANAGE -> false;
            case PERM_USER_VIEW, PERM_TASK_MANAGE, PERM_REPORT_VIEW, PERM_BUG_MANAGE, 
                PERM_DOCUMENT_MANAGE, PERM_AI_USE -> true;
            default -> false;
        };
    }
    
    // 测试工程师权限检查
    private boolean checkTesterPermission(String permission) {
        return switch (permission) {
            case PERM_USER_MANAGE, PERM_PROJECT_CREATE, PERM_PROJECT_MANAGE, PERM_TASK_CREATE,
                PERM_TASK_MANAGE, PERM_REPORT_GENERATE, PERM_RISK_MANAGE, PERM_CHANGE_MANAGE,
                PERM_DOCUMENT_MANAGE, PERM_AI_USE -> false;
            case PERM_USER_VIEW, PERM_REPORT_VIEW, PERM_BUG_MANAGE -> true;
            default -> false;
        };
    }
    
    // 产品经理权限检查
    private boolean checkProductPermission(String permission) {
        return switch (permission) {
            case PERM_USER_MANAGE, PERM_PROJECT_CREATE, PERM_PROJECT_MANAGE, PERM_TASK_CREATE,
                PERM_TASK_MANAGE, PERM_REPORT_GENERATE, PERM_RISK_MANAGE, PERM_BUG_MANAGE,
                PERM_CHANGE_MANAGE, PERM_DOCUMENT_MANAGE -> false;
            case PERM_USER_VIEW, PERM_REPORT_VIEW, PERM_AI_USE -> true;
            default -> false;
        };
    }
    
    // UI设计师权限检查
    private boolean checkDesignerPermission(String permission) {
        return switch (permission) {
            case PERM_USER_MANAGE, PERM_PROJECT_CREATE, PERM_PROJECT_MANAGE, PERM_TASK_CREATE,
                PERM_TASK_MANAGE, PERM_REPORT_GENERATE, PERM_RISK_MANAGE, PERM_BUG_MANAGE,
                PERM_CHANGE_MANAGE, PERM_DOCUMENT_MANAGE, PERM_AI_USE -> false;
            case PERM_USER_VIEW, PERM_REPORT_VIEW -> true;
            default -> false;
        };
    }
    
    // 访客权限检查（只读）
    private boolean checkGuestPermission(String permission) {
        return permission.equals(PERM_REPORT_VIEW);
    }
    
    // 获取当前登录用户ID
    public Integer getCurrentUserId(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            return null;
        }
        User user = userService.findByUsername(username);
        return user != null ? user.getId() : null;
    }
    
    // 验证用户是否为管理员
    public boolean isAdmin(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            return false;
        }
        User user = userService.findByUsername(username);
        return user != null && user.getRoleId() == 1;
    }
    
    // 获取用户角色名称
    public String getRoleName(Integer roleId) {
        return switch (roleId) {
            case 1 -> "admin";
            case 2 -> "pm";
            case 3 -> "developer";
            case 4 -> "tester";
            case 5 -> "product";
            case 6 -> "designer";
            case 7 -> "guest";
            default -> "guest";
        };
    }
}