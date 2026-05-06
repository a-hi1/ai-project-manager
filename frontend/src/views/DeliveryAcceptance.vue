<template>
  <div class="delivery-acceptance">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2>交付验收</h2>
          </div>
          <el-button type="primary" @click="showDeliverableDialog = true">提交交付物</el-button>
        </div>
      </template>

      <el-table :data="deliverables" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="交付物名称" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <template v-if="scope.row.status === 'pending'">
              <el-button type="success" size="small" @click="approveDeliverable(scope.row)">通过</el-button>
              <el-button type="danger" size="small" @click="rejectDeliverable(scope.row)">拒绝</el-button>
            </template>
            <el-button type="info" size="small" @click="viewDeliverable(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 提交交付物对话框 -->
    <el-dialog v-model="showDeliverableDialog" title="提交交付物" width="600px">
      <el-form :model="deliverableForm" label-width="100px">
        <el-form-item label="交付物名称">
          <el-input v-model="deliverableForm.name" placeholder="请输入交付物名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="deliverableForm.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="文件路径">
          <el-input v-model="deliverableForm.filePath" placeholder="请输入文件路径或上传文件" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeliverableDialog = false">取消</el-button>
        <el-button type="primary" @click="submitDeliverable">提交</el-button>
      </template>
    </el-dialog>

    <!-- 交付物详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="交付物详情" width="600px">
      <el-form :model="deliverableDetail" label-width="100px">
        <el-form-item label="交付物名称">
          <el-input v-model="deliverableDetail.name" disabled />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="deliverableDetail.description" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="文件路径">
          <el-input v-model="deliverableDetail.filePath" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="deliverableDetail.status" disabled />
        </el-form-item>
        <el-form-item label="审批意见" v-if="deliverableDetail.comments">
          <el-input v-model="deliverableDetail.comments" type="textarea" disabled />
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const deliverables = ref<any[]>([]);
const showDeliverableDialog = ref(false);
const showDetailDialog = ref(false);

const deliverableForm = ref({
  name: '',
  description: '',
  filePath: ''
});

const deliverableDetail = ref<any>({});

const fetchDeliverables = async () => {
  try {
    const result: any = await apiClient.get(`/deliverable/project/${projectId}`);
    if (result.success) {
      deliverables.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取交付物列表失败');
  }
};

const submitDeliverable = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const result: any = await apiClient.post('/deliverable/create', {
      projectId,
      name: deliverableForm.value.name,
      description: deliverableForm.value.description,
      filePath: deliverableForm.value.filePath,
      submittedBy: user.id,
      status: 'pending'
    });
    if (result.success) {
      ElMessage.success('交付物提交成功');
      showDeliverableDialog.value = false;
      resetForm();
      fetchDeliverables();
    } else {
      ElMessage.error(result.message || '提交失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const viewDeliverable = (deliverable: any) => {
  deliverableDetail.value = { ...deliverable };
  showDetailDialog.value = true;
};

const approveDeliverable = async (deliverable: any) => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const result: any = await apiClient.put(`/deliverable/approve/${deliverable.id}`, {
      reviewerId: user.id
    });
    if (result.success) {
      ElMessage.success('交付物已通过');
      fetchDeliverables();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const rejectDeliverable = async (deliverable: any) => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const result: any = await apiClient.put(`/deliverable/reject/${deliverable.id}`, {
      reviewerId: user.id
    });
    if (result.success) {
      ElMessage.success('交付物已拒绝');
      fetchDeliverables();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const resetForm = () => {
  deliverableForm.value = {
    name: '',
    description: '',
    filePath: ''
  };
};

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  };
  return typeMap[status] || 'info';
};

const getStatusName = (status: string) => {
  const nameMap: Record<string, string> = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已拒绝'
  };
  return nameMap[status] || status;
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchDeliverables();
});
</script>

<style scoped>
.delivery-acceptance {
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
