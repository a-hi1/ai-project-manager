<template>
  <div class="ai-task-splitter">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2>AI智能任务拆分</h2>
          </div>
        </div>
      </template>

      <el-form :model="requirementForm" label-width="120px">
        <el-form-item label="需求描述">
          <el-input
            v-model="requirementForm.requirement"
            type="textarea"
            :rows="5"
            placeholder="请输入需求描述，例如：实现用户登录功能，包括用户名密码验证、记住登录状态、密码找回等..."
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="splitTask" :loading="loading">
            <el-icon><MagicStick /></el-icon>
            AI智能拆分
          </el-button>
          <el-button @click="requirementForm.requirement = ''" :disabled="loading">
            <el-icon><RefreshLeft /></el-icon>
            清空
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="splitResult" class="split-result">
        <el-divider content-position="left">拆分结果</el-divider>
        <div v-if="taskList.length > 0" class="task-list">
          <el-table :data="taskList" style="width: 100%" :row-key="(_row: any, index: number) => index">
            <el-table-column label="序号" width="60" type="index" />
            <el-table-column prop="name" label="任务名称" min-width="200" />
            <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
            <el-table-column prop="duration" label="工期(天)" width="100" />
            <el-table-column prop="dependencies" label="前置任务" min-width="200">
              <template #default="scope">
                <el-tag
                  v-for="(dep, index) in scope.row.dependencies"
                  :key="index"
                  size="small"
                  style="margin: 2px"
                >
                  {{ dep }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="action-buttons">
            <el-button type="primary" @click="createTasks">
              <el-icon><DocumentAdd /></el-icon>
              创建任务
            </el-button>
            <el-button @click="exportTasks">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
        <div v-else class="raw-result">
          <el-alert title="原始拆分结果" type="info" show-icon style="margin-bottom: 10px" />
          <pre>{{ splitResult }}</pre>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, MagicStick, RefreshLeft, DocumentAdd, Download } from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id) || 1;

const loading = ref(false);
const splitResult = ref('');
const taskList = ref<any[]>([]);

const requirementForm = ref({
  requirement: ''
});

const splitTask = async () => {
  if (!requirementForm.value.requirement) {
    ElMessage.warning('请输入需求描述');
    return;
  }

  loading.value = true;
  try {
    const apiResult: any = await apiClient.post('/ai/split-task', { requirement: requirementForm.value.requirement });
    if (apiResult.success) {
      splitResult.value = apiResult.data;
      try {
        // 更健壮的JSON解析逻辑
        let jsonStr = apiResult.data;
        // 移除可能的markdown代码块标记
        jsonStr = jsonStr.replace(/```json\s*/g, '').replace(/```\s*$/g, '').trim();
        
        // 找到第一个{和最后一个}
        const startIndex = jsonStr.indexOf('{');
        const endIndex = jsonStr.lastIndexOf('}');
        
        if (startIndex !== -1 && endIndex !== -1 && endIndex > startIndex) {
          jsonStr = jsonStr.substring(startIndex, endIndex + 1);
          const parsed = JSON.parse(jsonStr);
          taskList.value = parsed['任务列表'] || parsed.tasks || [];
          console.log('解析后的任务列表:', taskList.value);
        } else {
          throw new Error('未找到有效的JSON格式');
        }
      } catch {
        console.log('JSON解析失败');
        taskList.value = [];
      }
      ElMessage.success('拆分完成');
    } else {
      ElMessage.error(apiResult.message || '拆分失败');
    }
  } catch {
    ElMessage.error('网络错误，请检查后端服务是否正常启动');
  } finally {
    loading.value = false;
  }
};

const createTasks = async () => {
  if (taskList.value.length === 0) {
    ElMessage.warning('没有可创建的任务');
    return;
  }

  try {
    let successCount = 0;
    for (const task of taskList.value) {
      const result: any = await apiClient.post('/task/create', {
        projectId: projectId,
        name: task.任务名称 || task.name,
        description: task.描述 || task.description || '',
        duration: task.工期 || task.duration || 1,
        createdBy: JSON.parse(localStorage.getItem('user') || '{}').id || 1,
        status: 'todo'
      });

      if (result.success) {
        successCount++;
      }
    }
    ElMessage.success(`成功创建 ${successCount} 个任务`);
    taskList.value = [];
    splitResult.value = '';
    requirementForm.value.requirement = '';
  } catch (error) {
    ElMessage.error('创建任务失败');
  }
};

const exportTasks = () => {
  if (taskList.value.length === 0) {
    ElMessage.warning('没有可导出的任务');
    return;
  }

  const dataStr = JSON.stringify(taskList.value, null, 2);
  const dataBlob = new Blob([dataStr], { type: 'application/json' });
  const url = URL.createObjectURL(dataBlob);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'tasks.json';
  link.click();
  URL.revokeObjectURL(url);
  ElMessage.success('导出成功');
};

const goBack = () => {
  router.back();
};
</script>

<style scoped>
.ai-task-splitter {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
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

.split-result {
  margin-top: 30px;
}

.task-list {
  margin-top: 15px;
}

.action-buttons {
  margin-top: 20px;
  text-align: right;
}

.raw-result {
  margin-top: 15px;
}

.raw-result pre {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Consolas', 'Monaco', monospace;
  line-height: 1.6;
}
</style>
