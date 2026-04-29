package com.example.backend.service;

import com.example.backend.entity.Notification;
import com.example.backend.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    public Notification createNotification(Integer userId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setReadStatus(false);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);
        return notification;
    }

    public List<Notification> getNotificationsByUserId(Integer userId) {
        return notificationMapper.findByUserId(userId);
    }

    public int getUnreadCount(Integer userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    public void markAllAsRead(Integer userId) {
        notificationMapper.markAllAsRead(userId);
    }

    public void sendTaskAssignedNotification(Integer userId, String taskName, String projectName) {
        String title = "新任务分配";
        String content = String.format("您被分配了新任务：%s，项目：%s", taskName, projectName);
        createNotification(userId, title, content, "task_assigned");
    }

    public void sendRiskAlertNotification(Integer userId, String riskName, String projectName) {
        String title = "高风险预警";
        String content = String.format("项目 %s 中发现高风险项：%s", projectName, riskName);
        createNotification(userId, title, content, "risk_alert");
    }

    public void sendProjectStatusChangeNotification(Integer userId, String projectName, String newStatus) {
        String title = "项目状态变更";
        String content = String.format("项目 %s 状态已变更为：%s", projectName, newStatus);
        createNotification(userId, title, content, "project_status");
    }

    public void sendChangeRequestNotification(Integer userId, String changeTitle, String projectName) {
        String title = "变更请求待审批";
        String content = String.format("项目 %s 有新的变更请求：%s，需要您审批", projectName, changeTitle);
        createNotification(userId, title, content, "change_request");
    }
}
