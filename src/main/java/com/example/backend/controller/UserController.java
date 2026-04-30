package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    @GetMapping("/list")
    public Map<String, Object> list() {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("获取用户列表请求");
            List<User> userList = userService.list();
            result.put("success", true);
            result.put("data", userList);
            logger.info("获取用户列表成功，共 {} 条记录", userList.size());
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            result.put("success", false);
            result.put("message", "获取用户列表失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("更新用户请求: userId={}", user.getId());
            userService.updateById(user);
            result.put("success", true);
            result.put("data", user);
            logger.info("更新用户成功");
        } catch (Exception e) {
            logger.error("更新用户失败", e);
            result.put("success", false);
            result.put("message", "更新用户失败: " + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/delete")
    public Map<String, Object> delete(@RequestParam Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("删除用户请求: userId={}", id);
            userService.removeById(id);
            result.put("success", true);
            logger.info("删除用户成功");
        } catch (Exception e) {
            logger.error("删除用户失败", e);
            result.put("success", false);
            result.put("message", "删除用户失败: " + e.getMessage());
        }
        return result;
    }
}