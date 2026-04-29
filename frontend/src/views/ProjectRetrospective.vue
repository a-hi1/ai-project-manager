<template>
  <div class="project-retrospective">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>项目复盘</h2>
          <el-button type="primary" @click="generateRetrospective">生成复盘报告</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="复盘数据" name="data">
          <div v-if="retrospective" class="retrospective-content">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-title">任务统计</div>
                  <div class="stat-value">{{ retrospective.completedTasks }}/{{ retrospective.totalTasks }}</div>
                  <div class="stat-label">已完成任务 / 总任务</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-title">任务完成率</div>
                  <div class="stat-value">{{ retrospective.taskCompletionRate }}%</div>
                  <div class="stat-label">任务完成百分比</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-title">总工时</div>
                  <div class="stat-value">{{ retrospective.totalHours }}</div>
                  <div class="stat-label">小时</div>
                </el-card>
              </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-title">风险统计</div>
                  <div class="stat-value">{{ retrospective.resolvedRisks }}/{{ retrospective.totalRisks }}</div>
                  <div class="stat-label">已解决风险 / 总风险</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card class="stat-card">
                  <div class="stat-title">缺陷统计</div>
                  <div class="stat-value">{{ retrospective.resolvedBugs }}/{{ retrospective.totalBugs }}</div>
                  <div class="stat-label">已修复缺陷 / 总缺陷</div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          <el-empty v-else description="暂无复盘数据，请点击生成复盘报告" />
        </el-tab-pane>

        <el-tab-pane label="编辑复盘" name="edit">
          <el-form :model="retrospectiveForm" label-width="120px">
            <el-form-item label="项目总结">
              <el-input v-model="retrospectiveForm.summary" type="textarea" rows="3" />
            </el-form-item>
            <el-form-item label="关键成就">
              <el-input v-model="retrospectiveForm.keyAchievements" type="textarea" rows="3" />
            </el-form-item>
            <el-form-item label="主要挑战">
              <el-input v-model="retrospectiveForm.keyChallenges" type="textarea" rows="3" />
            </el-form-item>
            <el-form-item label="经验教训">
              <el-input v-model="retrospectiveForm.lessonsLearned" type="textarea" rows="3" />
            </el-form-item>
            <el-form-item label="改进建议">
              <el-input v-model="retrospectiveForm.recommendations" type="textarea" rows="3" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveRetrospective">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';

const route = useRoute();
const projectId = Number(route.params.id);

const activeTab = ref('data');
const retrospective = ref<any>(null);
const retrospectiveForm = ref({
  summary: '',
  keyAchievements: '',
  keyChallenges: '',
  lessonsLearned: '',
  recommendations: ''
});

const fetchRetrospective = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/project-retrospective/project/${projectId}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
    const result = await response.json();
    if (result.success && result.data.length > 0) {
      retrospective.value = result.data[0];
      retrospectiveForm.value = {
        summary: retrospective.value.summary || '',
        keyAchievements: retrospective.value.keyAchievements || '',
        keyChallenges: retrospective.value.keyChallenges || '',
        lessonsLearned: retrospective.value.lessonsLearned || '',
        recommendations: retrospective.value.recommendations || ''
      };
    }
  } catch (error) {
    ElMessage.error('获取复盘数据失败');
  }
};

const generateRetrospective = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const response = await fetch(`http://localhost:8080/api/project-retrospective/generate/${projectId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ userId: user.id })
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success('复盘报告生成成功');
      fetchRetrospective();
    } else {
      ElMessage.error(result.message || '生成失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const saveRetrospective = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/project-retrospective/update', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        ...retrospective.value,
        ...retrospectiveForm.value
      })
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success('保存成功');
      fetchRetrospective();
    } else {
      ElMessage.error(result.message || '保存失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

onMounted(() => {
  fetchRetrospective();
});
</script>

<style scoped>
.project-retrospective {
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

.retrospective-content {
  padding: 20px 0;
}

.stat-card {
  text-align: center;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}
</style>