<template>
  <div class="gantt-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button type="primary" @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回项目详情
        </el-button>
        <div class="title-section">
          <div class="title-icon">
            <el-icon :size="30"><Calendar /></el-icon>
          </div>
          <div class="title-content">
            <h1>项目甘特图</h1>
            <p class="subtitle">任务进度可视化管理</p>
          </div>
        </div>
      </div>
      <div class="header-buttons">
        <el-button type="primary" @click="showTaskDialog = true" class="action-btn">
          <el-icon><Plus /></el-icon>
          添加任务
        </el-button>
        <el-button type="success" @click="showMilestoneDialog = true" class="action-btn">
          <el-icon><Flag /></el-icon>
          添加里程碑
        </el-button>
      </div>
    </div>

    <!-- 图表容器 -->
    <div class="chart-card">
      <!-- 工具栏 -->
      <div class="chart-toolbar">
        <div class="toolbar-left">
          <div class="filter-group">
            <div class="filter-label">
              <el-icon><Document /></el-icon>
              <span>状态</span>
            </div>
            <el-select v-model="filterStatus" placeholder="全部" clearable size="default" style="width: 110px" @change="renderChart">
              <el-option label="待处理" value="todo" />
              <el-option label="进行中" value="in_progress" />
              <el-option label="待审核" value="review" />
              <el-option label="已完成" value="done" />
            </el-select>
          </div>
          <div class="filter-group">
            <div class="filter-label">
