<template>
  <div class="projects">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="router.push('/')">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-button>
            <el-icon :size="24" color="#409eff"><Folder /></el-icon>
            <h2>项目列表</h2>
          </div>
          <el-button type="primary" @click="router.push('/project/create')" size="large" v-if="['admin', 'pm'].includes(currentRole)">
            <el-icon><Plus /></el-icon>
            创建项目
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索项目名称或描述..."
          clearable
          prefix-icon="Search"
          style="width: 300px"
          @input="handleSearch"
        />
        <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width: 150px" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="待启动" value="pending">
            <el-tag size="small" type="info">待启动</el-tag>
          </el-option>
          <el-option label="进行中" value="in_progress">
            <el-tag size="small" type="warning">进行中</el-tag>
          </el-option>
          <el-option label="已完成" value="completed">
            <el-tag size="small" type="success">已完成</el-tag>
          </el-option>
        </el-select>
      </div>

      <!-- 项目统计 -->
      <el-row :gutter="20" class="stats-row" v-if="!loading">
        <el-col :xs="12" :sm="6">
          <div class="stat-item">
            <div class="stat-number">{{ projects.length }}</div>
            <div class="stat-label">总项目</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-item">
            <div class="stat-number" style="color: #e6a23c;">{{ inProgressCount }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-item">
            <div class="stat-number" style="color: #67c23a;">{{ completedCount }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-item">
            <div class="stat-number" style="color: #909399;">{{ pendingCount }}</div>
            <div class="stat-label">待启动</div>
          </div>
        </el-col>
      </el-row>

      <!-- 项目表格 -->
      <el-table 
        :data="filteredProjects" 
        style="width: 100%" 
        v-loading="loading"
        :empty-text="searchKeyword || filterStatus ? '没有找到匹配的项目' : '暂无项目，点击右上角创建新项目'"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="项目名称" min-width="200">
          <template #default="scope">
            <div class="project-name-cell">
              <el-icon :size="18" color="#409eff"><Folder /></el-icon>
              <span class="project-name">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="项目描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="项目周期" width="220">
          <template #default="scope">
            <div class="date-range-cell" v-if="scope.row.startDate && scope.row.endDate">
              <el-icon :size="14" color="#909399"><Calendar /></el-icon>
              <span>{{ formatDate(scope.row.startDate) }} ~ {{ formatDate(scope.row.endDate) }}</span>
            </div>
            <span v-else class="no-data">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light" round>
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="viewProject(scope.row.id)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button 
              type="warning" 
              size="small" 
              plain 
              @click="editProject(scope.row)"
              v-if="['admin', 'pm'].includes(currentRole)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              plain 
              @click="deleteProject(scope.row.id)"
              v-if="currentRole === 'admin'"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="filteredProjects.length > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="filteredProjects.length"
        class="pagination"
      />
    </el-card>

    <!-- 编辑项目对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑项目" width="550px" destroy-on-close>
      <el-form :model="editForm" label-width="100px" :rules="editRules" ref="editFormRef">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="项目周期" prop="dateRange">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
            @change="updateEditDates"
          />
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option label="待启动" value="pending">
              <el-tag size="small" type="info">待启动</el-tag>
            </el-option>
            <el-option label="进行中" value="in_progress">
              <el-tag size="small" type="warning">进行中</el-tag>
            </el-option>
            <el-option label="已完成" value="completed">
              <el-tag size="small" type="success">已完成</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateProject" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Folder, Plus, View, Edit, Delete, Calendar, HomeFilled } from '@element-plus/icons-vue';
import apiClient from '../utils/api';
import { getRoleType } from '../utils/rolePermission';
import type { Project } from '../types';

const router = useRouter();
const projects = ref<Project[]>([]);
const loading = ref(false);
const saving = ref(false);
const editDialogVisible = ref(false);
const editForm = ref<Project>({ name: '', description: '', status: 'pending', startDate: '', endDate: '' });
const dateRange = ref<Date[] | string[]>([]);
const searchKeyword = ref('');
const filterStatus = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const editFormRef = ref();
const currentRole = ref('');

const editRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }]
};

const filteredProjects = computed(() => {
  let result = projects.value;
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(p => 
      p.name?.toLowerCase().includes(keyword) || 
      p.description?.toLowerCase().includes(keyword)
    );
  }
  
  if (filterStatus.value) {
    result = result.filter(p => p.status === filterStatus.value);
  }
  
  return result;
});

const inProgressCount = computed(() => projects.value.filter(p => p.status === 'in_progress').length);
const completedCount = computed(() => projects.value.filter(p => p.status === 'completed').length);
const pendingCount = computed(() => projects.value.filter(p => p.status === 'pending').length);

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

const formatDate = (dateStr: any) => {
  if (!dateStr) return '';
  if (typeof dateStr === 'string') {
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
  if (Array.isArray(dateStr) && dateStr.length >= 3) {
    return `${dateStr[0]}-${String(dateStr[1]).padStart(2, '0')}-${String(dateStr[2]).padStart(2, '0')}`;
  }
  return '';
};

const handleSearch = () => {
  currentPage.value = 1;
};

const fetchProjects = async () => {
  loading.value = true;
  try {
    const result: any = await apiClient.get('/project/list');
    console.log('项目列表数据:', result);
    if (result.success) {
      projects.value = result.data.map((p: any) => ({
        ...p,
        startDate: formatDate(p.startDate),
        endDate: formatDate(p.endDate)
      }));
    } else {
      ElMessage.error(result.message || '获取项目列表失败');
    }
  } catch (error) {
    console.error('获取项目列表失败:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const viewProject = (id: number) => {
  router.push(`/project/${id}`);
};

const editProject = (project: Project) => {
  editForm.value = { ...project };
  if (project.startDate && project.endDate) {
    dateRange.value = [new Date(project.startDate), new Date(project.endDate)];
  } else {
    dateRange.value = [];
  }
  editDialogVisible.value = true;
};

const updateEditDates = (val: string[]) => {
  if (val && val.length === 2) {
    editForm.value.startDate = val[0] ? new Date(val[0]).toISOString().split('T')[0] : '';
    editForm.value.endDate = val[1] ? new Date(val[1]).toISOString().split('T')[0] : '';
  }
};

const updateProject = async () => {
  const valid = await editFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
    const result: any = await apiClient.put('/project/update', editForm.value);
    if (result.success) {
      ElMessage.success('更新成功');
      editDialogVisible.value = false;
      fetchProjects();
    } else {
      ElMessage.error(result.message || '更新失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    saving.value = false;
  }
};

const deleteProject = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此项目？此操作不可撤销！', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const result: any = await apiClient.delete(`/project/delete/${id}`);
    if (result.success) {
      ElMessage.success('删除项目成功');
      fetchProjects();
    } else {
      ElMessage.error(result.message || '删除项目失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
    }
  }
};

onMounted(() => {
  fetchProjects();
  const userStr = localStorage.getItem('user');
  if (userStr) {
    const user = JSON.parse(userStr);
    currentRole.value = getRoleType(user.roleId);
  }
});
</script>

<style scoped>
.projects {
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
  gap: 12px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.project-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.project-name {
  font-weight: 500;
  color: #303133;
}

.date-range-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 13px;
}

.no-data {
  color: #c0c4cc;
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
  .search-bar .el-input {
    width: 100% !important;
  }
}
</style>
