<template>
  <div class="kanban-board">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回项目详情
            </el-button>
            <h2>任务看板</h2>
          </div>
          <div class="kanban-stats">
            <el-tag type="info" effect="dark">待处理: {{ todoTasks.length }}</el-tag>
            <el-tag type="warning" effect="dark">进行中: {{ inProgressTasks.length }}</el-tag>
            <el-tag type="primary" effect="dark">待审核: {{ reviewTasks.length }}</el-tag>
            <el-tag type="success" effect="dark">已完成: {{ doneTasks.length }}</el-tag>
          </div>
        </div>
      </template>

      <div class="kanban-columns">
        <!-- 待处理列 -->
        <div class="kanban-column" :class="{ 'drag-over': dragOverColumn === 'todo' }">
          <div class="column-header">
            <div class="column-title-wrapper">
              <div class="column-dot" style="background-color: #909399;"></div>
              <span class="column-title">待处理</span>
            </div>
            <el-badge :value="todoTasks.length" class="column-count" type="info" />
          </div>
          <div class="column-body" 
               @dragover.prevent="onDragOver('todo')" 
               @dragleave="onDragLeave"
               @drop="onDrop('todo', $event)">
            <div v-if="todoTasks.length === 0" class="empty-column">
              <el-icon :size="24" color="#dcdfe6"><Plus /></el-icon>
              <span>拖拽任务到此处</span>
            </div>
            <div
              v-for="task in todoTasks"
              :key="task.id"
              class="task-card"
              :class="{ 'dragging': draggedTask?.id === task.id }"
              draggable="true"
              @dragstart="onDragStart(task, $event)"
              @dragend="onDragEnd"
              @click="editTask(task)"
            >
              <div class="task-name">{{ task.name }}</div>
              <div class="task-description" v-if="task.description">{{ task.description }}</div>
              <div class="task-meta">
                <el-tag size="small" :type="getPriorityType(task.priority)" effect="light">
                  {{ getPriorityName(task.priority) }}
                </el-tag>
                <div class="task-assignee" v-if="task.assigneeId">
                  <el-avatar :size="20" class="assignee-avatar">{{ getAssigneeInitials(task.assigneeId) }}</el-avatar>
                  <span class="assignee-name">{{ getAssigneeName(task.assigneeId) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 进行中列 -->
        <div class="kanban-column" :class="{ 'drag-over': dragOverColumn === 'in_progress' }">
          <div class="column-header">
            <div class="column-title-wrapper">
              <div class="column-dot" style="background-color: #e6a23c;"></div>
              <span class="column-title">进行中</span>
            </div>
            <el-badge :value="inProgressTasks.length" class="column-count" type="warning" />
          </div>
          <div class="column-body" 
               @dragover.prevent="onDragOver('in_progress')" 
               @dragleave="onDragLeave"
               @drop="onDrop('in_progress', $event)">
            <div v-if="inProgressTasks.length === 0" class="empty-column">
              <el-icon :size="24" color="#dcdfe6"><Plus /></el-icon>
              <span>拖拽任务到此处</span>
            </div>
            <div
              v-for="task in inProgressTasks"
              :key="task.id"
              class="task-card"
              :class="{ 'dragging': draggedTask?.id === task.id }"
              draggable="true"
              @dragstart="onDragStart(task, $event)"
              @dragend="onDragEnd"
              @click="editTask(task)"
            >
              <div class="task-name">{{ task.name }}</div>
              <div class="task-description" v-if="task.description">{{ task.description }}</div>
              <div class="task-progress-wrapper">
                <el-progress :percentage="task.progress || 0" :stroke-width="6" :show-text="true" />
              </div>
              <div class="task-meta">
                <el-tag size="small" :type="getPriorityType(task.priority)" effect="light">
                  {{ getPriorityName(task.priority) }}
                </el-tag>
                <div class="task-assignee" v-if="task.assigneeId">
                  <el-avatar :size="20" class="assignee-avatar">{{ getAssigneeInitials(task.assigneeId) }}</el-avatar>
                  <span class="assignee-name">{{ getAssigneeName(task.assigneeId) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 待审核列 -->
        <div class="kanban-column" :class="{ 'drag-over': dragOverColumn === 'review' }">
          <div class="column-header">
            <div class="column-title-wrapper">
              <div class="column-dot" style="background-color: #409eff;"></div>
              <span class="column-title">待审核</span>
            </div>
            <el-badge :value="reviewTasks.length" class="column-count" type="primary" />
          </div>
          <div class="column-body" 
               @dragover.prevent="onDragOver('review')" 
               @dragleave="onDragLeave"
               @drop="onDrop('review', $event)">
            <div v-if="reviewTasks.length === 0" class="empty-column">
              <el-icon :size="24" color="#dcdfe6"><Plus /></el-icon>
              <span>拖拽任务到此处</span>
            </div>
            <div
              v-for="task in reviewTasks"
              :key="task.id"
              class="task-card"
              :class="{ 'dragging': draggedTask?.id === task.id }"
              draggable="true"
              @dragstart="onDragStart(task, $event)"
              @dragend="onDragEnd"
              @click="editTask(task)"
            >
              <div class="task-name">{{ task.name }}</div>
              <div class="task-description" v-if="task.description">{{ task.description }}</div>
              <div class="task-progress-wrapper">
                <el-progress :percentage="task.progress || 0" :stroke-width="6" :show-text="true" />
              </div>
              <div class="task-meta">
                <el-tag size="small" :type="getPriorityType(task.priority)" effect="light">
                  {{ getPriorityName(task.priority) }}
                </el-tag>
                <div class="task-assignee" v-if="task.assigneeId">
                  <el-avatar :size="20" class="assignee-avatar">{{ getAssigneeInitials(task.assigneeId) }}</el-avatar>
                  <span class="assignee-name">{{ getAssigneeName(task.assigneeId) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 已完成列 -->
        <div class="kanban-column" :class="{ 'drag-over': dragOverColumn === 'done' }">
          <div class="column-header">
            <div class="column-title-wrapper">
              <div class="column-dot" style="background-color: #67c23a;"></div>
              <span class="column-title">已完成</span>
            </div>
            <el-badge :value="doneTasks.length" class="column-count" type="success" />
          </div>
          <div class="column-body" 
               @dragover.prevent="onDragOver('done')" 
               @dragleave="onDragLeave"
               @drop="onDrop('done', $event)">
            <div v-if="doneTasks.length === 0" class="empty-column">
              <el-icon :size="24" color="#dcdfe6"><Plus /></el-icon>
              <span>拖拽任务到此处</span>
            </div>
            <div
              v-for="task in doneTasks"
              :key="task.id"
              class="task-card task-done"
              :class="{ 'dragging': draggedTask?.id === task.id }"
              draggable="true"
              @dragstart="onDragStart(task, $event)"
              @dragend="onDragEnd"
              @click="editTask(task)"
            >
              <div class="task-name">{{ task.name }}</div>
              <div class="task-description" v-if="task.description">{{ task.description }}</div>
              <div class="task-progress-wrapper">
                <el-progress :percentage="100" :stroke-width="6" status="success" />
              </div>
              <div class="task-meta">
                <el-tag size="small" type="success" effect="light">已完成</el-tag>
                <div class="task-assignee" v-if="task.assigneeId">
                  <el-avatar :size="20" class="assignee-avatar">{{ getAssigneeInitials(task.assigneeId) }}</el-avatar>
                  <span class="assignee-name">{{ getAssigneeName(task.assigneeId) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 任务编辑对话框 -->
    <el-dialog v-model="showTaskDialog" title="任务详情" width="600px" destroy-on-close>
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.name" disabled />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-select v-model="taskForm.status" @change="onStatusChange" style="width: 100%">
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
        <el-form-item label="进度">
          <el-slider v-model="taskForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="statusComment" type="textarea" :rows="3" placeholder="请输入状态变更备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTask" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, Plus } from '@element-plus/icons-vue';
import apiClient from '../utils/api';
import type { Task, User, TaskStatus } from '../types';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const tasks = ref<Task[]>([]);
const users = ref<User[]>([]);
const showTaskDialog = ref(false);
const statusComment = ref('');
const draggedTask = ref<Task | null>(null);
const dragOverColumn = ref<string>('');
const saving = ref(false);

const taskForm = ref<Partial<Task> & { progress: number }>({
  id: undefined,
  name: '',
  description: '',
  status: 'todo',
  progress: 0
});

const todoTasks = computed(() => tasks.value.filter(t => t.status === 'todo'));
const inProgressTasks = computed(() => tasks.value.filter(t => t.status === 'in_progress'));
const reviewTasks = computed(() => tasks.value.filter(t => t.status === 'review'));
const doneTasks = computed(() => tasks.value.filter(t => t.status === 'done'));

const fetchTasks = async () => {
  try {
    const result: any = await apiClient.get(`/task/project/${projectId}`);
    if (result.success) {
      tasks.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取任务失败');
  }
};

const fetchUsers = async () => {
  try {
    const result: any = await apiClient.get('/user/list');
    if (result.success) {
      users.value = result.data;
    }
  } catch (error) {
    console.error('获取用户失败');
  }
};

const getAssigneeName = (userId: number | undefined) => {
  if (!userId) return '';
  const user = users.value.find(u => u.id === userId);
  return user && user.username ? user.username : '';
};

const getAssigneeInitials = (userId: number | undefined) => {
  if (!userId) return '?';
  const user = users.value.find(u => u.id === userId);
  if (user && user.username && typeof user.username === 'string' && user.username.length > 0) {
    return user.username.charAt(0).toUpperCase();
  }
  return '?';
};

const getPriorityType = (priority: string | undefined) => {
  const typeMap: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  };
  return typeMap[priority || ''] || 'info';
};

const getPriorityName = (priority: string | undefined) => {
  const nameMap: Record<string, string> = {
    high: '高',
    medium: '中',
    low: '低'
  };
  return nameMap[priority || ''] || priority || '';
};

const onDragStart = (task: Task, event: DragEvent) => {
  draggedTask.value = task;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', String(task.id));
    event.dataTransfer.setData('application/json', JSON.stringify({id: task.id, status: task.status}));
  }
};

const onDragOver = (column: string) => {
  dragOverColumn.value = column;
};

const onDragLeave = () => {
  dragOverColumn.value = '';
};

const onDragEnd = () => {
  dragOverColumn.value = '';
  draggedTask.value = null;
};

const onDrop = async (newStatus: string, event: DragEvent) => {
  event.preventDefault();
  dragOverColumn.value = '';
  
  if (!draggedTask.value || draggedTask.value.status === newStatus) return;

  const oldStatus = draggedTask.value.status;
  const taskId = draggedTask.value.id;
  const targetStatus = newStatus as TaskStatus;
  
  // 优化：先本地更新，让界面立即响应
  const taskIndex = tasks.value.findIndex(t => t.id === taskId);
  if (taskIndex !== -1) {
    tasks.value[taskIndex].status = targetStatus;
    tasks.value[taskIndex].progress = targetStatus === 'done' ? 100 : (tasks.value[taskIndex].progress || 0);
    // 触发响应式更新
    tasks.value = [...tasks.value];
  }
  
  try {
    const updatedTask = {
      id: taskId,
      status: targetStatus,
      progress: targetStatus === 'done' ? 100 : (draggedTask.value.progress || 0)
    };

    const result: any = await apiClient.put('/task/update', updatedTask);
    if (result.success) {
      try {
        await apiClient.post('/task-status-log/create', {
          taskId: taskId,
          fromStatus: oldStatus,
          toStatus: targetStatus,
          changedBy: JSON.parse(localStorage.getItem('user') || '{}').id,
          comment: ''
        });
      } catch { /* ignore log failure */ }
      ElMessage.success(`任务已移动到${getStatusName(targetStatus)}`);
      fetchTasks();
    } else {
      ElMessage.error(result.message || '更新失败');
      fetchTasks();
    }
  } catch (error) {
    console.error('更新任务状态失败', error);
    ElMessage.error('更新任务状态失败');
    fetchTasks();
  }

  draggedTask.value = null;
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

const editTask = (task: Task) => {
  taskForm.value = { ...task, progress: task.progress || 0 };
  showTaskDialog.value = true;
};

const onStatusChange = (newStatus: TaskStatus) => {
  if (newStatus === 'done') {
    taskForm.value.progress = 100;
  }
};

const saveTask = async () => {
  saving.value = true;
  try {
    const oldTask = tasks.value.find(t => t.id === taskForm.value.id);
    const oldStatus = oldTask?.status || 'todo';

    // 先本地更新，让界面立即响应
    const taskIndex = tasks.value.findIndex(t => t.id === taskForm.value.id);
    if (taskIndex !== -1) {
      tasks.value[taskIndex] = { ...tasks.value[taskIndex], ...taskForm.value };
      tasks.value = [...tasks.value];
    }

    const result: any = await apiClient.put('/task/update', taskForm.value);
    if (result.success) {
      if (oldStatus !== taskForm.value.status && taskForm.value.status) {
        try {
          await apiClient.post('/task-status-log/create', {
            taskId: taskForm.value.id,
            fromStatus: oldStatus,
            toStatus: taskForm.value.status,
            changedBy: JSON.parse(localStorage.getItem('user') || '{}').id,
            comment: statusComment.value
          });
        } catch { /* ignore log failure */ }
      }
      ElMessage.success('任务更新成功');
      showTaskDialog.value = false;
      fetchTasks();
      statusComment.value = '';
    } else {
      ElMessage.error(result.message || '更新任务失败');
      fetchTasks();
    }
  } catch (error) {
    console.error('更新任务失败', error);
    ElMessage.error('更新任务失败');
    fetchTasks();
  } finally {
    saving.value = false;
  }
};

const goBack = () => {
  router.push(`/project/${projectId}`);
};

onMounted(() => {
  fetchTasks();
  fetchUsers();
});
</script>

<style scoped>
.kanban-board {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.kanban-stats {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.kanban-columns {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding: 10px 0;
  min-height: 500px;
}

.kanban-column {
  min-width: 300px;
  flex: 1;
  background-color: #f5f7fa;
  border-radius: 12px;
  padding: 12px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.kanban-column.drag-over {
  border-color: #409eff;
  background-color: #ecf5ff;
  transform: scale(1.02);
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 8px;
  margin-bottom: 8px;
}

.column-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.column-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.column-title {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.column-count :deep(.el-badge__content) {
  font-size: 12px;
  padding: 0 8px;
}

.column-body {
  min-height: 400px;
  padding: 4px;
}

.empty-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  color: #909399;
  font-size: 13px;
  gap: 8px;
  transition: all 0.3s;
}

.empty-column:hover {
  border-color: #409eff;
  color: #409eff;
}

.task-card {
  background-color: white;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 10px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.task-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
  border-color: #d9ecff;
}

.task-card.dragging {
  opacity: 0.5;
  transform: rotate(2deg);
}

.task-card.task-done {
  opacity: 0.8;
}

.task-card.task-done .task-name {
  text-decoration: line-through;
  color: #909399;
}

.task-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
  font-size: 14px;
  line-height: 1.4;
}

.task-description {
  font-size: 12px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-progress-wrapper {
  margin-bottom: 10px;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.task-assignee {
  display: flex;
  align-items: center;
  gap: 4px;
}

.assignee-avatar {
  background-color: #409eff;
  color: white;
  font-size: 10px;
}

.assignee-name {
  font-size: 12px;
  color: #606266;
}

/* 响应式 */
@media (max-width: 1200px) {
  .kanban-columns {
    flex-wrap: wrap;
  }
  .kanban-column {
    min-width: calc(50% - 8px);
  }
}

@media (max-width: 768px) {
  .kanban-column {
    min-width: 100%;
  }
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
