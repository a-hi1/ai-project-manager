<template>
  <div class="projects">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><Folder /></el-icon>
            <h2>项目列表</h2>
          </div>
          <el-button type="primary" @click="router.push('/project/create')">
            <el-icon><Plus /></el-icon>
            创建项目
          </el-button>
        </div>
      </template>

      <el-table :data="projects" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="项目ID" width="80" />
        <el-table-column prop="name" label="项目名称" min-width="200" />
        <el-table-column prop="description" label="项目描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="项目周期" width="240">
          <template #default="scope">
            <span v-if="scope.row.startDate && scope.row.endDate">
              {{ formatDate(scope.row.startDate) }} 至 {{ formatDate(scope.row.endDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewProject(scope.row.id)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button type="warning" size="small" @click="editProject(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteProject(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑项目" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="项目名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="项目周期">
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
        <el-form-item label="项目状态">
          <el-select v-model="editForm.status">
            <el-option label="待启动" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateProject">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Folder, Plus, View, Edit, Delete } from '@element-plus/icons-vue';

const router = useRouter();
const projects = ref<any[]>([]);
const loading = ref(false);
const editDialogVisible = ref(false);
const editForm = ref<any>({});
const dateRange = ref<any[]>([]);

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

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

const fetchProjects = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/project/list', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    const result = await response.json();
    if (result.success) {
      projects.value = result.data;
    } else {
      ElMessage.error(result.message || '获取项目列表失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const viewProject = (id: number) => {
  router.push(`/project/${id}`);
};

const editProject = (project: any) => {
  editForm.value = { ...project };
  if (project.startDate && project.endDate) {
    dateRange.value = [new Date(project.startDate), new Date(project.endDate)];
  } else {
    dateRange.value = [];
  }
  editDialogVisible.value = true;
};

const updateEditDates = (val: any[]) => {
  if (val && val.length === 2) {
    editForm.value.startDate = val[0] ? new Date(val[0]).toISOString().split('T')[0] : '';
    editForm.value.endDate = val[1] ? new Date(val[1]).toISOString().split('T')[0] : '';
  }
};

const updateProject = async () => {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/project/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(editForm.value)
    });
    const result = await response.json();
    if (result.success) {
      ElMessage.success('更新成功');
      editDialogVisible.value = false;
      fetchProjects();
    } else {
      ElMessage.error(result.message || '更新失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  }
};

const deleteProject = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此项目？此操作不可撤销！', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/project/delete/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    const result = await response.json();
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
  gap: 10px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
</style>
