<template>
  <div class="feasibility-analysis">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="router.push(`/project/${projectId}`)">
              <el-icon><ArrowLeft /></el-icon>
              返回项目详情
            </el-button>
            <h2>可行性分析</h2>
          </div>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            添加可行性分析
          </el-button>
        </div>
      </template>

      <el-table :data="analyses" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="分析ID" width="80" />
        <el-table-column prop="title" label="分析标题" min-width="200" />
        <el-table-column prop="technicalFeasibility" label="技术可行性" min-width="150" show-overflow-tooltip />
        <el-table-column prop="economicFeasibility" label="经济可行性" min-width="150" show-overflow-tooltip />
        <el-table-column prop="marketFeasibility" label="市场可行性" min-width="150" show-overflow-tooltip />
        <el-table-column prop="riskAssessment" label="风险评估" min-width="150" show-overflow-tooltip />
        <el-table-column prop="conclusion" label="结论" min-width="150" />
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editAnalysis(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteAnalysis(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑可行性分析对话框 -->
    <el-dialog v-model="showCreateDialog" :title="isEdit ? '编辑可行性分析' : '添加可行性分析'" width="600px">
      <el-form :model="analysisForm" label-width="100px">
        <el-form-item label="分析标题" prop="title">
          <el-input v-model="analysisForm.title" placeholder="请输入分析标题" />
        </el-form-item>
        <el-form-item label="技术可行性" prop="technicalFeasibility">
          <el-input v-model="analysisForm.technicalFeasibility" type="textarea" :rows="3" placeholder="请输入技术可行性分析" />
        </el-form-item>
        <el-form-item label="经济可行性" prop="economicFeasibility">
          <el-input v-model="analysisForm.economicFeasibility" type="textarea" :rows="3" placeholder="请输入经济可行性分析" />
        </el-form-item>
        <el-form-item label="市场可行性" prop="marketFeasibility">
          <el-input v-model="analysisForm.marketFeasibility" type="textarea" :rows="3" placeholder="请输入市场可行性分析" />
        </el-form-item>
        <el-form-item label="风险评估" prop="riskAssessment">
          <el-input v-model="analysisForm.riskAssessment" type="textarea" :rows="3" placeholder="请输入风险评估" />
        </el-form-item>
        <el-form-item label="结论" prop="conclusion">
          <el-input v-model="analysisForm.conclusion" placeholder="请输入结论" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAnalysis" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Plus, Edit, Delete } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);
const analyses = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);
const showCreateDialog = ref(false);
const isEdit = ref(false);
const analysisForm = ref({
  id: null,
  projectId: projectId,
  title: '',
  technicalFeasibility: '',
  economicFeasibility: '',
  marketFeasibility: '',
  riskAssessment: '',
  conclusion: '',
  creatorId: null
});

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const fetchAnalyses = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/feasibility-analysis/project/${projectId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    const result = await response.json();
    console.log('可行性分析列表响应:', result);
    if (result.success) {
      analyses.value = result.data || [];
    } else {
      ElMessage.error(result.message || '获取可行性分析列表失败');
    }
  } catch (error) {
    console.error('获取可行性分析列表错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const saveAnalysis = async () => {
  if (!analysisForm.value.title) {
    ElMessage.warning('请输入分析标题');
    return;
  }

  saving.value = true;
  try {
    // 设置创建人
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      analysisForm.value.creatorId = user.id;
    }
    analysisForm.value.projectId = projectId;

    const url = isEdit.value ? 'http://localhost:8080/api/feasibility-analysis/update' : 'http://localhost:8080/api/feasibility-analysis/create';
    const method = isEdit.value ? 'PUT' : 'POST';
    
    console.log('提交数据:', analysisForm.value);
    
    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(analysisForm.value)
    });
    const result = await response.json();
    console.log('保存响应:', result);
    if (result.success) {
      ElMessage.success(isEdit.value ? '编辑可行性分析成功' : '添加可行性分析成功');
      showCreateDialog.value = false;
      resetForm();
      await fetchAnalyses();
    } else {
      ElMessage.error(result.message || (isEdit.value ? '编辑可行性分析失败' : '添加可行性分析失败'));
    }
  } catch (error) {
    console.error('保存错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    saving.value = false;
  }
};

const editAnalysis = (analysis: any) => {
  isEdit.value = true;
  analysisForm.value = { ...analysis };
  showCreateDialog.value = true;
};

const deleteAnalysis = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此可行性分析？此操作不可撤销！', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/feasibility-analysis/delete/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    const result = await response.json();
    if (result.success) {
      ElMessage.success('删除可行性分析成功');
      await fetchAnalyses();
    } else {
      ElMessage.error(result.message || '删除可行性分析失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
    }
  }
};

const resetForm = () => {
  analysisForm.value = {
    id: null,
    projectId: projectId,
    title: '',
    technicalFeasibility: '',
    economicFeasibility: '',
    marketFeasibility: '',
    riskAssessment: '',
    conclusion: '',
    creatorId: null
  };
  isEdit.value = false;
};

onMounted(() => {
  fetchAnalyses();
});
</script>

<style scoped>
.feasibility-analysis {
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
