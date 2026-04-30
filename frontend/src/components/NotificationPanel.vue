<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
      <el-button circle @click="showNotificationPanel = true" class="notification-btn">
        <el-icon :size="20"><Bell /></el-icon>
      </el-button>
    </el-badge>

    <el-drawer
      v-model="showNotificationPanel"
      title="通知中心"
      direction="rtl"
      size="420px"
      class="notification-drawer"
    >
      <template #header>
        <div class="drawer-header">
          <span class="drawer-title">通知中心</span>
          <el-button type="primary" text size="small" @click="markAllAsRead" v-if="unreadCount > 0">
            全部已读
          </el-button>
        </div>
      </template>

      <div class="notification-list" v-loading="loading">
        <div v-if="notifications.length === 0 && !loading" class="empty-notification">
          <el-icon :size="64" color="#c0c4cc"><Bell /></el-icon>
          <p class="empty-text">暂无通知</p>
          <p class="empty-desc">您暂未收到任何通知</p>
        </div>

        <div v-else class="notification-items">
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ unread: !notification.readStatus }"
            @click="markAsRead(notification.id)"
          >
            <div class="notification-icon" :class="getNotificationIconClass(notification.type)">
              <el-icon :size="24">
                <component :is="getNotificationIcon(notification.type)" />
              </el-icon>
            </div>

            <div class="notification-content">
              <div class="notification-title">
                {{ notification.title }}
                <span v-if="!notification.readStatus" class="unread-dot"></span>
              </div>
              <div class="notification-text">{{ notification.content }}</div>
              <div class="notification-time">{{ formatTime(notification.createTime) }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { Bell, Message, Warning, Document, Flag } from '@element-plus/icons-vue';
import { ElMessage, ElNotification } from 'element-plus';
import webSocketService from '../utils/websocket';
import apiClient from '../utils/api';

const showNotificationPanel = ref(false);
const notifications = ref<any[]>([]);
const unreadCount = ref(0);
const loading = ref(false);
const userStr = localStorage.getItem('user');
const userId = userStr ? JSON.parse(userStr).id : 1;

const safeFetch = async (url: string, options?: RequestInit) => {
  const fullUrl = `http://localhost:8080${url}`;
  const token = localStorage.getItem('token');
  const headers: any = options?.headers || {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const response = await fetch(fullUrl, { ...options, headers });
  if (!response.ok) return { success: false, message: `HTTP ${response.status}` };
  const contentType = response.headers.get('content-type') || '';
  if (!contentType.includes('application/json')) return { success: false, message: '非JSON响应' };
  return response.json();
};

const getNotificationIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    'task_assigned': Message,
    'task_progress': Flag,
    'risk_alert': Warning,
    'project_status': Document,
    'change_request': Document,
    'milestone': Flag
  };
  return iconMap[type] || Bell;
};

const getNotificationIconClass = (type: string) => {
  const classMap: Record<string, string> = {
    'task_assigned': 'task',
    'task_progress': 'progress',
    'risk_alert': 'warning',
    'project_status': 'project',
    'change_request': 'change',
    'milestone': 'milestone'
  };
  return classMap[type] || 'default';
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const loadNotifications = async () => {
  loading.value = true;
  try {
    const data = await safeFetch(`/api/notification/list?userId=${userId}`);
    if (data.success) {
      notifications.value = data.data || [];
    }
  } catch (error) {
    console.error('加载通知失败:', error);
  } finally {
    loading.value = false;
  }
};

const loadUnreadCount = async () => {
  try {
    const data = await safeFetch(`/api/notification/unread-count?userId=${userId}`);
    if (data.success) {
      unreadCount.value = data.data || 0;
    }
  } catch (error) {
    console.error('加载未读数量失败:', error);
  }
};

const markAsRead = async (notificationId: number) => {
  try {
    const data = await safeFetch(`/api/notification/mark-read?notificationId=${notificationId}&userId=${userId}`, {
      method: 'POST'
    });
    if (data.success) {
      const notification = notifications.value.find(n => n.id === notificationId);
      if (notification) {
        notification.readStatus = true;
      }
    }
  } catch (error) {
    console.error('标记已读失败:', error);
  }
};

const markAllAsRead = async () => {
  try {
    const data = await safeFetch(`/api/notification/mark-all-read?userId=${userId}`, {
      method: 'POST'
    });
    if (data.success) {
      notifications.value.forEach(n => n.readStatus = true);
      ElMessage.success('已全部标记为已读');
    }
  } catch (error) {
    console.error('标记全部已读失败:', error);
    ElMessage.error('操作失败');
  }
};

const handleNewNotification = (notification: any) => {
  notifications.value.unshift(notification);
  ElNotification({
    title: notification.title,
    message: notification.content,
    type: 'info',
    duration: 5000
  });
};

const handleUnreadCountUpdate = (count: number) => {
  unreadCount.value = count;
};

onMounted(() => {
  loadNotifications();
  loadUnreadCount();
  webSocketService.connect(userId, handleNewNotification, handleUnreadCountUpdate);
});

onUnmounted(() => {
  webSocketService.disconnect();
});
</script>

<style scoped>
.notification-bell {
  display: inline-block;
  position: relative;
}

.notification-btn {
  background: transparent;
  border: none;
  color: #606266;
  transition: all 0.3s;
}

.notification-btn:hover {
  background: #f5f7fa;
  color: #409eff;
}

.notification-badge :deep(.el-badge__content) {
  background: #f56c6c;
  border-color: #f56c6c;
}

.notification-drawer :deep(.el-drawer__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #ebeef5;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.drawer-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.notification-list {
  height: calc(100vh - 120px);
  overflow-y: auto;
  padding: 16px;
}

.notification-list::-webkit-scrollbar {
  width: 6px;
}

.notification-list::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

.notification-list::-webkit-scrollbar-track {
  background: #f5f7fa;
}

.empty-notification {
  text-align: center;
  color: #909399;
  padding: 80px 20px;
}

.empty-text {
  font-size: 16px;
  margin-top: 20px;
  color: #909399;
}

.empty-desc {
  font-size: 14px;
  color: #c0c4cc;
  margin-top: 8px;
}

.notification-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #ebeef5;
}

.notification-item:hover {
  background: #f5f7fa;
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.notification-item.unread {
  background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
  border-left: 3px solid #409eff;
}

.notification-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 12px;
  flex-shrink: 0;
}

.notification-icon.task {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.notification-icon.progress {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}

.notification-icon.warning {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
  color: white;
}

.notification-icon.project {
  background: linear-gradient(135deg, #909399 0%, #a8abb2 100%);
  color: white;
}

.notification-icon.change {
  background: linear-gradient(135deg, #909399 0%, #a8abb2 100%);
  color: white;
}

.notification-icon.milestone {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.notification-icon.default {
  background: linear-gradient(135deg, #909399 0%, #a8abb2 100%);
  color: white;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #409eff;
  border-radius: 50%;
  flex-shrink: 0;
}

.notification-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-time {
  font-size: 12px;
  color: #c0c4cc;
}
</style>
