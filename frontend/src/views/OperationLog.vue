<template>
  <div class="operation-log">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2>操作日志</h2>
          </div>
          <el-button type="primary" @click="refreshLogs">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operation" label="操作" width="250" />
        <el-table-column prop="content" label="内容" min-width="200" />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        :page-size="20"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="fetchLogs"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Refresh, ArrowLeft } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';

const router = useRouter();

const logs = ref<any[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const total = ref(0);

const fetchLogs = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/log/list', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    if (result.success) {
      logs.value = result.data || [];
      total.value = logs.value.length;
    }
  } catch (error) {
    ElMessage.error('获取操作日志失败');
  } finally {
    loading.value = false;
  }
};

const refreshLogs = () => {
  fetchLogs();
};

const formatTime = (time: string) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchLogs();
});
</script>

<style scoped>
.operation-log {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
</style>
