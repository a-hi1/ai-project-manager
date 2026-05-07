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
        <el-table-column prop="fileName" label="文件名" width="200" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="320">
          <template #default="scope">
            <template v-if="scope.row.status === 'pending'">
              <el-button type="success" size="small" @click="approveDeliverable(scope.row)">通过</el-button>
              <el-button type="danger" size="small" @click="rejectDeliverable(scope.row)">拒绝</el-button>
            </template>
            <el-button type="info" size="small" @click="viewDeliverable(scope.row)">查看</el-button>
            <el-button type="primary" size="small" @click="downloadDeliverable(scope.row)" v-if="scope.row.filePath">下载</el-button>
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
        <el-form-item label="选择文件">
          <el-upload
            drag
            :auto-upload="false"
            :limit="1"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-upload="beforeUpload"
            ref="uploadRef">
            <el-icon class="el-icon--upload" :size="40"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持所有格式的文件，单个文件不超过 100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDeliverableDialog = false">取消</el-button>
        <el-button type="primary" @click="submitDeliverable" :loading="submitting">提交</el-button>
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
import { ArrowLeft, UploadFilled } from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const deliverables = ref<any[]>([]);
const showDeliverableDialog = ref(false);
const showDetailDialog = ref(false);
const submitting = ref(false);
const fileList = ref<any[]>([]);
const selectedFile = ref<File | null>(null);

const deliverableForm = ref({
  name: '',
  description: ''
});

const deliverableDetail = ref<any>({});

const fetchDeliverables = async () => {
  try {
    const result: any = await apiClient.get(`/deliverable/project/${projectId}`);
    if (result.data || result.success) {
      deliverables.value = result.data || [];
    }
  } catch (error) {
    ElMessage.error('获取交付物列表失败');
  }
};

const handleFileChange = (file: any) => {
  selectedFile.value = file.raw;
};

const handleFileRemove = () => {
  selectedFile.value = null;
};

const beforeUpload = (file: File) => {
  const isLt100M = file.size / 1024 / 1024 < 100;
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过 100MB!');
    return false;
  }
  return true;
};

const submitDeliverable = async () => {
  if (!deliverableForm.value.name) {
    ElMessage.warning('请输入交付物名称');
    return;
  }
  
  if (!selectedFile.value) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }
  
  submitting.value = true;
  
  try {
    const formData = new FormData();
    formData.append('file', selectedFile.value);
    formData.append('projectId', projectId.toString());
    formData.append('name', deliverableForm.value.name);
    if (deliverableForm.value.description) {
      formData.append('description', deliverableForm.value.description);
    }
    
    // 不手动设置 Content-Type，让 axios 自动处理
    const result: any = await apiClient.post('/deliverable/upload', formData);
    
    if (result.data || result.success) {
      ElMessage.success('交付物提交成功');
      showDeliverableDialog.value = false;
      resetForm();
      fetchDeliverables();
    } else {
      ElMessage.error(result.message || '提交失败');
    }
  } catch (error: any) {
    console.error('上传错误:', error);
    ElMessage.error(error.response?.data?.message || '网络错误');
  } finally {
    submitting.value = false;
  }
};

const downloadDeliverable = (deliverable: any) => {
  if (!deliverable.filePath) {
    ElMessage.warning('该交付物没有文件');
    return;
  }
  
  const filePath = encodeURIComponent(deliverable.filePath);
  window.open(`/api/deliverable/download?filePath=${filePath}`, '_blank');
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
    if (result.data || result.success) {
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
      reviewerId: user.id,
      comments: ''
    });
    if (result.data || result.success) {
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
    description: ''
  };
  fileList.value = [];
  selectedFile.value = null;
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

.upload-demo {
  margin-bottom: 20px;
}
</style>
