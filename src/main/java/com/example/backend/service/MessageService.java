package com.example.backend.service;

import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.example.backend.mapper.MessageMapper;
import com.example.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    
    @Autowired
    private MessageMapper messageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public Message sendMessage(Integer senderId, Integer receiverId, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType("text");
        message.setReadStatus(false);
        message.setCreateTime(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        webSocketService.sendMessageToUser(receiverId, message);
        
        User sender = userMapper.selectById(senderId);
        String senderName = sender != null ? sender.getUsername() : "未知用户";
        notificationService.createNotification(
            receiverId,
            "新消息",
            String.format("您收到了来自 %s 的消息", senderName),
            "new_message"
        );
        
        return message;
    }
    
    @Transactional
    public Message sendReply(Integer senderId, Integer receiverId, String content, String replyTo) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType("reply");
        message.setReplyTo(replyTo);
        message.setReadStatus(false);
        message.setCreateTime(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        webSocketService.sendMessageToUser(receiverId, message);
        
        return message;
    }
    
    public List<Message> getConversation(Integer userId1, Integer userId2) {
        return messageMapper.findConversation(userId1, userId2);
    }
    
    public List<Message> getMessagesByReceiver(Integer receiverId) {
        return messageMapper.findByReceiverId(receiverId);
    }
    
    public List<Message> getRecentConversations(Integer userId) {
        return messageMapper.findRecentConversations(userId);
    }
    
    @Transactional
    public void markAllAsRead(Integer receiverId) {
        messageMapper.markAllAsRead(receiverId);
    }
    
    @Transactional
    public void markAsRead(Integer receiverId, Integer senderId) {
        messageMapper.markAsReadBySender(receiverId, senderId);
    }
    
    public int getUnreadCount(Integer userId) {
        return messageMapper.countUnreadByReceiverId(userId);
    }
    
    @Transactional
    public void sendSystemMessage(Integer receiverId, String content) {
        Message message = new Message();
        message.setSenderId(0);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType("system");
        message.setReadStatus(false);
        message.setCreateTime(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        webSocketService.sendMessageToUser(receiverId, message);
    }
    
    public List<Message> getUnreadMessages(Integer userId) {
        return messageMapper.findUnreadByReceiverId(userId);
    }
}