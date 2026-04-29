package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("注册请求: username={}, email={}", user.getUsername(), user.getEmail());
            User registeredUser = userService.register(user);
            result.put("success", true);
            result.put("data", registeredUser);
        } catch (Exception e) {
            logger.error("注册失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            logger.info("=== 登录请求开始 ===");
            logger.info("用户名: {}", username);
            logger.info("密码长度: {}", password != null ? password.length() : 0);

            logger.info("调用 userService.login...");
            String token = userService.login(username, password);
            logger.info("token 返回: {}", token != null ? "成功" : "null");

            if (token != null) {
                logger.info("登录成功，查找用户信息...");
                User user = userService.findByUsername(username);
                logger.info("用户信息: {}", user);

                result.put("success", true);
                result.put("token", token);
                result.put("user", user);
                logger.info("=== 登录请求完成（成功） ===");
            } else {
                logger.warn("登录失败: 用户名或密码错误");
                result.put("success", false);
                result.put("message", "用户名或密码错误");
                logger.info("=== 登录请求完成（失败） ===");
            }
        } catch (Exception e) {
            logger.error("=== 登录异常 ===", e);
            logger.error("异常类型: {}", e.getClass().getName());
            logger.error("异常消息: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }
        return result;
    }
}