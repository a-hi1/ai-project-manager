<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-left">
          <h1>项目管理系统</h1>
        </div>
        <div class="header-right">
          <span>{{ user?.username }}</span>
          <el-button type="primary" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/projects">
              <el-icon><Folder /></el-icon>
              <span>项目管理</span>
            </el-menu-item>
            <el-menu-item index="/project/create">
              <el-icon><Plus /></el-icon>
              <span>创建项目</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Folder, Plus } from '@element-plus/icons-vue';

const router = useRouter();
const user = ref<any>(null);
const activeMenu = ref('/projects');

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  ElMessage.success('退出登录成功');
  router.push('/login');
};

const handleMenuSelect = (key: string) => {
  activeMenu.value = key;
  router.push(key);
};

onMounted(() => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    user.value = JSON.parse(userStr);
  }
});
</script>

<style scoped>
.home {
  height: 100vh;
  background-color: #f5f7fa;
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #409eff;
  color: white;
  padding: 0 20px;
}

.header-left h1 {
  margin: 0;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.el-aside {
  background-color: #303133;
}

.el-menu-vertical-demo {
  height: 100%;
  background-color: #303133;
}

.el-menu-vertical-demo .el-menu-item {
  color: #e4e7ed;
}

.el-menu-vertical-demo .el-menu-item.is-active {
  background-color: #409eff;
  color: white;
}

.el-main {
  padding: 20px;
}
</style>