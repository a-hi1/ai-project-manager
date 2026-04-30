package com.example.backend.service;

import com.example.backend.entity.Notification;
import com.example.backend.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private WebSocketService webSocketService;

    @Transactional
    public Notification createNotification(Integer userId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setReadStatus(false);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);

        webSocketService.sendNotificationToUser(userId, notification);
        int unreadCount = getUnreadCount(userId);
        webSocketService.sendUnreadCountUpdate(userId, unreadCount);

        return notification;
    }

    public List<Notification> getNotificationsByUserId(Integer userId) {
        return notificationMapper.findByUserId(userId);
    }

    public int getUnreadCount(Integer userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAllAsRead(Integer userId) {
        notificationMapper.markAllAsRead(userId);
        webSocketService.sendUnreadCountUpdate(userId, 0);
    }

    @Transactional
    public void markAsRead(Integer notificationId, Integer userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.setReadStatus(true);
            notificationMapper.updateById(notification);
            int unreadCount = getUnreadCount(userId);
            webSocketService.sendUnreadCountUpdate(userId, unreadCount);
        }
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

    public void sendTaskProgressNotification(Integer userId, String taskName, String progress) {
        String title = "任务进度更新";
        String content = String.format("任务 %s 进度更新为 %s%%", taskName, progress);
        createNotification(userId, title, content, "task_progress");
    }

    public void sendMilestoneNotification(Integer userId, String milestoneName, String projectName) {
        String title = "里程碑提醒";
        String content = String.format("项目 %s 的里程碑 %s 即将到达", projectName, milestoneName);
        createNotification(userId, title, content, "milestone");
    }
}
