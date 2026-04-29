<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="register-header">
          <h2>注册</h2>
        </div>
      </template>
      <el-form :model="registerForm" ref="registerFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="registerForm.roleId" placeholder="请选择角色">
            <el-option label="管理员" :value="1" />
            <el-option label="项目经理" :value="2" />
            <el-option label="开发人员" :value="3" />
            <el-option label="测试人员" :value="4" />
            <el-option label="访客" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="register" class="register-button">注册</el-button>
          <el-button @click="switchToLogin">返回登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();
const registerFormRef = ref();
const registerForm = ref({
  username: '',
  password: '',
  email: '',
  roleId: 3
});

const register = async () => {
  console.log('注册按钮被点击');
  console.log('表单数据:', registerForm.value);
  
  if (!registerForm.value.username || !registerForm.value.password || !registerForm.value.email) {
    ElMessage.warning('请填写完整信息');
    return;
  }
  
  try {
    console.log('发送请求到后端...');
    const response = await fetch('http://localhost:8080/api/user/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(registerForm.value)
    });

    console.log('响应状态:', response.status);
    const result = await response.json();
    console.log('响应数据:', result);
    
    if (result.success) {
      ElMessage.success('注册成功，请登录');
      router.push('/login');
    } else {
      ElMessage.error(result.message || '注册失败');
    }
  } catch (error) {
    console.error('网络错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  }
};

const switchToLogin = () => {
  console.log('切换到登录页面');
  router.push('/login');
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.register-card {
  width: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-header {
  text-align: center;
}

.register-header h2 {
  margin: 0;
  color: #303133;
}

.register-button {
  width: 100%;
  margin-bottom: 10px;
}
</style>