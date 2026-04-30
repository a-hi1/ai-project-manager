<template>
  <div class="document-archive">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2>文档归档</h2>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="archiveAllDocuments">归档所有文档</el-button>
          </div>
        </div>
      </template>

      <el-table :data="archives" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="文档标题" />
        <el-table-column prop="docType" label="文档类型" width="120">
          <template #default="scope">
            <el-tag>{{ getDocTypeName(scope.row.docType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sourceTable" label="来源表" width="120" />
        <el-table-column prop="archivedAt" label="归档时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const archives = ref<any[]>([]);

const fetchArchives = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/document-archive/project/${projectId}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
    const result = await response.json();
    if (result.success) {
      archives.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取归档列表失败');
  }
};

const archiveAllDocuments = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const response = await fetch(`http://localhost:8080/api/document-archive/archive-project/${projectId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ userId: user.id })
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success('文档归档成功');
      fetchArchives();
    } else {
      ElMessage.error(result.message || '归档失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const getDocTypeName = (docType: string) => {
  const nameMap: Record<string, string> = {
    requirement: '需求文档',
    feasibility: '可行性分析',
    risk: '风险文档',
    task: '任务文档',
    bug: '缺陷文档',
    project_archive: '项目归档'
  };
  return nameMap[docType] || docType;
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchArchives();
});
</script>

<style scoped>
.document-archive {
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
</style>