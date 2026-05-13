<template>
  <div class="dashboard">
    <div class="page-header">
      <div class="page-header-left">
        <h1 class="page-title">控制台</h1>
        <p class="page-subtitle">欢迎使用AI项目管理系统 - {{ ROLE_MODULES[userRole]?.[0] || '通用用户' }}</p>
      </div>
    </div>

    <!-- 角色功能介绍 -->
    <el-card shadow="hover" class="role-intro-card">
      <template #header>
        <div class="card-header">
          <span>功能权限介绍</span>
        </div>
      </template>
      <div class="role-intro-content">
        <el-tag v-if="currentUser" type="success" size="large">
          当前用户: {{ currentUser.username }} ({{ userRole }})
        </el-tag>
        <ul class="feature-list">
          <li v-for="(feature, index) in ROLE_MODULES[userRole]" :key="index">
            <el-icon><CircleCheck /></el-icon>
            {{ feature }}
          </li>
        </ul>
      </div>
    </el-card>

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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Folder, Document, Warning, CircleClose, CircleCheck } from '@element-plus/icons-vue';
import * as echarts from 'echarts';
import apiClient from '../utils/api';
import type { Project, DashboardStats, RiskAlert, User } from '../types';
import { getRoleType, ROLE_MODULES, type RoleType } from '../utils/rolePermission';

const router = useRouter();

// 获取当前用户信息和角色
const getCurrentUser = (): User | null => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    return JSON.parse(userStr);
  }
  return null;
};

const currentUser = ref<User | null>(getCurrentUser());
const userRole = computed<RoleType>(() => {
  if (currentUser.value && currentUser.value.roleId) {
    return getRoleType(currentUser.value.roleId);
  }
  return 'guest';
});

const stats = ref<DashboardStats>({
  totalProjects: 0,
  totalTasks: 0,
  totalRisks: 0,
  totalBugs: 0
});

const projects = ref<Project[]>([]);
const riskAlerts = ref<RiskAlert[]>([]);

const projectChartRef = ref<HTMLElement | null>(null);
const taskChartRef = ref<HTMLElement | null>(null);
const riskChartRef = ref<HTMLElement | null>(null);

let projectChart: echarts.ECharts | null = null;
let taskChart: echarts.ECharts | null = null;
let riskChart: echarts.ECharts | null = null;

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
    const projectsResult: any = await apiClient.get('/project/list');
    projects.value = projectsResult.data || [];
    stats.value.totalProjects = projects.value.length;

    let totalTasks = 0;
    let totalRisks = 0;
    let totalBugs = 0;

    // 并行请求所有项目的数据
    const projectPromises = projects.value.map(async (project) => {
      try {
        // 并行请求该项目的任务、风险、缺陷数据
        const [tasksResult, risksResult, bugsResult] = await Promise.all([
          apiClient.get(`/task/project/${project.id}`),
          apiClient.get(`/risk/project/${project.id}`),
          apiClient.get(`/bug/project/${project.id}`)
        ]);

        return {
          tasks: tasksResult.data?.length || 0,
          risks: risksResult.data?.length || 0,
          bugs: bugsResult.data?.length || 0
        };
      } catch {
        // 如果某个项目的请求失败，返回0值，不影响其他项目
        return { tasks: 0, risks: 0, bugs: 0 };
      }
    });

    // 等待所有项目的数据请求完成
    const results = await Promise.all(projectPromises);

    // 汇总统计数据
    results.forEach(result => {
      totalTasks += result.tasks;
      totalRisks += result.risks;
      totalBugs += result.bugs;
    });

    stats.value.totalTasks = totalTasks;
    stats.value.totalRisks = totalRisks;
    stats.value.totalBugs = totalBugs;

    updateCharts();
  } catch (error) {
    console.error('获取数据失败', error);
  }
};

const updateCharts = () => {
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

  // 并行请求所有项目的任务数据
  const projectPromises = projects.value.map(async (project) => {
    try {
      const tasksResult: any = await apiClient.get(`/task/project/${project.id}`);
      const overdueTasks = (tasksResult.data || []).filter((t: any) => {
        if (!t.endDate || t.status === 'done') return false;
        return new Date(t.endDate) < new Date();
      });

      return overdueTasks.map((task: any) => ({
        title: `项目"${project.name}"存在延期任务`,
        description: `任务"${task.name}"已过期，但状态为"${getStatusName(task.status)}"`,
        type: 'warning'
      }));
    } catch {
      return [];
    }
  });

  // 等待所有项目的数据请求完成
  const results = await Promise.all(projectPromises);

  // 汇总所有风险预警
  riskAlerts.value = results.flat();

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
  padding: 0;
}

.role-intro-card {
  margin-bottom: 24px;
}

.role-intro-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #606266;
}

.feature-list li .el-icon {
  color: #67c23a;
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
