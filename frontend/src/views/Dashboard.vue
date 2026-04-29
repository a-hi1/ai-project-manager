<template>
  <div class="dashboard">
    <el-container>
      <el-header>
        <div class="header-left">
          <div class="logo">
            <el-icon :size="24"><Management /></el-icon>
            <span class="logo-text">AI项目管理系统</span>
          </div>
        </div>
        <div class="header-right">
          <NotificationPanel />
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :icon="User" />
              <span class="username">{{ user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-container>
        <el-aside :width="isSidebarCollapsed ? '64px' : '220px'">
          <div class="sidebar-toggle" @click="toggleSidebar">
            <el-icon :size="20">
              <Fold v-if="!isSidebarCollapsed" />
              <Expand v-else />
            </el-icon>
          </div>
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            :collapse="isSidebarCollapsed"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/projects">
              <el-icon><Folder /></el-icon>
              <template #title>项目列表</template>
            </el-menu-item>
            <el-menu-item index="/project/create">
              <el-icon><Plus /></el-icon>
              <template #title>创建项目</template>
            </el-menu-item>
            <el-menu-item index="/operation-log">
              <el-icon><Document /></el-icon>
              <template #title>操作日志</template>
            </el-menu-item>
            <el-divider />
            <el-sub-menu index="ai">
              <template #title>
                <el-icon><ChatDotRound /></el-icon>
                <span>AI辅助</span>
              </template>
              <el-menu-item index="ai-chat">
                <el-icon><ChatDotRound /></el-icon>
                <template #title>AI助手</template>
              </el-menu-item>
              <el-menu-item index="ai-requirement">
                <el-icon><MagicStick /></el-icon>
                <template #title>需求解析</template>
              </el-menu-item>
              <el-menu-item index="ai-task">
                <el-icon><List /></el-icon>
                <template #title>任务拆分</template>
              </el-menu-item>
              <el-menu-item index="ai-knowledge">
                <el-icon><Files /></el-icon>
                <template #title>知识库</template>
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>

        <el-main>
          <div class="page-header">
            <div class="page-header-left">
              <h1 class="page-title">控制台</h1>
              <p class="page-subtitle">欢迎使用AI项目管理系统</p>
            </div>
          </div>

          <el-row :gutter="20" class="stat-row">
            <el-col :xs="24" :sm="12" :md="6">
              <div class="stat-card">
                <div class="stat-icon project-icon">
                  <el-icon :size="28"><Folder /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalProjects }}</div>
                  <div class="stat-label">项目总数</div>
                </div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <div class="stat-card">
                <div class="stat-icon task-icon">
                  <el-icon :size="28"><Document /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalTasks }}</div>
                  <div class="stat-label">任务总数</div>
                </div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <div class="stat-card">
                <div class="stat-icon risk-icon">
                  <el-icon :size="28"><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalRisks }}</div>
                  <div class="stat-label">风险总数</div>
                </div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6">
              <div class="stat-card">
                <div class="stat-icon bug-icon">
                  <el-icon :size="28"><CircleClose /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.totalBugs }}</div>
                  <div class="stat-label">缺陷总数</div>
                </div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20" class="chart-row">
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>项目进度概览</span>
                  </div>
                </template>
                <div ref="projectChartRef" style="width: 100%; height: 280px;"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>任务状态分布</span>
                  </div>
                </template>
                <div ref="taskChartRef" style="width: 100%; height: 280px;"></div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" class="chart-row">
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>风险分析</span>
                    <el-button type="primary" link @click="checkRiskAlerts">刷新</el-button>
                  </div>
                </template>
                <div ref="riskChartRef" style="width: 100%; height: 280px;"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :lg="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>项目列表</span>
                    <el-button type="primary" link @click="router.push('/projects')">查看全部</el-button>
                  </div>
                </template>
                <el-table :data="projects" style="width: 100%" :show-header="true">
                  <el-table-column prop="name" label="项目名称" min-width="150" show-overflow-tooltip />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getStatusType(scope.row.status)" size="small">
                        {{ getStatusName(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="80" align="center">
                    <template #default="scope">
                      <el-button type="primary" link @click="viewProject(scope.row.id)">查看</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
          </el-row>

          <el-card shadow="hover" class="alert-card">
            <template #header>
              <div class="card-header">
                <span>风险预警</span>
                <el-button type="primary" link @click="checkRiskAlerts">刷新预警</el-button>
              </div>
            </template>
            <div v-if="riskAlerts.length === 0" class="empty-state">
              <el-icon :size="48" color="#dcdfe6"><CircleCheck /></el-icon>
              <p>暂无风险预警</p>
            </div>
            <el-alert
              v-for="(alert, index) in riskAlerts"
              :key="index"
              :title="alert.title"
              :type="alert.type"
              :description="alert.description"
              show-icon
              closable
              class="alert-item"
            />
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  User,
  Management,
  Folder,
  Plus,
  Document,
  Warning,
  CircleClose,
  ArrowDown,
  Expand,
  Fold,
  CircleCheck,
  ChatDotRound,
  MagicStick,
  Files,
  List
} from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import NotificationPanel from '../components/NotificationPanel.vue';

const router = useRouter();
const user = ref<any>(null);
const activeMenu = ref('/');
const isSidebarCollapsed = ref(false);

const stats = ref({
  totalProjects: 0,
  totalTasks: 0,
  totalRisks: 0,
  totalBugs: 0
});

const projects = ref<any[]>([]);
const riskAlerts = ref<any[]>([]);

const projectChartRef = ref<HTMLElement | null>(null);
const taskChartRef = ref<HTMLElement | null>(null);
const riskChartRef = ref<HTMLElement | null>(null);

let projectChart: echarts.ECharts | null = null;
let taskChart: echarts.ECharts | null = null;
let riskChart: echarts.ECharts | null = null;

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
};

const handleCommand = (command: string) => {
  if (command === 'logout') {
    logout();
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中');
  }
};

const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  ElMessage.success('退出登录成功');
  router.push('/login');
};

const handleMenuSelect = (key: string) => {
  console.log('菜单选择:', key);
  if (key === 'ai-chat') {
    router.push('/ai-chat');
  } else if (key === 'ai-requirement') {
    router.push('/ai-requirement');
  } else if (key === 'ai-task') {
    router.push('/ai-task');
  } else if (key === 'ai-knowledge') {
    router.push('/ai-knowledge');
  } else if (key.startsWith('/')) {
    router.push(key);
  }
};

const viewProject = (id: number) => {
  router.push(`/project/${id}`);
};

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'info',
    in_progress: 'warning',
    completed: 'success'
  };
  return typeMap[status] || 'info';
};

const getStatusName = (status: string) => {
  const nameMap: Record<string, string> = {
    pending: '待启动',
    in_progress: '进行中',
    completed: '已完成'
  };
  return nameMap[status] || status;
};

const fetchDashboardData = async () => {
  try {
    const token = localStorage.getItem('token');
    
    if (!token) {
      console.error('未找到token，请先登录');
      return;
    }

    const projectsResponse = await fetch('http://localhost:8080/api/project/list', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    if (!projectsResponse.ok) {
      console.error('获取项目列表失败:', projectsResponse.status, projectsResponse.statusText);
      if (projectsResponse.status === 403) {
        console.error('403错误:可能是token无效或已过期，请重新登录');
      }
      return;
    }
    
    const projectsResult = await projectsResponse.json();
    if (projectsResult.success) {
      projects.value = projectsResult.data || [];
      stats.value.totalProjects = projectsResult.data.length;
    }

    let totalTasks = 0;
    let totalRisks = 0;
    let totalBugs = 0;

    for (const project of projectsResult.data || []) {
      const tasksResponse = await fetch(`http://localhost:8080/api/task/project/${project.id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (tasksResponse.ok) {
        const tasksResult = await tasksResponse.json();
        if (tasksResult.success) {
          totalTasks += tasksResult.data.length;
        }
      }

      const risksResponse = await fetch(`http://localhost:8080/api/risk/project/${project.id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (risksResponse.ok) {
        const risksResult = await risksResponse.json();
        if (risksResult.success) {
          totalRisks += risksResult.data.length;
        }
      }

      const bugsResponse = await fetch(`http://localhost:8080/api/bug/project/${project.id}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (bugsResponse.ok) {
        const bugsResult = await bugsResponse.json();
        if (bugsResult.success) {
          totalBugs += bugsResult.data.length;
        }
      }
    }

    stats.value.totalTasks = totalTasks;
    stats.value.totalRisks = totalRisks;
    stats.value.totalBugs = totalBugs;

    updateCharts();
  } catch (error) {
    console.error('获取数据失败', error);
  }
};

const updateCharts = () => {
  const chartColor = '#409EFF';

  if (projectChart) {
    projectChart.setOption({
      tooltip: { trigger: 'item', confine: true },
      legend: { bottom: 10, left: 'center' },
      series: [{
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        data: [
          { value: projects.value.filter(p => p.status === 'pending').length, name: '待启动', itemStyle: { color: '#909399' } },
          { value: projects.value.filter(p => p.status === 'in_progress').length, name: '进行中', itemStyle: { color: '#E6A23C' } },
          { value: projects.value.filter(p => p.status === 'completed').length, name: '已完成', itemStyle: { color: '#67C23A' } }
        ]
      }]
    });
  }

  if (taskChart) {
    taskChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['待处理', '进行中', '待审核', '已完成'],
        axisTick: { alignWithLabel: true }
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'bar',
        barWidth: '50%',
        data: [
          { value: Math.round(stats.value.totalTasks * 0.3), itemStyle: { color: '#909399' } },
          { value: Math.round(stats.value.totalTasks * 0.25), itemStyle: { color: '#409EFF' } },
          { value: Math.round(stats.value.totalTasks * 0.15), itemStyle: { color: '#E6A23C' } },
          { value: Math.round(stats.value.totalTasks * 0.3), itemStyle: { color: '#67C23A' } }
        ],
        itemStyle: { borderRadius: [4, 4, 0, 0] }
      }]
    });
  }

  if (riskChart) {
    riskChart.setOption({
      tooltip: { trigger: 'item', confine: true },
      legend: { bottom: 10, left: 'center' },
      series: [{
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 'bold' }
        },
        data: [
          { value: Math.round(stats.value.totalRisks * 0.4), name: '已识别', itemStyle: { color: '#F56C6C' } },
          { value: Math.round(stats.value.totalRisks * 0.3), name: '监控中', itemStyle: { color: '#E6A23C' } },
          { value: Math.round(stats.value.totalRisks * 0.3), name: '已解决', itemStyle: { color: '#67C23A' } }
        ]
      }]
    });
  }
};

const checkRiskAlerts = async () => {
  riskAlerts.value = [];

  for (const project of projects.value) {
    const token = localStorage.getItem('token');

    const tasksResponse = await fetch(`http://localhost:8080/api/task/project/${project.id}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const tasksResult = await tasksResponse.json();

    if (tasksResult.success) {
      const overdueTasks = tasksResult.data.filter((t: any) => {
        if (!t.endDate || t.status === 'done') return false;
        return new Date(t.endDate) < new Date();
      });

      for (const task of overdueTasks) {
        riskAlerts.value.push({
          title: `项目"${project.name}"存在延期任务`,
          description: `任务"${task.name}"已过期，但状态为"${getStatusName(task.status)}"`,
          type: 'warning'
        });
      }
    }
  }

  if (riskAlerts.value.length === 0) {
    ElMessage.success('暂无风险预警');
  } else {
    ElMessage.warning(`发现${riskAlerts.value.length}个风险预警`);
  }
};

const initCharts = () => {
  if (projectChartRef.value) {
    projectChart = echarts.init(projectChartRef.value);
  }
  if (taskChartRef.value) {
    taskChart = echarts.init(taskChartRef.value);
  }
  if (riskChartRef.value) {
    riskChart = echarts.init(riskChartRef.value);
  }
};

onMounted(() => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    user.value = JSON.parse(userStr);
  }

  initCharts();
  fetchDashboardData();
});

onUnmounted(() => {
  projectChart?.dispose();
  taskChart?.dispose();
  riskChart?.dispose();
});
</script>

<style scoped>
.dashboard {
  height: 100vh;
  background-color: #f5f7fa;
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  color: white;
  padding: 0 20px;
  height: 60px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 12px;
  border-radius: 20px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.username {
  font-size: 14px;
}

.el-aside {
  background-color: #ffffff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  transition: width 0.3s ease;
  overflow-x: hidden;
}

.sidebar-toggle {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 48px;
  cursor: pointer;
  color: #606266;
  border-bottom: 1px solid #ebeef5;
  transition: color 0.3s;
}

.sidebar-toggle:hover {
  color: #409EFF;
}

.sidebar-menu {
  border-right: none;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

.el-main {
  padding: 20px;
  overflow-y: auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 16px;
}

.project-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.task-icon {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
}

.risk-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.bug-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.alert-card {
  margin-top: 20px;
}

.alert-item {
  margin-bottom: 12px;
}

.alert-item:last-child {
  margin-bottom: 0;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-state p {
  margin-top: 12px;
  font-size: 14px;
}

@media (max-width: 768px) {
  .el-header {
    padding: 0 12px;
  }

  .logo-text {
    display: none;
  }

  .el-main {
    padding: 12px;
  }

  .page-title {
    font-size: 20px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
  }

  .stat-value {
    font-size: 24px;
  }
}
</style>
