<template>
  <div class="knowledge-base">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>知识库</h2>
          <el-button type="primary" @click="showAddDialog = true">添加知识</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词搜索">
          <el-input v-model="searchForm.keyword" placeholder="请输入关键词" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchKnowledge">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="documents" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="docType" label="类型" width="100">
          <template #default="scope">
            <el-tag>{{ getDocTypeName(scope.row.docType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDocument(scope.row)">查看</el-button>
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
          <el-input v-model="knowledgeForm.content" type="textarea" rows="5" />
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
      <el-form label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="currentDocument.title" disabled />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="currentDocument.content" type="textarea" rows="10" disabled />
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

const documents = ref<any[]>([]);
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
  try {
    const response = await fetch('http://localhost:8080/api/ai/knowledge/1', {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    const result = await response.json();
    if (result.success) {
      documents.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取知识库失败');
  }
};

const searchKnowledge = async () => {
  if (!searchForm.value.keyword) {
    fetchKnowledge();
    return;
  }

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
  }
};

const addKnowledge = async () => {
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
    }
  } catch (error) {
    ElMessage.error('添加失败');
  }
};

const viewDocument = (doc: any) => {
  currentDocument.value = { ...doc };
  showDetailDialog.value = true;
};

const resetForm = () => {
  knowledgeForm.value = {
    title: '',
    content: '',
    docType: 'requirement'
  };
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

onMounted(() => {
  fetchKnowledge();
});
</script>

<style scoped>
.knowledge-base {
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
</style>