﻿﻿<template>
  <div class="bug-tracking">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2>缺陷跟踪</h2>
          </div>
          <el-button type="primary" @click="showBugDialog = true">提交Bug</el-button>
        </div>
      </template>

      <el-table :data="bugs" style="width: 100%">
        <el-table-column prop="id" label="Bug ID" width="80" />
        <el-table-column prop="title" label="Bug标题" />
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="scope">
            <el-tag :type="getSeverityType(scope.row.severity)">{{ scope.row.severity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)">{{ scope.row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getBugStatusType(scope.row.status)">{{ getBugStatusName(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'open'" type="success" size="small" @click="assignBug(scope.row)">分配</el-button>
            <el-button v-if="scope.row.status === 'assigned'" type="warning" size="small" @click="resolveBug(scope.row)">修复</el-button>
            <el-button type="info" size="small" @click="viewBug(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 提交Bug对话框 -->
    <el-dialog v-model="showBugDialog" title="提交Bug" width="600px">
      <el-form :model="bugForm" label-width="100px">
        <el-form-item label="Bug标题">
          <el-input v-model="bugForm.title" placeholder="请输入Bug标题" />
        </el-form-item>
        <el-form-item label="Bug描述">
          <el-input v-model="bugForm.description" type="textarea" placeholder="请输入Bug描述" />
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="bugForm.severity" placeholder="请选择严重程度">
            <el-option label="致命" value="critical" />
            <el-option label="严重" value="major" />
            <el-option label="一般" value="minor" />
            <el-option label="轻微" value="trivial" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="bugForm.priority" placeholder="请选择优先级">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBugDialog = false">取消</el-button>
        <el-button type="primary" @click="submitBug">提交</el-button>
      </template>
    </el-dialog>

    <!-- Bug详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="Bug详情" width="600px">
      <el-form :model="bugDetail" label-width="100px">
        <el-form-item label="Bug标题">
          <el-input v-model="bugDetail.title" disabled />
        </el-form-item>
        <el-form-item label="Bug描述">
          <el-input v-model="bugDetail.description" type="textarea" disabled />
        </el-form-item>
        <el-form-item label="严重程度">
          <el-input v-model="bugDetail.severity" disabled />
        </el-form-item>
        <el-form-item label="优先级">
          <el-input v-model="bugDetail.priority" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="bugDetail.status" disabled />
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 分配Bug对话框 -->
    <el-dialog v-model="showAssignDialog" title="分配Bug" width="500px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="Bug">
          <el-input v-model="assignForm.bugTitle" disabled />
        </el-form-item>
        <el-form-item label="分配给">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择人员">
            <el-option v-for="user in projectMembers" :key="user.userId" :label="user.username" :value="user.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssignDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确认分配</el-button>
      </template>
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

const bugs = ref<any[]>([]);
const projectMembers = ref<any[]>([]);
const showBugDialog = ref(false);
const showDetailDialog = ref(false);
const showAssignDialog = ref(false);

const bugForm = ref({
  title: '',
  description: '',
  severity: 'minor',
  priority: 'medium'
});

const bugDetail = ref<any>({});

const assignForm = ref({
  bugId: '',
  bugTitle: '',
  assigneeId: null as number | null
});

const fetchBugs = async () => {
  try {
    const result: any = await apiClient.get(`/bug/project/${projectId}`);
    if (result.success) {
      bugs.value = result.data;
    }
  } catch (error) {
    ElMessage.error('获取Bug列表失败');
  }
};

const fetchProjectMembers = async () => {
  try {
    const result: any = await apiClient.get(`/project/member/list/${projectId}`);
    if (result.success) {
      projectMembers.value = result.data;
    }
  } catch (error) {
    console.error('获取项目成员失败');
  }
};

const submitBug = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    const result: any = await apiClient.post('/bug/create', {
      projectId,
      title: bugForm.value.title,
      description: bugForm.value.description,
      severity: bugForm.value.severity,
      priority: bugForm.value.priority,
      reporterId: user.id,
      status: 'open'
    });
    if (result.success) {
      ElMessage.success('Bug提交成功');
      showBugDialog.value = false;
      resetBugForm();
      fetchBugs();
    } else {
      ElMessage.error(result.message || '提交失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const viewBug = (bug: any) => {
  bugDetail.value = { ...bug };
  showDetailDialog.value = true;
};

const assignBug = (bug: any) => {
  assignForm.value = {
    bugId: bug.id,
    bugTitle: bug.title,
    assigneeId: null
  };
  showAssignDialog.value = true;
};

const confirmAssign = async () => {
  try {
    const result: any = await apiClient.put(`/bug/assign/${assignForm.value.bugId}`, {
      assigneeId: assignForm.value.assigneeId
    });
    if (result.success) {
      ElMessage.success('Bug分配成功');
      showAssignDialog.value = false;
      fetchBugs();
    } else {
      ElMessage.error(result.message || '分配失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const resolveBug = async (bug: any) => {
  try {
    const result: any = await apiClient.put(`/bug/resolve/${bug.id}`);
    if (result.success) {
      ElMessage.success('Bug已修复');
      fetchBugs();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  }
};

const resetBugForm = () => {
  bugForm.value = {
    title: '',
    description: '',
    severity: 'minor',
    priority: 'medium'
  };
};

const getSeverityType = (severity: string) => {
  const typeMap: Record<string, string> = {
    critical: 'danger',
    major: 'warning',
    minor: 'info',
    trivial: 'info'
  };
  return typeMap[severity] || 'info';
};

const getPriorityType = (priority: string) => {
  const typeMap: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  };
  return typeMap[priority] || 'info';
};

const getBugStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    open: 'danger',
    assigned: 'warning',
    resolved: 'success',
    closed: 'info'
  };
  return typeMap[status] || 'info';
};

const getBugStatusName = (status: string) => {
  const nameMap: Record<string, string> = {
    open: '打开',
    assigned: '已分配',
    resolved: '已修复',
    closed: '已关闭'
  };
  return nameMap[status] || status;
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  fetchBugs();
  fetchProjectMembers();
});
</script>

<style scoped>
.bug-tracking {
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