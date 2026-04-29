<template>
  <div class="change-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>变更管理</h2>
          <el-button type="primary" @click="showChangeDialog = true">提交变更请求</el-button>
        </div>
      </template>

      <el-table :data="changeRequests" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="变更标题" />
        <el-table-column prop="description" label="变更描述" />
        <el-table-column prop="reason" label="变更原因" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="scope">
            <template v-if="scope.row.status === 'pending'">
              <el-button type="success" size="small" @click="approveChange(scope.row)">批准</el-button>
              <el-button type="danger" size="small" @click="rejectChange(scope.row)">拒绝</el-button>
            </template>
            <el-button type="info" size="small" @click="viewChange(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 变更请求对话框 -->
    <el-dialog v-model="showChangeDialog" title="提交变更请求" width="600px">
      <el-form :model="changeForm" label-width="100px">
        <el-form-item label="变更标题">
          <el-input v-model="changeForm.title" placeholder="请输入变更标题" />
        </el-form-item>
        <el-form-item label="变更描述">
          <el-input v-model="changeForm.description" type="textarea" placeholder="请输入变更描述" />
        </el-form-item>
        <el-form-item label="变更原因">
          <el-input v-model="changeForm.reason" type="textarea" placeholder="请输入变更原因" />
        </el-form-item>
        <el-form-item label="影响评估">
          <el-input v-model="changeForm.impact" type="textarea" placeholder="请输入影响评估" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangeDialog = false">取消</el-button>
        <el-button type="primary" @click="submitChange">提交</el-button>
      </template>
    </el-dialog>

    <!-- 变更详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="变更详情" width="600px">
      <el-form :model="changeDetail" label-width="100px">
        <el-form-item label="变更标题">
          <el-input v-model="changeDetail.title" disabled />
        </el-form-item>
        <el-form-item label="变更描述">
          <el-input v-model="changeDetail.description" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="变更原因">
          <el-input v-model="changeDetail.reason" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="影响评估">
          <el-input v-model="changeDetail.impact" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="changeDetail.status" disabled />
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';

const route = useRoute();
const projectId = Number(route.params.id);

const changeRequests = ref<any[]>([]);
const showChangeDialog = ref(false);
const showDetailDialog = ref(false);

const changeForm = ref({
  title: '',
  description: '',
  reason: '',
  impact: ''
});

const changeDetail = ref<any>({});

const fetchChangeRequests = async () => {
  try {
    const response = await fetch(`http://localhost:8080/api/change-request/project/${projectId}`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });
    const result = await response.json();
    if (result.success) {
      changeRequests.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取变更请求失败');
  }
};

const submitChange = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const response = await fetch('http://localhost:8080/api/change-request/create', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({
        projectId,
        title: changeForm.value.title,
        description: changeForm.value.description,
        reason: changeForm.value.reason,
        impact: changeForm.value.impact,
        requesterId: user.id,
        status: 'pending'
      })
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success('变更请求提交成功');
      showChangeDialog.value = false;
      resetForm();
      fetchChangeRequests();
    } else {
      ElMessage.error(result.message || '提交失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const approveChange = async (change: any) => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const response = await fetch(`http://localhost:8080/api/change-request/approve/${change.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ reviewerId: user.id })
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success('变更请求已批准');
      fetchChangeRequests();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const rejectChange = async (change: any) => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const response = await fetch(`http://localhost:8080/api/change-request/reject/${change.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ reviewerId: user.id })
    });

    const result = await response.json();
    if (result.success) {
      ElMessage.success('变更请求已拒绝');
      fetchChangeRequests();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const viewChange = (change: any) => {
  changeDetail.value = { ...change };
  showDetailDialog.value = true;
};

const resetForm = () => {
  changeForm.value = {
    title: '',
    description: '',
    reason: '',
    impact: ''
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
    approved: '已批准',
    rejected: '已拒绝'
  };
  return nameMap[status] || status;
};

onMounted(() => {
  fetchChangeRequests();
});
</script>

<style scoped>
.change-management {
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