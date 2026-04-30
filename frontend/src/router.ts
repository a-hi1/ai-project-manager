import { createRouter, createWebHistory } from 'vue-router';
import { ElMessage } from 'element-plus';
import { hasPermission, getHomePath, type RoleType } from './utils/rolePermission';

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

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('./components/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('./components/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('./views/Home.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('./views/Dashboard.vue')
      },
      {
        path: 'projects',
        name: 'Projects',
        component: () => import('./views/Projects.vue')
      },
      {
        path: 'project/create',
        name: 'CreateProject',
        component: () => import('./views/CreateProject.vue')
      },
      {
        path: 'project/:id',
        name: 'ProjectDetail',
        component: () => import('./views/ProjectDetail.vue')
      },
      {
        path: 'project/:id/requirement',
        name: 'RequirementResearch',
        component: () => import('./views/RequirementResearch.vue')
      },
      {
        path: 'project/:id/feasibility',
        name: 'FeasibilityAnalysis',
        component: () => import('./views/FeasibilityAnalysis.vue')
      },
      {
        path: 'project/:id/tasks',
        name: 'ProgressTracking',
        component: () => import('./views/ProgressTracking.vue')
      },
      {
        path: 'project/:id/gantt',
        name: 'GanttChart',
        component: () => import('./views/GanttChart.vue')
      },
      {
        path: 'project/:id/kanban',
        name: 'KanbanBoard',
        component: () => import('./views/KanbanBoard.vue')
      },
      {
        path: 'project/:id/risks',
        name: 'RiskManagement',
        component: () => import('./views/RiskManagement.vue')
      },
      {
        path: 'project/:id/bugs',
        name: 'BugTracking',
        component: () => import('./views/BugTracking.vue')
      },
      {
        path: 'project/:id/changes',
        name: 'ChangeManagement',
        component: () => import('./views/ChangeManagement.vue')
      },
      {
        path: 'project/:id/delivery',
        name: 'DeliveryAcceptance',
        component: () => import('./views/DeliveryAcceptance.vue')
      },
      {
        path: 'project/:id/documents',
        name: 'DocumentArchive',
        component: () => import('./views/DocumentArchive.vue')
      },
      {
        path: 'project/:id/retrospective',
        name: 'ProjectRetrospective',
        component: () => import('./views/ProjectRetrospective.vue')
      },
      {
        path: 'project/:id/end-report',
        name: 'ProjectEndReport',
        component: () => import('./views/ProjectEndReport.vue')
      },
      {
        path: 'operation-log',
        name: 'OperationLog',
        component: () => import('./views/OperationLog.vue')
      },
      {
        path: 'ai-chat',
        name: 'AiChatAssistant',
        component: () => import('./views/AiChatAssistant.vue')
      },
      {
        path: 'ai-requirement',
        name: 'AiRequirementParser',
        component: () => import('./views/AiRequirementParser.vue')
      },
      {
        path: 'ai-task',
        name: 'AiTaskSplitter',
        component: () => import('./views/AiTaskSplitter.vue')
      },
      {
        path: 'ai-knowledge',
        name: 'KnowledgeBase',
        component: () => import('./views/KnowledgeBase.vue')
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('./views/UserManagement.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('./views/NotFound.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  
  // 检查是否需要登录
  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录');
    return next('/login');
  }
  
  // 如果已登录，访问登录/注册页，跳转到首页
  if ((to.path === '/login' || to.path === '/register') && token) {
    let userRole: RoleType = 'guest';
    if (userStr) {
      const user = JSON.parse(userStr);
      userRole = mapRoleIdToCode(user.roleId);
    }
    return next(getHomePath(userRole));
  }
  
  // 权限检查
  if (token && userStr) {
    const user = JSON.parse(userStr);
    const userRole = mapRoleIdToCode(user.roleId);
    
    // 检查是否有权限访问该路由
    const routePath = to.path;
    if (!hasPermission(userRole, routePath)) {
      ElMessage.error('您没有权限访问该页面');
      // 如果是根路径，按角色跳转到对应首页
      if (to.path === '/') {
        return next(getHomePath(userRole));
      }
      // 否则跳回首页
      return next(from.path !== '/' ? from.path : getHomePath(userRole));
    }
  }
  
  next();
});

export default router;
