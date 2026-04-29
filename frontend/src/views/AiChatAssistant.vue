<template>
  <div class="ai-chat-assistant">
    <el-card class="chat-card">
      <template #header>
        <div class="card-header">
          <h2>AI项目助手</h2>
          <el-button type="primary" size="small" @click="clearConversation">清空对话</el-button>
        </div>
      </template>

      <div class="quick-questions">
        <el-tag
          v-for="(q, index) in quickQuestions"
          :key="index"
          class="quick-tag"
          @click="useQuickQuestion(q)"
        >
          {{ q }}
        </el-tag>
      </div>

      <div class="chat-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-hint">
          您好！我是AI项目助手，可以帮您：
          <ul>
            <li>分析项目进度和风险</li>
            <li>回答关于项目的问题</li>
            <li>提供项目改进建议</li>
          </ul>
        </div>
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.role]">
          <div class="message-avatar">
            <el-avatar :icon="msg.role === 'user' ? 'User' : 'ChatDotRound'" />
          </div>
          <div class="message-content">
            <div class="message-text" v-if="msg.role === 'user'">{{ msg.message }}</div>
            <div class="message-text" v-else v-html="formatMessage(msg.message)"></div>
            <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </div>
        <div v-if="isStreaming" class="message ai">
          <div class="message-avatar">
            <el-avatar icon="ChatDotRound" />
          </div>
          <div class="message-content">
            <div class="message-text streaming-text" v-html="formatMessage(streamingContent)"></div>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="请输入您的问题，例如：我们项目现在有多少延期的任务？"
          @keyup.enter="sendMessage"
          :disabled="isStreaming"
        />
        <el-button type="primary" @click="sendMessage" :loading="isStreaming" :disabled="!inputMessage.trim()">
          {{ isStreaming ? 'AI回复中...' : '发送' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import { ElMessage } from 'element-plus';

const messages = ref<any[]>([]);
const inputMessage = ref('');
const isStreaming = ref(false);
const streamingContent = ref('');
const messagesContainer = ref<HTMLElement | null>(null);
const token = localStorage.getItem('token') || '';

const quickQuestions = [
  '如何管理项目风险？',
  '项目管理的最佳实践有哪些？',
  '如何提高团队效率？',
  '项目延期了怎么办？'
];

const formatMessage = (msg: string) => {
  return msg.replace(/\n/g, '<br>').replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
};

const useQuickQuestion = (question: string) => {
  inputMessage.value = question;
  sendMessage();
};

const clearConversation = () => {
  messages.value = [];
};

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isStreaming.value) {
    return;
  }

  const userMessage = inputMessage.value.trim();
  inputMessage.value = '';
  isStreaming.value = true;
  streamingContent.value = '';

  messages.value.push({
    role: 'user',
    message: userMessage,
    createdAt: new Date().toISOString()
  });
  scrollToBottom();

  try {
    const response = await fetch('http://localhost:8080/api/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        message: userMessage
      })
    });

    if (!response.ok) {
      throw new Error('请求失败');
    }

    const reader = response.body?.getReader();
    if (!reader) {
      throw new Error('无法读取响应');
    }

    const decoder = new TextDecoder();
    let fullResponse = '';
    let isDone = false;

    while (!isDone) {
      const { done, value } = await reader.read();
      if (done) {
        isDone = true;
        break;
      }

      const chunk = decoder.decode(value, { stream: true });
      const lines = chunk.split('\n');

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6);
          if (data === '[DONE]') {
            isDone = true;
            break;
          }
          if (data && data.trim()) {
            streamingContent.value = data;
            fullResponse = data;
            scrollToBottom();
          }
        }
      }
    }

    isStreaming.value = false;

    messages.value.push({
      role: 'ai',
      message: fullResponse,
      createdAt: new Date().toISOString()
    });
    streamingContent.value = '';
    scrollToBottom();

  } catch (error) {
    isStreaming.value = false;
    ElMessage.error('发送失败');
    messages.value.push({
      role: 'ai',
      message: '抱歉，我暂时无法回答这个问题，请稍后重试。',
      createdAt: new Date().toISOString()
    });
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
};

const formatTime = (dateStr: string) => {
  const date = new Date(dateStr);
  return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
};
</script>

<style scoped>
.ai-chat-assistant {
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

.quick-questions {
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}

.quick-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  cursor: pointer;
}

.chat-card {
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
}

.empty-hint {
  text-align: center;
  color: #909399;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
}

.empty-hint ul {
  text-align: left;
  display: inline-block;
  margin-top: 10px;
}

.empty-hint li {
  margin: 5px 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f5f7fa;
}

.message {
  display: flex;
  margin-bottom: 20px;
}

.message.user {
  flex-direction: row-reverse;
}

.message.ai {
  flex-direction: row;
}

.message-avatar {
  margin: 0 10px;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
}

.message.user .message-text {
  background-color: #409eff;
  color: white;
}

.message.ai .message-text {
  background-color: white;
  color: #303133;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  white-space: pre-wrap;
}

.streaming-text {
  min-height: 24px;
}

.chat-input {
  display: flex;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.chat-input .el-textarea {
  flex: 1;
}
</style>
