package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectList(wrapper);
    }

    public int getUnreadCount(Integer userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getReadStatus, false);
        return Math.toIntExact(notificationMapper.selectCount(wrapper));
    }

    @Transactional
    public void markAllAsRead(Integer userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).eq(Notification::getReadStatus, false);
        Notification updateNotification = new Notification();
        updateNotification.setReadStatus(true);
        notificationMapper.update(updateNotification, wrapper);
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

    public void sendTaskProgressNotification(Integer userId, String taskName, Integer progress) {
        String title = "任务进度更新";
        String content = String.format("任务 %s 进度更新为 %d%%", taskName, progress);
        createNotification(userId, title, content, "task_progress");
    }

    public void sendMilestoneNotification(Integer userId, String milestoneName, String projectName) {
        String title = "里程碑提醒";
        String content = String.format("项目 %s 的里程碑 %s 即将到达", projectName, milestoneName);
        createNotification(userId, title, content, "milestone");
    }
    
    public void sendBugReportedNotification(Integer userId, String bugTitle, String projectName) {
        String title = "新缺陷报告";
        String content = String.format("项目 %s 报告了新缺陷：%s", projectName, bugTitle);
        createNotification(userId, title, content, "bug_reported");
    }
    
    public void sendBugAssignedNotification(Integer userId, String bugTitle, String projectName) {
        String title = "缺陷分配";
        String content = String.format("您被分配了新缺陷：%s，项目：%s", bugTitle, projectName);
        createNotification(userId, title, content, "bug_assigned");
    }
    
    public void sendBugFixedNotification(Integer userId, String bugTitle, String projectName) {
        String title = "缺陷已修复";
        String content = String.format("您关注的缺陷已修复：%s，项目：%s", bugTitle, projectName);
        createNotification(userId, title, content, "bug_fixed");
    }
    
    public void sendDeadlineWarningNotification(Integer userId, String taskName, String deadline) {
        String title = "任务截止预警";
        String content = String.format("任务 %s 即将到期，截止日期：%s", taskName, deadline);
        createNotification(userId, title, content, "deadline_warning");
    }
    
    public void sendOverdueWarningNotification(Integer userId, String taskName) {
        String title = "任务逾期警告";
        String content = String.format("任务 %s 已逾期，请尽快处理", taskName);
        createNotification(userId, title, content, "overdue_warning");
    }
    
    public void sendProgressStagnationNotification(Integer userId, String taskName, int days) {
        String title = "进度停滞提醒";
        String content = String.format("任务 %s 已有 %d 天未更新进度，请确认状态", taskName, days);
        createNotification(userId, title, content, "stagnation_warning");
    }
    
    public void sendProjectCompletionNotification(Integer userId, String projectName) {
        String title = "项目完成";
        String content = String.format("恭喜！项目 %s 已完成", projectName);
        createNotification(userId, title, content, "project_completed");
    }
    
    public void sendProjectDelayNotification(Integer userId, String projectName, String expectedDate, String actualDate) {
        String title = "项目延期预警";
        String content = String.format("项目 %s 预计延期，原计划：%s，预计完成：%s", projectName, expectedDate, actualDate);
        createNotification(userId, title, content, "project_delay");
    }
    
    public void sendDailyReportNotification(Integer userId, String projectName) {
        String title = "日报已生成";
        String content = String.format("项目 %s 的日报已生成，请查看", projectName);
        createNotification(userId, title, content, "daily_report");
    }
    
    public void sendWeeklyReportNotification(Integer userId, String projectName) {
        String title = "周报已生成";
        String content = String.format("项目 %s 的周报已生成，请查看", projectName);
        createNotification(userId, title, content, "weekly_report");
    }
    
    public void sendDeliverySubmittedNotification(Integer userId, String deliverableName, String projectName) {
        String title = "交付物已提交";
        String content = String.format("项目 %s 提交了新的交付物：%s，请审核", projectName, deliverableName);
        createNotification(userId, title, content, "delivery_submitted");
    }
    
    public void sendDeliveryApprovedNotification(Integer userId, String deliverableName, String projectName) {
        String title = "交付物已验收";
        String content = String.format("交付物 %s 已通过验收，项目：%s", deliverableName, projectName);
        createNotification(userId, title, content, "delivery_approved");
    }
    
    public void sendDocumentUpdatedNotification(Integer userId, String documentName, String projectName) {
        String title = "文档已更新";
        String content = String.format("项目 %s 的文档 %s 已更新", projectName, documentName);
        createNotification(userId, title, content, "document_updated");
    }
    
    public void sendSystemAlertNotification(Integer userId, String alertType, String message) {
        String title = "系统预警";
        String content = String.format("[%s] %s", alertType, message);
        createNotification(userId, title, content, "system_alert");
    }
}