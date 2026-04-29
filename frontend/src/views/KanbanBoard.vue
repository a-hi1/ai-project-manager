<template>
  <div class="kanban-board">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>任务看板</h2>
          <div class="kanban-stats">
            <el-tag type="info">待处理: {{ todoTasks.length }}</el-tag>
            <el-tag type="warning">进行中: {{ inProgressTasks.length }}</el-tag>
            <el-tag type="primary">待审核: {{ reviewTasks.length }}</el-tag>
            <el-tag type="success">已完成: {{ doneTasks.length }}</el-tag>
          </div>
        </div>
      </template>

      <div class="kanban-columns">
        <div class="kanban-column">
          <div class="column-header">
            <span class="column-title">待处理</span>
            <span class="column-count">{{ todoTasks.length }}</span>
          </div>
          <div class="column-body" @dragover.prevent @drop="onDrop('todo', $event)">
            <div
              v-for="task in todoTasks"
              :key="task.id"
              class="task-card"
              draggable="true"
              @dragstart="onDragStart(task, $event)"
              @click="editTask(task)"
            >
              <div class="task-name">{{ task.name }}</div>
              <div class="task-meta">
                <el-tag size="small" :type="getPriorityType(task.priority)">{{ task.priority }}</el-tag>
                <span v-if="task.assignedTo" class="assignee">{{ getAssigneeName(task.assignedTo) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="kanban-column">
          <div class="column-header">
            <span class="column-title">进行中</span>
            <span class="column-count">{{ inProgressTasks.length }}</span>
          </div>
          <div class="column-body" @dragover.prevent @drop="onDrop('in_progress', $event)">
            <div
              v-for="task in inProgressTasks"
              :key="task.id"
              class="task-card"
              draggable="true"
              @dragstart="onDragStart(task, $event)"
              @click="editTask(task)"
            >
              <div class="task-name">{{ task.name }}</div>
              <div class="task-progress">
                <el-progress :percentage="task.progress || 0" :stroke-width="6" />
              </div>
              <div class="task-meta">
                <el-tag size="small" :type="getPriorityType(task.priority)">{{ task.priority }}</el-tag>
                <span v-if="task.assignedTo" class="assignee">{{ getAssigneeName(task.assignedTo) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="kanban-column">
          <div class="column-header">
            <span class="column-title">待审核</span>
            <span class="column-count">{{ reviewTasks.length }}</span>
          </div>
          <div class="column-body" @dragover.prevent @drop="onDrop('review', $event)">
            <div
              v-for="task in reviewTasks"
              :key="task.id"
              class="task-card"
              draggable="true"
              @dragstart="onDragStart(task, $event)"
              @click="editTask(task)"
            >
              <div class="task-name">{{ task.name }}</div>
              <div class="task-progress">
                <el-progress :percentage="task.progress || 0" :stroke-width="6" />
              </div>
              <div class="task-meta">
                <el-tag size="small" :type="getPriorityType(task.priority)">{{ task.priority }}</el-tag>
                <span v-if="task.assignedTo" class="assignee">{{ getAssigneeName(task.assignedTo) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="kanban-column">
          <div class="column-header">
            <span class="column-title">已完成</span>
            <span class="column-count">{{ doneTasks.length }}</span>
          </div>
          <div class="column-body" @dragover.prevent @drop="onDrop('done', $event)">
            <div
              v-for="task in doneTasks"
              :key="task.id"
              class="task-card task-done"
              draggable="true"
              @dragstart="onDragStart(task, $event)"
              @click="editTask(task)"
            >
              <div class="task-name">{{ task.name }}</div>
              <div class="task-progress">
                <el-progress :percentage="100" :stroke-width="6" />
              </div>
              <div class="task-meta">
                <el-tag size="small" type="success">已完成</el-tag>
                <span v-if="task.assignedTo" class="assignee">{{ getAssigneeName(task.assignedTo) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 任务编辑对话框 -->
    <el-dialog v-model="showTaskDialog" title="任务详情" width="600px">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.name" disabled />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-select v-model="taskForm.status" @change="onStatusChange">
            <el-option label="待处理" value="todo" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="待审核" value="review" />
            <el-option label="已完成" value="done" />
          </el-select>
        </el-form-item>
        <el-form-item label="进度">
          <el-slider v-model="taskForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="statusComment" type="textarea" placeholder="请输入状态变更备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTask">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';

const route = useRoute();
const projectId = Number(route.params.id);

const tasks = ref<any[]>([]);
const users = ref<any[]>([]);
const showTaskDialog = ref(false);
const statusComment = ref('');
const draggedTask = ref<any>(null);

const taskForm = ref({
  id: '',
  name: '',
  description: '',
  status: '',
  progress: 0
});

const todoTasks = computed(() => tasks.value.filter(t => t.status === 'todo'));
const inProgressTasks = computed(() => tasks.value.filter(t => t.status === 'in_progress'));
const reviewTasks = computed(() => tasks.value.filter(t => t.status === 'review'));
const doneTasks = computed(() => tasks.value.filter(t => t.status === 'done'));

const fetchTasks = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/task/project/${projectId}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
    const result = await response.json();
    if (result.success) {
      tasks.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取任务失败');
  }
};

const fetchUsers = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/role/list', {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
    const result = await response.json();
    if (result.success) {
      users.value = result.data;
    }
  } catch (error) {
    console.error('获取用户失败');
  }
};

const getAssigneeName = (userId: number) => {
  const user = users.value.find(u => u.id === userId);
  return user ? user.username : '';
};

const getPriorityType = (priority: string) => {
  const typeMap: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  };
  return typeMap[priority] || 'info';
};

const onDragStart = (task: any, event: DragEvent) => {
  draggedTask.value = task;
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move';
  }
};

const onDrop = async (newStatus: string, event: DragEvent) => {
  if (!draggedTask.value || draggedTask.value.status === newStatus) return;

  const oldStatus = draggedTask.value.status;
  draggedTask.value.status = newStatus;

  if (newStatus === 'done') {
    draggedTask.value.progress = 100;
  }

  try {
    const response = await fetch('http://localhost:8080/api/task/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(draggedTask.value)
    });

    const result = await response.json();
    if (result.success) {
      const logResponse = await fetch('http://localhost:8080/api/task-status-log/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          taskId: draggedTask.value.id,
          fromStatus: oldStatus,
          toStatus: newStatus,
          changedBy: JSON.parse(localStorage.getItem('user') || '{}').id,
          comment: ''
        })
      });
      ElMessage.success(`任务已移动到${getStatusName(newStatus)}`);
      fetchTasks();
    }
  } catch (error) {
    ElMessage.error('更新任务状态失败');
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

const editTask = (task: any) => {
  taskForm.value = { ...task };
  showTaskDialog.value = true;
};

const onStatusChange = (newStatus: string) => {
  if (newStatus === 'done') {
    taskForm.value.progress = 100;
  }
};

const saveTask = async () => {
  try {
    const oldTask = tasks.value.find(t => t.id === taskForm.value.id);
    const oldStatus = oldTask?.status || '';

    const response = await fetch('http://localhost:8080/api/task/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(taskForm.value)
    });

    const result = await response.json();
    if (result.success) {
      if (oldStatus !== taskForm.value.status) {
        await fetch('http://localhost:8080/api/task-status-log/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify({
            taskId: taskForm.value.id,
            fromStatus: oldStatus,
            toStatus: taskForm.value.status,
            changedBy: JSON.parse(localStorage.getItem('user') || '{}').id,
            comment: statusComment.value
          })
        });
      }
      ElMessage.success('任务更新成功');
      showTaskDialog.value = false;
      fetchTasks();
      statusComment.value = '';
    }
  } catch (error) {
    ElMessage.error('更新任务失败');
  }
};

onMounted(() => {
  fetchTasks();
  fetchUsers();
});
</script>

<style scoped>
.kanban-board {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.kanban-stats {
  display: flex;
  gap: 10px;
}

.kanban-columns {
  display: flex;
  gap: 15px;
  overflow-x: auto;
  padding: 10px 0;
}

.kanban-column {
  min-width: 280px;
  flex: 1;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 10px;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.column-title {
  font-weight: bold;
  color: #303133;
}

.column-count {
  background-color: #409eff;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.column-body {
  min-height: 400px;
  padding: 10px;
}

.task-card {
  background-color: white;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.task-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.task-card.task-done {
  opacity: 0.7;
}

.task-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.task-progress {
  margin-bottom: 8px;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.assignee {
  font-size: 12px;
  color: #909399;
}
</style>