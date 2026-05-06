<template>
  <div class="ai-chat-assistant">
    <div class="chat-layout">
      <!-- 侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <div class="sidebar-title">
            <el-icon :size="20" color="#409eff"><ChatDotRound /></el-icon>
            <span>AI 项目助手</span>
          </div>
          <el-button type="primary" plain size="small" @click="createNewConversation">
            <el-icon><Plus /></el-icon>
            新会话
          </el-button>
        </div>
        
        <!-- 项目选择 -->
        <div class="project-selector">
          <div class="selector-title">
            <el-icon><FolderOpened /></el-icon>
            <span>选择项目</span>
          </div>
          <el-select 
            v-model="selectedProjectId" 
            placeholder="选择项目" 
            clearable 
            style="width: 100%"
            @change="onProjectChange"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
          <div v-if="selectedProjectId" class="selected-project-info">
            <div class="info-row">
              <span class="info-label">项目状态:</span>
              <el-tag :type="getStatusType(project.status)" size="small">{{ project.status }}</el-tag>
            </div>
          </div>
        </div>
        
        <div class="sidebar-content">
          <div class="conversation-list">
            <div 
              v-for="conv in conversations" 
              :key="conv.id"
              class="conversation-item"
              :class="{ active: activeConversationId === conv.id }"
              @click="selectConversation(conv)"
            >
              <div class="conv-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="conv-info">
                <div class="conv-title">{{ conv.title }}</div>
                <div class="conv-time">{{ formatConvTime(conv.updatedAt) }}</div>
              </div>
            </div>
            
            <div v-if="conversations.length === 0" class="empty-conversations">
              <el-icon :size="40" color="#e0e0e0"><ChatDotRound /></el-icon>
              <p>暂无会话历史</p>
              <p>开始新对话吧~</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 聊天主区域 -->
      <div class="chat-container">
        <div class="chat-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack" class="back-btn">
              <el-icon><ArrowLeft /></el-icon>
            </el-button>
            <h2>AI 项目助手</h2>
          </div>
          <div class="header-right">
            <el-button type="default" @click="showUploadDialog = true" class="upload-btn">
              <el-icon><Upload /></el-icon>
              上传文档
            </el-button>
            <el-button type="primary" size="small" @click="clearConversation" class="clear-btn">
              <el-icon><Delete /></el-icon>
              清空对话
            </el-button>
          </div>
        </div>

        <div class="chat-messages" ref="messagesContainer">
          <div v-if="messages.length === 0" class="empty-hint">
            <div class="welcome-icon">
              <el-icon :size="64" color="#409eff"><ChatDotRound /></el-icon>
            </div>
            <h3>您好！我是 AI 项目助手</h3>
            <p>我可以帮您：</p>
            <!-- AI快捷功能 -->
            <div v-if="selectedProjectId" class="ai-quick-functions">
              <div class="function-title">
                <el-icon><MagicStick /></el-icon>
                <span>AI功能</span>
              </div>
              <div class="function-buttons">
                <el-button type="primary" plain size="small" @click="analyzeProjectHealth">
                  <el-icon><TrendCharts /></el-icon>
                  健康度分析
                </el-button>
                <el-button type="success" plain size="small" @click="generateDailyReport">
                  <el-icon><Document /></el-icon>
                  生成日报
                </el-button>
                <el-button type="warning" plain size="small" @click="suggestTasks">
                  <el-icon><List /></el-icon>
                  任务建议
                </el-button>
                <el-button type="info" plain size="small" @click="summarizeProject">
                  <el-icon><Reading /></el-icon>
                  项目总结
                </el-button>
              </div>
            </div>
            
            <div class="quick-actions">
              <div
                v-for="(q, index) in quickQuestions"
                :key="index"
                class="quick-action-card"
                @click="useQuickQuestion(q)"
              >
                {{ q }}
              </div>
            </div>
          </div>
          <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
            <div class="message-avatar">
              <el-avatar :icon="msg.role === 'user' ? 'User' : 'ChatDotRound'" :size="36" />
            </div>
            <div class="message-content">
              <div class="message-text" v-if="msg.role === 'user'">{{ msg.message }}</div>
              <div class="message-text ai-message" v-else v-html="formatMessage(msg.message)"></div>
              <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
            </div>
          </div>
          <div v-if="isStreaming" class="message ai">
            <div class="message-avatar">
              <el-avatar icon="ChatDotRound" :size="36" />
            </div>
            <div class="message-content">
              <div v-if="isThinking" class="thinking-dots">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
              <div v-else class="message-text ai-message streaming-text" v-html="formatMessage(streamingContent)"></div>
            </div>
          </div>
        </div>

        <div class="chat-input-wrapper">
          <div class="chat-input">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              placeholder="发送消息给 AI 项目助手..."
              @keyup.enter.ctrl="sendMessage"
              :disabled="isStreaming"
              class="input-field"
            />
            <el-button type="primary" @click="sendMessage" :loading="isStreaming" :disabled="!inputMessage.trim()" class="send-btn">
              <el-icon><Promotion /></el-icon>
              <span v-if="!isStreaming">发送</span>
            </el-button>
          </div>
          <div class="input-hint">按 Ctrl + Enter 发送消息</div>
        </div>
      </div>
    </div>

    <!-- 上传文档对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传文档" width="500px">
      <el-upload
        class="upload-demo"
        drag
        :http-request="customUpload"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
        :limit="1"
        :file-list="fileList"
      >
        <el-icon class="el-icon--upload" :size="40"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          拖拽文档到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">支持 .txt, .md, .docx 等文本文件</div>
        </template>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ChatDotRound, Delete, Promotion, Upload, UploadFilled, Plus, FolderOpened, MagicStick, TrendCharts, Document, List, Reading } from '@element-plus/icons-vue';
import apiClient from '../utils/api';

const router = useRouter();
const messages = ref<any[]>([]);
const inputMessage = ref('');
const isStreaming = ref(false);
const isThinking = ref(false);
const streamingContent = ref('');
const messagesContainer = ref<HTMLElement | null>(null);
const token = localStorage.getItem('token') || '';
const showUploadDialog = ref(false);
const fileList = ref<any[]>([]);
const conversations = ref<any[]>([]);
const activeConversationId = ref<string | null>(null);
const projects = ref<any[]>([]);
const selectedProjectId = ref<number | null>(null);
const loadingProjects = ref(false);

const quickQuestions = [
  '如何管理项目风险？',
  '项目管理的最佳实践',
  '如何提高团队效率？',
  '项目延期了怎么办？'
];

const project = computed(() => {
  if (!selectedProjectId.value) return null;
  return projects.value.find(p => p.id === selectedProjectId.value) || null;
});

const getStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    'pending': 'info',
    'in_progress': 'primary',
    'completed': 'success',
    'on_hold': 'warning',
    'cancelled': 'danger'
  };
  return typeMap[status] || 'info';
};

const loadProjects = async () => {
  loadingProjects.value = true;
  try {
    const result: any = await apiClient.get('/project/list');
    if (result.success) {
      projects.value = result.data;
    }
  } catch (error) {
    console.error('加载项目失败:', error);
  } finally {
    loadingProjects.value = false;
  }
};

const onProjectChange = () => {
  if (selectedProjectId.value) {
    ElMessage.success(`已选择项目: ${project.value?.name}`);
  } else {
    ElMessage.info('已取消选择项目');
  }
};

const callAIFunction = async (endpoint: string, functionName: string) => {
  if (!selectedProjectId.value) {
    ElMessage.warning('请先选择一个项目');
    return;
  }

  isStreaming.value = true;
  isThinking.value = true;

  try {
    const result: any = await apiClient.post(`/api/ai/${endpoint}/${selectedProjectId.value}`);

    if (result.success && result.data) {
      isThinking.value = false;
      const fullResponse = result.data;
      let i = 0;
      const typingSpeed = 20;

      const typeWriter = () => {
        if (i < fullResponse.length) {
          streamingContent.value = fullResponse.substring(0, i + 1);
          i++;
          scrollToBottom();
          setTimeout(typeWriter, typingSpeed + Math.random() * 20);
        } else {
          isStreaming.value = false;
          messages.value.push({
            role: 'ai',
            message: fullResponse,
            createdAt: new Date().toISOString()
          });
          scrollToBottom();
        }
      };

      setTimeout(typeWriter, 150);
    } else {
      isStreaming.value = false;
      isThinking.value = false;
      ElMessage.error(result.message || `${functionName}失败`);
    }
  } catch (error) {
    isStreaming.value = false;
    isThinking.value = false;
    ElMessage.error(`${functionName}失败，请检查网络连接`);
  }
};

const analyzeProjectHealth = () => callAIFunction('analyze-health', '健康度分析');
const generateDailyReport = () => callAIFunction('daily-report', '生成日报');
const suggestTasks = () => callAIFunction('suggest-tasks', '任务建议');
const summarizeProject = () => callAIFunction('summarize', '项目总结');

const customUpload = async (options: any) => {
  const { file } = options;
  const formData = new FormData();
  formData.append('file', file);
  if (selectedProjectId.value) {
    formData.append('projectId', selectedProjectId.value.toString());
  }
  
  try {
    const result: any = await apiClient.post('/ai/upload-document', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    options.onSuccess(result);
  } catch (error) {
    options.onError(error);
  }
};

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${token}`
}));

const formatMessage = (msg: string) => {
  if (!msg) return '';
  return msg
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`(.*?)`/g, '<code class="inline-code">$1</code>')
    .replace(/\n/g, '<br>');
};

const formatTime = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN');
};

const formatConvTime = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const useQuickQuestion = (question: string) => {
  inputMessage.value = question;
  sendMessage();
};

const loadConversations = async () => {
  try {
    const result: any = await apiClient.get('/ai/conversation/history');
    if (result.success) {
      // 保持当前会话ID和消息状态
      const currentId = activeConversationId.value;
      const currentMessages = messages.value;
      
      const groupedConversations = groupConversations(result.data);
      
      // 只保留有消息的会话
      const validConversations = groupedConversations.filter(conv => 
        conv.messages && conv.messages.length > 0
      );
      
      conversations.value = validConversations;
      
      // 如果当前有活跃会话且有消息，尝试在列表中找到并更新
      if (currentId && currentMessages.length > 0) {
        const existingConv = validConversations.find(c => c.id === currentId);
        if (existingConv) {
          // 更新列表中已有的话，保持显示当前消息
          existingConv.messages = [...currentMessages];
        } else {
          // 列表中没有的话，把当前会话添加到列表
          const newConv = {
            id: currentId,
            title: currentMessages[0]?.message.substring(0, 30) + '...',
            messages: [...currentMessages],
            updatedAt: new Date().toISOString()
          };
          conversations.value.unshift(newConv);
        }
      }
      
      // 只有列表有内容且当前没有会话时才选第一个
      if (validConversations.length > 0 && !currentId) {
        const firstConv = validConversations[0];
        selectConversation(firstConv);
      }
    }
  } catch (error) {
    console.error('加载会话历史失败:', error);
  }
};

const groupConversations = (allMessages: any[]) => {
  const convMap = new Map<string, any>();
  let currentConvId: string | null = null;
  let currentTitle = '新会话';
  
  // 改进分组逻辑：按用户消息间隔分组，避免随机分组
  allMessages.forEach(msg => {
    if (msg.role === 'user' && !currentConvId) {
      // 新用户消息，创建新会话
      currentConvId = 'conv_' + Date.now() + '_' + Math.random().toString(36).slice(2, 7);
      currentTitle = msg.message.length > 30 ? msg.message.substring(0, 30) + '...' : msg.message;
      convMap.set(currentConvId, {
        id: currentConvId,
        title: currentTitle,
        messages: [],
        updatedAt: msg.createdAt
      });
    }
    
    if (currentConvId) {
      const conv = convMap.get(currentConvId);
      conv.messages.push(msg);
      conv.updatedAt = msg.createdAt;
    }
  });
  
  return Array.from(convMap.values()).sort((a, b) => 
    new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
  );
};

const selectConversation = (conv: any) => {
  activeConversationId.value = conv.id;
  messages.value = conv.messages;
  scrollToBottom();
};

const createNewConversation = () => {
  activeConversationId.value = 'conv_' + Date.now().toString() + '_' + Math.random().toString(36).slice(2, 7);
  messages.value = [];
  ElMessage.success('新会话已创建');
};

const loadConversationHistory = async () => {
  loadConversations();
};

const clearConversation = async () => {
  try {
    const result: any = await apiClient.delete('/ai/conversation/clear');
    if (result.success) {
      conversations.value = [];
      messages.value = [];
      activeConversationId.value = null;
      ElMessage.success('对话已清空');
    } else {
      ElMessage.error(result.message || '清空失败');
    }
  } catch (error) {
    ElMessage.error('清空失败，请检查网络连接');
  }
};

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isStreaming.value) {
    return;
  }

  const userMessage = inputMessage.value.trim();
  inputMessage.value = '';
  isStreaming.value = true;
  isThinking.value = true;
  streamingContent.value = '';

  const isFirstMessage = messages.value.length === 0;

  messages.value.push({
    role: 'user',
    message: userMessage,
    createdAt: new Date().toISOString()
  });
  scrollToBottom();

  try {
    const chatResult: any = await apiClient.post('/ai/chat', {
        message: userMessage,
        projectId: selectedProjectId.value
      });
    
    if (chatResult.success) {
      isThinking.value = false;
      
      const fullResponse = chatResult.data;
      let i = 0;
      const typingSpeed = 20;
      
      const typeWriter = () => {
        if (i < fullResponse.length) {
          streamingContent.value = fullResponse.substring(0, i + 1);
          i++;
          scrollToBottom();
          setTimeout(typeWriter, typingSpeed + Math.random() * 20);
        } else {
          isStreaming.value = false;
          messages.value.push({
            role: 'ai',
            message: fullResponse,
            createdAt: new Date().toISOString()
          });
          streamingContent.value = '';
          scrollToBottom();
          
          if (isFirstMessage && !activeConversationId.value) {
            createNewConversation();
          }
          
          loadConversationHistory();
        }
      };
      
      setTimeout(typeWriter, 150);
    } else {
      isStreaming.value = false;
      isThinking.value = false;
      messages.value.push({
        role: 'ai',
        message: '抱歉，我暂时无法回答这个问题，请稍后重试。',
        createdAt: new Date().toISOString()
      });
      ElMessage.error(chatResult.message || '请求失败');
    }
  } catch {
    isStreaming.value = false;
    isThinking.value = false;
    ElMessage.error('发送失败，请检查网络连接');
    messages.value.push({
      role: 'ai',
      message: '抱歉，我暂时无法回答这个问题，请稍后重试。您可以尝试：\n1. 检查网络连接\n2. 确认后端服务是否正常启动\n3. 查看控制台是否有错误信息',
      createdAt: new Date().toISOString()
    });
  }
};

const beforeUpload = (file: any) => {
  const isText = file.type.startsWith('text/') || file.name.endsWith('.txt') || file.name.endsWith('.md') || file.name.endsWith('.docx');
  if (!isText) {
    ElMessage.error('只能上传文本文件！');
  }
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB！');
  }
  return isText && isLt10M;
};

const handleUploadSuccess = (response: any) => {
  if (response.success) {
    ElMessage.success('文档上传成功！');
    showUploadDialog.value = false;
    fileList.value = [];
    inputMessage.value = `请帮我分析一下刚刚上传的文档：${response.fileName}`;
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

const handleUploadError = (_error: any) => {
  ElMessage.error('上传失败，请检查网络连接');
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  loadProjects().then(() => {
    // 检查localStorage中是否有保存的项目ID
    const savedProjectId = localStorage.getItem('selectedProjectId');
    if (savedProjectId) {
      const project = projects.value.find(p => p.id === Number(savedProjectId));
      if (project) {
        selectedProjectId.value = Number(savedProjectId);
        // 清除已保存的项目ID，避免下次访问时自动选中
        localStorage.removeItem('selectedProjectId');
      }
    }
  });
  loadConversationHistory();
});
</script>

<style scoped>
.ai-chat-assistant {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.chat-layout {
  display: flex;
  gap: 20px;
  max-width: 1600px;
  margin: 0 auto;
  height: calc(100vh - 40px);
}

/* 侧边栏 */
.sidebar {
  width: 300px;
  flex-shrink: 0;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 项目选择器 */
.project-selector {
  padding: 16px;
  border-bottom: 1px solid #e5e5e5;
  background-color: #fafafa;
}

.selector-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.selected-project-info {
  margin-top: 12px;
  padding: 10px;
  background-color: white;
  border-radius: 8px;
  border: 1px solid #e5e5e5;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-size: 13px;
  color: #666;
}

/* AI快捷功能 */
.ai-quick-functions {
  margin-bottom: 24px;
  padding: 16px;
  background-color: white;
  border-radius: 12px;
  border: 1px solid #e5e5e5;
}

.function-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.function-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.function-buttons .el-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e5e5e5;
  background-color: #fafafa;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.conversation-item:hover {
  background-color: #f5f5f5;
}

.conversation-item.active {
  background-color: #ecf5ff;
  border-color: #409eff;
}

.conv-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.conversation-item.active .conv-icon {
  background-color: #409eff;
  color: white;
}

.conv-info {
  flex: 1;
  min-width: 0;
}

.conv-title {
  font-size: 14px;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.conv-time {
  font-size: 12px;
  color: #999;
}

.empty-conversations {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.empty-conversations p {
  margin: 8px 0 0 0;
  color: #999;
  font-size: 14px;
}

/* 聊天区域 */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #e5e5e5;
  background-color: #fafafa;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  gap: 8px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.back-btn, .clear-btn, .upload-btn {
  padding: 8px 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background-color: #fafafa;
}

.empty-hint {
  text-align: center;
  padding: 80px 20px;
}

.welcome-icon {
  margin-bottom: 24px;
}

.empty-hint h3 {
  margin: 0 0 12px;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
}

.empty-hint p {
  margin: 0 0 32px;
  font-size: 16px;
  color: #666;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  max-width: 600px;
  margin: 0 auto;
}

.quick-action-card {
  padding: 16px;
  background-color: white;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  color: #333;
}

.quick-action-card:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.message {
  display: flex;
  margin-bottom: 24px;
  align-items: flex-start;
  gap: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.message.ai {
  flex-direction: row;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 75%;
  display: flex;
  flex-direction: column;
}

.message-text {
  padding: 14px 18px;
  border-radius: 18px;
  line-height: 1.7;
  word-wrap: break-word;
  font-size: 15px;
}

.message.user .message-text {
  background-color: #409eff;
  color: white;
  border-bottom-right-radius: 6px;
}

.message.ai .message-text {
  background-color: white;
  color: #1a1a1a;
  border: 1px solid #e5e5e5;
  border-bottom-left-radius: 6px;
}

.ai-message {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.streaming-text {
  min-height: 40px;
}

.inline-code {
  background-color: #f0f2f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
  color: #e83e8c;
}

.thinking-dots {
  display: flex;
  gap: 8px;
  padding: 14px 18px;
  background-color: white;
  border: 1px solid #e5e5e5;
  border-radius: 18px;
  border-bottom-left-radius: 6px;
}

.thinking-dots .dot {
  width: 8px;
  height: 8px;
  background-color: #909399;
  border-radius: 50%;
  animation: bounce 1s infinite ease-in-out both;
}

.thinking-dots .dot:nth-child(1) {
  animation-delay: -0.3s;
}

.thinking-dots .dot:nth-child(2) {
  animation-delay: -0.15s;
}

@keyframes bounce {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.message-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 6px;
  text-align: left;
}

.chat-input-wrapper {
  padding: 16px 24px;
  border-top: 1px solid #e5e5e5;
  background-color: #fafafa;
}

.chat-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-field {
  flex: 1;
}

.input-field :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 1px solid #e5e5e5;
  resize: none;
  font-size: 15px;
  padding: 12px 16px;
}

.input-field :deep(.el-textarea__inner:focus) {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.send-btn {
  height: 72px;
  padding: 0 24px;
  border-radius: 12px;
  font-weight: 500;
}

.input-hint {
  margin-top: 8px;
  text-align: center;
  font-size: 12px;
  color: #999;
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
</style>
