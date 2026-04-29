package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.AiConversation;
import com.example.backend.mapper.AiConversationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiConversationService extends ServiceImpl<AiConversationMapper, AiConversation> {
    public List<AiConversation> getByProjectId(Integer projectId) {
        return baseMapper.findByProjectId(projectId);
    }

    public AiConversation saveMessage(AiConversation conversation) {
        save(conversation);
        return conversation;
    }
}