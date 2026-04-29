<template>
  <div class="progress-tracking">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="router.push(`/project/${projectId}`)">
              <el-icon><ArrowLeft /></el-icon>
              返回项目详情
            </el-button>
            <h2>进度跟踪</h2>
          </div>
          <el-button type="primary" @click="showTaskDialogFunc">
            <el-icon><Plus /></el-icon>
            添加任务
          </el-button>
        </div>
      </template>

      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon"><Document /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ tasks.length }}</div>
              <div class="stat-label">总任务数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon success"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ completedTasksCount }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon warning"><Clock /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ inProgressTasksCount }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon primary"><TrendCharts /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ projectProgress }}%</div>
              <div class="stat-label">整体进度</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="任务列表" name="taskList">
          <el-table :data="tasks" style="width: 100%" v-loading="loading" row-key="id" :tree-props="{ children: 'children' }">
            <el-table-column prop="name" label="任务名称" min-width="250" />
            <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
            <el-table-column label="父任务" width="150">
              <template #default="scope">
                <span v-if="scope.row.parentId" class="parent-task-tag">
                  {{ getParentTaskName(scope.row.parentId) }}
                </span>
                <span v-else class="no-parent-tag">-</span>
              </template>
            </el-table-column>
            <el-table-column label="工期" width="100">
              <template #default="scope">{{ scope.row.duration }}天</template>
            </el-table-column>
            <el-table-column prop="priority" label="优先级" width="100">
              <template #default="scope">
                <el-tag :type="getPriorityType(scope.row.priority)">{{ getPriorityName(scope.row.priority) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="progress" label="进度" width="200">
              <template #default="scope">
                <el-progress :percentage="scope.row.progress || 0" :stroke-width="10" :status="scope.row.progress === 100 ? 'success' : undefined" />
              </template>
            </el-table-column>
            <el-table-column label="日期范围" width="200">
              <template #default="scope">
                <div class="date-range">
                  <div>{{ formatDate(scope.row.startDate) }}</div>
                  <el-icon class="arrow"><ArrowRight /></el-icon>
                  <div>{{ formatDate(scope.row.endDate) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="300" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="addSubTask(scope.row)">
                  <el-icon><Plus /></el-icon>
                  子任务
                </el-button>
                <el-button type="primary" size="small" @click="editTask(scope.row)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="success" size="small" @click="showWorkLogDialog(scope.row)">
                  <el-icon><Document /></el-icon>
                  工时
                </el-button>
                <el-button type="danger" size="small" @click="deleteTask(scope.row.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="工时记录" name="workLogs">
          <el-table :data="workLogs" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="taskName" label="任务名称" min-width="150" />
            <el-table-column prop="hours" label="工时" width="100">
              <template #default="scope">{{ scope.row.hours }} 小时</template>
            </el-table-column>
            <el-table-column prop="workDate" label="工作日期" width="120">
              <template #default="scope">{{ formatDate(scope.row.workDate) }}</template>
            </el-table-column>
            <el-table-column prop="description" label="工作描述" show-overflow-tooltip />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 添加/编辑任务对话框 -->
    <el-dialog v-model="showTaskDialog" :title="isEditTask ? '编辑任务' : '添加任务'" width="700px">
      <el-form :model="taskForm" label-width="120px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input v-model="taskForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="父任务" prop="parentId">
          <el-select v-model="taskForm.parentId" placeholder="请选择父任务（可选）" clearable style="width: 100%">
            <el-option label="无（顶级任务）" :value="null" />
            <el-option 
              v-for="task in availableParentTasks" 
              :key="task.id" 
              :label="task.name" 
              :value="task.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker 
            v-model="taskForm.startDate" 
            type="date" 
            placeholder="选择开始日期" 
            style="width: 100%" 
            @change="calculateDuration" 
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker 
            v-model="taskForm.endDate" 
            type="date" 
            placeholder="选择结束日期" 
            style="width: 100%" 
            @change="calculateDuration" 
          />
        </el-form-item>
        <el-form-item label="工期（天）">
          <el-input-number 
            v-model="taskForm.duration" 
            :min="1" 
            style="width: 100%" 
            @change="calculateEndDate" 
          />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="taskForm.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="taskForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待处理" value="todo" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="待审核" value="review" />
            <el-option label="已完成" value="done" />
          </el-select>
        </el-form-item>
        <el-form-item label="进度" prop="progress">
          <el-slider v-model="taskForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTask" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 填报工时对话框 -->
    <el-dialog v-model="showWorkLogDialogVisible" title="填报工时" width="500px">
      <el-form :model="workLogForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="workLogForm.taskName" disabled />
        </el-form-item>
        <el-form-item label="工时（小时）">
          <el-input-number v-model="workLogForm.hours" :min="0.5" :max="24" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工作日期">
          <el-date-picker v-model="workLogForm.workDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工作描述">
          <el-input v-model="workLogForm.description" type="textarea" :rows="3" placeholder="请输入工作描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWorkLogDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWorkLog" :loading="saving">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Plus, Edit, Delete, Document, CircleCheck, Clock, TrendCharts, ArrowRight } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const tasks = ref<any[]>([]);
const workLogs = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);
const activeTab = ref('taskList');
const showTaskDialog = ref(false);
const showWorkLogDialogVisible = ref(false);
const isEditTask = ref(false);

const taskForm = ref({
  id: null,
  projectId: projectId,
  parentId: null,
  name: '',
  description: '',
  startDate: '',
  endDate: '',
  duration: 7,
  priority: 'medium',
  status: 'todo',
  progress: 0,
  createdBy: null
});

const workLogForm = ref({
  taskId: null,
  taskName: '',
  hours: 1,
  workDate: new Date(),
  description: ''
});

const currentUserId = computed(() => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user).id : null;
});

const availableParentTasks = computed(() => {
  if (!taskForm.value.id) {
    return tasks.value;
  }
  return tasks.value.filter(t => t.id !== taskForm.value.id);
});

const completedTasksCount = computed(() => {
  return tasks.value.filter(t => t.status === 'done').length;
});

const inProgressTasksCount = computed(() => {
  return tasks.value.filter(t => t.status === 'in_progress').length;
});

const projectProgress = computed(() => {
  if (tasks.value.length === 0) return 0;
  const totalProgress = tasks.value.reduce((sum, t) => sum + (t.progress || 0), 0);
  return Math.round(totalProgress / tasks.value.length);
});

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

const getParentTaskName = (parentId: number) => {
  const parentTask = tasks.value.find(t => t.id === parentId);
  return parentTask ? parentTask.name : '-';
};

const calculateDuration = () => {
  if (taskForm.value.startDate && taskForm.value.endDate) {
    const start = new Date(taskForm.value.startDate);
    const end = new Date(taskForm.value.endDate);
    const diffTime = end.getTime() - start.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    taskForm.value.duration = diffDays > 0 ? diffDays : 1;
  }
};

const calculateEndDate = () => {
  if (taskForm.value.startDate && taskForm.value.duration) {
    const start = new Date(taskForm.value.startDate);
    const end = new Date(start);
    end.setDate(start.getDate() + taskForm.value.duration - 1);
    taskForm.value.endDate = `${end.getFullYear()}-${String(end.getMonth() + 1).padStart(2, '0')}-${String(end.getDate()).padStart(2, '0')}`;
  }
};

const fetchTasks = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/task/project/${projectId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    console.log('任务列表响应:', result);
    if (result.success) {
      tasks.value = result.data || [];
    } else {
      ElMessage.error(result.message || '获取任务失败');
    }
  } catch (error) {
    console.error('获取任务错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const fetchWorkLogs = async () => {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/work-log/user/${currentUserId.value}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    if (result.success) {
      workLogs.value = result.data || [];
    }
  } catch (error) {
    console.error('获取工时记录错误:', error);
  }
};

const resetTaskForm = () => {
  taskForm.value = {
    id: null,
    projectId: projectId,
    parentId: null,
    name: '',
    description: '',
    startDate: '',
    endDate: '',
    duration: 7,
    priority: 'medium',
    status: 'todo',
    progress: 0,
    createdBy: null
  };
};

const showTaskDialogFunc = () => {
  isEditTask.value = false;
  resetTaskForm();
  showTaskDialog.value = true;
};

const addSubTask = (parentTask: any) => {
  isEditTask.value = false;
  resetTaskForm();
  taskForm.value.parentId = parentTask.id;
  taskForm.value.name = '';
  if (parentTask.startDate) {
    taskForm.value.startDate = parentTask.startDate;
    const start = new Date(parentTask.startDate);
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    taskForm.value.endDate = `${end.getFullYear()}-${String(end.getMonth() + 1).padStart(2, '0')}-${String(end.getDate()).padStart(2, '0')}`;
    taskForm.value.duration = 7;
  }
  showTaskDialog.value = true;
};

const editTask = (task: any) => {
  isEditTask.value = true;
  taskForm.value = { ...task };
  showTaskDialog.value = true;
};

const saveTask = async () => {
  if (!taskForm.value.name) {
    ElMessage.warning('请输入任务名称');
    return;
  }

  saving.value = true;
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      taskForm.value.createdBy = JSON.parse(userStr).id;
    }

    const url = isEditTask.value ? 'http://localhost:8080/api/task/update' : 'http://localhost:8080/api/task/create';
    const method = isEditTask.value ? 'PUT' : 'POST';

    console.log('提交任务数据:', taskForm.value);

    const token = localStorage.getItem('token');
    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(taskForm.value)
    });
    const result = await response.json();
    console.log('保存任务响应:', result);
    if (result.success) {
      ElMessage.success(isEditTask.value ? '更新任务成功' : '添加任务成功');
      showTaskDialog.value = false;
      await fetchTasks();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    console.error('保存任务错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    saving.value = false;
  }
};

const deleteTask = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此任务？此操作不可撤销！', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/task/delete/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    if (result.success) {
      ElMessage.success('删除任务成功');
      await fetchTasks();
    } else {
      ElMessage.error(result.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
    }
  }
};

const showWorkLogDialog = (task: any) => {
  workLogForm.value = {
    taskId: task.id,
    taskName: task.name,
    hours: 1,
    workDate: new Date(),
    description: ''
  };
  showWorkLogDialogVisible.value = true;
};

const submitWorkLog = async () => {
  saving.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/work-log/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        taskId: workLogForm.value.taskId,
        userId: currentUserId.value,
        hours: workLogForm.value.hours,
        workDate: workLogForm.value.workDate,
        description: workLogForm.value.description
      })
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success('工时填报成功');
      showWorkLogDialogVisible.value = false;
      await fetchWorkLogs();
    } else {
      ElMessage.error(result.message || '填报失败');
    }
  } catch (error) {
    console.error('提交工时错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    saving.value = false;
  }
};

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    todo: 'info',
    in_progress: 'warning',
    review: 'primary',
    done: 'success'
  };
  return typeMap[status] || 'info';
};

const getStatusName = (status: string) => {
  const nameMap: Record<string, string> = {
    todo: '待处理',
    in_progress: '进行中',
    review: '待审核',
    done: '已完成'
  };
  return nameMap[status] || status;
};

const getPriorityType = (priority: string) => {
  const typeMap: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  };
  return typeMap[priority] || 'info';
};

const getPriorityName = (priority: string) => {
  const nameMap: Record<string, string> = {
    high: '高',
    medium: '中',
    low: '低'
  };
  return nameMap[priority] || priority;
};

onMounted(() => {
  fetchTasks();
  fetchWorkLogs();
});
</script>

<style scoped>
.progress-tracking {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stats-row {
  margin: 20px 0;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  font-size: 32px;
  color: #409eff;
}

.stat-icon.success {
  color: #67c23a;
}

.stat-icon.warning {
  color: #e6a23c;
}

.stat-icon.primary {
  color: #409eff;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.date-range .arrow {
  color: #909399;
}

.parent-task-tag {
  color: #409eff;
  font-size: 13px;
}

.no-parent-tag {
  color: #909399;
  font-size: 13px;
}
</style>
