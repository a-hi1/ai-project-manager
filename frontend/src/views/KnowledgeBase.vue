<template>
  <div class="knowledge-base">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2>知识库</h2>
          </div>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加知识
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词搜索">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchKnowledge">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="fetchKnowledge">
            <el-icon><RefreshLeft /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="documents" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="docType" label="类型" width="120">
          <template #default="scope">
            <el-tag :type="getDocTypeTag(scope.row.docType)">{{ getDocTypeName(scope.row.docType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">{{ formatDateTime(scope.row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDocument(scope.row)">
              <el-icon><View /></el-icon>
              查看
            </el-button>
            <el-button type="danger" size="small" @click="deleteDocument(scope.row.id)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加知识对话框 -->
    <el-dialog v-model="showAddDialog" title="添加知识" width="600px">
      <el-form :model="knowledgeForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="knowledgeForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="knowledgeForm.content" type="textarea" :rows="8" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="文档类型">
          <el-select v-model="knowledgeForm.docType" placeholder="请选择类型">
            <el-option label="需求文档" value="requirement" />
            <el-option label="技术文档" value="technical" />
            <el-option label="管理文档" value="management" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="addKnowledge">添加</el-button>
      </template>
    </el-dialog>

    <!-- 知识详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="知识详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="标题">{{ currentDocument.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getDocTypeTag(currentDocument.docType)">{{ getDocTypeName(currentDocument.docType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentDocument.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div class="detail-content">{{ currentDocument.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  ArrowLeft,
  Plus,
  Search,
  RefreshLeft,
  View,
  Delete
} from '@element-plus/icons-vue';

const router = useRouter();
const documents = ref<any[]>([]);
const loading = ref(false);
const showAddDialog = ref(false);
const showDetailDialog = ref(false);
const token = localStorage.getItem('token') || '';

const searchForm = ref({
  keyword: ''
});

const knowledgeForm = ref({
  title: '',
  content: '',
  docType: 'requirement'
});

const currentDocument = ref<any>({});

const fetchKnowledge = async () => {
  loading.value = true;
  try {
    const response = await fetch('http://localhost:8080/api/ai/knowledge/1', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    if (result.success) {
      documents.value = result.data;
    } else {
      ElMessage.error(result.message || '获取知识库失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  } finally {
    loading.value = false;
  }
};

const searchKnowledge = async () => {
  if (!searchForm.value.keyword) {
    fetchKnowledge();
    return;
  }

  loading.value = true;
  try {
    const response = await fetch(`http://localhost:8080/api/ai/knowledge/search/1?keyword=${encodeURIComponent(searchForm.value.keyword)}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    if (result.success) {
      documents.value = result.data;
      ElMessage.success('搜索完成');
    }
  } catch (error) {
    ElMessage.error('搜索失败');
  } finally {
    loading.value = false;
  }
};

const addKnowledge = async () => {
  if (!knowledgeForm.value.title || !knowledgeForm.value.content) {
    ElMessage.warning('请填写完整信息');
    return;
  }

  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const response = await fetch('http://localhost:8080/api/ai/knowledge/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        projectId: 1,
        title: knowledgeForm.value.title,
        content: knowledgeForm.value.content,
        docType: knowledgeForm.value.docType,
        createdBy: user.id
      })
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success('添加成功');
      showAddDialog.value = false;
      resetForm();
      fetchKnowledge();
    } else {
      ElMessage.error(result.message || '添加失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，请稍后重试');
  }
};

const viewDocument = (doc: any) => {
  currentDocument.value = { ...doc };
  showDetailDialog.value = true;
};

const deleteDocument = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除此知识文档？此操作不可撤销！', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const response = await fetch(`http://localhost:8080/api/ai/knowledge/delete/${id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    const result = await response.json();
    if (result.success) {
      ElMessage.success('删除成功');
      fetchKnowledge();
    } else {
      ElMessage.error(result.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('网络错误，请稍后重试');
    }
  }
};

const resetForm = () => {
  knowledgeForm.value = {
    title: '',
    content: '',
    docType: 'requirement'
  };
};

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const getDocTypeName = (docType: string) => {
  const nameMap: Record<string, string> = {
    requirement: '需求文档',
    technical: '技术文档',
    management: '管理文档',
    other: '其他'
  };
  return nameMap[docType] || docType;
};

const getDocTypeTag = (docType: string) => {
  const typeMap: Record<string, string> = {
    requirement: 'primary',
    technical: 'success',
    management: 'warning',
    other: 'info'
  };
  return typeMap[docType] || 'info';
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchKnowledge();
});
</script>

<style scoped>
.knowledge-base {
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

.search-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.detail-content {
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
