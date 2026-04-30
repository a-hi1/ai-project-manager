package com.example.backend.controller;

import com.example.backend.entity.Notification;
import com.example.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Map<String, Object> getNotifications(@RequestParam Integer userId) {
        List<Notification> notifications = notificationService.getNotificationsByUserId(userId);
        return Map.of("success", true, "data", notifications);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestParam Integer userId) {
        int count = notificationService.getUnreadCount(userId);
        return Map.of("success", true, "data", count);
    }

    @PostMapping("/mark-all-read")
    public Map<String, Object> markAllAsRead(@RequestParam Integer userId) {
        notificationService.markAllAsRead(userId);
        return Map.of("success", true, "message", "已标记全部已读");
    }

    @PostMapping("/mark-read")
    public Map<String, Object> markAsRead(@RequestParam Integer notificationId, @RequestParam Integer userId) {
        notificationService.markAsRead(notificationId, userId);
        return Map.of("success", true, "message", "已标记为已读");
    }

    @PostMapping("/create")
    public Map<String, Object> createNotification(@RequestBody Notification notification) {
        Notification created = notificationService.createNotification(
            notification.getUserId(),
            notification.getTitle(),
            notification.getContent(),
            notification.getType()
        );
        return Map.of("success", true, "data", created);
    }
}
