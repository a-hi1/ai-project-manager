package com.example.backend.service;

import com.example.backend.entity.Permission;
import com.example.backend.mapper.PermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
    @Autowired
    private PermissionMapper permissionMapper;
    
    public Permission selectById(Integer id) {
        return permissionMapper.selectById(id);
    }
}