﻿﻿<template>
  <div class="create-project">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2><el-icon><DocumentAdd /></el-icon>创建新项目</h2>
        </div>
      </template>

      <el-form
        :model="projectForm"
        ref="projectFormRef"
        :rules="formRules"
        label-width="120px"
        class="project-form"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model="projectForm.name"
            placeholder="请输入项目名称（如：智能管理系统）"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="4"
            placeholder="请描述项目核心目标、主要功能和预期成果"
            resize="none"
          />
        </el-form-item>

        <el-divider class="ai-divider">
          <span class="divider-text">
            <el-button
              type="primary"
              size="small"
              @click="getAISuggestion"
              :loading="aiLoading"
              class="ai-btn"
            >
              <el-icon><MagicStick /></el-icon>
              AI智能推荐
            </el-button>
          </span>
        </el-divider>

        <div v-if="aiSuggestion" class="ai-suggestion">
          <el-alert type="success" :closable="false" show-icon class="suggestion-alert">
            <template #title>
              <strong class="suggestion-title">
                <el-icon><Star /></el-icon>
                AI推荐方案
              </strong>
            </template>
            <div class="suggestion-content" v-html="formatSuggestion(aiSuggestion)"></div>
            <div class="suggestion-actions">
              <el-button type="success" size="default" @click="applySuggestion">
                <el-icon><Check /></el-icon>
                一键应用推荐方案
              </el-button>
            </div>
          </el-alert>
        </div>

        <el-form-item label="项目周期" prop="dateRange">
          <el-date-picker
            v-model="projectForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%;"
            size="large"
            @change="updateDates"
          />
        </el-form-item>

        <el-form-item label="项目状态" prop="status">
          <el-select v-model="projectForm.status" placeholder="请选择项目状态" size="large" style="width: 100%;">
            <el-option label="待启动" value="pending">
              <div class="option-content">
                <el-tag size="small" type="info">待启动</el-tag>
                <span class="option-desc">项目尚未正式启动</span>
              </div>
            </el-option>
            <el-option label="进行中" value="in_progress">
              <div class="option-content">
                <el-tag size="small" type="warning">进行中</el-tag>
                <span class="option-desc">项目正在执行中</span>
              </div>
            </el-option>
            <el-option label="已完成" value="completed">
              <div class="option-content">
                <el-tag size="small" type="success">已完成</el-tag>
                <span class="option-desc">项目已全部完成</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item class="button-group">
          <el-button type="primary" size="large" @click="createProject" :loading="createLoading">
            <el-icon><Check /></el-icon>
            创建项目
          </el-button>
          <el-button size="large" @click="router.push('/projects')">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { MagicStick, Check, Close, DocumentAdd, Star } from '@element-plus/icons-vue';
import apiClient from '../utils/api';
import { getRoleType } from '../utils/rolePermission';

const router = useRouter();
const projectFormRef = ref();
const aiLoading = ref(false);
const createLoading = ref(false);
const aiSuggestion = ref('');
const token = localStorage.getItem('token') || '';

const projectForm = ref({
  name: '',
  description: '',
  dateRange: [],
  startDate: '',
  endDate: '',
  status: 'pending',
  createdBy: 1
});

const formRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 50, message: '项目名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入项目描述', trigger: 'blur' },
    { min: 10, max: 500, message: '项目描述长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  dateRange: [
    { required: true, message: '请选择项目周期', trigger: 'change' },
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (!value || value.length < 2) {
          callback(new Error('请选择项目周期'));
        } else {
          callback();
        }
      },
      trigger: 'change'
    }
  ]
};

const getAISuggestion = async () => {
  if (!projectForm.value.name || !projectForm.value.description) {
    ElMessage.warning('请先填写项目名称和描述');
    return;
  }

  aiLoading.value = true;
  try {
    if (!token) {
      ElMessage.error('请先登录');
      return;
    }

    const apiResult: any = await apiClient.post('/ai/suggest-plan', {
      projectName: projectForm.value.name,
      coreGoal: projectForm.value.description
    });
    if (apiResult.success) {
      aiSuggestion.value = apiResult.data?.aiSuggestion || JSON.stringify(apiResult.data);
      ElMessage.success('AI推荐完成！');
    } else {
      ElMessage.error(apiResult.message || 'AI推荐失败，请稍后重试');
    }
  } catch (error: any) {
    console.error('AI推荐错误:', error);
    ElMessage.error(`获取AI建议失败: ${error.message || '网络错误'}`);
  } finally {
    aiLoading.value = false;
  }
};

const formatSuggestion = (text: string) => {
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
};

const applySuggestion = () => {
  ElMessage.success('已应用AI推荐方案');
  aiSuggestion.value = '';
};

const updateDates = (val: any[]) => {
  if (val && val.length === 2) {
    projectForm.value.startDate = val[0] ? new Date(val[0]).toISOString().split('T')[0] : '';
    projectForm.value.endDate = val[1] ? new Date(val[1]).toISOString().split('T')[0] : '';
  }
};

const createProject = async () => {
  try {
    console.log('当前表单数据:', projectForm.value);
    
    // 验证表单
    await projectFormRef.value.validate();
    
    console.log('表单验证通过');
    
    if (!projectForm.value.startDate || !projectForm.value.endDate) {
      ElMessage.warning('请选择项目周期');
      return;
    }

    createLoading.value = true;
    if (!token) {
      ElMessage.error('请先登录');
      createLoading.value = false;
      return;
    }

    // 构建发送的数据，不包含 dateRange 字段
    const submitData = {
      name: projectForm.value.name,
      description: projectForm.value.description,
      startDate: projectForm.value.startDate,
      endDate: projectForm.value.endDate,
      status: projectForm.value.status,
      createdBy: projectForm.value.createdBy
    };

    console.log('发送项目数据:', submitData);
    
    const apiResult: any = await apiClient.post('/project/create', submitData);
    console.log('响应数据:', apiResult);
    
    if (apiResult.success) {
      ElMessage.success('创建项目成功！');
      setTimeout(() => {
        router.push('/projects');
      }, 500);
    } else {
      ElMessage.error(apiResult.message || '创建项目失败');
    }
  } catch (error: any) {
    console.error('创建项目错误:', error);
    if (error !== false) {
      ElMessage.error(`网络错误: ${error.message || '请稍后重试'}`);
    }
  } finally {
    createLoading.value = false;
  }
};

onMounted(() => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      const userData = JSON.parse(userStr);
      projectForm.value.createdBy = userData.id || 1;
      
      const currentRole = getRoleType(userData.roleId);
      if (!['admin', 'pm'].includes(currentRole)) {
        ElMessage.warning('您没有创建项目的权限');
        router.push('/projects');
      }
    } catch (e) {
      console.error('解析用户信息失败', e);
    }
  }
});
</script>

<style scoped>
.create-project {
  padding: 30px;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
}

.form-card {
  max-width: 800px;
  margin: 0 auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  border: none;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.project-form {
  padding: 20px 0;
}

.project-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

.ai-divider {
  margin: 30px 0;
}

.divider-text {
  padding: 0 20px;
}

.ai-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.ai-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.ai-suggestion {
  margin: 0 0 30px 0;
}

.suggestion-alert {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(103, 194, 58, 0.15);
}

.suggestion-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #67c23a;
}

.suggestion-content {
  margin-top: 12px;
  line-height: 1.8;
  color: #303133;
}

.suggestion-actions {
  margin-top: 16px;
  text-align: center;
}

.option-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.option-desc {
  font-size: 12px;
  color: #909399;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 20px;
}

.button-group :deep(.el-button) {
  min-width: 140px;
}

.button-group :deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.button-group :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}
</style>
