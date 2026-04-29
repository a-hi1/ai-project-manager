package com.example.backend.aspect;

import com.example.backend.entity.OperationLog;
import com.example.backend.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Pointcut("execution(* com.example.backend.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return result;

            HttpServletRequest request = attributes.getRequest();
            String method = request.getMethod();
            if (!"POST".equals(method) && !"PUT".equals(method) && !"DELETE".equals(method)) {
                return result;
            }

            String path = request.getRequestURI();
            if (path.contains("/login") || path.contains("/register")) {
                return result;
            }

            String operation = extractOperation(joinPoint);
            String ip = getClientIp(request);

            String username = "unknown";
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {
                    username = extractUsernameFromToken(token);
                } catch (Exception e) {
                    username = "token_invalid";
                }
            }

            String content = String.format("%s %s", method, path);

            OperationLog log = new OperationLog();
            log.setOperation(operation);
            log.setContent(content);
            log.setIp(ip);
            log.setUsername(username);
            log.setCreateTime(LocalDateTime.now());

            operationLogMapper.insert(log);
        } catch (Exception e) {
            System.err.println("记录操作日志失败: " + e.getMessage());
        }

        return result;
    }

    private String extractOperation(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        return className + "." + methodName;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String extractUsernameFromToken(String token) {
        return "user";
    }
}
