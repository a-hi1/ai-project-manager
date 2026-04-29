package com.example.backend.service;

import com.example.backend.entity.OperationLog;
import com.example.backend.mapper.OperationLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationLogService {
    @Autowired
    private OperationLogMapper operationLogMapper;

    public void logOperation(Integer userId, String username, String operation, String content, String ip) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setOperation(operation);
        log.setContent(content);
        log.setIp(ip);
        log.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(log);
    }

    public List<OperationLog> getRecentLogs() {
        return operationLogMapper.findRecentLogs();
    }

    public List<OperationLog> getLogsByUserId(Integer userId) {
        return operationLogMapper.findByUserId(userId);
    }
}
