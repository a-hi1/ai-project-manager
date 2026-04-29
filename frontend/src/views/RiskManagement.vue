<template>
  <div class="risk-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="router.push(`/project/${projectId}`)">
              <el-icon><ArrowLeft /></el-icon>
              返回项目详情
            </el-button>
            <h2>风险管理</h2>
          </div>
          <el-button type="primary" @click="showRiskDialog = true">
            <el-icon><Plus /></el-icon>
            添加风险
          </el-button>
        </div>
      </template>

      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon"><Warning /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ risks.length }}</div>
              <div class="stat-label">总风险数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon danger"><CircleClose /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ highRisksCount }}</div>
              <div class="stat-label">高风险</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon primary"><Monitor /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ monitoringRisksCount }}</div>
              <div class="stat-label">监控中</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <el-icon class="stat-icon success"><CircleCheck /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ resolvedRisksCount }}</div>
              <div class="stat-label">已解决</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-table :data="risks" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="风险名称" min-width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="probability" label="概率" width="120">
          <template #default="scope">
            <el-progress :percentage="scope.row.probability" :color="getProbabilityColor(scope.row.probability)" />
          </template>
        </el-table-column>
        <el-table-column prop="impact" label="影响" width="120">
          <template #default="scope">
            <el-progress :percentage="scope.row.impact" :color="getImpactColor(scope.row.impact)" />
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="100">
          <template #default="scope">{{ getCategoryName(scope.row.category) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险值" width="100">
          <template #default="scope">
            <el-tag :type="getRiskLevel(scope.row.probability, scope.row.impact)">
              {{ Math.round((scope.row.probability * scope.row.impact) / 100) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建日期" width="120">
          <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editRisk(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteRisk(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 风险对话框 -->
    <el-dialog v-model="showRiskDialog" :title="isEditRisk ? '编辑风险' : '添加风险'" width="700px">
      <el-form :model="riskForm" label-width="100px">
        <el-form-item label="风险名称" prop="name">
          <el-input v-model="riskForm.name" placeholder="请输入风险名称" />
        </el-form-item>
        <el-form-item label="风险描述" prop="description">
          <el-input v-model="riskForm.description" type="textarea" :rows="3" placeholder="请输入风险描述" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发生概率(%)" prop="probability">
              <el-slider v-model="riskForm.probability" :min="0" :max="100" show-input />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="影响程度(%)" prop="impact">
              <el-slider v-model="riskForm.impact" :min="0" :max="100" show-input />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="风险类别" prop="category">
          <el-select v-model="riskForm.category" placeholder="请选择类别" style="width: 100%">
            <el-option label="技术风险" value="technical" />
            <el-option label="人员风险" value="human" />
            <el-option label="进度风险" value="schedule" />
            <el-option label="质量风险" value="quality" />
            <el-option label="外部风险" value="external" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前状态" prop="status">
          <el-select v-model="riskForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="已识别" value="identified" />
            <el-option label="分析中" value="analyzing" />
            <el-option label="计划中" value="planning" />
            <el-option label="监控中" value="monitoring" />
            <el-option label="已解决" value="resolved" />
          </el-select>
        </el-form-item>
        <el-form-item label="应对措施" prop="mitigationPlan">
          <el-input v-model="riskForm.mitigationPlan" type="textarea" :rows="3" placeholder="请输入应对措施" />
        </el-form-item>
        <el-form-item label="应急预案" prop="contingencyPlan">
          <el-input v-model="riskForm.contingencyPlan" type="textarea" :rows="3" placeholder="请输入应急预案" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRiskDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRisk" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Plus, Edit, Delete, Warning, CircleClose, Monitor, CircleCheck } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const risks = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);
const showRiskDialog = ref(false);
const isEditRisk = ref(false);

const riskForm = ref({
  id: null,
  projectId: projectId,
  name: '',
  description: '',
  probability: 50,
  impact: 50,
  category: 'technical',
  status: 'identified',
  mitigationPlan: '',
  contingencyPlan: '',
  identifiedBy: null,
  identifiedAt: new Date()
});

const highRisksCount = computed(() => {
  return risks.value.filter(r => {
    const riskValue = (r.probability * r.impact) / 100;
    return riskValue >= 40;
  }).length;
});

const monitoringRisksCount = computed(() => {
  return risks.value.filter(r => r.status === 'monitoring').length;
});

const resolvedRisksCount = computed(() => {
  return risks.value.filter(r => r.status === 'resolved').length;
});

const formatDate = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

const getCategoryName = (category: string) => {
  const categoryMap: Record<string, string> = {
    technical: '技术风险',
    human: '人员风险',
    schedule: '进度风险',
    quality: '质量风险',
    external: '外部风险'
  };
  return categoryMap[category] || category;
};

const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'identified': 'info',
    'analyzing': 'warning',
    'planning': 'warning',
    'monitoring': 'primary',
    'resolved': 'success'
  };
  return statusMap[status] || 'info';
};

const getStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    'identified': '已识别',
    'analyzing': '分析中',
    'planning': '计划中',
    'monitoring': '监控中',
    'resolved': '已解决'
  };
  return statusMap[status] || status;
};

const getProbabilityColor = (probability: number) => {
  if (probability >= 70) return '#f56c6c';
  if (probability >= 40) return '#e6a23c';
  return '#67c23a';
};

const getImpactColor = (impact: number) => {
  if (impact >= 70) return '#f56c6c';
  if (impact >= 40) return '#e6a23c';
  return '#67c23a';
};

const getRiskLevel = (probability: number, impact: number) => {
  const riskValue = (probability * impact) / 100;
  if (riskValue >= 40) return 'danger';
  if (riskValue >= 20) return 'warning';
  return 'success';
};

const fetchRisks = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/risk/project/${projectId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    console.log('风险列表响应:', result);
    if (result.success) {
      risks.value = result.data || [];
    } else {
      ElMessage.error(result.message || '获取风险列表失败');
    }
  } catch (error) {
    console.error('获取风险列表错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const resetRiskForm = () => {
  riskForm.value = {
    id: null,
    projectId: projectId,
    name: '',
    description: '',
    probability: 50,
    impact: 50,
    category: 'technical',
    status: 'identified',
    mitigationPlan: '',
    contingencyPlan: '',
    identifiedBy: null,
    identifiedAt: new Date()
  };
  isEditRisk.value = false;
};

const saveRisk = async () => {
  if (!riskForm.value.name) {
    ElMessage.warning('请输入风险名称');
    return;
  }

  saving.value = true;
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      riskForm.value.identifiedBy = JSON.parse(userStr).id;
    }

    const url = isEditRisk.value ? 'http://localhost:8080/api/risk/update' : 'http://localhost:8080/api/risk/create';
    const method = isEditRisk.value ? 'PUT' : 'POST';

    console.log('提交风险数据:', riskForm.value);

    const token = localStorage.getItem('token');
    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(riskForm.value)
    });
    const result = await response.json();
    console.log('保存风险响应:', result);
    if (result.success) {
      ElMessage.success(isEditRisk.value ? '更新风险成功' : '添加风险成功');
      showRiskDialog.value = false;
      await fetchRisks();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    console.error('保存风险错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    saving.value = false;
  }
};

const editRisk = (risk: any) => {
  isEditRisk.value = true;
  riskForm.value = { ...risk };
  showRiskDialog.value = true;
};

const deleteRisk = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此风险？此操作不可撤销！', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/risk/delete/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    if (result.success) {
      ElMessage.success('删除风险成功');
      await fetchRisks();
    } else {
      ElMessage.error(result.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
    }
  }
};

onMounted(() => {
  fetchRisks();
});
</script>

<style scoped>
.risk-management {
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

.stats-row {
  margin: 20px 0;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  font-size: 32px;
  color: #e6a23c;
}

.stat-icon.danger {
  color: #f56c6c;
}

.stat-icon.primary {
  color: #409eff;
}

.stat-icon.success {
  color: #67c23a;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>
