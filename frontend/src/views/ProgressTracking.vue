﻿﻿<template>
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
          <el-button type="primary" @click="openCreateDialog" size="large">
            <el-icon><Plus /></el-icon>
            添加任务
          </el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #409eff15;">
              <el-icon :size="24" color="#409eff"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ tasks.length }}</div>
              <div class="stat-label">总任务</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #e6a23c15;">
              <el-icon :size="24" color="#e6a23c"><Loading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number" style="color: #e6a23c;">{{ inProgressTasks.length }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #67c23a15;">
              <el-icon :size="24" color="#67c23a"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number" style="color: #67c23a;">{{ completedTasks.length }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #f56c6c15;">
              <el-icon :size="24" color="#f56c6c"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number" style="color: #f56c6c;">{{ overdueTasks.length }}</div>
              <div class="stat-label">已逾期</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索任务名称..."
          clearable
          prefix-icon="Search"
          style="width: 300px"
          @input="handleSearch"
        />
        <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width: 150px" @change="handleSearch">
          <el-option label="全部" value="" />
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
      </div>

      <!-- 任务表格 -->
      <el-table 
        :data="filteredTasks" 
        style="width: 100%" 
        v-loading="loading"
        :empty-text="searchKeyword || filterStatus ? '没有找到匹配的任务' : '暂无任务，点击右上角添加新任务'"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="任务名称" min-width="180">
          <template #default="scope">
            <div class="task-name-cell">
              <el-icon :size="16" color="#409eff"><List /></el-icon>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light" round size="small">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)" effect="light" size="small">
              {{ getPriorityName(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="160" align="center">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.progress || 0" 
              :status="scope.row.status === 'done' ? 'success' : ''"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column label="工时" width="120" align="center">
          <template #default="scope">
            <div class="work-hours-cell">
              <el-icon :size="14" color="#909399"><Timer /></el-icon>
              <span>{{ scope.row.actualHours || 0 }} / {{ scope.row.estimatedHours || 0 }} h</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="editTask(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="warning" size="small" plain @click="reportHours(scope.row)">
              <el-icon><Timer /></el-icon>
              工时
            </el-button>
            <el-button type="danger" size="small" plain @click="deleteTask(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="filteredTasks.length > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="filteredTasks.length"
        class="pagination"
      />
    </el-card>

    <!-- 创建/编辑任务对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="isEdit ? '编辑任务' : '添加任务'" 
      width="600px"
      destroy-on-close
    >
      <el-form :model="taskForm" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="taskForm.name" placeholder="请输入任务名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input 
            v-model="taskForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入任务描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="任务状态" prop="status">
          <el-select v-model="taskForm.status" style="width: 100%">
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
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="taskForm.priority" style="width: 100%">
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
        <el-form-item label="进度">
          <el-slider v-model="taskForm.progress" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="预计工时">
          <el-input-number v-model="taskForm.estimatedHours" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="taskForm.assignedTo" placeholder="选择负责人" style="width: 100%" clearable>
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTask" :loading="saving">
          {{ isEdit ? '保存修改' : '确认添加' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 工时填报对话框 -->
    <el-dialog v-model="showHoursDialog" title="工时填报" width="500px" destroy-on-close>
      <el-form :model="hoursForm" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="hoursForm.taskName" disabled />
        </el-form-item>
        <el-form-item label="本次工时">
          <el-input-number v-model="hoursForm.hours" :min="0.5" :max="24" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工作描述">
          <el-input v-model="hoursForm.description" type="textarea" :rows="3" placeholder="描述本次工作内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showHoursDialog = false">取消</el-button>
        <el-button type="primary" @click="saveHours" :loading="savingHours">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  ArrowLeft, Plus, Edit, Delete, Search, List, Timer, 
  Loading, CircleCheck, Warning 
} from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);
const tasks = ref<any[]>([]);
const users = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);
const savingHours = ref(false);
const showCreateDialog = ref(false);
const showHoursDialog = ref(false);
const isEdit = ref(false);
const searchKeyword = ref('');
const filterStatus = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const formRef = ref();

const taskForm = ref({
  id: null,
  projectId: projectId,
  name: '',
  description: '',
  status: 'todo',
  priority: 'medium',
  progress: 0,
  estimatedHours: 0,
  actualHours: 0,
  assignedTo: null,
  creatorId: null
});

const hoursForm = ref({
  taskId: null,
  taskName: '',
  hours: 1,
  description: ''
});

const formRules = {
  name: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 100, message: '任务名称长度应在 2-100 个字符之间', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择任务状态', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ]
};

const inProgressTasks = computed(() => tasks.value.filter(t => t.status === 'in_progress'));
const completedTasks = computed(() => tasks.value.filter(t => t.status === 'done'));
const overdueTasks = computed(() => tasks.value.filter(t => {
  if (t.status === 'done') return false;
  if (!t.endDate) return false;
  return new Date(t.endDate) < new Date();
}));

const filteredTasks = computed(() => {
  let result = tasks.value;
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(t => t.name?.toLowerCase().includes(keyword));
  }
  
  if (filterStatus.value) {
    result = result.filter(t => t.status === filterStatus.value);
  }
  
  return result;
});

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

const handleSearch = () => {
  currentPage.value = 1;
};

const openCreateDialog = () => {
  isEdit.value = false;
  resetForm();
  showCreateDialog.value = true;
};

const fetchTasks = async () => {
  loading.value = true;
  try {
const `/task/project/${projectId}`: any = await apiClient.get(response);
    if (result.success) {
      tasks.value = result.data || [];
    } else {
      ElMessage.error(result.message || '获取任务列表失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const fetchUsers = async () => {
  try {
    const result: any = await apiClient.get('/role/list');
    if (result.success) {
      users.value = result.data || [];
    }
  } catch (error) {
    console.error('获取用户列表失败');
  }
};

const saveTask = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      taskForm.value.creatorId = user.id;
    }
    taskForm.value.projectId = projectId;

    const url = isEdit.value ? '/task/update' : '/task/create';
    const method = isEdit.value ? 'PUT' : 'POST';

    const result: any = await apiClient.get(url);
    if (result.success) {
      ElMessage.success(isEdit.value ? '编辑任务成功' : '添加任务成功');
      showCreateDialog.value = false;
      resetForm();
      await fetchTasks();
    } else {
      ElMessage.error(result.message || (isEdit.value ? '编辑任务失败' : '添加任务失败'));
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    saving.value = false;
  }
};

const editTask = (task: any) => {
  isEdit.value = true;
  taskForm.value = { ...task };
  showCreateDialog.value = true;
};

const deleteTask = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此任务？此操作不可撤销！', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });
const `/task/delete/${id}`: any = await apiClient.delete(response);
    if (result.success) {
      ElMessage.success('删除任务成功');
      await fetchTasks();
    } else {
      ElMessage.error(result.message || '删除任务失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
    }
  }
};

const reportHours = (task: any) => {
  hoursForm.value = {
    taskId: task.id,
    taskName: task.name,
    hours: 1,
    description: ''
  };
  showHoursDialog.value = true;
};

const saveHours = async () => {
  savingHours.value = true;
  try {
    const task = tasks.value.find(t => t.id === hoursForm.value.taskId);
    if (task) {
      task.actualHours = (task.actualHours || 0) + hoursForm.value.hours;
      
    const result: any = await apiClient.put('/task/update', task)
      });
      if (result.success) {
        ElMessage.success('工时填报成功');
        showHoursDialog.value = false;
        await fetchTasks();
      } else {
        ElMessage.error(result.message || '工时填报失败');
      }
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    savingHours.value = false;
  }
};

const resetForm = () => {
  taskForm.value = {
    id: null,
    projectId: projectId,
    name: '',
    description: '',
    status: 'todo',
    priority: 'medium',
    progress: 0,
    estimatedHours: 0,
    actualHours: 0,
    assignedTo: null,
    creatorId: null
  };
  isEdit.value = false;
};

onMounted(() => {
  fetchTasks();
  fetchUsers();
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
  gap: 15px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.task-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.work-hours-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
  }
  .search-bar .el-input,
  .search-bar .el-select {
    width: 100% !important;
  }
}
</style>
