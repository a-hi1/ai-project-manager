<template>
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
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            添加需求调研
          </el-button>
        </div>
      </template>

      <el-table :data="researches" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="调研ID" width="80" />
        <el-table-column prop="title" label="调研标题" min-width="200" />
        <el-table-column prop="description" label="调研描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="researchMethod" label="调研方法" min-width="150" />
        <el-table-column prop="researchResult" label="调研结果" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editResearch(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteResearch(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑需求调研对话框 -->
    <el-dialog v-model="showCreateDialog" :title="isEdit ? '编辑需求调研' : '添加需求调研'" width="600px">
      <el-form :model="researchForm" label-width="100px">
        <el-form-item label="调研标题" prop="title">
          <el-input v-model="researchForm.title" placeholder="请输入调研标题" />
        </el-form-item>
        <el-form-item label="调研描述" prop="description">
          <el-input v-model="researchForm.description" type="textarea" :rows="3" placeholder="请输入调研描述" />
        </el-form-item>
        <el-form-item label="调研方法" prop="researchMethod">
          <el-input v-model="researchForm.researchMethod" placeholder="请输入调研方法" />
        </el-form-item>
        <el-form-item label="调研结果" prop="researchResult">
          <el-input v-model="researchForm.researchResult" type="textarea" :rows="3" placeholder="请输入调研结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="saveResearch" :loading="saving">保存</el-button>
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
const researches = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);
const showCreateDialog = ref(false);
const isEdit = ref(false);
const researchForm = ref({
  id: null,
  projectId: projectId,
  title: '',
  description: '',
  researchMethod: '',
  researchResult: '',
  creatorId: null
});

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const fetchResearches = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/requirement-research/project/${projectId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    const result = await response.json();
    console.log('需求调研列表响应:', result);
    if (result.success) {
      researches.value = result.data || [];
    } else {
      ElMessage.error(result.message || '获取需求调研列表失败');
    }
  } catch (error) {
    console.error('获取需求调研列表错误:', error);
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const saveResearch = async () => {
  if (!researchForm.value.title) {
    ElMessage.warning('请输入调研标题');
    return;
  }

  saving.value = true;
  try {
    // 设置创建人
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      researchForm.value.creatorId = user.id;
    }
    researchForm.value.projectId = projectId;

    const url = isEdit.value ? 'http://localhost:8080/api/requirement-research/update' : 'http://localhost:8080/api/requirement-research/create';
    const method = isEdit.value ? 'PUT' : 'POST';
    
    console.log('提交数据:', researchForm.value);
    
    const response = await fetch(url, {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(researchForm.value)
    });
    const result = await response.json();
    console.log('保存响应:', result);
    if (result.success) {
      ElMessage.success(isEdit.value ? '编辑需求调研成功' : '添加需求调研成功');
      showCreateDialog.value = false;
      resetForm();
      await fetchResearches();
    } else {
      ElMessage.error(result.message || (isEdit.value ? '编辑需求调研失败' : '添加需求调研失败'));
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

    const token = localStorage.getItem('token');
    const response = await fetch(`http://localhost:8080/api/requirement-research/delete/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    const result = await response.json();
    if (result.success) {
      ElMessage.success('删除需求调研成功');
      await fetchResearches();
    } else {
      ElMessage.error(result.message || '删除需求调研失败');
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
  gap: 10px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}
</style>
