package com.example.backend.interceptor;

import com.example.backend.annotation.RequirePermission;
import com.example.backend.annotation.RequireRole;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.utils.JwtUtils;
import com.example.backend.utils.PermissionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionUtils permissionUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            sendError(response, "未登录或token无效", 401);
            return false;
        }

        token = token.substring(7);
        if (!jwtUtils.validateToken(token)) {
            sendError(response, "token无效", 401);
            return false;
        }

        String username = jwtUtils.getUsernameFromToken(token);
        User user = userService.findByUsername(username);
        if (user == null) {
            sendError(response, "用户不存在", 401);
            return false;
        }

        request.setAttribute("userId", user.getId());
        request.setAttribute("userRole", user.getRoleId());
        request.setAttribute("username", username);

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && requireRole.value().length > 0) {
            boolean hasRole = false;
            String userRoleName = permissionUtils.getRoleName(user.getRoleId());
            for (String role : requireRole.value()) {
                if (role.equals(userRoleName)) {
                    hasRole = true;
                    break;
                }
            }
            if (!hasRole) {
                sendError(response, "角色权限不足", 403);
                return false;
            }
        }

        RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            requirePermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (requirePermission != null && requirePermission.value().length > 0) {
            boolean hasPermission = false;
            for (String permission : requirePermission.value()) {
                if (permissionUtils.hasPermission(request, permission)) {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission) {
                sendError(response, "权限不足", 403);
                return false;
            }
        }

        return true;
    }

    private void sendError(HttpServletResponse response, String message, int status) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
