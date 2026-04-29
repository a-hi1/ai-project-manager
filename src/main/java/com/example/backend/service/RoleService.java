package com.example.backend.service;

import com.example.backend.entity.Role;
import com.example.backend.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    @Autowired
    private RoleMapper roleMapper;
    
    public Role selectById(Integer id) {
        return roleMapper.selectById(id);
    }
    
    public List<Role> list() {
        return roleMapper.findAll();
    }
}