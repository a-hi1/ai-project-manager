import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('./views/Dashboard.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('./components/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('./components/Register.vue')
  },
  {
    path: '/projects',
    name: 'Projects',
    component: () => import('./views/Projects.vue')
  },
  {
    path: '/project/create',
    name: 'CreateProject',
    component: () => import('./views/CreateProject.vue')
  },
  {
    path: '/project/:id',
    name: 'ProjectDetail',
    component: () => import('./views/ProjectDetail.vue')
  },
  {
    path: '/project/:id/requirement',
    name: 'RequirementResearch',
    component: () => import('./views/RequirementResearch.vue')
  },
  {
    path: '/project/:id/feasibility',
    name: 'FeasibilityAnalysis',
    component: () => import('./views/FeasibilityAnalysis.vue')
  },
  {
    path: '/project/:id/tasks',
    name: 'ProgressTracking',
    component: () => import('./views/ProgressTracking.vue')
  },
  {
    path: '/project/:id/gantt',
    name: 'GanttChart',
    component: () => import('./views/GanttChart.vue')
  },
  {
    path: '/project/:id/kanban',
    name: 'KanbanBoard',
    component: () => import('./views/KanbanBoard.vue')
  },
  {
    path: '/project/:id/risks',
    name: 'RiskManagement',
    component: () => import('./views/RiskManagement.vue')
  },
  {
    path: '/project/:id/bugs',
    name: 'BugTracking',
    component: () => import('./views/BugTracking.vue')
  },
  {
    path: '/project/:id/changes',
    name: 'ChangeManagement',
    component: () => import('./views/ChangeManagement.vue')
  },
  {
    path: '/project/:id/delivery',
    name: 'DeliveryAcceptance',
    component: () => import('./views/DeliveryAcceptance.vue')
  },
  {
    path: '/project/:id/documents',
    name: 'DocumentArchive',
    component: () => import('./views/DocumentArchive.vue')
  },
  {
    path: '/project/:id/retrospective',
    name: 'ProjectRetrospective',
    component: () => import('./views/ProjectRetrospective.vue')
  },
  {
    path: '/operation-log',
    name: 'OperationLog',
    component: () => import('./views/OperationLog.vue')
  },
  {
    path: '/ai-chat',
    name: 'AiChatAssistant',
    component: () => import('./views/AiChatAssistant.vue')
  },
  {
    path: '/ai-requirement',
    name: 'AiRequirementParser',
    component: () => import('./views/AiRequirementParser.vue')
  },
  {
    path: '/ai-task',
    name: 'AiTaskSplitter',
    component: () => import('./views/AiTaskSplitter.vue')
  },
  {
    path: '/ai-knowledge',
    name: 'KnowledgeBase',
    component: () => import('./views/KnowledgeBase.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from) => {
  const token = localStorage.getItem('token');
  if (to.path !== '/login' && to.path !== '/register' && !token) {
    return '/login';
  }
});

export default router;
