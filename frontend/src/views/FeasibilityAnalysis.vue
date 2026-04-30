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
          <el-button type="primary" @click="openCreateDialog" size="large">
            <el-icon><Plus /></el-icon>
            添加可行性分析
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索分析标题或内容..."
          clearable
          prefix-icon="Search"
          style="width: 300px"
          @input="handleSearch"
        />
      </div>

      <!-- 数据表格 -->
      <el-table 
        :data="filteredAnalyses" 
        style="width: 100%" 
        v-loading="loading"
        :empty-text="searchKeyword ? '没有找到匹配的分析记录' : '暂无可行性分析记录，点击右上角添加'"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="分析标题" min-width="200">
          <template #default="scope">
            <div class="title-cell">
              <el-icon :size="16" color="#67c23a"><DataAnalysis /></el-icon>
              <span>{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="technicalFeasibility" label="技术可行性" min-width="150" show-overflow-tooltip />
        <el-table-column prop="economicFeasibility" label="经济可行性" min-width="150" show-overflow-tooltip />
        <el-table-column prop="marketFeasibility" label="市场可行性" min-width="150" show-overflow-tooltip />
        <el-table-column prop="riskAssessment" label="风险评估" min-width="150" show-overflow-tooltip />
        <el-table-column prop="conclusion" label="结论" min-width="120">
          <template #default="scope">
            <el-tag 
              :type="getConclusionType(scope.row.conclusion)" 
              effect="light" 
              size="small"
            >
              {{ scope.row.conclusion || '待评估' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            <div class="time-cell">
              <el-icon :size="14" color="#909399"><Clock /></el-icon>
              {{ formatDateTime(scope.row.createdAt) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" plain @click="editAnalysis(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" plain @click="deleteAnalysis(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="filteredAnalyses.length > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="filteredAnalyses.length"
        class="pagination"
      />
    </el-card>

    <!-- 创建/编辑可行性分析对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="isEdit ? '编辑可行性分析' : '添加可行性分析'" 
      width="650px"
      destroy-on-close
    >
      <el-form :model="analysisForm" label-width="110px" :rules="formRules" ref="formRef">
        <el-form-item label="分析标题" prop="title">
          <el-input v-model="analysisForm.title" placeholder="请输入分析标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="技术可行性" prop="technicalFeasibility">
          <el-input 
            v-model="analysisForm.technicalFeasibility" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入技术可行性分析"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="经济可行性" prop="economicFeasibility">
          <el-input 
            v-model="analysisForm.economicFeasibility" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入经济可行性分析"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="市场可行性" prop="marketFeasibility">
          <el-input 
            v-model="analysisForm.marketFeasibility" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入市场可行性分析"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="风险评估" prop="riskAssessment">
          <el-input 
            v-model="analysisForm.riskAssessment" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入风险评估"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="结论" prop="conclusion">
          <el-select v-model="analysisForm.conclusion" placeholder="请选择结论" style="width: 100%">
            <el-option label="可行" value="可行">
              <el-tag size="small" type="success">可行</el-tag>
            </el-option>
            <el-option label="基本可行" value="基本可行">
              <el-tag size="small" type="warning">基本可行</el-tag>
            </el-option>
            <el-option label="不可行" value="不可行">
              <el-tag size="small" type="danger">不可行</el-tag>
            </el-option>
            <el-option label="需进一步评估" value="需进一步评估">
              <el-tag size="small" type="info">需进一步评估</el-tag>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAnalysis" :loading="saving">
          {{ isEdit ? '保存修改' : '确认添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, Plus, Edit, Delete, Search, DataAnalysis, Clock } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);
const analyses = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);
const showCreateDialog = ref(false);
const isEdit = ref(false);
const searchKeyword = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const formRef = ref();

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

const formRules = {
  title: [
    { required: true, message: '请输入分析标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在 2-100 个字符之间', trigger: 'blur' }
  ],
  conclusion: [
    { required: true, message: '请选择结论', trigger: 'change' }
  ]
};

const filteredAnalyses = computed(() => {
  if (!searchKeyword.value) return analyses.value;
  const keyword = searchKeyword.value.toLowerCase();
  return analyses.value.filter(a => 
    a.title?.toLowerCase().includes(keyword) || 
    a.technicalFeasibility?.toLowerCase().includes(keyword) ||
    a.economicFeasibility?.toLowerCase().includes(keyword) ||
    a.marketFeasibility?.toLowerCase().includes(keyword)
  );
});

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const getConclusionType = (conclusion: string) => {
  const typeMap: Record<string, string> = {
    '可行': 'success',
    '基本可行': 'warning',
    '不可行': 'danger',
    '需进一步评估': 'info'
  };
  return typeMap[conclusion] || 'info';
};

const handleSearch = () => {
  currentPage.value = 1;
};

const openCreateDialog = () => {
  isEdit.value = false;
  resetForm();
  showCreateDialog.value = true;
};

const fetchAnalyses = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/feasibility-analysis/project/${projectId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
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
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
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
      headers: { 'Authorization': `Bearer ${token}` }
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
  gap: 15px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.search-bar {
  margin-bottom: 20px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.time-cell {
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
  .search-bar .el-input {
    width: 100% !important;
  }
}
</style>
