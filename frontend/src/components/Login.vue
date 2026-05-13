<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <el-icon :size="48" color="#409EFF"><Management /></el-icon>
          </div>
          <h1 class="title">软件项目管理系统</h1>
          <p class="subtitle">AI驱动 · 专业高效的软件项目管理平台</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="demo-accounts">
          <p class="demo-title">演示账号</p>
          <div class="demo-list">
            <el-tag v-for="(account, index) in demoAccounts" :key="index" @click="fillAccount(account)">
              {{ account.role }}：{{ account.username }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="login-footer">
        <p>软件项目管理系统 · 专业高效的项目管理平台</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { User, Lock, Management } from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const router = useRouter();
const loginFormRef = ref<FormInstance>();
const loading = ref(false);

const loginForm = reactive({
  username: '',
  password: ''
});

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
};

const demoAccounts = [
  { role: '管理员', username: 'admin', password: '123456' },
  { role: '项目经理', username: 'pm', password: '123456' },
  { role: '开发人员', username: 'developer', password: '123456' },
  { role: '测试人员', username: 'tester', password: '123456' },
  { role: '产品经理', username: 'product', password: '123456' },
  { role: 'UI设计师', username: 'designer', password: '123456' },
  { role: '访客', username: 'guest', password: '123456' }
];

const fillAccount = (account: { username: string; password: string }) => {
  loginForm.username = account.username;
  loginForm.password = account.password;
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return;

    loading.value = true;
    try {
      const result: any = await apiClient.post('/user/login', loginForm);

      if (result.success) {
        localStorage.setItem('token', result.token);
        if (result.user) {
          localStorage.setItem('user', JSON.stringify(result.user));
        }
        ElMessage.success('登录成功');

        router.push('/');
      } else {
        ElMessage.error(result.message || '登录失败');
      }
    } catch (error) {
      ElMessage.error('网络错误，请稍后重试');
    } finally {
      loading.value = false;
    }
  });
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-bg {
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  margin-bottom: 16px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.login-form {
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
}

.demo-accounts {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.demo-title {
  font-size: 12px;
  color: #909399;
  margin: 0 0 12px 0;
}

.demo-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.demo-list .el-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.demo-list .el-tag:hover {
  transform: scale(1.05);
}

.login-footer {
  text-align: center;
  margin-top: 24px;
}

.login-footer p {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin: 0;
}
</style>
