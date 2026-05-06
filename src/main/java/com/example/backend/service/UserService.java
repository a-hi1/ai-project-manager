package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.User;
import com.example.backend.mapper.UserMapper;
import com.example.backend.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private JwtUtils jwtUtils;

    public User register(User user) {
        logger.info("UserService.register: username={}", user.getUsername());
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("邮箱不能为空");
        }
        if (baseMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        if (user.getRoleId() == null) {
            user.setRoleId(5);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        baseMapper.insert(user);
        logger.info("UserService.register: success, userId={}", user.getId());
        return user;
    }

    public String login(String username, String password) {
        logger.info("UserService.login: username={}", username);
        try {
            logger.info("尝试查询用户...");
            User user = baseMapper.findByUsername(username);
            logger.info("UserService.login: user found = {}", user != null ? "yes" : "no");
            if (user == null) {
                logger.warn("UserService.login: user not found for username={}", username);
                return null;
            }
            logger.info("UserService.login: stored password = {}, input password = {}",
                    user.getPassword(), password);
            if (!user.getPassword().equals(password)) {
                logger.warn("UserService.login: password mismatch for username={}", username);
                return null;
            }
            logger.info("尝试生成 token...");
            String token = jwtUtils.generateToken(username, user.getId());
            logger.info("UserService.login: success, token generated for username={}", username);
            return token;
        } catch (Exception e) {
            logger.error("UserService.login 异常", e);
            throw e;
        }
    }

    public User findByUsername(String username) {
        logger.info("UserService.findByUsername: username={}", username);
        return baseMapper.findByUsername(username);
    }

    public User selectById(Integer id) {
        logger.info("UserService.selectById: id={}", id);
        return baseMapper.selectById(id);
    }
}