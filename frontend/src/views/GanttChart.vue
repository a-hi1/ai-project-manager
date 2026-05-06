<template>
  <div class="gantt-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button type="primary" @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回项目详情
        </el-button>
        <div class="title-section">
          <div class="title-icon">
            <el-icon :size="30"><Calendar /></el-icon>
          </div>
          <div class="title-content">
            <h1>项目甘特图</h1>
            <p class="subtitle">任务进度可视化管理</p>
          </div>
        </div>
      </div>
      <div class="header-buttons">
        <el-button type="primary" @click="showTaskDialog = true" class="action-btn">
          <el-icon><Plus /></el-icon>
          添加任务
        </el-button>
        <el-button type="success" @click="showMilestoneDialog = true" class="action-btn">
          <el-icon><Flag /></el-icon>
          添加里程碑
        </el-button>
      </div>
    </div>

    <!-- 图表容器 -->
    <div class="chart-card">
      <!-- 工具栏 -->
      <div class="chart-toolbar">
        <div class="toolbar-left">
          <div class="filter-group">
            <div class="filter-label">
              <el-icon><Document /></el-icon>
              <span>状态</span>
            </div>
            <el-select v-model="filterStatus" placeholder="全部" clearable size="default" style="width: 110px" @change="renderChart">
              <el-option label="待处理" value="todo" />
              <el-option label="进行中" value="in_progress" />
              <el-option label="待审核" value="review" />
              <el-option label="已完成" value="done" />
            </el-select>
          </div>
          <div class="filter-group">
            <div class="filter-label">
              <el-icon><Flag /></el-icon>
              <span>优先级</span>
            </div>
            <el-select v-model="filterPriority" placeholder="全部" clearable size="default" style="width: 110px" @change="renderChart">
              <el-option label="高" value="high" />
              <el-option label="中" value="medium" />
              <el-option label="低" value="low" />
            </el-select>
          </div>
          <div class="filter-group">
            <div class="filter-label">
              <el-icon><Search /></el-icon>
              <span>搜索</span>
            </div>
            <el-input v-model="searchQuery" placeholder="搜索任务..." clearable size="default" style="width: 180px" @input="renderChart" />
          </div>
        </div>
        <div class="toolbar-right">
          <el-button size="default" @click="expandAll" class="tool-btn">
            <el-icon><Document /></el-icon>
            展开全部
          </el-button>
          <el-button size="default" @click="collapseAll" class="tool-btn">
            <el-icon><Folder /></el-icon>
            折叠全部
          </el-button>
          <el-tag type="info" effect="dark" size="default">
            {{ filteredTasks.length }} / {{ allTaskCount }} 项任务
          </el-tag>
        </div>
      </div>

      <!-- 图表主体 -->
      <div class="chart-body">
        <!-- 任务侧边栏 -->
        <div class="task-sidebar">
          <div class="sidebar-header">
            <div class="header-title">
              <el-icon><List /></el-icon>
              <span>任务列表</span>
            </div>
          </div>
          <div class="task-list" @wheel.prevent="handleWheel">
            <div
              v-for="(task, idx) in filteredTasks"
              :key="task.id ?? idx"
              class="task-row"
              :class="{ milestone: task.isMilestone }"
              :style="{ paddingLeft: (task.level * 20 + 16) + 'px' }"
              @click="editTask(task)"
            >
              <div
                v-if="task.hasChildren"
                class="fold-btn"
                @click.stop="toggleFold(task); renderChart()"
              >
                <el-icon :size="14">
                  <component :is="collapsedTasks.has(task.id ?? 0) ? ArrowRight : ArrowLeft" />
                </el-icon>
              </div>
              <div v-else class="fold-btn placeholder"></div>
              <div class="task-cell">
                <div class="task-name-row">
                  <span class="task-name">{{ task.name }}</span>
                </div>
                <div class="task-info">
                  <span class="task-date">
                    <el-icon :size="12"><Calendar /></el-icon>
                    {{ formatTaskDates(task) }}
                  </span>
                </div>
              </div>
              <el-tag :type="getStatusType(task)" effect="dark" size="small" class="task-tag">
                {{ getStatusText(task) }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- ECharts图表区域 -->
        <div class="chart-area" ref="chartEl"></div>
      </div>
    </div>

    <!-- 添加/编辑任务对话框 -->
    <el-dialog v-model="showTaskDialog" :title="isEditTask ? '编辑任务' : '添加任务'" width="580px" class="task-dialog" destroy-on-close>
      <el-form :model="taskForm" label-width="100px" class="task-form">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" size="large" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="父任务">
          <el-select v-model="taskForm.parentId" placeholder="无（顶层任务）" clearable style="width: 100%" size="large">
            <el-option v-for="t in flatAllTasks" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="taskForm.startDate" type="date" placeholder="选择开始日期" style="width: 100%" value-format="YYYY-MM-DD" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="taskForm.endDate" type="date" placeholder="选择结束日期" style="width: 100%" value-format="YYYY-MM-DD" size="large" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工期（天）">
              <el-input-number v-model="taskForm.duration" :min="1" style="width: 100%" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="taskForm.priority" placeholder="选择优先级" style="width: 100%" size="large">
                <el-option label="高" value="high">
                  <el-tag size="small" type="danger">高</el-tag>
                </el-option>
                <el-option label="中" value="medium">
                  <el-tag size="small" type="warning">中</el-tag>
                </el-option>
                <el-option label="低" value="low">
                  <el-tag size="small" type="info">低</el-tag>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务状态">
          <el-select v-model="taskForm.status" placeholder="选择状态" style="width: 100%" size="large">
            <el-option label="待处理" value="todo">
              <el-tag size="small" type="info">待处理</el-tag>
            </el-option>
            <el-option label="进行中" value="in_progress">
              <el-tag size="small" type="warning">进行中</el-tag>
            </el-option>
            <el-option label="待审核" value="review">
              <el-tag size="small" type="primary">待审核</el-tag>
            </el-option>
            <el-option label="已完成" value="done">
              <el-tag size="small" type="success">已完成</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="任务进度">
          <el-slider v-model="taskForm.progress" :min="0" :max="100" :step="5" show-input size="large" />
        </el-form-item>
        <el-form-item label="是否里程碑">
          <el-switch v-model="taskForm.isMilestone" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false" size="large">取消</el-button>
        <el-button type="primary" @click="saveTask" :loading="saving" size="large">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加里程碑对话框 -->
    <el-dialog v-model="showMilestoneDialog" title="添加里程碑" width="520px" class="task-dialog" destroy-on-close>
      <el-form :model="milestoneForm" label-width="100px" class="task-form">
        <el-form-item label="里程碑名称">
          <el-input v-model="milestoneForm.name" placeholder="请输入里程碑名称" size="large" />
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker v-model="milestoneForm.dueDate" type="date" placeholder="选择到期日期" style="width: 100%" value-format="YYYY-MM-DD" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMilestoneDialog = false" size="large">取消</el-button>
        <el-button type="primary" @click="saveMilestone" :loading="saving" size="large">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import {
  ArrowLeft,
  Plus,
  Calendar,
  Search,
  Document,
  Flag,
  List,
  Folder,
  ArrowRight
} from '@element-plus/icons-vue';
import apiClient from '../utils/api';
import type { Task, GanttTask, TaskStatus } from '../types';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const chartEl = ref<HTMLElement | null>(null);
let chart: echarts.ECharts | null = null;

const rawTasks = ref<Task[]>([]);
const showTaskDialog = ref(false);
const showMilestoneDialog = ref(false);
const isEditTask = ref(false);
const collapsedTasks = ref(new Set<number>());
const filterStatus = ref('');
const filterPriority = ref('');
const searchQuery = ref('');
const saving = ref(false);

const taskForm = ref({
  id: null as number | null,
  name: '',
  description: '',
  parentId: null as number | null,
  startDate: '',
  endDate: '',
  duration: 1,
  priority: 'medium',
  status: 'todo' as TaskStatus,
  projectId: projectId,
  isMilestone: false,
  level: 0,
  progress: 0
});

const milestoneForm = ref({
  name: '',
  dueDate: '',
  projectId: projectId
});

const COLORS: Record<string, { bg: string; border: string; text: string }> = {
  done: { bg: '#d4edda', border: '#28a745', text: '#155724' },
  completed: { bg: '#d4edda', border: '#28a745', text: '#155724' },
  in_progress: { bg: '#cce5ff', border: '#409eff', text: '#004085' },
  review: { bg: '#e6f7ff', border: '#1890ff', text: '#0050b3' },
  todo: { bg: '#e2e3e5', border: '#6c757d', text: '#383d41' },
  pending: { bg: '#e2e3e5', border: '#6c757d', text: '#383d41' }
};

const buildTaskTree = (tasks: Task[]) => {
  const map = new Map<number, GanttTask>();
  const roots: GanttTask[] = [];
  tasks.forEach(t => {
    const taskId = t.id ?? Date.now() + Math.random();
    map.set(taskId, { ...t, id: taskId, children: [], hasChildren: false, level: 0, isMilestone: !!t.milestoneId, progress: t.progress ?? 0 } as GanttTask);
  });
  tasks.forEach(t => {
    const taskId = t.id ?? 0;
    const node = map.get(taskId);
    if (!node) return;
    if (t.parentId && map.has(t.parentId)) {
      const parent = map.get(t.parentId);
      if (parent) {
        parent.children.push(node);
        parent.hasChildren = true;
      }
    } else {
      roots.push(node);
    }
  });
  return { roots, map };
};

const allTaskCount = computed(() => rawTasks.value.length);

const flatAllTasks = computed(() => {
  const result: GanttTask[] = [];
  const { roots } = buildTaskTree(rawTasks.value);
  const walk = (nodes: GanttTask[]) => { for (const n of nodes) { result.push(n); if (n.hasChildren) walk(n.children); } };
  walk(roots);
  return result;
});

const filteredTasks = computed(() => {
  const { roots } = buildTaskTree(rawTasks.value);
  const result: GanttTask[] = [];
  const walk = (nodes: GanttTask[]) => {
    for (const node of nodes) {
      let match = true;
      if (filterStatus.value && node.status !== filterStatus.value) match = false;
      if (filterPriority.value && node.priority !== filterPriority.value) match = false;
      if (searchQuery.value && !node.name.toLowerCase().includes(searchQuery.value.toLowerCase())) match = false;
      if (match) result.push(node);
      if (node.hasChildren && !collapsedTasks.value.has(node.id ?? 0)) walk(node.children);
    }
  };
  walk(roots);
  return result;
});

const toggleFold = (task: GanttTask) => {
  const taskId = task.id ?? 0;
  if (collapsedTasks.value.has(taskId)) collapsedTasks.value.delete(taskId);
  else collapsedTasks.value.add(taskId);
};

const expandAll = () => { collapsedTasks.value.clear(); renderChart(); };
const collapseAll = () => {
  const { roots } = buildTaskTree(rawTasks.value);
  const collect = (nodes: GanttTask[]) => { for (const n of nodes) { if (n.hasChildren) { collapsedTasks.value.add(n.id ?? 0); collect(n.children); } } };
  collect(roots);
  renderChart();
};

const formatTaskDates = (task: Task) => {
  if (task.startDate && task.endDate) {
    const s = new Date(task.startDate);
    const e = new Date(task.endDate);
    return `${s.getMonth() + 1}/${s.getDate()} - ${e.getMonth() + 1}/${e.getDate()}`;
  }
  return '无日期';
};

const getStatusType = (task: Task) => {
  const m: Record<string, string> = { todo: 'info', pending: 'info', in_progress: 'warning', review: 'primary', done: 'success', completed: 'success' };
  return m[task.status] || 'info';
};

const getStatusText = (task: Task) => {
  const m: Record<string, string> = { todo: '待处理', pending: '待处理', in_progress: '进行中', review: '待审核', done: '已完成', completed: '已完成' };
  return m[task.status] || task.status;
};

const getStatusTextByKey = (statusKey: string) => {
  const m: Record<string, string> = { todo: '待处理', pending: '待处理', in_progress: '进行中', review: '待审核', done: '已完成', completed: '已完成' };
  return m[statusKey] || statusKey;
};

const renderChart = () => {
  if (!chartEl.value) return;
  if (!chart) {
    chart = echarts.init(chartEl.value, undefined, { renderer: 'canvas' });
    window.addEventListener('resize', () => chart?.resize());
  }

  const tasks = filteredTasks.value;
  console.log('渲染甘特图，任务数量:', tasks.length);
  
  if (tasks.length === 0) { 
    chart.clear();
    chart.setOption({
      title: {
        text: '暂无任务数据',
        left: 'center',
        top: 'center',
        textStyle: { 
          color: '#909399',
          fontSize: 16,
          fontWeight: 500
        }
      }
    });
    return;
  }

  let minDate = new Date();
  let maxDate = new Date();
  
  const validStartDates = tasks
    .filter(t => t.startDate)
    .map(t => {
      const startDate = t.startDate as string;
      return new Date(startDate).getTime();
    });
  const validEndDates = tasks
    .filter(t => t.endDate)
    .map(t => {
      const endDate = t.endDate as string;
      return new Date(endDate).getTime();
    });
  const allDates = [...validStartDates, ...validEndDates];
  
  if (allDates.length > 0) {
    minDate = new Date(Math.min(...allDates));
    maxDate = new Date(Math.max(...allDates));
    minDate.setDate(minDate.getDate() - 7);
    maxDate.setDate(maxDate.getDate() + 7);
  } else {
    minDate.setDate(minDate.getDate() - 30);
    maxDate.setDate(maxDate.getDate() + 30);
  }

  const todayStr = new Date().toISOString().split('T')[0];

  const taskNames = tasks.map(t => t.name);
  const chartData = tasks.map((t, idx) => ({
    value: [
      t.startDate || minDate.toISOString().split('T')[0],
      t.endDate || maxDate.toISOString().split('T')[0],
      idx,
      t.name,
      t.progress || 0,
      t.status || 'todo',
      t.isMilestone || false
    ],
    itemStyle: {
      color: COLORS[t.status]?.border || COLORS.todo.border
    }
  }));

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.98)',
      borderColor: '#e5e7eb',
      borderWidth: 1,
      textStyle: { color: '#374151', fontSize: 13 },
      formatter: (params: any) => {
        if (!params.data) return '';
        const d = params.data.value;
        if (d[6]) {
          return `
            <div style="padding: 4px 0;">
              <div style="font-weight: 600; color: #e6a23c; margin-bottom: 6px; font-size: 14px;">🎯 ${d[3] || '里程碑'}</div>
              <div style="color: #6b7280;">日期：${d[0]}</div>
            </div>
          `;
        }
        return `
          <div style="padding: 4px 0;">
            <div style="font-weight: 600; color: #1f2937; margin-bottom: 8px; font-size: 14px;">${d[3]}</div>
            <div style="color: #6b7280; margin-bottom: 4px;">开始日期：${d[0]}</div>
            <div style="color: #6b7280; margin-bottom: 4px;">结束日期：${d[1]}</div>
            <div style="color: #6b7280; margin-bottom: 4px;">进度：${d[4]}%</div>
            <div style="color: #6b7280;">状态：${getStatusTextByKey(d[5])}</div>
          </div>
        `;
      }
    },
    grid: { left: 30, right: 40, top: 40, bottom: 70 },
    xAxis: {
      type: 'time',
      min: minDate.getTime(),
      max: maxDate.getTime(),
      axisLabel: {
        formatter: (val: number) => {
          const d = new Date(val);
          return `${d.getMonth() + 1}/${d.getDate()}`;
        },
        fontSize: 12,
        color: '#6b7280',
        margin: 10
      },
      splitLine: { 
        show: true, 
        lineStyle: { 
          color: '#f3f4f6',
          type: 'dashed'
        } 
      },
      axisLine: { lineStyle: { color: '#e5e7eb' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'category',
      data: taskNames,
      inverse: true,
      axisLabel: {
        fontSize: 13,
        color: '#374151',
        overflow: 'truncate',
        ellipsis: '...',
        width: 160,
        fontWeight: 500
      },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { 
        show: true, 
        lineStyle: { 
          color: '#f3f4f6',
          type: 'dashed'
        } 
      }
    },
    dataZoom: [
      {
        type: 'slider',
        start: 0,
        end: 100,
        height: 24,
        bottom: 10,
        borderColor: '#e5e7eb',
        fillerColor: 'rgba(64,158,255,0.15)',
        handleStyle: { 
          color: '#409eff', 
          borderColor: '#409eff',
          borderWidth: 1
        },
        textStyle: { fontSize: 11, color: '#6b7280' },
        brushSelect: false
      },
      {
        type: 'inside'
      }
    ],
    series: [
      {
        type: 'custom',
        renderItem: (_params: any, api: any) => {
          const taskIndex = api.value(2);
          const startDate = api.value(0);
          const endDate = api.value(1);
          const startCoord = api.coord([startDate, taskIndex]);
          const endCoord = api.coord([endDate, taskIndex]);
          const taskName = api.value(3);
          const progress = api.value(4);
          const status = api.value(5);
          const isMilestone = api.value(6);
          const barH = 26;
          const y = startCoord[1] - barH / 2;

          if (isMilestone) {
            const cx = startCoord[0];
            const cy = startCoord[1];
            const r = 10;
            return {
              type: 'group',
              children: [
                { 
                  type: 'circle', 
                  shape: { cx, cy, r }, 
                  style: { 
                    fill: '#e6a23c', 
                    stroke: '#d97706', 
                    lineWidth: 2
                  }, 
                  z2: 10 
                },
                {
                  type: 'text',
                  style: { 
                    text: '★', 
                    x: cx, 
                    y: cy + 4, 
                    fill: '#fff', 
                    font: 'bold 12px sans-serif', 
                    textAlign: 'center'
                  },
                  z2: 11
                }
              ]
            };
          }

          const colors = COLORS[status] || COLORS.todo;
          const barW = Math.max(endCoord[0] - startCoord[0], 10);
          const r = 5;

          const progressW = Math.max(barW * (progress / 100), 0);

          const children: any[] = [
            {
              type: 'rect',
              shape: { x: startCoord[0], y, width: barW, height: barH, r: r },
              style: { fill: colors.bg, stroke: colors.border, lineWidth: 1.5 }
            }
          ];

          if (progress > 0) {
            children.push({
              type: 'rect',
              shape: { x: startCoord[0], y, width: progressW, height: barH, r: [r, 0, 0, r] },
              style: { fill: colors.border, opacity: 0.4 }
            });
          }

          if (taskName && barW > 80) {
            children.push({
              type: 'text',
              style: { 
                text: taskName, 
                x: startCoord[0] + 10, 
                y: y + barH / 2 + 3, 
                fill: colors.text, 
                font: '12px "Microsoft YaHei"',
                textVerticalAlign: 'middle'
              },
              silent: true
            });
          }

          if (barW > 60) {
            children.push({
              type: 'text',
              style: { 
                text: `${progress}%`, 
                x: startCoord[0] + barW - 10, 
                y: y + barH / 2 + 3, 
                fill: colors.text, 
                font: 'bold 11px "Microsoft YaHei"', 
                textVerticalAlign: 'middle',
                textAlign: 'right'
              },
              silent: true
            });
          }

          return { type: 'group', children };
        },
        data: chartData,
        encode: { x: [0, 1], y: 2 },
        clip: true,
        z: 2
      },
      {
        type: 'line',
        markLine: {
          silent: true,
          symbol: 'none',
          lineStyle: { 
            color: '#f56c6c', 
            type: 'dashed', 
            width: 2
          },
          label: { 
            show: true, 
            formatter: '今天', 
            fontSize: 12, 
            color: '#f56c6c', 
            position: 'start',
            fontWeight: 500,
            backgroundColor: 'rgba(255,255,255,0.9)',
            padding: [2, 6]
          },
          data: [{ xAxis: todayStr }]
        }
      }
    ]
  };

  chart.setOption(option, { notMerge: true });
};

const handleWheel = (e: WheelEvent) => {
  if (!chart) return;
  const current = chart.getOption() as any;
  const dz = current.dataZoom?.[0];
  if (!dz) return;
  const start = dz.start ?? 0;
  const end = dz.end ?? 100;
  const range = end - start;
  const delta = e.deltaY > 0 ? range * 0.1 : -range * 0.1;
  const newStart = Math.max(0, Math.min(100 - range, start + delta));
  chart.dispatchAction({ type: 'dataZoom', start: newStart, end: newStart + range });
};

const fetchTasks = async () => {
  try {
    const apiResult: any = await apiClient.get(`/task/project/${projectId}`);
    console.log('获取任务数据:', apiResult);
    if (apiResult.success) {
      rawTasks.value = apiResult.data.map((t: any) => ({ ...t, level: t.level ?? (t.parentId ? 1 : 0) }));
      nextTick(() => renderChart());
    } else {
      ElMessage.error(apiResult.message || '获取任务失败');
      // 添加一些模拟数据用于测试
      addMockData();
    }
  } catch (error) {
    console.error('获取任务失败:', error);
    ElMessage.error('网络错误，使用模拟数据');
    // 添加一些模拟数据用于测试
    addMockData();
  }
};

const addMockData = () => {
  rawTasks.value = [
    { id: 1, projectId, name: '项目启动', description: '', startDate: '2026-04-20', endDate: '2026-04-25', status: 'done', parentId: null } as Task,
    { id: 2, projectId, name: '需求分析', description: '', startDate: '2026-04-22', endDate: '2026-04-30', status: 'in_progress', parentId: null } as Task,
    { id: 3, projectId, name: '技术设计', description: '', startDate: '2026-04-28', endDate: '2026-05-10', status: 'in_progress', parentId: null } as Task,
    { id: 4, projectId, name: '开发阶段', description: '', startDate: '2026-05-05', endDate: '2026-05-20', status: 'todo', parentId: null } as Task,
    { id: 5, projectId, name: 'MVP版本', description: '', startDate: '2026-04-30', endDate: '2026-04-30', status: 'todo', parentId: null, milestoneId: 1 } as Task
  ];
  nextTick(() => renderChart());
};

const saveTask = async () => {
  saving.value = true;
  try {
    const userData = JSON.parse(localStorage.getItem('user') || '{}');
    const taskData = { 
      ...taskForm.value, 
      projectId, 
      createdBy: userData.id 
    };
    
    // 先本地更新，让界面立即响应
    if (isEditTask.value && taskForm.value.id !== null) {
      const taskIndex = rawTasks.value.findIndex(t => t.id === taskForm.value.id);
      if (taskIndex !== -1) {
        const updateTask = { ...rawTasks.value[taskIndex] };
        // 只更新Task类型兼容的字段
        updateTask.name = taskForm.value.name;
        updateTask.description = taskForm.value.description;
        updateTask.parentId = taskForm.value.parentId;
        updateTask.startDate = taskForm.value.startDate;
        updateTask.endDate = taskForm.value.endDate;
        updateTask.duration = taskForm.value.duration;
        updateTask.priority = taskForm.value.priority as any;
        updateTask.status = taskForm.value.status;
        (updateTask as any).progress = taskForm.value.progress;
        rawTasks.value[taskIndex] = updateTask;
        rawTasks.value = [...rawTasks.value];
        nextTick(() => renderChart());
      }
    }
    
    let apiResult: any;
    if (isEditTask.value) {
      apiResult = await apiClient.put('/task/update', taskData);
    } else {
      apiResult = await apiClient.post('/task/create', taskData);
    }
    
    if (apiResult.success) {
      ElMessage.success(isEditTask.value ? '更新任务成功' : '创建任务成功');
      showTaskDialog.value = false;
      await fetchTasks();
      resetTaskForm();
    } else {
      ElMessage.error(apiResult.message || '操作失败');
      await fetchTasks();
    }
  } catch {
    ElMessage.error('网络错误');
    await fetchTasks();
  } finally {
    saving.value = false;
  }
};

const editTask = (task: GanttTask) => {
  if (task.isMilestone) return;
  isEditTask.value = true;
  taskForm.value = {
    id: task.id ?? null,
    name: task.name,
    description: task.description,
    parentId: task.parentId ?? null,
    startDate: task.startDate || '',
    endDate: task.endDate || '',
    duration: task.duration || 1,
    priority: task.priority || 'medium',
    status: task.status,
    projectId: projectId,
    isMilestone: task.isMilestone,
    level: task.level,
    progress: task.progress
  };
  showTaskDialog.value = true;
};

const saveMilestone = async () => {
  saving.value = true;
  try {
    const apiResult: any = await apiClient.post('/milestone/create', milestoneForm.value);
    if (apiResult.success) {
      ElMessage.success('创建里程碑成功');
      showMilestoneDialog.value = false;
      resetMilestoneForm();
    } else {
      ElMessage.error(apiResult.message || '操作失败');
    }
  } catch {
    ElMessage.error('网络错误');
  } finally {
    saving.value = false;
  }
};

const resetTaskForm = () => {
  taskForm.value = { 
    id: null, 
    name: '', 
    description: '', 
    parentId: null, 
    startDate: '', 
    endDate: '', 
    duration: 1, 
    priority: 'medium', 
    status: 'todo' as TaskStatus,
    projectId: projectId,
    isMilestone: false, 
    level: 0, 
    progress: 0 
  };
  isEditTask.value = false;
};

const resetMilestoneForm = () => { milestoneForm.value = { name: '', dueDate: '', projectId }; };

const goBack = () => { router.push(`/project/${projectId}`); };

watch([filterStatus, filterPriority, searchQuery], () => nextTick(() => renderChart()));

// 监听任务数据变化，自动重新渲染甘特图
watch(rawTasks, () => {
  nextTick(() => renderChart());
}, { deep: true });

onMounted(() => { fetchTasks(); });
</script>

<style scoped>
.gantt-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4ff 100%);
  padding: 20px;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.back-btn {
  border-radius: 8px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.title-content h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
}

.subtitle {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #909399;
}

.header-buttons {
  display: flex;
  gap: 12px;
}

.action-btn {
  border-radius: 8px;
  padding: 10px 18px;
}

.chart-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.chart-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(90deg, #fafafa 0%, white 100%);
  flex-shrink: 0;
  gap: 16px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tool-btn {
  border-radius: 6px;
}

.chart-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.task-sidebar {
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid #f0f0f0;
  background: #fafafa;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(90deg, #fafafa 0%, white 100%);
  flex-shrink: 0;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.task-list {
  flex: 1;
  overflow-y: auto;
}

.task-row {
  height: 48px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  gap: 8px;
  transition: all 0.2s ease;
}

.task-row:hover {
  background: #e8f4ff;
}

.task-row.milestone {
  background: #fff9e6;
}

.task-row.milestone:hover {
  background: #fef3d7;
}

.fold-btn {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  color: #909399;
  transition: color 0.2s ease;
  border-radius: 4px;
}

.fold-btn:hover {
  color: #409eff;
  background: rgba(64, 158, 255, 0.1);
}

.fold-btn.placeholder {
  visibility: hidden;
}

.task-cell {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.task-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.task-name {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #909399;
}

.task-tag {
  flex-shrink: 0;
}

.chart-area {
  flex: 1;
  min-width: 0;
  background: white;
}

.task-dialog {
  border-radius: 16px;
}

.task-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  padding: 20px 28px;
}

.task-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 17px;
  font-weight: 600;
}

.task-dialog :deep(.el-dialog__headerbtn) {
  top: 18px;
}

.task-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px;
}

.task-dialog :deep(.el-dialog__body) {
  padding: 28px;
}

.task-form {
  margin-top: 4px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .gantt-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 20px;
  }
  
  .header-left {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
  }
  
  .title-section {
    flex-wrap: wrap;
  }
  
  .task-sidebar {
    width: 240px;
  }
}

@media (max-width: 768px) {
  .title-content h1 {
    font-size: 19px;
  }
  
  .task-sidebar {
    width: 200px;
  }
}
</style>
