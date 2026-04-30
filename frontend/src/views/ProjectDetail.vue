<template>
  <div class="project-detail">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <div class="header-left">
        <el-button type="primary" @click="router.push('/projects')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回项目列表
        </el-button>
        <div class="title-section">
          <div class="title-icon">
            <el-icon :size="32"><Folder /></el-icon>
          </div>
          <div class="title-content">
            <h1>{{ project.name || '加载中...' }}</h1>
            <div class="title-meta">
              <el-tag :type="getStatusType(project.status)" effect="dark" round size="small">
                {{ getStatusName(project.status) }}
              </el-tag>
              <span class="duration" v-if="project.startDate && project.endDate">
                <el-icon><Calendar /></el-icon>
                {{ project.startDate }} ~ {{ project.endDate }}
              </span>
            </div>
          </div>
        </div>
      </div>
      <div class="header-buttons">
        <el-button type="primary" @click="editProject = true" class="edit-btn">
          <el-icon><Edit /></el-icon>
          编辑项目
        </el-button>
      </div>
    </div>

    <!-- 项目信息卡片 -->
    <el-card class="info-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="card-title">
            <el-icon><Document /></el-icon>
            <span>项目基本信息</span>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border class="info-desc">
        <el-descriptions-item label="项目描述" :span="2">
          <div class="desc-text">{{ project.description || '暂无描述' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="项目状态">
          <div class="status-badge">
            <el-tag :type="getStatusType(project.status)" effect="dark" size="default">
              {{ getStatusName(project.status) }}
            </el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="开始日期">
          <div class="date-cell">
            <div class="date-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <span>{{ project.startDate || '未设置' }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="结束日期">
          <div class="date-cell">
            <div class="date-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <span>{{ project.endDate || '未设置' }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="项目周期">
          <div class="date-cell">
            <div class="date-icon flag">
              <el-icon><Flag /></el-icon>
            </div>
            <span>{{ calculateDuration() }}</span>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 功能模块 -->
    <div class="section-title">
      <div class="title-decorator"></div>
      <el-icon :size="22" color="#409eff"><Document /></el-icon>
      <h2>功能模块</h2>
    </div>

    <el-row :gutter="20" class="module-cards">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="module in modules" :key="module.path">
        <el-card class="module-card" @click="goToModule(module)" :body-style="{ padding: '24px' }" shadow="hover">
          <div class="module-content">
            <div class="module-icon-wrapper" :style="{ backgroundColor: module.color + '15' }">
              <el-icon :size="32" :color="module.color">
                <component :is="module.icon" />
              </el-icon>
            </div>
            <div class="module-info">
              <h4>{{ module.name }}</h4>
              <p>{{ module.description }}</p>
            </div>
            <el-icon class="arrow-icon" :size="18"><ArrowRight /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑项目对话框 -->
    <el-dialog v-model="editProject" title="编辑项目" width="550px" class="edit-dialog" destroy-on-close>
      <el-form :model="projectForm" label-width="100px" :rules="editRules" ref="projectFormRef">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称" size="large" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="projectForm.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="projectForm.startDate" type="date" placeholder="选择开始日期" style="width: 100%" value-format="YYYY-MM-DD" size="large" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="projectForm.endDate" type="date" placeholder="选择结束日期" style="width: 100%" value-format="YYYY-MM-DD" size="large" />
        </el-form-item>
        <el-form-item label="项目状态" prop="status">
          <el-select v-model="projectForm.status" style="width: 100%" size="large">
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
        <el-button @click="editProject = false" size="large">取消</el-button>
        <el-button type="primary" @click="updateProject" :loading="saving" size="large">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Document,
  Edit,
  ArrowLeft,
  ArrowRight,
  Calendar,
  List,
  Warning,
  Flag,
  Plus,
  Folder
} from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);
const project = ref<any>({ name: '', description: '', status: '', startDate: '', endDate: '' });
const editProject = ref(false);
const projectForm = ref<any>({ name: '', description: '', status: '', startDate: '', endDate: '' });
const saving = ref(false);
const loading = ref(false);
const projectFormRef = ref();

const editRules = {
  name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择项目状态', trigger: 'change' }]
};

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
    icon: List,
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
    icon: Warning,
    color: '#F56C6C'
  },
  {
    name: '变更管理',
    path: '/changes',
    description: '项目变更申请与审批',
    icon: Flag,
    color: '#909399'
  },
  {
    name: '交付验收',
    path: '/delivery',
    description: '项目交付物验收',
    icon: Document,
    color: '#409EFF'
  },
  {
    name: '文档归档',
    path: '/documents',
    description: '项目文档管理与归档',
    icon: Document,
    color: '#67C23A'
  },
  {
    name: '项目复盘',
    path: '/retrospective',
    description: '项目总结与经验教训',
    icon: Document,
    color: '#E6A23C'
  },
  {
    name: '结束报告',
    path: '/end-report',
    description: '生成项目结束报告',
    icon: Document,
    color: '#409EFF'
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
  return nameMap[status] || status || '未设置';
};

const calculateDuration = () => {
  if (!project.value.startDate || !project.value.endDate) return '未设置';
  const start = new Date(project.value.startDate);
  const end = new Date(project.value.endDate);
  const diff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
  return diff > 0 ? `${diff} 天` : '无效日期';
};

const goToModule = (module: any) => {
  router.push(`/project/${projectId}${module.path}`);
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

const fetchProject = async () => {
  loading.value = true;
  try {
    console.log('正在获取项目详情，项目ID:', projectId);
    const result: any = await apiClient.get(`/project/get/${projectId}`);
    console.log('项目详情数据响应:', result);
    
    if (result.success) {
      const data = result.data;
      console.log('项目数据:', data);
      project.value = {
        ...data,
        startDate: formatDate(data.startDate),
        endDate: formatDate(data.endDate)
      };
      projectForm.value = {
        ...data,
        startDate: data.startDate ? formatDate(data.startDate) : '',
        endDate: data.endDate ? formatDate(data.endDate) : ''
      };
    } else {
      ElMessage.error(result.message || '获取项目详情失败');
      useMockData();
    }
  } catch (error) {
    console.error('获取项目详情失败:', error);
    ElMessage.error('网络错误，请稍后重试');
    useMockData();
  } finally {
    loading.value = false;
  }
};

const useMockData = () => {
  console.log('使用模拟数据');
  project.value = {
    id: projectId,
    name: '示例项目 - ' + projectId,
    description: '这是一个示例项目的描述，用于展示项目详情页面的功能。',
    status: 'in_progress',
    startDate: '2026-04-01',
    endDate: '2026-06-30'
  };
  projectForm.value = { ...project.value };
};

const updateProject = async () => {
  const valid = await projectFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
    const formData = {
      ...projectForm.value,
      startDate: projectForm.value.startDate ? projectForm.value.startDate : null,
      endDate: projectForm.value.endDate ? projectForm.value.endDate : null
    };
    console.log('提交的项目数据:', formData);
    const result: any = await apiClient.put('/project/update', formData);
    if (result.success) {
      ElMessage.success('更新成功');
      editProject.value = false;
      fetchProject();
    } else {
      ElMessage.error(result.message || '更新失败');
    }
  } catch (error) {
    console.error('更新项目失败:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  fetchProject();
});
</script>

<style scoped>
.project-detail {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f4ff 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 32px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
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
  width: 56px;
  height: 56px;
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
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.3;
}

.title-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
}

.duration {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.header-buttons {
  display: flex;
  gap: 12px;
}

.edit-btn {
  border-radius: 8px;
  padding: 10px 20px;
}

.info-card {
  margin-bottom: 32px;
  border-radius: 16px;
  border: none;
}

.info-card :deep(.el-card__header) {
  padding: 20px 32px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(90deg, #fafafa 0%, white 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-title .el-icon {
  color: #409eff;
}

.info-desc {
  padding: 8px 0;
}

.info-desc :deep(.el-descriptions__label) {
  background: #fafafa;
  font-weight: 600;
  color: #606266;
  width: 120px;
}

.info-desc :deep(.el-descriptions__body) {
  color: #303133;
}

.desc-text {
  line-height: 1.6;
  color: #606266;
}

.status-badge {
  display: flex;
  justify-content: center;
}

.date-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #409eff15 0%, #66b1ff15 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
}

.date-icon.flag {
  background: linear-gradient(135deg, #e6a23c15 0%, #f0c78a15 100%);
  color: #e6a23c;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.title-decorator {
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, #409eff 0%, #66b1ff 100%);
  border-radius: 2px;
}

.section-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
}

.module-cards {
  margin: 0 -10px;
}

.module-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  border-radius: 12px;
}

.module-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
  border-color: #e0e8f0;
}

.module-content {
  display: flex;
  align-items: center;
  gap: 18px;
}

.module-icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.module-card:hover .module-icon-wrapper {
  transform: scale(1.1);
}

.module-info {
  flex: 1;
  min-width: 0;
}

.module-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  transition: color 0.3s ease;
}

.module-card:hover h4 {
  color: #409eff;
}

.module-info p {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  flex-shrink: 0;
  transition: all 0.3s ease;
  color: #c0c4cc;
}

.module-card:hover .arrow-icon {
  transform: translateX(6px);
  color: #409eff;
}

.edit-dialog {
  border-radius: 16px;
}

.edit-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  padding: 20px 28px;
}

.edit-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.edit-dialog :deep(.el-dialog__headerbtn) {
  top: 18px;
}

.edit-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px;
}

.edit-dialog :deep(.el-dialog__body) {
  padding: 28px;
}

.edit-dialog :deep(.el-dialog__footer) {
  padding: 20px 28px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .project-detail {
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
}

@media (max-width: 768px) {
  .title-content h1 {
    font-size: 20px;
  }
  
  .info-card :deep(.el-card__header) {
    padding: 16px 20px;
  }
  
  .info-desc :deep(.el-descriptions__label) {
    width: 100px;
  }
}
</style>
