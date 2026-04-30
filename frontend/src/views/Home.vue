<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-left">
          <h1>项目管理系统</h1>
          <el-tag v-if="currentRoleInfo" :type="getRoleTagType(user?.roleId)" size="small" class="role-tag">
            {{ currentRoleInfo.name }}
          </el-tag>
        </div>
        <div class="header-right">
          <NotificationPanel />
          <span>{{ user?.username }}</span>
          <el-button type="danger" plain size="small" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="220px">
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical-demo"
            @select="handleMenuSelect"
            :collapse="isCollapse"
          >
            <!-- 动态菜单：按权限显示 -->
            <template v-for="menu in visibleMenus" :key="menu.index">
              <!-- 有子菜单的菜单项 -->
              <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.index">
                <template #title>
                  <el-icon><component :is="menu.icon" /></el-icon>
                  <span>{{ menu.title }}</span>
                </template>
                <el-menu-item
                  v-for="child in menu.children"
                  :key="child.index"
                  :index="child.index"
                  @click="handleSubMenuClick(child.index, $event)"
                >
                  <el-icon><component :is="child.icon" /></el-icon>
                  <span>{{ child.title }}</span>
                </el-menu-item>
              </el-sub-menu>
              
              <!-- 无子菜单的菜单项 -->
              <el-menu-item v-else :index="menu.index">
                <el-icon><component :is="menu.icon" /></el-icon>
                <span>{{ menu.title }}</span>
              </el-menu-item>
            </template>
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
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Folder, Plus, Document, Edit, Calendar, List, Warning, Flag } from '@element-plus/icons-vue';
import NotificationPanel from '../components/NotificationPanel.vue';
import { getMenusByRole, getRoleName, getHomePath, ROLES, type MenuItem, type RoleType } from '../utils/rolePermission';

const router = useRouter();
const route = useRoute();
const user = ref<any>(null);
const userRole = ref<RoleType>('guest');
const activeMenu = ref('/projects');
const isCollapse = ref(false);

// 当前角色信息
const currentRoleInfo = computed(() => {
  return ROLES[userRole.value];
});

// 按角色权限显示的菜单
const visibleMenus = computed(() => {
  return getMenusByRole(userRole.value);
});

// 根据角色获取标签颜色
const getRoleTagType = (roleId: number) => {
  const typeMap: Record<number, any> = {
    1: 'danger',    // 管理员
    2: 'warning',   // 项目经理
    3: 'primary',   // 开发者
    4: 'success',   // 测试
    5: 'info',      // 产品
    6: '',          // 设计师
    7: 'info'       // 访客
  };
  return typeMap[roleId] || 'info';
};

// 处理菜单选择
const handleMenuSelect = (key: string) => {
  activeMenu.value = key;
  // 处理主菜单点击，排除只有子菜单的父菜单
  if (!key.includes(':id') && key !== 'ai') {
    router.push(key);
  }
};

// 处理子菜单点击（需要替换项目ID）
const handleSubMenuClick = (index: string, event: Event) => {
  event.stopPropagation();
  // 如果路由有 :id 占位符，需要找到当前项目ID
  if (index.includes(':id')) {
    // 尝试从当前路由获取项目ID
    const projectId = route.params.id;
    if (projectId) {
      const realPath = index.replace(':id', String(projectId));
      activeMenu.value = realPath;
      router.push(realPath);
    } else {
      ElMessage.warning('请先选择一个项目');
      router.push('/projects');
    }
  } else {
    activeMenu.value = index;
    router.push(index);
  }
};

// 退出登录
const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  ElMessage.success('退出登录成功');
  router.push('/login');
};

// 初始化用户信息和菜单
const initUser = () => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    user.value = JSON.parse(userStr);
    // 根据roleId获取角色代码
    userRole.value = mapRoleIdToCode(user.value.roleId);
    // 设置当前菜单高亮
    activeMenu.value = route.path || getHomePath(userRole.value);
  }
};

// roleId 映射到 角色代码
const mapRoleIdToCode = (roleId: number): RoleType => {
  const map: Record<number, RoleType> = {
    1: 'admin',
    2: 'pm',
    3: 'developer',
    4: 'tester',
    5: 'product',
    6: 'designer',
    7: 'guest'
  };
  return map[roleId] || 'guest';
};

// 监听路由变化，更新菜单高亮
watch(() => route.path, (newPath) => {
  activeMenu.value = newPath;
});

onMounted(() => {
  initUser();
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
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.role-tag {
  margin-left: 8px;
  font-size: 12px;
  border-radius: 4px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
}

.header-right :deep(.notification-btn) {
  color: white;
}

.header-right :deep(.notification-btn:hover) {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.el-aside {
  background-color: #ffffff;
  border-right: 1px solid #e5e7eb;
  overflow-y: auto;
}

.el-menu-vertical-demo {
  height: 100%;
  background-color: #ffffff;
  border-right: none;
}

.el-menu-vertical-demo :deep(.el-menu-item),
.el-menu-vertical-demo :deep(.el-sub-menu__title) {
  color: #303133;
  height: 52px;
  line-height: 52px;
  font-size: 14px;
}

.el-menu-vertical-demo :deep(.el-menu-item:hover),
.el-menu-vertical-demo :deep(.el-sub-menu__title:hover) {
  background-color: #ecf5ff;
  color: #409eff;
}

.el-menu-vertical-demo :deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
  border-right: 3px solid #409eff;
}

.el-menu-vertical-demo :deep(.el-sub-menu .el-menu) {
  background-color: #f5f7fa;
}

.el-menu-vertical-demo :deep(.el-sub-menu .el-menu-item) {
  padding-left: 45px !important;
}

.el-main {
  padding: 24px;
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>
