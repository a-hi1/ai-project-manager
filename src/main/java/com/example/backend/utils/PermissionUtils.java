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
    
    public boolean hasPermission(HttpServletRequest request, String permission) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            return false;
        }
        
        User user = userService.findByUsername(username);
        // 这里简化处理，实际应该根据用户角色查询对应的权限
        // 暂时返回true，后续需要完善权限验证逻辑
        return true;
    }
}