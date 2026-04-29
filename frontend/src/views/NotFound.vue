<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-icon">
        <el-icon :size="120" color="#dcdfe6"><Warning /></el-icon>
      </div>
      <h1 class="error-code">{{ errorCode }}</h1>
      <p class="error-message">{{ errorMessage }}</p>
      <el-button type="primary" size="large" @click="goHome">
        <el-icon><House /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { Warning, House } from '@element-plus/icons-vue';

const router = useRouter();
const errorCode = ref('404');
const errorMessage = ref('抱歉，您访问的页面不存在');

const goHome = () => {
  router.push('/');
};

onMounted(() => {
  const path = window.location.pathname;
  if (path.includes('500')) {
    errorCode.value = '500';
    errorMessage.value = '抱歉，服务器出了点问题';
  }
});
</script>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.error-content {
  text-align: center;
  color: #ffffff;
}

.error-icon {
  margin-bottom: 24px;
}

.error-code {
  font-size: 120px;
  font-weight: 700;
  margin: 0;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.error-message {
  font-size: 20px;
  margin: 16px 0 32px;
  opacity: 0.9;
}

.error-content .el-button {
  padding: 16px 32px;
  font-size: 16px;
}
</style>
