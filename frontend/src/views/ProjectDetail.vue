<template>
  <div class="project-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="router.push('/projects')">
              <el-icon><ArrowLeft /></el-icon>
              返回项目列表
            </el-button>
            <h2>{{ project.name }}</h2>
          </div>
          <el-button type="primary" @click="editProject = true">
            <el-icon><Edit /></el-icon>
            编辑项目
          </el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目描述">
          {{ project.description }}
        </el-descriptions-item>
        <el-descriptions-item label="项目状态">
          <el-tag :type="getStatusType(project.status)">{{ getStatusName(project.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ project.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ project.endDate }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-divider content-position="center">功能模块</el-divider>

    <el-row :gutter="20" class="module-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="module in modules" :key="module.path">
        <el-card class="module-card" @click="goToModule(module)">
          <div class="module-icon" :style="{ backgroundColor: module.color }">
            <el-icon :size="32">
              <component :is="module.icon" />
            </el-icon>
          </div>
          <h3>{{ module.name }}</h3>
          <p>{{ module.description }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="editProject" title="编辑项目" width="500px">
      <el-form :model="projectForm" label-width="100px">
        <el-form-item label="项目名称">
          <el-input v-model="projectForm.name" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="projectForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="projectForm.startDate" type="date" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="projectForm.endDate" type="date" />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="projectForm.status">
            <el-option label="待启动" value="pending" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editProject = false">取消</el-button>
        <el-button type="primary" @click="updateProject">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Document,
  Edit,
  ArrowLeft,
  DataAnalysis,
  Calendar,
  List,
  Warning,
  CircleClose,
  RefreshLeft,
  Box,
  FolderOpened,
  DocumentAdd
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);
const project = ref<any>({});
const editProject = ref(false);
const projectForm = ref<any>({});

const modules = [
  {
    name: '需求调研',
    path: '/requirement',
    description: '需求收集与分析管理',
    icon: Document,
    color: '#409EFF'
  },
  {
    name: '可行性分析',
    path: '/feasibility',
    description: '技术、经济、市场可行性评估',
    icon: DataAnalysis,
    color: '#67C23A'
  },
  {
    name: '任务管理',
    path: '/tasks',
    description: 'WBS分解、甘特图、看板、里程碑',
    icon: List,
    color: '#E6A23C'
  },
  {
    name: '甘特图',
    path: '/gantt',
    description: '任务进度可视化管理',
    icon: Calendar,
    color: '#909399'
  },
  {
    name: '看板',
    path: '/kanban',
    description: '敏捷开发任务看板',
    icon: List,
    color: '#F56C6C'
  },
  {
    name: '风险管理',
    path: '/risks',
    description: '风险识别、评估与控制',
    icon: Warning,
    color: '#E6A23C'
  },
  {
    name: '缺陷跟踪',
    path: '/bugs',
    description: 'Bug管理与跟踪',
    icon: CircleClose,
    color: '#F56C6C'
  },
  {
    name: '变更管理',
    path: '/changes',
    description: '项目变更申请与审批',
    icon: RefreshLeft,
    color: '#909399'
  },
  {
    name: '交付验收',
    path: '/delivery',
    description: '项目交付物验收',
    icon: Box,
    color: '#409EFF'
  },
  {
    name: '文档归档',
    path: '/documents',
    description: '项目文档管理与归档',
    icon: FolderOpened,
    color: '#67C23A'
  },
  {
    name: '项目复盘',
    path: '/retrospective',
    description: '项目总结与经验教训',
    icon: DocumentAdd,
    color: '#E6A23C'
  }
];

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

const goToModule = (module: any) => {
  router.push(`/project/${projectId}${module.path}`);
};

const fetchProject = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/project/get/${projectId}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const result = await response.json();
    if (result.success) {
      project.value = result.data;
      projectForm.value = { ...result.data };
    } else {
      ElMessage.error(result.message || '获取项目详情失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  }
};

const updateProject = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/project/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(projectForm.value)
    });
    const result = await response.json();
    if (result.success) {
      ElMessage.success('更新成功');
      editProject.value = false;
      fetchProject();
    } else {
      ElMessage.error(result.message || '更新失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  }
};

onMounted(() => {
  fetchProject();
});
</script>

<style scoped>
.project-detail {
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
}

.module-cards {
  margin-top: 20px;
}

.module-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.module-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.module-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 15px;
}

.module-card h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.module-card p {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}
</style>
