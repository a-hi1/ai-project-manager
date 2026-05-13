package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    
    List<Message> findConversation(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);
    
    List<Message> findByReceiverId(@Param("receiverId") Integer receiverId);
    
    List<Message> findUnreadByReceiverId(@Param("receiverId") Integer receiverId);
    
    List<Message> findRecentConversations(@Param("userId") Integer userId);
    
    int countUnreadByReceiverId(@Param("receiverId") Integer receiverId);
    
    void markAllAsRead(@Param("receiverId") Integer receiverId);
    
    void markAsReadBySender(@Param("receiverId") Integer receiverId, @Param("senderId") Integer senderId);
}