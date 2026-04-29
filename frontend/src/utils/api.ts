import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from './router';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;

      switch (status) {
        case 400:
          ElMessage.error(data.message || '请求参数错误');
          break;
        case 401:
          ElMessage.error('登录已过期，请重新登录');
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          router.push('/login');
          break;
        case 403:
          ElMessage.error(data.message || '您没有权限执行此操作');
          break;
        case 404:
          ElMessage.error('请求的资源不存在');
          break;
        case 500:
          ElMessage.error(data.message || '服务器内部错误');
          break;
        default:
          ElMessage.error(data.message || '请求失败');
      }
    } else if (error.request) {
      ElMessage.error('网络连接失败，请检查网络');
    } else {
      ElMessage.error('请求配置错误');
    }

    return Promise.reject(error);
  }
);

export default apiClient;
