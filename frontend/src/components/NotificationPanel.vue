<template>
  <div class="notification-bell">
    <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
      <el-button circle @click="showNotificationPanel = true">
        <el-icon :size="20"><Bell /></el-icon>
      </el-button>
    </el-badge>

    <el-drawer
      v-model="showNotificationPanel"
      title="通知中心"
      direction="rtl"
      size="400px"
    >
      <div class="notification-list">
        <div class="empty-notification">
          <el-icon :size="48" color="#909399"><Bell /></el-icon>
          <p>暂无通知</p>
          <p style="font-size: 12px; color: #c0c4cc;">通知功能开发中...</p>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Bell } from '@element-plus/icons-vue';

const showNotificationPanel = ref(false);
const notifications = ref<any[]>([]);
const unreadCount = ref(0);
</script>

<style scoped>
.notification-bell {
  display: inline-block;
}

.notification-list {
  height: calc(100vh - 120px);
  overflow-y: auto;
}

.empty-notification {
  text-align: center;
  color: #909399;
  padding: 80px 20px;
}

.empty-notification p {
  margin-top: 16px;
}
</style>
