﻿﻿﻿﻿﻿﻿﻿<template>
  <div class="requirement-research">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="router.push(`/project/${projectId}`)">
              <el-icon><ArrowLeft /></el-icon>
              返回项目详情
            </el-button>
            <h2>需求调研</h2>
          </div>
          <el-button type="primary" @click="openCreateDialog" size="large">
            <el-icon><Plus /></el-icon>
            添加需求调研
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索调研标题或描述..."
          clearable
          prefix-icon="Search"
          style="width: 300px"
          @input="handleSearch"
        />
      </div>

      <!-- 数据表格 -->
      <el-table 
        :data="filteredResearches" 
        style="width: 100%" 
        v-loading="loading"
        :empty-text="searchKeyword ? '没有找到匹配的调研记录' : '暂无需求调研记录，点击右上角添加'"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="调研标题" min-width="200">
          <template #default="scope">
            <div class="title-cell">
              <el-icon :size="16" color="#409eff"><Document /></el-icon>
              <span>{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="调研描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="researchMethod" label="调研方法" min-width="150">
          <template #default="scope">
            <el-tag size="small" type="info" effect="light">{{ scope.row.researchMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="researchResult" label="调研结果" min-width="200" show-overflow-tooltip />
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
            <el-button type="primary" size="small" plain @click="editResearch(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" plain @click="deleteResearch(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="filteredResearches.length > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        :total="filteredResearches.length"
        class="pagination"
      />
    </el-card>

    <!-- 创建/编辑需求调研对话框 -->
    <el-dialog 
      v-model="showCreateDialog" 
      :title="isEdit ? '编辑需求调研' : '添加需求调研'" 
      width="600px"
      destroy-on-close
    >
      <el-form :model="researchForm" label-width="100px" :rules="formRules" ref="formRef">
        <el-form-item label="调研标题" prop="title">
          <el-input v-model="researchForm.title" placeholder="请输入调研标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="调研描述" prop="description">
          <el-input 
            v-model="researchForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入调研描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="调研方法" prop="researchMethod">
          <el-select v-model="researchForm.researchMethod" placeholder="请选择调研方法" style="width: 100%">
            <el-option label="问卷调查" value="问卷调查" />
            <el-option label="访谈" value="访谈" />
            <el-option label="观察法" value="观察法" />
            <el-option label="文档分析" value="文档分析" />
            <el-option label="原型测试" value="原型测试" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="调研结果" prop="researchResult">
          <el-input 
            v-model="researchForm.researchResult" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入调研结果"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveResearch" :loading="saving">
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
import { ArrowLeft, Plus, Edit, Delete, Document, Clock } from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);
const researches = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);
const showCreateDialog = ref(false);
const isEdit = ref(false);
const searchKeyword = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const formRef = ref();

const researchForm = ref({
  id: null,
  projectId: projectId,
  title: '',
  description: '',
  researchMethod: '',
  researchResult: '',
  creatorId: null
});

const formRules = {
  title: [
    { required: true, message: '请输入调研标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在 2-100 个字符之间', trigger: 'blur' }
  ],
  researchMethod: [
    { required: true, message: '请选择调研方法', trigger: 'change' }
  ],
  researchResult: [
    { required: true, message: '请输入调研结果', trigger: 'blur' }
  ]
};

const filteredResearches = computed(() => {
  if (!searchKeyword.value) return researches.value;
  const keyword = searchKeyword.value.toLowerCase();
  return researches.value.filter(r => 
    r.title?.toLowerCase().includes(keyword) || 
    r.description?.toLowerCase().includes(keyword) ||
    r.researchMethod?.toLowerCase().includes(keyword)
  );
});

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const handleSearch = () => {
  currentPage.value = 1;
};

const openCreateDialog = () => {
  isEdit.value = false;
  resetForm();
  showCreateDialog.value = true;
};

const fetchResearches = async () => {
  loading.value = true;
  try {
    const apiResult: any = await apiClient.get(`/requirement-research/project/${projectId}`);
    console.log('需求调研列表响应:', apiResult);
    if (apiResult.success) {
      researches.value = apiResult.data || [];
    } else {
      ElMessage.error(apiResult.message || '获取需求调研列表失败');
    }
  } catch (error) {
    console.error('获取需求调研列表错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const saveResearch = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      researchForm.value.creatorId = user.id;
    }
    researchForm.value.projectId = projectId;

    let apiResult: any;
    if (isEdit.value) {
      apiResult = await apiClient.put('/requirement-research/update', researchForm.value);
    } else {
      apiResult = await apiClient.post('/requirement-research/create', researchForm.value);
    }
    
    console.log('保存响应:', apiResult);
    if (apiResult.success) {
      ElMessage.success(isEdit.value ? '编辑需求调研成功' : '添加需求调研成功');
      showCreateDialog.value = false;
      resetForm();
      await fetchResearches();
    } else {
      ElMessage.error(apiResult.message || (isEdit.value ? '编辑需求调研失败' : '添加需求调研失败'));
    }
  } catch (error) {
    console.error('保存错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    saving.value = false;
  }
};

const editResearch = (research: any) => {
  isEdit.value = true;
  researchForm.value = { ...research };
  showCreateDialog.value = true;
};

const deleteResearch = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此需求调研？此操作不可撤销！', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const apiResult: any = await apiClient.delete(`/requirement-research/delete/${id}`);
    if (apiResult.success) {
      ElMessage.success('删除需求调研成功');
      await fetchResearches();
    } else {
      ElMessage.error(apiResult.message || '删除需求调研失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
    }
  }
};

const resetForm = () => {
  researchForm.value = {
    id: null,
    projectId: projectId,
    title: '',
    description: '',
    researchMethod: '',
    researchResult: '',
    creatorId: null
  };
  isEdit.value = false;
};

onMounted(() => {
  fetchResearches();
});
</script>

<style scoped>
.requirement-research {
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
