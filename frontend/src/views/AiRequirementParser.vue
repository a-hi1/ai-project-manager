﻿﻿﻿<template>
  <div class="ai-requirement-parser">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2>AI需求文档解析</h2>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="上传文档" name="upload">
          <el-upload
            class="upload-demo"
            drag
            :http-request="customUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :limit="1"
            :disabled="isParsing"
          >
            <el-icon class="el-icon--upload" :size="40"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽需求文档到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">支持文本文件、Office文档、代码文件等（.txt/.md/.docx/.xlsx/.pptx/.js/.ts/.py/.java/.pdf等）</div>
            </template>
          </el-upload>
        </el-tab-pane>

        <el-tab-pane label="粘贴内容" name="paste">
          <el-form :model="contentForm" label-width="100px">
            <el-form-item label="需求内容">
              <el-input
                v-model="contentForm.content"
                type="textarea"
                :rows="10"
                placeholder="请粘贴需求文档内容，尽可能详细地描述项目需求，AI将自动分析..."
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="parseContent" :loading="isParsing">
                <el-icon><MagicStick /></el-icon>
                开始解析
              </el-button>
              <el-button @click="contentForm.content = ''" :disabled="isParsing">
                <el-icon><RefreshLeft /></el-icon>
                清空
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div v-if="parseResult" class="parse-result">
        <el-divider content-position="left">解析结果</el-divider>
        <div v-if="parsedData" class="result-content">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" v-if="parsedData['功能点']">
              <el-card class="result-card">
                <template #header>
                  <div class="card-header">
                    <el-icon color="#409eff"><Box /></el-icon>
                    <span>功能点</span>
                  </div>
                </template>
                <el-tag
                  v-for="(point, index) in parsedData['功能点']"
                  :key="index"
                  class="result-tag"
                  type="info"
                >
                  {{ point }}
                </el-tag>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" v-if="parsedData['业务规则']">
              <el-card class="result-card">
                <template #header>
                  <div class="card-header">
                    <el-icon color="#67c23a"><DataAnalysis /></el-icon>
                    <span>业务规则</span>
                  </div>
                </template>
                <el-tag
                  v-for="(rule, index) in parsedData['业务规则']"
                  :key="index"
                  type="success"
                  class="result-tag"
                >
                  {{ rule }}
                </el-tag>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" v-if="parsedData['需求冲突']">
              <el-card class="result-card">
                <template #header>
                  <div class="card-header">
                    <el-icon color="#e6a23c"><Warning /></el-icon>
                    <span>需求冲突</span>
                  </div>
                </template>
                <el-alert
                  v-for="(conflict, index) in parsedData['需求冲突']"
                  :key="index"
                  :title="conflict"
                  type="warning"
                  show-icon
                  style="margin-bottom: 8px"
                />
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12" v-if="parsedData['优化建议']">
              <el-card class="result-card">
                <template #header>
                  <div class="card-header">
                    <el-icon color="#909399"><Edit /></el-icon>
                    <span>优化建议</span>
                  </div>
                </template>
                <el-tag
                  v-for="(suggestion, index) in parsedData['优化建议']"
                  :key="index"
                  type="info"
                  class="result-tag"
                >
                  {{ suggestion }}
                </el-tag>
              </el-card>
            </el-col>
          </el-row>
        </div>
        <div v-else class="raw-result">
          <el-alert title="原始解析结果" type="info" show-icon style="margin-bottom: 10px" />
          <pre>{{ parseResult }}</pre>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowLeft,
  UploadFilled,
  MagicStick,
  RefreshLeft,
  Box,
  DataAnalysis,
  Warning,
  Edit
} from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const router = useRouter();
const activeTab = ref('paste');
const isParsing = ref(false);
const parseResult = ref('');
const parsedData = ref<any>(null);
const token = localStorage.getItem('token') || '';
const uploadUrl = computed(() => `/ai/upload-document?projectId=1`);

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${token}`
  };
});

const contentForm = ref({
  content: ''
});

const customUpload = async (options: any) => {
  const { file } = options;
  isParsing.value = true;
  const formData = new FormData();
  formData.append('file', file);
  formData.append('projectId', '1');
  
  try {
    const result: any = await apiClient.post('/ai/upload-document', formData);
    options.onSuccess(result);
  } catch (error) {
    options.onError(error);
  } finally {
    isParsing.value = false;
  }
};

const beforeUpload = (file: any) => {
  const allowedExtensions = [
    '.txt', '.md', '.rst', '.rtf',
    '.docx', '.doc', '.xlsx', '.xls', '.pptx', '.ppt',
    '.js', '.ts', '.py', '.java', '.go', '.cpp', '.c', '.h',
    '.html', '.css', '.json', '.xml', '.yaml', '.yml',
    '.csv', '.log', '.sql', '.ini', '.cfg', '.conf',
    '.sh', '.bat', '.ps1',
    '.pdf'
  ];
  const fileNameLower = file.name.toLowerCase();
  const isAllowed = file.type.startsWith('text/') || 
                   file.type.startsWith('application/') || 
                   allowedExtensions.some(ext => fileNameLower.endsWith(ext));
  
  if (!isAllowed) {
    ElMessage.error('文件类型不支持！');
  }
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB！');
  }
  return isAllowed && isLt10M;
};

const parseContent = async () => {
  if (!contentForm.value.content) {
    ElMessage.warning('请输入需求内容');
    return;
  }

  isParsing.value = true;
  try {
    console.log('开始解析需求内容...');
    const apiResult: any = await apiClient.post('/ai/parse-document', { content: contentForm.value.content });
    console.log('解析结果:', apiResult);
    
    if (apiResult.success) {
      parseResult.value = apiResult.data;
      try {
        // 尝试解析JSON结果 - 更健壮的解析逻辑
        let jsonStr = apiResult.data;
        // 移除可能的markdown代码块标记
        jsonStr = jsonStr.replace(/```json\s*/g, '').replace(/```\s*$/g, '').trim();
        
        // 找到第一个{和最后一个}
        const startIndex = jsonStr.indexOf('{');
        const endIndex = jsonStr.lastIndexOf('}');
        
        if (startIndex !== -1 && endIndex !== -1 && endIndex > startIndex) {
          jsonStr = jsonStr.substring(startIndex, endIndex + 1);
          parsedData.value = JSON.parse(jsonStr);
          console.log('解析后的JSON数据:', parsedData.value);
        } else {
          throw new Error('未找到有效的JSON格式');
        }
      } catch {
        console.log('JSON解析失败，显示原始结果');
        parsedData.value = null;
      }
      ElMessage.success('解析完成');
    } else {
      ElMessage.error(apiResult.message || '解析失败');
    }
  } catch {
    console.error('解析过程出错');
    ElMessage.error('网络错误，请检查后端服务是否正常启动');
  } finally {
    isParsing.value = false;
  }
};

const handleUploadSuccess = (response: any) => {
  console.log('上传成功，响应:', response);
  
  // 检查 response 是否存在
  if (!response) {
    ElMessage.error('服务器无响应');
    return;
  }
  
  if (response.success) {
    parseResult.value = response.data;
    try {
      // 更健壮的JSON解析逻辑
      let jsonStr = response.data;
      // 移除可能的markdown代码块标记
      jsonStr = jsonStr.replace(/```json\s*/g, '').replace(/```\s*$/g, '').trim();
      
      // 找到第一个{和最后一个}
      const startIndex = jsonStr.indexOf('{');
      const endIndex = jsonStr.lastIndexOf('}');
      
      if (startIndex !== -1 && endIndex !== -1 && endIndex > startIndex) {
        jsonStr = jsonStr.substring(startIndex, endIndex + 1);
        parsedData.value = JSON.parse(jsonStr);
        console.log('上传解析后的JSON数据:', parsedData.value);
      } else {
        throw new Error('未找到有效的JSON格式');
      }
    } catch (e) {
      console.log('上传结果JSON解析失败:', e);
      parsedData.value = null;
    }
    ElMessage.success('上传并解析完成');
  } else {
    ElMessage.error(response?.message || '上传失败');
  }
};

const handleUploadError = () => {
  console.error('上传失败');
  ElMessage.error('上传失败，请检查后端服务或文件格式');
};

const goBack = () => {
  router.back();
};
</script>

<style scoped>
.ai-requirement-parser {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
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

.el-icon--upload {
  color: #409eff;
  margin-bottom: 10px;
}

.el-upload__text em {
  color: #409eff;
  font-style: normal;
}

.parse-result {
  margin-top: 30px;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-card {
  margin-bottom: 10px;
}

.result-card :deep(.el-card__header) {
  background-color: #f5f7fa;
}

.result-tag {
  margin: 5px;
}

.raw-result {
  margin-top: 15px;
}

.raw-result pre {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Consolas', 'Monaco', monospace;
  line-height: 1.6;
}
</style>
