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
            <el-button type="danger" @click="clearAllArchives" v-if="archives.length > 0">
              清空归档
            </el-button>
            <el-button type="primary" @click="archiveAllDocuments">
              归档所有文档
            </el-button>
          </div>
        </div>
      </template>

      <!-- 功能说明 -->
      <el-alert
        title="文档归档功能说明"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #default>
          <p>文档归档用于保存项目生命周期中的重要文档，包括：</p>
          <ul style="margin: 10px 0 0 20px">
            <li>• 需求文档：项目原始需求和变更记录</li>
            <li>• 风险文档：识别到的风险和应对措施</li>
            <li>• 缺陷文档：bug 修复记录</li>
            <li>• 项目归档：项目结束时的完整文档快照</li>
          </ul>
          <p style="margin-top: 10px">归档文档可以用于：</p>
          <ul style="margin: 10px 0 0 20px">
            <li>• 审计和合规检查</li>
            <li>• 后续项目参考</li>
            <li>• 知识传承</li>
            <li>• 问题追溯</li>
          </ul>
        </template>
      </el-alert>

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
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="danger" size="small" link @click="deleteArchive(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const archives = ref<any[]>([]);

const fetchArchives = async () => {
  try {
    const result: any = await apiClient.get(`/document-archive/project/${projectId}`);
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
    const result: any = await apiClient.post(`/document-archive/archive-project/${projectId}`, {
      userId: user.id
    });
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

const deleteArchive = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除此归档文档吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const result: any = await apiClient.delete(`/document-archive/${id}`);
    if (result.success) {
      ElMessage.success('删除成功');
      fetchArchives();
    } else {
      ElMessage.error(result.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const clearAllArchives = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有归档文档吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    const result: any = await apiClient.delete(`/document-archive/project/${projectId}`);
    if (result.success) {
      ElMessage.success('清空成功');
      fetchArchives();
    } else {
      ElMessage.error(result.message || '清空失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空失败');
    }
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

.header-actions {
  display: flex;
  gap: 10px;
}
</style>
