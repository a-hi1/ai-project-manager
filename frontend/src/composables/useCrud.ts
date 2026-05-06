import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import apiClient from '../utils/api';
import type { ApiResponse } from '../types';

export function useCrud<T>(basePath: string) {
  const list = ref<T[]>([]);
  const loading = ref(false);
  const saving = ref(false);

  async function fetchAll(params?: Record<string, unknown>) {
    loading.value = true;
    try {
      const result = await apiClient.get(basePath + '/list', { params }) as ApiResponse<T[]>;
      if (result.success) {
        list.value = result.data || [];
      }
      return result;
    } catch {
      ElMessage.error('获取数据失败');
      return null;
    } finally {
      loading.value = false;
    }
  }

  async function fetchByProject(projectId: number) {
    loading.value = true;
    try {
      const result = await apiClient.get(`${basePath}/project/${projectId}`) as ApiResponse<T[]>;
      if (result.success) {
        list.value = result.data || [];
      }
      return result;
    } catch {
      ElMessage.error('获取数据失败');
      return null;
    } finally {
      loading.value = false;
    }
  }

  async function create(data: Partial<T>) {
    saving.value = true;
    try {
      const result = await apiClient.post(`${basePath}/create`, data) as ApiResponse<T>;
      if (result.success) {
        ElMessage.success('创建成功');
      }
      return result;
    } catch {
      return null;
    } finally {
      saving.value = false;
    }
  }

  async function update(data: Partial<T> & { id: number | string }) {
    saving.value = true;
    try {
      const result = await apiClient.put(`${basePath}/update`, data) as ApiResponse<T>;
      if (result.success) {
        ElMessage.success('更新成功');
      }
      return result;
    } catch {
      return null;
    } finally {
      saving.value = false;
    }
  }

  async function remove(id: number | string) {
    saving.value = true;
    try {
      const result = await apiClient.delete(`${basePath}/delete/${id}`) as ApiResponse<null>;
      if (result.success) {
        ElMessage.success('删除成功');
      }
      return result;
    } catch {
      return null;
    } finally {
      saving.value = false;
    }
  }

  return {
    list,
    loading,
    saving,
    fetchAll,
    fetchByProject,
    create,
    update,
    remove
  };
}
