<template>
  <div class="project-end-report">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button type="primary" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-button>
            <h2>项目结束报告</h2>
          </div>
          <div class="header-actions">
            <el-button type="primary" @click="generateReport" :loading="loading">
              <el-icon><Refresh /></el-icon>
              生成报告
            </el-button>
            <el-button type="success" @click="exportReport" :loading="exporting">
              <el-icon><Download /></el-icon>
              导出HTML
            </el-button>
            <el-button type="warning" @click="exportPDF" :loading="exportingPDF">
              <el-icon><Document /></el-icon>
              导出PDF
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="report" class="report-content">
        <!-- 项目基本信息 -->
        <div class="section">
          <h3 class="section-title">
            <el-icon :size="20" color="#409eff"><Document /></el-icon>
            项目基本信息
          </h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="项目名称" :span="2">
              {{ report.projectInfo?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="项目状态">
              <el-tag :type="getStatusType(report.projectInfo?.status)" effect="dark">
                {{ getStatusName(report.projectInfo?.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="开始日期">
              {{ report.projectInfo?.startDate || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="结束日期">
              {{ report.projectInfo?.endDate || '未设置' }}
            </el-descriptions-item>
            <el-descriptions-item label="项目周期">
              {{ report.projectInfo?.durationDays || 0 }} 天
            </el-descriptions-item>
            <el-descriptions-item label="项目经理">
              {{ report.projectInfo?.manager || '未指定' }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 任务统计 -->
        <div class="section">
          <h3 class="section-title">
            <el-icon :size="20" color="#67c23a"><List /></el-icon>
            任务统计
          </h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #409eff15;">
                  <el-icon :size="28" color="#409eff"><List /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.taskStats?.total || 0 }}</div>
                  <div class="stat-label">总任务</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #67c23a15;">
                  <el-icon :size="28" color="#67c23a"><List /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.taskStats?.completed || 0 }}</div>
                  <div class="stat-label">已完成</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #e6a23c15;">
                  <el-icon :size="28" color="#e6a23c"><Flag /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.taskStats?.completionRate || 0 }}%</div>
                  <div class="stat-label">完成率</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #f56c6c15;">
                  <el-icon :size="28" color="#f56c6c"><Flag /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.totalHours || 0 }}</div>
                  <div class="stat-label">总工时</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 风险统计 -->
        <div class="section">
          <h3 class="section-title">
            <el-icon :size="20" color="#e6a23c"><Warning /></el-icon>
            风险统计
          </h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #90939915;">
                  <el-icon :size="28" color="#909399"><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.riskStats?.total || 0 }}</div>
                  <div class="stat-label">总风险</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #67c23a15;">
                  <el-icon :size="28" color="#67c23a"><List /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.riskStats?.resolved || 0 }}</div>
                  <div class="stat-label">已解决</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #f56c6c15;">
                  <el-icon :size="28" color="#f56c6c"><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.riskStats?.high || 0 }}</div>
                  <div class="stat-label">高风险</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #e6a23c15;">
                  <el-icon :size="28" color="#e6a23c"><Flag /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.riskStats?.unresolved || 0 }}</div>
                  <div class="stat-label">未解决</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 缺陷统计 -->
        <div class="section">
          <h3 class="section-title">
            <el-icon :size="20" color="#f56c6c"><Warning /></el-icon>
            缺陷统计
          </h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #90939915;">
                  <el-icon :size="28" color="#909399"><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.bugStats?.total || 0 }}</div>
                  <div class="stat-label">总缺陷</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #67c23a15;">
                  <el-icon :size="28" color="#67c23a"><List /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.bugStats?.resolved || 0 }}</div>
                  <div class="stat-label">已修复</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #f56c6c15;">
                  <el-icon :size="28" color="#f56c6c"><Warning /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.bugStats?.unresolved || 0 }}</div>
                  <div class="stat-label">未修复</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 里程碑统计 -->
        <div class="section">
          <h3 class="section-title">
            <el-icon :size="20" color="#409eff"><Flag /></el-icon>
            里程碑统计
          </h3>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #409eff15;">
                  <el-icon :size="28" color="#409eff"><Flag /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.milestoneStats?.total || 0 }}</div>
                  <div class="stat-label">总里程碑</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #67c23a15;">
                  <el-icon :size="28" color="#67c23a"><CircleCheck /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.milestoneStats?.completed || 0 }}</div>
                  <div class="stat-label">已完成</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #e6a23c15;">
                  <el-icon :size="28" color="#e6a23c"><Timer /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.milestoneStats?.upcoming || 0 }}</div>
                  <div class="stat-label">待完成</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 交付物统计 -->
        <div class="section">
          <h3 class="section-title">
            <el-icon :size="20" color="#67c23a"><Box /></el-icon>
            交付物统计
          </h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #409eff15;">
                  <el-icon :size="28" color="#409eff"><Box /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.deliverableStats?.total || 0 }}</div>
                  <div class="stat-label">总交付物</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #67c23a15;">
                  <el-icon :size="28" color="#67c23a"><CircleCheck /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.deliverableStats?.accepted || 0 }}</div>
                  <div class="stat-label">已验收</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #e6a23c15;">
                  <el-icon :size="28" color="#e6a23c"><Timer /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.deliverableStats?.pending || 0 }}</div>
                  <div class="stat-label">待验收</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #f56c6c15;">
                  <el-icon :size="28" color="#f56c6c"><CircleClose /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.deliverableStats?.rejected || 0 }}</div>
                  <div class="stat-label">已拒绝</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 变更请求统计 -->
        <div class="section">
          <h3 class="section-title">
            <el-icon :size="20" color="#909399"><RefreshLeft /></el-icon>
            变更请求统计
          </h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #409eff15;">
                  <el-icon :size="28" color="#409eff"><RefreshLeft /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.changeStats?.total || 0 }}</div>
                  <div class="stat-label">总变更</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #67c23a15;">
                  <el-icon :size="28" color="#67c23a"><CircleCheck /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.changeStats?.approved || 0 }}</div>
                  <div class="stat-label">已批准</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #e6a23c15;">
                  <el-icon :size="28" color="#e6a23c"><Timer /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.changeStats?.pending || 0 }}</div>
                  <div class="stat-label">待审批</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-icon" style="background: #f56c6c15;">
                  <el-icon :size="28" color="#f56c6c"><CircleClose /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ report.changeStats?.rejected || 0 }}</div>
                  <div class="stat-label">已拒绝</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 报告摘要 -->
        <div class="section" v-if="report.summary">
          <h3 class="section-title">
            <el-icon :size="20" color="#409eff"><Document /></el-icon>
            报告摘要
          </h3>
          <el-card>
            <pre class="summary-content">{{ report.summary }}</pre>
          </el-card>
        </div>

        <!-- 生成时间 -->
        <div class="report-footer">
          报告生成时间：{{ formatDate(report.generatedAt) }}
        </div>
      </div>

      <el-empty v-else description="暂无报告数据，请点击生成报告按钮" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  ArrowLeft,
  Refresh,
  Download,
  Document,
  List,
  Warning,
  Flag,
  CircleCheck,
  Timer,
  Box,
  CircleClose,
  RefreshLeft
} from '@element-plus/icons-vue';
import apiClient from '../utils/api';
import html2pdf from 'html2pdf.js';

const route = useRoute();
const router = useRouter();
const projectId = Number(route.params.id);

const report = ref<any>(null);
const loading = ref(false);
const exporting = ref(false);
const exportingPDF = ref(false);

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    pending: 'info',
    in_progress: 'warning',
    completed: 'success'
  };
  return typeMap[status] || 'info';
};

const getStatusName = (status: string) => {
  const nameMap: Record<string, string> = {
    pending: '待启动',
    in_progress: '进行中',
    completed: '已完成'
  };
  return nameMap[status] || status;
};

const formatDate = (date: string) => {
  if (!date) return '';
  return new Date(date).toLocaleString('zh-CN');
};

const generateReport = async () => {
  loading.value = true;
  try {
    const result: any = await apiClient.get(`/project-retrospective/report/${projectId}`);
    if (result.success) {
      report.value = result.data;
      ElMessage.success('报告生成成功');
    } else {
      ElMessage.error(result.message || '生成失败');
    }
  } catch (error) {
    ElMessage.error('网络错误');
  } finally {
    loading.value = false;
  }
};

const exportReport = async () => {
  exporting.value = true;
  try {
    const response = await apiClient.get(`/project-retrospective/report/export/${projectId}`, {
      responseType: 'blob'
    });

    const blob = response;
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `project-end-report-${projectId}.html`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    ElMessage.success('导出成功');
  } catch (error) {
    ElMessage.error('导出失败');
  } finally {
    exporting.value = false;
  }
};

const exportPDF = async () => {
  if (!report.value) {
    ElMessage.warning('请先生成报告');
    return;
  }

  exportingPDF.value = true;
  try {
    const element = document.querySelector('.report-content') as HTMLElement;
    if (!element) {
      ElMessage.error('无法获取报告内容');
      return;
    }

    const opt = {
      margin: 10,
      filename: `project-end-report-${projectId}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };

    await html2pdf().set(opt).from(element).save();
    ElMessage.success('PDF导出成功');
  } catch (error) {
    console.error('PDF导出失败:', error);
    ElMessage.error('PDF导出失败');
  } finally {
    exportingPDF.value = false;
  }
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  generateReport();
});
</script>

<style scoped>
.project-end-report {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.project-end-report :deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.project-end-report :deep(.el-card__header) {
  background: white;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.section {
  margin: 24px 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.stat-card {
  text-align: center;
  padding: 16px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.summary-content {
  white-space: pre-wrap;
  font-family: inherit;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  margin: 0;
  padding: 16px;
  background: #fafbfc;
  border-radius: 6px;
}

.report-footer {
  margin-top: 32px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
  color: #909399;
  font-size: 12px;
  text-align: center;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
  color: #606266;
  background: #fafbfc;
}

:deep(.el-descriptions__content) {
  color: #303133;
}
</style>
