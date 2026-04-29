package com.example.backend.controller;

import com.example.backend.entity.Role;
import com.example.backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    @Autowired
    private RoleService roleService;
    
    @GetMapping("/list")
    public Map<String, Object> getRoleList() {
        List<Role> roles = roleService.list();
        return Map.of("success", true, "data", roles);
    }
}