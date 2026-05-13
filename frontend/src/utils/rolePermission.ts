/**
 * 角色权限配置
 * 定义每个角色可以访问的功能模块和菜单
 * 完全按照项目文档实现
 */

import {
  Folder, Plus, Document, Edit, Calendar, List, Warning,
  Flag, TrendCharts, Reading,
  ChatDotRound, Upload, UploadFilled, MagicStick,
  Operation, Files, Box, Timer, View, User,
  House
} from '@element-plus/icons-vue';

// 角色类型定义
export type RoleType = 'admin' | 'pm' | 'developer' | 'tester' | 'product' | 'designer' | 'guest';

// 角色信息
export interface RoleInfo {
  code: RoleType;
  name: string;
  description: string;
}

// 菜单项定义
export interface MenuItem {
  index: string;
  title: string;
  icon: any;
  roles: RoleType[];
  children?: MenuItem[];
}

// 系统角色列表
export const ROLES: Record<RoleType, RoleInfo> = {
  admin: {
    code: 'admin',
    name: '系统管理员',
    description: '系统最高权限，可访问全部功能'
  },
  pm: {
    code: 'pm',
    name: '项目经理',
    description: '负责项目全周期管理'
  },
  developer: {
    code: 'developer',
    name: '开发者',
    description: '负责项目开发和任务执行'
  },
  tester: {
    code: 'tester',
    name: '测试工程师',
    description: '负责项目测试和缺陷管理'
  },
  product: {
    code: 'product',
    name: '产品经理',
    description: '负责产品规划和需求管理'
  },
  designer: {
    code: 'designer',
    name: 'UI设计师',
    description: '负责界面设计工作'
  },
  guest: {
    code: 'guest',
    name: '访客',
    description: '只读权限，仅可查看'
  }
};

// 系统菜单配置（完全按照文档权限设置）
export const SYSTEM_MENUS: MenuItem[] = [
  // ------------------------------
  // 首页
  // ------------------------------
  {
    index: '/dashboard',
    title: '首页',
    icon: House,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer', 'guest']
  },
  
  // ------------------------------
  // 项目管理模块
  // ------------------------------
  {
    index: '/projects',
    title: '项目列表',
    icon: Folder,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer', 'guest']
  },
  {
    index: '/project/create',
    title: '创建项目',
    icon: Plus,
    roles: ['admin', 'pm']
  },
  
  // ------------------------------
  // 系统管理模块
  // ------------------------------
  {
    index: '/users',
    title: '用户管理',
    icon: User,
    roles: ['admin']
  },
  {
    index: '/operation-log',
    title: '操作日志',
    icon: Operation,
    roles: ['admin', 'pm']
  },
  
  // ------------------------------
  // AI助手模块
  // ------------------------------
  {
    index: 'ai',
    title: 'AI智能助手',
    icon: ChatDotRound,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer'],
    children: [
      {
        index: '/ai-chat',
        title: 'AI对话',
        icon: ChatDotRound,
        roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer']
      },
      {
        index: '/ai-knowledge',
        title: '知识库',
        icon: UploadFilled,
        roles: ['admin', 'pm', 'developer', 'product', 'designer']
      },
      {
        index: '/ai-requirement',
        title: '需求解析',
        icon: MagicStick,
        roles: ['admin', 'pm', 'product']
      },
      {
        index: '/ai-task',
        title: '任务拆分',
        icon: List,
        roles: ['admin', 'pm']
      }
    ]
  }
];

// 仅用于权限验证的隐藏路由配置（完全按照文档权限设置）
const hiddenPermissionRoutes: MenuItem[] = [
  // 项目详情页
  {
    index: '/project/:id',
    title: '项目详情',
    icon: Document,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer', 'guest']
  },
  // 需求调研
  {
    index: '/project/:id/requirement',
    title: '需求调研',
    icon: Reading,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer', 'guest']
  },
  // 可行性分析
  {
    index: '/project/:id/feasibility',
    title: '可行性分析',
    icon: TrendCharts,
    roles: ['admin', 'pm', 'product', 'guest']
  },
  // 任务管理/进度追踪
  {
    index: '/project/:id/tasks',
    title: '任务管理',
    icon: List,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer', 'guest']
  },
  // 甘特图
  {
    index: '/project/:id/gantt',
    title: '甘特图',
    icon: Calendar,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer', 'guest']
  },
  // 看板
  {
    index: '/project/:id/kanban',
    title: '看板',
    icon: Box,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer', 'guest']
  },
  // 风险管理
  {
    index: '/project/:id/risks',
    title: '风险管理',
    icon: Warning,
    roles: ['admin', 'pm', 'developer', 'tester', 'guest']
  },
  // 缺陷管理
  {
    index: '/project/:id/bugs',
    title: '缺陷管理',
    icon: Flag,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'guest']
  },
  // 变更管理
  {
    index: '/project/:id/changes',
    title: '变更管理',
    icon: Edit,
    roles: ['admin', 'pm', 'developer', 'product', 'guest']
  },
  // 交付验收
  {
    index: '/project/:id/delivery',
    title: '交付验收',
    icon: Files,
    roles: ['admin', 'pm', 'developer', 'guest']
  },
  // 文档归档
  {
    index: '/project/:id/documents',
    title: '文档归档',
    icon: Upload,
    roles: ['admin', 'pm', 'developer', 'tester', 'product', 'designer', 'guest']
  },
  // 项目复盘
  {
    index: '/project/:id/retrospective',
    title: '项目复盘',
    icon: Timer,
    roles: ['admin', 'pm', 'product', 'guest']
  },
  // 项目结束报告
  {
    index: '/project/:id/end-report',
    title: '项目结束报告',
    icon: View,
    roles: ['admin', 'pm', 'product', 'guest']
  }
];

// 角色对应的功能模块描述（完全按照文档）
export const ROLE_MODULES: Record<RoleType, string[]> = {
  admin: [
    '项目全周期管理',
    '用户管理',
    '系统设置',
    'AI助手（全部功能）',
    '操作日志查看'
  ],
  pm: [
    '项目创建和管理',
    '任务分配和追踪',
    '甘特图和看板管理',
    '风险和变更管理',
    'AI助手（全部功能）',
    '报告生成'
  ],
  developer: [
    '任务执行和进度更新',
    '缺陷修复',
    '查看项目文档',
    'AI助手（对话、建议）',
    '文档上传'
  ],
  tester: [
    '缺陷报告和管理',
    '查看任务进度',
    '查看项目文档',
    'AI助手（对话）'
  ],
  product: [
    '需求管理',
    '可行性分析',
    '项目复盘和报告',
    'AI助手（全部功能）',
    '文档管理'
  ],
  designer: [
    '设计任务管理',
    '文档上传和查看',
    '查看项目进度',
    'AI助手（对话）'
  ],
  guest: [
    '查看项目列表',
    '查看项目详情',
    '查看任务进度',
    '查看文档（只读）'
  ]
};

// 检查用户是否有权限访问某个路由
export function hasPermission(role: RoleType, routePath: string): boolean {
  // 登录和注册页所有人都可以访问
  if (routePath === '/login' || routePath === '/register') {
    return true;
  }
  
  // 检查是否是项目详情相关的基础路由（用于权限验证）
  const checkRoutePermission = (menus: MenuItem[]): boolean => {
    for (const menu of menus) {
      // 直接匹配或匹配通配符路由
      if (matchRoute(menu.index, routePath) && menu.roles.includes(role)) {
        return true;
      }
      // 检查子菜单
      if (menu.children) {
        for (const child of menu.children) {
          if (matchRoute(child.index, routePath) && child.roles.includes(role)) {
            return true;
          }
        }
      }
    }
    return false;
  };
  
  // 检查显示的菜单
  if (checkRoutePermission(SYSTEM_MENUS)) {
    return true;
  }
  
  // 检查隐藏的权限路由
  if (checkRoutePermission(hiddenPermissionRoutes)) {
    return true;
  }
  
  return false;
}

// 路由匹配（支持通配符）
function matchRoute(pattern: string, path: string): boolean {
  if (pattern === path) {
    return true;
  }
  // 处理通配符路由，如 /project/:id -> 匹配 /project/1, /project/2 等
  if (pattern.includes(':')) {
    const patternParts = pattern.split('/');
    const pathParts = path.split('/');
    if (patternParts.length !== pathParts.length) {
      return false;
    }
    for (let i = 0; i < patternParts.length; i++) {
      if (patternParts[i].startsWith(':')) {
        continue; // 通配符部分直接通过
      }
      if (patternParts[i] !== pathParts[i]) {
        return false;
      }
    }
    return true;
  }
  return false;
}

// 获取用户角色对应的菜单列表
export function getMenusByRole(role: RoleType): MenuItem[] {
  const result: MenuItem[] = [];
  const seenIndexes = new Set<string>(); // 防止重复
  
  for (const menu of SYSTEM_MENUS) {
    // 检查主菜单权限
    const hasMenuPermission = menu.roles.includes(role);
    
    if (hasMenuPermission && !seenIndexes.has(menu.index)) {
      seenIndexes.add(menu.index);
      const menuCopy = { ...menu };
      
      // 过滤子菜单权限
      if (menuCopy.children) {
        menuCopy.children = menuCopy.children.filter(child => 
          child.roles.includes(role)
        );
      }
      
      result.push(menuCopy);
    }
  }
  
  return result;
}

// 获取用户角色名称
export function getRoleName(roleCode: string): string {
  const role = ROLES[roleCode as RoleType];
  return role ? role.name : '未知角色';
}

// 获取当前用户的首页路径（按角色）
export function getHomePath(_role: RoleType): string {
  return '/dashboard';
}

// 根据roleId获取角色类型
export function getRoleType(roleId: number): RoleType {
  const roleMap: Record<number, RoleType> = {
    1: 'admin',
    2: 'pm',
    3: 'developer',
    4: 'tester',
    5: 'product',
    6: 'designer',
    7: 'guest'
  };
  return roleMap[roleId] || 'guest';
}

export default {
  ROLES,
  SYSTEM_MENUS,
  ROLE_MODULES,
  hasPermission,
  getMenusByRole,
  getRoleName,
  getHomePath,
  getRoleType
};
