// 项目类型
export interface Project {
  id?: number;
  name: string;
  description: string;
  status: ProjectStatus;
  startDate: string;
  endDate: string;
  createdBy?: number;
}

export type ProjectStatus = 'pending' | 'in_progress' | 'completed';

// 任务类型
export interface Task {
  id?: number;
  projectId: number;
  name: string;
  description: string;
  status: TaskStatus;
  startDate?: string;
  endDate?: string;
  duration?: number;
  assigneeId?: number;
  assigneeName?: string;
  priority?: TaskPriority;
  parentId?: number | null;
  dependencies?: string[];
  children?: Task[];
  milestoneId?: number;
  progress?: number;
}

export type TaskStatus = 'todo' | 'in_progress' | 'review' | 'done';
export type TaskPriority = 'low' | 'medium' | 'high' | 'urgent';

// 里程碑类型
export interface Milestone {
  id?: number;
  projectId: number;
  name: string;
  date: string;
  description: string;
}

// 风险类型
export interface Risk {
  id?: number;
  projectId: number;
  title: string;
  description: string;
  level: RiskLevel;
  status: RiskStatus;
  mitigation: string;
  createdBy?: number;
}

export type RiskLevel = 'low' | 'medium' | 'high' | 'critical';
export type RiskStatus = 'identified' | 'monitoring' | 'resolved';

// 缺陷类型
export interface Bug {
  id?: number;
  projectId: number;
  title: string;
  description: string;
  severity: BugSeverity;
  status: BugStatus;
  assigneeId?: number;
  assigneeName?: string;
  reporterId?: number;
  reporterName?: string;
}

export type BugSeverity = 'low' | 'medium' | 'high' | 'critical';
export type BugStatus = 'open' | 'in_progress' | 'resolved' | 'closed';

// 变更请求类型
export interface ChangeRequest {
  id?: number;
  projectId: number;
  title: string;
  description: string;
  reason: string;
  impact: string;
  status: ChangeStatus;
  requesterId?: number;
  requesterName?: string;
}

export type ChangeStatus = 'pending' | 'approved' | 'rejected';

// 交付物类型
export interface Deliverable {
  id?: number;
  projectId: number;
  name: string;
  description: string;
  status: DeliverableStatus;
  fileUrl?: string;
}

export type DeliverableStatus = 'pending' | 'submitted' | 'approved' | 'rejected';

// 用户类型
export interface User {
  id?: number;
  username: string;
  email: string;
  password?: string;
  roleId: number;
  status: number;
}

// 通知类型
export interface Notification {
  id?: number;
  userId: number;
  title: string;
  content: string;
  type: NotificationType;
  isRead: boolean;
  createdAt: string;
}

export type NotificationType = 'info' | 'warning' | 'error' | 'success';

// AI对话类型
export interface ChatMessage {
  id?: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp?: string;
}

export interface Conversation {
  id: string;
  title: string;
  messages: ChatMessage[];
  createdAt: string;
}

// 通用API响应类型
export interface ApiResponse<T = unknown> {
  success: boolean;
  data: T;
  message?: string;
  total?: number;
  token?: string;
  user?: User;
}

// 甘特图任务树节点
export interface GanttTask extends Task {
  children: GanttTask[];
  level: number;
  hasChildren: boolean;
  isMilestone: boolean;
  progress: number;
  milestoneDate?: string;
}

// 仪表板统计
export interface DashboardStats {
  totalProjects: number;
  totalTasks: number;
  totalRisks: number;
  totalBugs: number;
}

// 风险预警
export interface RiskAlert {
  title: string;
  description: string;
  type: 'warning' | 'error' | 'info';
}

// 项目详情模块
export interface ProjectModule {
  name: string;
  path: string;
  description: string;
  icon: unknown;
  color: string;
}
