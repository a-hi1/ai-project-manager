<template>
  <div class="gantt-chart">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>甘特图</h2>
          <div class="header-actions">
            <el-button type="primary" @click="showTaskDialog = true">添加任务</el-button>
            <el-button type="success" @click="showMilestoneDialog = true">添加里程碑</el-button>
          </div>
        </div>
      </template>

      <div class="gantt-container">
        <div class="gantt-sidebar">
          <div class="sidebar-header">任务名称</div>
          <div class="task-list">
            <div v-for="task in tasks" :key="task.id" class="task-item" :style="{ paddingLeft: (task.level * 20) + 'px' }">
              <span v-if="task.isMilestone" class="milestone-icon">◆</span>
              <span>{{ task.name }}</span>
            </div>
          </div>
        </div>

        <div class="gantt-timeline">
          <div class="timeline-header">
            <div v-for="date in dateRange" :key="date" class="timeline-cell">
              {{ formatDate(date) }}
            </div>
          </div>
          <div class="timeline-body">
            <div v-for="task in tasks" :key="task.id" class="timeline-row">
              <div v-for="date in dateRange" :key="date" class="timeline-cell" :class="{ 'is-today': isToday(date) }">
                <div v-if="isTaskBar(task, date)" class="task-bar" :style="getTaskBarStyle(task)" @click="editTask(task)">
                  {{ task.name }}
                </div>
                <div v-if="isMilestone(task, date)" class="milestone-marker">◆</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 任务对话框 -->
    <el-dialog v-model="showTaskDialog" :title="isEditTask ? '编辑任务' : '添加任务'" width="600px">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="父任务">
          <el-select v-model="taskForm.parentId" placeholder="请选择父任务" clearable>
            <el-option v-for="t in tasks" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="taskForm.startDate" type="date" placeholder="选择开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="taskForm.endDate" type="date" placeholder="选择结束日期" />
        </el-form-item>
        <el-form-item label="工期（天）">
          <el-input-number v-model="taskForm.duration" :min="1" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="taskForm.priority" placeholder="选择优先级">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否里程碑">
          <el-switch v-model="taskForm.isMilestone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTask">保存</el-button>
      </template>
    </el-dialog>

    <!-- 里程碑对话框 -->
    <el-dialog v-model="showMilestoneDialog" title="添加里程碑" width="500px">
      <el-form :model="milestoneForm" label-width="100px">
        <el-form-item label="里程碑名称">
          <el-input v-model="milestoneForm.name" placeholder="请输入里程碑名称" />
        </el-form-item>
        <el-form-item label="到期日期">
          <el-date-picker v-model="milestoneForm.dueDate" type="date" placeholder="选择到期日期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMilestoneDialog = false">取消</el-button>
        <el-button type="primary" @click="saveMilestone">保存</el-button>
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
const milestones = ref<any[]>([]);
const showTaskDialog = ref(false);
const showMilestoneDialog = ref(false);
const isEditTask = ref(false);

const taskForm = ref({
  id: '',
  name: '',
  description: '',
  parentId: null as number | null,
  startDate: '',
  endDate: '',
  duration: 1,
  priority: 'medium',
  isMilestone: false
});

const milestoneForm = ref({
  name: '',
  dueDate: '',
  projectId: projectId
});

const dateRange = computed(() => {
  if (tasks.value.length === 0) {
    const today = new Date();
    const range = [];
    for (let i = 0; i < 30; i++) {
      const date = new Date(today);
      date.setDate(today.getDate() + i);
      range.push(date);
    }
    return range;
  }

  const dates: Date[] = [];
  const startDates = tasks.value.map(t => new Date(t.startDate)).filter(Boolean);
  const endDates = tasks.value.map(t => new Date(t.endDate)).filter(Boolean);

  if (startDates.length === 0 && endDates.length === 0) {
    const today = new Date();
    for (let i = 0; i < 30; i++) {
      const date = new Date(today);
      date.setDate(today.getDate() + i);
      dates.push(date);
    }
    return dates;
  }

  const minDate = startDates.length > 0 ? new Date(Math.min(...startDates.map(d => d.getTime()))) : new Date();
  const maxDate = endDates.length > 0 ? new Date(Math.max(...endDates.map(d => d.getTime()))) : new Date();

  const current = new Date(minDate);
  while (current <= maxDate) {
    dates.push(new Date(current));
    current.setDate(current.getDate() + 1);
  }

  return dates.length > 0 ? dates : [];
});

const formatDate = (date: Date) => {
  return `${date.getMonth() + 1}/${date.getDate()}`;
};

const isToday = (date: Date) => {
  const today = new Date();
  return date.toDateString() === today.toDateString();
};

const isTaskBar = (task: any, date: Date) => {
  if (!task.startDate || !task.endDate) return false;
  const start = new Date(task.startDate);
  const end = new Date(task.endDate);
  return date >= start && date <= end;
};

const isMilestone = (task: any, date: Date) => {
  if (!task.isMilestone || !task.startDate) return false;
  const milestoneDate = new Date(task.startDate);
  return date.toDateString() === milestoneDate.toDateString();
};

const getTaskBarStyle = (task: any) => {
  const startDate = new Date(task.startDate);
  const firstDate = dateRange.value[0];
  if (!firstDate) return {};

  const startOffset = Math.floor((startDate.getTime() - firstDate.getTime()) / (1000 * 60 * 60 * 24));
  const duration = task.duration || 1;

  return {
    left: `${startOffset * 30}px`,
    width: `${duration * 30}px`,
    backgroundColor: task.isMilestone ? '#e6a23c' : '#409eff'
  };
};

const fetchTasks = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/task/project/${projectId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const result = await response.json();
    if (result.success) {
      tasks.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取任务失败');
  }
};

const saveTask = async () => {
  try {
    const taskData = {
      ...taskForm.value,
      projectId,
      createdBy: JSON.parse(localStorage.getItem('user') || '{}').id
    };

    const url = isEditTask.value ? 'http://localhost:8080/api/task/update' : 'http://localhost:8080/api/task/create';
    const method = isEditTask.value ? 'PUT' : 'POST';

    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(taskData)
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success(isEditTask.value ? '更新任务成功' : '创建任务成功');
      showTaskDialog.value = false;
      fetchTasks();
      resetTaskForm();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const editTask = (task: any) => {
  isEditTask.value = true;
  taskForm.value = { ...task };
  showTaskDialog.value = true;
};

const saveMilestone = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/milestone/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(milestoneForm.value)
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success('创建里程碑成功');
      showMilestoneDialog.value = false;
      resetMilestoneForm();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const resetTaskForm = () => {
  taskForm.value = {
    id: '',
    name: '',
    description: '',
    parentId: null,
    startDate: '',
    endDate: '',
    duration: 1,
    priority: 'medium',
    isMilestone: false
  };
  isEditTask.value = false;
};

const resetMilestoneForm = () => {
  milestoneForm.value = {
    name: '',
    dueDate: '',
    projectId
  };
};

onMounted(() => {
  fetchTasks();
});
</script>

<style scoped>
.gantt-chart {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.gantt-container {
  display: flex;
  border: 1px solid #e4e7ed;
  overflow-x: auto;
}

.gantt-sidebar {
  min-width: 200px;
  border-right: 1px solid #e4e7ed;
}

.sidebar-header {
  height: 40px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  font-weight: bold;
}

.task-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  height: 30px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.task-item:hover {
  background-color: #f5f7fa;
}

.milestone-icon {
  color: #e6a23c;
  margin-right: 5px;
}

.gantt-timeline {
  flex: 1;
  min-width: 900px;
}

.timeline-header {
  display: flex;
  height: 40px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #f5f7fa;
}

.timeline-cell {
  width: 30px;
  min-width: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  border-right: 1px solid #f0f0f0;
}

.timeline-cell.is-today {
  background-color: #ecf5ff;
}

.timeline-body {
  max-height: 400px;
  overflow-y: auto;
}

.timeline-row {
  display: flex;
  height: 30px;
  border-bottom: 1px solid #f0f0f0;
}

.task-bar {
  height: 20px;
  line-height: 20px;
  font-size: 10px;
  color: white;
  border-radius: 3px;
  padding: 0 5px;
  cursor: pointer;
  position: absolute;
  z-index: 1;
}

.milestone-marker {
  color: #e6a23c;
  font-size: 16px;
}
</style>