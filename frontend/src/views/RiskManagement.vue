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
          <el-button type="primary" @click="openCreateDialog" size="large">
            <el-icon><Plus /></el-icon>
            添加风险
          </el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="16" class="stats-row">
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon-wrapper" style="background-color: #90939915;">
              <el-icon :size="24" color="#909399"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ risks.length }}</div>
              <div class="stat-label">总风险数</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon-wrapper" style="background-color: #f56c6c15;">
              <el-icon :size="24" color="#f56c6c"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number" style="color: #f56c6c;">{{ highRisksCount }}</div>
              <div class="stat-label">高风险</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon-wrapper" style="background-color: #409eff15;">
              <el-icon :size="24" color="#409eff"><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number" style="color: #409eff;">{{ monitoringRisksCount }}</div>
              <div class="stat-label">监控中</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card">
            <div class="stat-icon-wrapper" style="background-color: #67c23a15;">
              <el-icon :size="24" color="#67c23a"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number" style="color: #67c23a;">{{ resolvedRisksCount }}</div>
              <div class="stat-label">已解决</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索风险名称或描述..."
          clearable
          prefix-icon="Search"
          style="width: 300px"
          @input="handleSearch"
        />
        <el-select v-model="filterStatus" placeholder="筛选状态" clearable style="width: 150px" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="已识别" value="identified">
            <el-tag size="small" type="info">已识别</el-tag>
          </el-option>
          <el-option label="分析中" value="analyzing">
            <el-tag size="small" type="warning">分析中</el-tag>
          </el-option>
          <el-option label="计划中" value="planning">
            <el-tag size="small" type="warning">计划中</el-tag>
          </el-option>
          <el-option label="监控中" value="monitoring">
            <el-tag size="small" type="primary">监控中</el-tag>
          </el-option>
          <el-option label="已解决" value="resolved">
            <el-tag size="small" type="success">已解决</el-tag>
          </el-option>
        </el-select>
        <el-select v-model="filterCategory" placeholder="筛选类别" clearable style="width: 150px" @change="handleSearch">
          <el-option label="全部" value="" />
          <el-option label="技术风险" value="technical" />
          <el-option label="人员风险" value="human" />
          <el-option label="进度风险" value="schedule" />
          <el-option label="质量风险" value="quality" />
          <el-option label="外部风险" value="external" />
        </el-select>
      </div>

      <!-- 风险表格 -->
      <el-table 
        :data="filteredRisks" 
        style="width: 100%" 
        v-loading="loading"
        :empty-text="searchKeyword || filterStatus || filterCategory ? '没有找到匹配的风险' : '暂无风险记录，点击右上角添加新风险'"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="风险名称" min-width="180">
          <template #default="scope">
            <div class="risk-name-cell">
              <el-icon :size="16" :color="getRiskLevelColor(scope.row.probability, scope.row.impact)"><Warning /></el-icon>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="概率" width="140" align="center">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.probability" 
              :color="getProbabilityColor(scope.row.probability)"
              :stroke-width="6"
            />
          </template>
        </el-table-column>
        <el-table-column label="影响" width="140" align="center">
          <template #default="scope">
            <el-progress 
              :percentage="scope.row.impact" 
              :color="getImpactColor(scope.row.impact)"
              :stroke-width="6"
            />
          </template>
        </el-table-column>
        <el-table-column prop="category" label="类别" width="110" align="center">
          <template #default="scope">
            <el-tag size="small" type="info" effect="light">
              {{ getCategoryName(scope.row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" effect="light" round size="small">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险值" width="90" align="center">
          <template #default="scope">
            <el-tag 
              :type="getRiskLevelType(scope.row.probability, scope.row.impact)" 
              effect="dark" 
              round
              size="small"
            >
              {{ Math.round((scope.row.probability * scope.row.impact) / 100) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建日期" width="130" align="center">
          <template #default="scope">
            <div class="date-cell">
              <el-icon :size="14" color="#909399"><Calendar /></el-icon>
              {{ formatDate(scope.row.createdAt) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="editRisk(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" plain @click="deleteRisk(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="filteredRisks.length > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="filteredRisks.length"
        class="pagination"
      />
    </el-card>

    <!-- 风险对话框 -->
    <el-dialog 
      v-model="showRiskDialog" 
      :title="isEditRisk ? '编辑风险' : '添加风险'" 
      width="700px"
      destroy-on-close
    >
      <el-form :model="riskForm" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="风险名称" prop="name">
          <el-input v-model="riskForm.name" placeholder="请输入风险名称" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="风险描述" prop="description">
          <el-input 
            v-model="riskForm.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入风险描述"
            maxlength="500"
            show-word-limit
          />
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
            <el-option label="已识别" value="identified">
              <el-tag size="small" type="info">已识别</el-tag>
            </el-option>
            <el-option label="分析中" value="analyzing">
              <el-tag size="small" type="warning">分析中</el-tag>
            </el-option>
            <el-option label="计划中" value="planning">
              <el-tag size="small" type="warning">计划中</el-tag>
            </el-option>
            <el-option label="监控中" value="monitoring">
              <el-tag size="small" type="primary">监控中</el-tag>
            </el-option>
            <el-option label="已解决" value="resolved">
              <el-tag size="small" type="success">已解决</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="应对措施" prop="mitigationPlan">
          <el-input 
            v-model="riskForm.mitigationPlan" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入应对措施"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="应急预案" prop="contingencyPlan">
          <el-input 
            v-model="riskForm.contingencyPlan" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入应急预案"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRiskDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRisk" :loading="saving">
          {{ isEditRisk ? '保存修改' : '确认添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { 
  ArrowLeft, Plus, Edit, Delete, Warning, CircleClose, 
  Monitor, CircleCheck, Search, Calendar 
} from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const risks = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);
const showRiskDialog = ref(false);
const isEditRisk = ref(false);
const searchKeyword = ref('');
const filterStatus = ref('');
const filterCategory = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const formRef = ref();

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

const formRules = {
  name: [
    { required: true, message: '请输入风险名称', trigger: 'blur' },
    { min: 2, max: 100, message: '风险名称长度应在 2-100 个字符之间', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择风险类别', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择当前状态', trigger: 'change' }
  ]
};

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

const filteredRisks = computed(() => {
  let result = risks.value;
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    result = result.filter(r => 
      r.name?.toLowerCase().includes(keyword) || 
      r.description?.toLowerCase().includes(keyword)
    );
  }
  
  if (filterStatus.value) {
    result = result.filter(r => r.status === filterStatus.value);
  }
  
  if (filterCategory.value) {
    result = result.filter(r => r.category === filterCategory.value);
  }
  
  return result;
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

const getRiskLevelType = (probability: number, impact: number) => {
  const riskValue = (probability * impact) / 100;
  if (riskValue >= 40) return 'danger';
  if (riskValue >= 20) return 'warning';
  return 'success';
};

const getRiskLevelColor = (probability: number, impact: number) => {
  const riskValue = (probability * impact) / 100;
  if (riskValue >= 40) return '#f56c6c';
  if (riskValue >= 20) return '#e6a23c';
  return '#67c23a';
};

const handleSearch = () => {
  currentPage.value = 1;
};

const openCreateDialog = () => {
  isEditRisk.value = false;
  resetRiskForm();
  showRiskDialog.value = true;
};

const fetchRisks = async () => {
  loading.value = true;
  try {
    const result: any = await apiClient.get(`/risk/project/${projectId}`);
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
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      riskForm.value.identifiedBy = JSON.parse(userStr).id;
    }

    const url = isEditRisk.value ? '/risk/update' : '/risk/create';
    const method = isEditRisk.value ? 'PUT' : 'POST';

    console.log('提交风险数据:', riskForm.value);
    const result: any = isEditRisk.value 
      ? await apiClient.put(url, riskForm.value)
      : await apiClient.post(url, riskForm.value);
    console.log('保存风险响应:', result);
    if (result.success) {
      ElMessage.success(isEditRisk.value ? '更新风险成功' : '添加风险成功');
      showRiskDialog.value = false;
      resetRiskForm();
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
    const result: any = await apiClient.delete(`/risk/delete/${id}`);
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
  gap: 15px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.risk-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.date-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #606266;
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
  .search-bar .el-input,
  .search-bar .el-select {
    width: 100% !important;
  }
}
</style>
