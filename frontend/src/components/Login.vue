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
/* 登录页面 - 专业设计，无紫白渐变 */
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--bg);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 - 微妙的圆形元素 */
.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  border-radius: 9999px;
  background: linear-gradient(135deg, var(--primary-light), transparent);
  opacity: 0.5;
}

.login-container::before {
  width: 400px;
  height: 400px;
  top: -200px;
  left: -100px;
}

.login-container::after {
  width: 300px;
  height: 300px;
  bottom: -150px;
  right: -50px;
}

.login-bg {
  width: 100%;
  max-width: 440px;
  padding: var(--space-6);
  position: relative;
  z-index: 1;
}

.login-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  padding: var(--space-10);
  box-shadow: var(--shadow-xl);
  border: 1px solid var(--border-light);
  transition: all var(--transition-slow);
}

.login-card:hover {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: var(--space-8);
}

.logo {
  margin-bottom: var(--space-4);
}

.title {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
  letter-spacing: -0.02em;
}

.subtitle {
  font-size: 0.9375rem;
  color: var(--text-tertiary);
  margin: 0;
  font-weight: 400;
}

.login-form {
  margin-bottom: var(--space-6);
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 1rem;
  border-radius: var(--radius);
  font-weight: 600;
  letter-spacing: 0.01em;
}

.demo-accounts {
  text-align: center;
  padding-top: var(--space-5);
  border-top: 1px solid var(--border-light);
}

.demo-title {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  margin: 0 0 var(--space-3) 0;
  font-weight: 500;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.demo-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: var(--space-2);
}

.demo-list .el-tag {
  cursor: pointer;
  transition: all var(--transition);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius);
  font-weight: 500;
  font-size: 0.8125rem;
}

.demo-list .el-tag:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.login-footer {
  text-align: center;
  margin-top: var(--space-6);
}

.login-footer p {
  color: var(--text-tertiary);
  font-size: 0.875rem;
  margin: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-bg {
    padding: var(--space-4);
  }
  
  .login-card {
    padding: var(--space-6);
  }
  
  .title {
    font-size: 1.5rem;
  }
  
  .login-container::before,
  .login-container::after {
    display: none;
  }
}
</style>
