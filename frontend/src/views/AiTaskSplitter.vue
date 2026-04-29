<template>
  <div class="ai-task-splitter">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>AI智能任务拆分</h2>
        </div>
      </template>

      <el-form :model="requirementForm" label-width="100px">
        <el-form-item label="需求描述">
          <el-input v-model="requirementForm.requirement" type="textarea" rows="4" placeholder="请输入需求描述，例如：实现用户登录功能，包括用户名密码验证、记住登录状态等" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="splitTask" :loading="loading">AI智能拆分</el-button>
        </el-form-item>
      </el-form>

      <div v-if="splitResult" class="split-result">
        <el-divider>拆分结果</el-divider>
        <div v-if="taskList.length > 0" class="task-list">
          <el-table :data="taskList" style="width: 100%">
            <el-table-column prop="name" label="任务名称" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="duration" label="工期（天）" width="100" />
            <el-table-column prop="dependencies" label="前置任务">
              <template #default="scope">
                <el-tag v-for="dep in scope.row.dependencies" :key="dep" size="small">{{ dep }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="action-buttons">
            <el-button type="primary" @click="createTasks">创建任务</el-button>
          </div>
        </div>
        <div v-else class="raw-result">
          <pre>{{ splitResult }}</pre>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';

const route = useRoute();
const projectId = Number(route.params.id);

const loading = ref(false);
const splitResult = ref('');
const taskList = ref<any[]>([]);
const token = localStorage.getItem('token') || '';

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
    const response = await fetch('http://localhost:8080/api/ai/split-task', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ requirement: requirementForm.value.requirement })
    });

    const result = await response.json();
    if (result.success) {
      splitResult.value = result.data;
      try {
        const jsonMatch = result.data.match(/\{[\s\S]*\}/);
        if (jsonMatch) {
          const parsed = JSON.parse(jsonMatch[0]);
          taskList.value = parsed.任务列表 || [];
        }
      } catch (e) {
        taskList.value = [];
      }
      ElMessage.success('拆分完成');
    } else {
      ElMessage.error(result.message || '拆分失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
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
    for (const task of taskList.value) {
      await fetch('http://localhost:8080/api/task/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          projectId,
          name: task.任务名称 || task.name,
          description: task.描述 || task.description,
          duration: task.工期 || task.duration,
          createdBy: JSON.parse(localStorage.getItem('user') || '{}').id,
          status: 'todo'
        })
      });
    }
    ElMessage.success('任务创建成功');
    taskList.value = [];
    splitResult.value = '';
    requirementForm.value.requirement = '';
  } catch (error) {
    ElMessage.error('创建任务失败');
  }
};
</script>

<style scoped>
.ai-task-splitter {
  padding: 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.split-result {
  margin-top: 20px;
}

.task-list {
  margin-top: 15px;
}

.action-buttons {
  margin-top: 15px;
  text-align: right;
}

.raw-result pre {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
}
</style>