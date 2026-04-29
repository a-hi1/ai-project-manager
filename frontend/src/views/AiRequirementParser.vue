<template>
  <div class="ai-requirement-parser">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>AI需求文档解析</h2>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="上传文档" name="upload">
          <el-upload
            class="upload-demo"
            drag
            :action="uploadUrl"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :limit="1"
            :disabled="isParsing"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽需求文档到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">支持纯文本文件，AI将自动解析并提取功能点</div>
            </template>
          </el-upload>
        </el-tab-pane>

        <el-tab-pane label="粘贴内容" name="paste">
          <el-form :model="contentForm" label-width="100px">
            <el-form-item label="需求内容">
              <el-input v-model="contentForm.content" type="textarea" :rows="10" placeholder="请粘贴需求文档内容" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="parseContent" :loading="isParsing">开始解析</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div v-if="parseResult" class="parse-result">
        <el-divider>解析结果</el-divider>
        <div v-if="parsedData" class="result-content">
          <el-card v-if="parsedData['功能点']" class="result-card">
            <template #header>功能点</template>
            <el-tag v-for="(point, index) in parsedData['功能点']" :key="index" class="result-tag">{{ point }}</el-tag>
          </el-card>
          <el-card v-if="parsedData['业务规则']" class="result-card">
            <template #header>业务规则</template>
            <el-tag v-for="(rule, index) in parsedData['业务规则']" :key="index" type="success" class="result-tag">{{ rule }}</el-tag>
          </el-card>
          <el-card v-if="parsedData['需求冲突']" class="result-card">
            <template #header>需求冲突（警告）</template>
            <el-alert v-for="(conflict, index) in parsedData['需求冲突']" :key="index" :title="conflict" type="warning" show-icon />
          </el-card>
          <el-card v-if="parsedData['优化建议']" class="result-card">
            <template #header>优化建议</template>
            <el-tag v-for="(suggestion, index) in parsedData['优化建议']" :key="index" type="info" class="result-tag">{{ suggestion }}</el-tag>
          </el-card>
        </div>
        <div v-else class="raw-result">
          <pre>{{ parseResult }}</pre>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { UploadFilled } from '@element-plus/icons-vue';

const activeTab = ref('paste');
const isParsing = ref(false);
const parseResult = ref('');
const parsedData = ref<any>(null);
const token = localStorage.getItem('token') || '';
const uploadUrl = 'http://localhost:8080/api/ai/upload-document?projectId=1';

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${token}`
  };
});

const contentForm = ref({
  content: ''
});

const parseContent = async () => {
  if (!contentForm.value.content) {
    ElMessage.warning('请输入需求内容');
    return;
  }

  isParsing.value = true;
  try {
    const response = await fetch('http://localhost:8080/api/ai/parse-document', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ content: contentForm.value.content })
    });

    const result = await response.json();
    if (result.success) {
      parseResult.value = result.data;
      try {
        const jsonMatch = result.data.match(/\{[\s\S]*\}/);
        if (jsonMatch) {
          parsedData.value = JSON.parse(jsonMatch[0]);
        }
      } catch (e) {
        parsedData.value = null;
      }
      ElMessage.success('解析完成');
    } else {
      ElMessage.error(result.message || '解析失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  } finally {
    isParsing.value = false;
  }
};

const handleUploadSuccess = (response: any) => {
  if (response.success) {
    parseResult.value = response.data;
    try {
      const jsonMatch = response.data.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        parsedData.value = JSON.parse(jsonMatch[0]);
      }
    } catch (e) {
      parsedData.value = null;
    }
    ElMessage.success('上传并解析完成');
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

const handleUploadError = () => {
  ElMessage.error('上传失败');
};
</script>

<style scoped>
.ai-requirement-parser {
  padding: 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.parse-result {
  margin-top: 20px;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.result-card {
  margin-bottom: 10px;
}

.result-tag {
  margin: 5px;
}

.raw-result pre {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>