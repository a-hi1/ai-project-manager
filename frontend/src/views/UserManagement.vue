﻿﻿<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2>用户管理</h2>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="showAddDialog = true">
              <el-icon><Plus /></el-icon>
              新增用户
            </el-button>
            <el-button type="default" @click="fetchUsers">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="roleName" label="角色" width="140">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.roleId)">
              {{ getRoleName(row.roleId) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" @click="deleteUser(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="currentUser.id ? '编辑用户' : '新增用户'"
      width="500px"
    >
      <el-form :model="currentUser" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="currentUser.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" v-if="!currentUser.id">
          <el-input v-model="currentUser.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="currentUser.roleId" placeholder="请选择角色">
            <el-option label="系统管理员" :value="1" />
            <el-option label="项目经理" :value="2" />
            <el-option label="开发者" :value="3" />
            <el-option label="测试工程师" :value="4" />
            <el-option label="产品经理" :value="5" />
            <el-option label="UI设计师" :value="6" />
            <el-option label="访客" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="currentUserStatus" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue';
import apiClient from '../utils/api';
import { ElMessage, ElMessageBox } from 'element-plus';

const users = ref<any[]>([]);
const loading = ref(false);
const showAddDialog = ref(false);
const currentUser = ref<any>({
  id: null,
  username: '',
  email: '',
  password: '',
  roleId: 7,
  status: 1
});
const currentUserStatus = ref(true);

// 角色ID映射
const getRoleName = (roleId: number): string => {
  const roleMap: Record<number, string> = {
    1: '系统管理员',
    2: '项目经理',
    3: '开发者',
    4: '测试工程师',
    5: '产品经理',
    6: 'UI设计师',
    7: '访客'
  };
  return roleMap[roleId] || '未知';
};

const getRoleTagType = (roleId: number): string => {
  const typeMap: Record<number, string> = {
    1: 'danger',
    2: 'warning',
    3: 'primary',
    4: 'success',
    5: 'info',
    6: '',
    7: 'info'
  };
  return typeMap[roleId] || '';
};

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true;
  try {
console.log('正在获取用户列表...');
    
    const result: any = await apiClient.get('/user/list');
    console.log('用户列表响应结果:', result);
    
    if (result.success) {
      users.value = result.data || [];
      console.log('用户数据加载成功:', users.value);
    } else {
      ElMessage.error(result.message || '获取用户列表失败');
      console.log('后端返回失败，使用模拟数据');
      loadMockUsers();
    }
  } catch (error) {
    console.error('获取用户列表异常:', error);
    ElMessage.error('获取用户列表失败，使用模拟数据');
    loadMockUsers();
  } finally {
    loading.value = false;
  }
};

// 加载模拟用户数据
const loadMockUsers = () => {
  users.value = [
    { id: 1, username: 'admin', email: 'admin@example.com', roleId: 1, status: 1 },
    { id: 2, username: 'pm', email: 'pm@example.com', roleId: 2, status: 1 },
    { id: 3, username: 'developer', email: 'dev@example.com', roleId: 3, status: 1 },
    { id: 4, username: 'tester', email: 'test@example.com', roleId: 4, status: 1 },
    { id: 5, username: 'guest', email: 'guest@example.com', roleId: 7, status: 1 }
  ];
  console.log('已加载模拟用户数据');
};

// 打开编辑对话框
const openEditDialog = (row: any) => {
  currentUser.value = { ...row };
  currentUserStatus.value = row.status === 1;
  showAddDialog.value = true;
};

// 保存用户
const saveUser = async () => {
  try {
    const userData = {
      ...currentUser.value,
      status: currentUserStatus.value ? 1 : 0
    };
    
    const url = userData.id 
      ? '/user/update' 
      : '/user/register';
      
    const result: any = await apiClient.post(url, userData);
    if (result.success) {
      ElMessage.success(userData.id ? '更新成功' : '创建成功');
      showAddDialog.value = false;
      resetForm();
      fetchUsers();
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error('操作失败');
  }
};

// 删除用户
const deleteUser = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const result: any = await apiClient.delete(`/user/delete?id=${row.id}`);
    if (result.success) {
      ElMessage.success('删除成功');
      fetchUsers();
    } else {
      ElMessage.error(result.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

// 重置表单
const resetForm = () => {
  currentUser.value = {
    id: null,
    username: '',
    email: '',
    password: '',
    roleId: 7,
    status: 1
  };
  currentUserStatus.value = true;
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
.user-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
}

.header-right {
  display: flex;
  gap: 12px;
}
</style>
