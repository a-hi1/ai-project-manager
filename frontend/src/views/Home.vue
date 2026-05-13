<template>
  <div class="home">
    <el-container>
      <el-header>
        <div class="header-left">
          <h1>软件项目管理系统</h1>
          <el-tag v-if="currentRoleInfo && user?.roleId" :type="getRoleTagType(user!.roleId)" size="small" class="role-tag">
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
                  @click="handleSubMenuClick(child.index)"
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
import NotificationPanel from '../components/NotificationPanel.vue';
import { getMenusByRole, getHomePath, ROLES, getRoleType, type RoleType } from '../utils/rolePermission';

import type { User } from '../types';

const router = useRouter();
const route = useRoute();
const user = ref<User | null>(null);
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
  const typeMap: Record<number, string> = {
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
const handleSubMenuClick = (index: string) => {
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
    const parsedUser = JSON.parse(userStr);
    user.value = parsedUser;
    
    // 优先通过用户名判断角色（最可靠）
    const username = parsedUser.username;
    if (username) {
      const usernameMap: Record<string, RoleType> = {
        'admin': 'admin',
        'pm': 'pm',
        'developer': 'developer',
        'tester': 'tester',
        'product': 'product',
        'designer': 'designer',
        'guest': 'guest'
      };
      if (usernameMap[username]) {
        userRole.value = usernameMap[username];
      } else {
        // 用户名不匹配时，再使用getRoleType
        const roleId = parsedUser.roleId;
        if (roleId) {
          userRole.value = getRoleType(roleId);
        }
      }
      
      console.log('用户信息:', {
        username: username,
        roleId: parsedUser.roleId,
        determinedRole: userRole.value
      });
      
      // 设置当前菜单高亮
      activeMenu.value = route.path || getHomePath(userRole.value);
    }
  }
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
/* 首页布局 - 专业设计系统 */
.home {
  height: 100vh;
  background-color: var(--bg);
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  padding: 0 var(--space-6);
  height: 64px;
  box-shadow: var(--shadow);
  border-bottom: 1px solid var(--border-light);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.header-left h1 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--text-primary);
}

.role-tag {
  margin-left: var(--space-2);
  font-size: 0.75rem;
  border-radius: var(--radius-sm);
  font-weight: 600;
  letter-spacing: 0.02em;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.header-right :deep(.notification-btn) {
  color: var(--text-secondary);
  transition: all var(--transition);
}

.header-right :deep(.notification-btn:hover) {
  background: var(--bg-tertiary);
  color: var(--primary);
}

.el-aside {
  background-color: var(--bg-secondary);
  border-right: 1px solid var(--border-light);
  overflow-y: auto;
  transition: all var(--transition);
}

.el-menu-vertical-demo {
  height: 100%;
  background-color: var(--bg-secondary);
  border-right: none;
}

.el-menu-vertical-demo :deep(.el-menu-item),
.el-menu-vertical-demo :deep(.el-sub-menu__title) {
  color: var(--text-secondary);
  height: 48px;
  line-height: 48px;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all var(--transition);
  border-radius: var(--radius);
  margin: 4px 12px;
}

.el-menu-vertical-demo :deep(.el-menu-item:hover),
.el-menu-vertical-demo :deep(.el-sub-menu__title:hover) {
  background-color: var(--bg-tertiary);
  color: var(--primary-dark);
}

.el-menu-vertical-demo :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, var(--primary-light), rgba(59, 130, 246, 0.1));
  color: var(--primary-dark);
  font-weight: 600;
}

.el-menu-vertical-demo :deep(.el-sub-menu .el-menu) {
  background-color: transparent;
}

.el-menu-vertical-demo :deep(.el-sub-menu .el-menu-item) {
  padding-left: 56px !important;
  margin: 2px 12px;
  font-size: 0.8125rem;
}

.el-main {
  padding: 0;
  background-color: var(--bg);
  overflow-y: auto;
}

/* 响应式 */
@media (max-width: 768px) {
  .el-header {
    padding: 0 var(--space-4);
  }
  
  .header-left h1 {
    font-size: 1rem;
  }
  
  .el-aside {
    position: fixed;
    z-index: 200;
    height: 100vh;
  }
}
</style>
