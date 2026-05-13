package com.example.backend.controller;

import com.example.backend.entity.Message;
import com.example.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    
    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> data) {
        Integer senderId = Integer.valueOf(data.get("senderId").toString());
        Integer receiverId = Integer.valueOf(data.get("receiverId").toString());
        String content = (String) data.get("content");

        Message message = messageService.sendMessage(senderId, receiverId, content);

        return Map.of("success", true, "data", message);
    }

    @PostMapping("/reply")
    public Map<String, Object> sendReply(@RequestBody Map<String, Object> data) {
        Integer senderId = Integer.valueOf(data.get("senderId").toString());
        Integer receiverId = Integer.valueOf(data.get("receiverId").toString());
        String content = (String) data.get("content");
        String replyTo = data.get("replyTo") != null ? data.get("replyTo").toString() : null;

        Message message = messageService.sendReply(senderId, receiverId, content, replyTo);

        return Map.of("success", true, "data", message);
    }

    @GetMapping("/conversation")
    public Map<String, Object> getConversation(
            @RequestParam Integer userId1,
            @RequestParam Integer userId2) {
        List<Message> messages = messageService.getConversation(userId1, userId2);
        return Map.of("success", true, "data", messages);
    }

    @GetMapping("/list")
    public Map<String, Object> getMessages(@RequestParam Integer receiverId) {
        List<Message> messages = messageService.getMessagesByReceiver(receiverId);
        return Map.of("success", true, "data", messages);
    }

    @GetMapping("/recent")
    public Map<String, Object> getRecentConversations(@RequestParam Integer userId) {
        List<Message> conversations = messageService.getRecentConversations(userId);
        return Map.of("success", true, "data", conversations);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestParam Integer userId) {
        int count = messageService.getUnreadCount(userId);
        return Map.of("success", true, "data", count);
    }

    @PostMapping("/mark-all-read")
    public Map<String, Object> markAllAsRead(@RequestParam Integer userId) {
        messageService.markAllAsRead(userId);
        return Map.of("success", true, "message", "已标记全部已读");
    }

    @PostMapping("/mark-read")
    public Map<String, Object> markAsRead(
            @RequestParam Integer receiverId,
            @RequestParam Integer senderId) {
        messageService.markAsRead(receiverId, senderId);
        return Map.of("success", true, "message", "已标记为已读");
    }

    @GetMapping("/unread")
    public Map<String, Object> getUnreadMessages(@RequestParam Integer userId) {
        List<Message> messages = messageService.getUnreadMessages(userId);
        return Map.of("success", true, "data", messages);
    }
}