package com.example.backend.service;

import com.example.backend.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(Integer userId, Notification notification) {
        String destination = "/user/" + userId + "/queue/notifications";
        messagingTemplate.convertAndSend(destination, notification);
    }

    public void sendUnreadCountUpdate(Integer userId, int unreadCount) {
        String destination = "/user/" + userId + "/queue/unread-count";
        messagingTemplate.convertAndSend(destination, unreadCount);
    }

    public void sendGlobalNotification(Notification notification) {
        String destination = "/topic/notifications";
        messagingTemplate.convertAndSend(destination, notification);
    }
}
