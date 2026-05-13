package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Message::getSenderId, userId1).eq(Message::getReceiverId, userId2))
               .or(w -> w.eq(Message::getSenderId, userId2).eq(Message::getReceiverId, userId1))
               .orderByAsc(Message::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    public List<Message> getMessagesByReceiver(Integer receiverId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, receiverId)
               .orderByDesc(Message::getCreateTime);
        return messageMapper.selectList(wrapper);
    }

    public List<Message> getRecentConversations(Integer userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getSenderId, userId).or().eq(Message::getReceiverId, userId)
               .orderByDesc(Message::getCreateTime)
               .last("LIMIT 50");
        return messageMapper.selectList(wrapper);
    }

    @Transactional
    public void markAllAsRead(Integer receiverId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, receiverId).eq(Message::getReadStatus, false);
        Message updateMessage = new Message();
        updateMessage.setReadStatus(true);
        messageMapper.update(updateMessage, wrapper);
    }

    @Transactional
    public void markAsRead(Integer receiverId, Integer senderId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, receiverId)
               .eq(Message::getSenderId, senderId)
               .eq(Message::getReadStatus, false);
        Message updateMessage = new Message();
        updateMessage.setReadStatus(true);
        messageMapper.update(updateMessage, wrapper);
    }

    public int getUnreadCount(Integer userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId).eq(Message::getReadStatus, false);
        return Math.toIntExact(messageMapper.selectCount(wrapper));
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
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getReceiverId, userId).eq(Message::getReadStatus, false)
               .orderByDesc(Message::getCreateTime);
        return messageMapper.selectList(wrapper);
    }
}