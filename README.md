# AI驱动软件项目管理系统

一个基于人工智能的全生命周期软件项目管理系统，完整覆盖软件项目管理的四大核心阶段：**项目初始-项目计划-项目执行控制-项目结束**，通过人工智能技术解决传统项目管理中的痛点。

## 技术栈

### 后端
- Spring Boot 3.2.5 + Java 17
- MyBatis Plus 3.5.5
- LangChain4j 0.29.0 (AI能力)
- PostgreSQL 16 + pgvector (向量数据库)
- Redis 7 (缓存)
- Spring Cache + Redis 缓存方案

### 前端
- Vue3 + TypeScript
- Element Plus
- ECharts (数据可视化)
- Vue Router

### 部署
- Docker Compose (一键部署所有服务)
- 多阶段构建优化Dockerfile

## 功能模块

### 1. 用户权限管理
- 用户注册、登录、JWT认证
- RBAC权限控制（5个角色：admin、pm、developer、tester、guest）
- 操作日志审计

### 2. 项目初始阶段
- 项目创建、团队组建
- 需求调研管理
- 可行性分析
- **AI智能表单填充** - 输入项目目标，AI自动推荐项目周期、团队配置

### 3. 项目计划阶段
- WBS任务分解（层级结构、父子任务）
- 任务依赖关系管理
- 甘特图可视化
- 里程碑设置
- 风险登记册

### 4. 项目执行控制阶段
- 任务看板（Kanban四状态：待处理/进行中/待审核/已完成）
- 拖拽改变任务状态
- 进度跟踪、工时填报
- 变更管理（提交、审批）
- 缺陷跟踪（bug管理）

### 5. 项目结束阶段
- 交付验收管理
- 文档归档
- 项目复盘

### 6. AI能力集成
- 需求文档智能解析（Word/PDF）
- 智能任务拆分（WBS自动生成）
- RAG知识库（文档向量化存储）
- **AI流式输出** - 打字机效果，类似ChatGPT
- **智能进度预测** - 基于历史数据预测交付时间
- 自然语言交互助手

### 7. 性能与安全
- **Redis缓存** - 用户权限、项目统计、知识库缓存
- **数据库索引优化** - 30+索引覆盖所有常用查询
- **XSS防护** - 输入过滤防止跨站脚本攻击
- **接口防刷** - 同一IP每分钟最多100次请求
- **全局异常处理** - 统一错误返回格式
- **单元测试** - 核心服务测试覆盖率60%+

## 快速启动

### 一键启动（推荐）

```bash
# Windows
.\start.ps1

# Linux/Mac
./start.sh
```

这将自动启动所有服务：
- PostgreSQL + pgvector 数据库
- Redis 缓存
- Spring Boot 后端
- Vue 前端

### 手动启动

#### 1. 启动数据库和Redis

```bash
docker-compose up -d
```

#### 2. 配置OpenAI API Key（可选）

如果需要使用AI功能，请设置环境变量：

```bash
export OPENAI_API_KEY=your-api-key
```

#### 3. 启动后端

```bash
mvn spring-boot:run
```

后端服务将运行在 http://localhost:8080

#### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务将运行在 http://localhost:5173

### 5. 访问系统

打开浏览器访问 http://localhost:5173，使用以下演示账号登录：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 项目经理 | pm | 123456 |
| 开发人员 | developer | 123456 |
| 测试人员 | tester | 123456 |
| 访客 | guest | 123456 |

## 演示数据

系统预置了2个完整的演示项目：

### 项目1：AI智能办公系统（进行中）
- 8个主要任务 + 3个子任务
- 3条风险记录
- 3个里程碑
- 3个缺陷
- 完整的工时记录和变更请求

### 项目2：电商导购系统（已完成）
- 11个已完成任务
- 完整的复盘报告
- 所有风险已解决

## 项目结构

```
├── src/main/java/com/example/backend/
│   ├── controller/        # REST控制器（18个）
│   ├── service/          # 业务服务（20个）
│   ├── mapper/           # 数据访问层（18个）
│   ├── entity/           # 实体类（20+个）
│   ├── config/           # 配置类（Security、Redis、MyBatisPlus）
│   ├── filter/          # 过滤器（XSS、防刷）
│   ├── aspect/           # AOP切面（操作日志）
│   └── utils/            # 工具类
│
├── frontend/src/
│   ├── views/           # 页面组件（24个）
│   ├── components/      # 通用组件
│   ├── router.ts        # 路由配置
│   └── utils/           # 工具函数
│
├── docker-compose.yml   # Docker部署配置
├── README.md           # 项目说明
└── start.sh/start.ps1  # 一键启动脚本
```

## API接口

主要API接口位于 `http://localhost:8080/api/`：

| 模块 | 接口 | 说明 |
|------|------|------|
| 用户 | `/api/user/login` | 登录 |
| 用户 | `/api/user/register` | 注册 |
| 项目 | `/api/project/*` | 项目CRUD |
| 任务 | `/api/task/*` | 任务CRUD、分页查询 |
| 风险 | `/api/risk/*` | 风险管理 |
| 缺陷 | `/api/bug/*` | 缺陷管理 |
| AI | `/api/ai/chat` | AI对话 |
| AI | `/api/ai/predict-progress/{id}` | 进度预测 |
| AI | `/api/ai/suggest-plan` | 智能表单填充 |
| 通知 | `/api/notification/*` | 站内通知 |
| 导出 | `/api/export/tasks/{id}` | 导出任务Excel |
| 日志 | `/api/log/list` | 操作日志 |

## 答辩演示脚本

### 演示流程（5分钟）

#### 1. 项目介绍（30秒）
- "各位老师好，我今天演示的是一个AI驱动的软件项目管理系统"
- "系统完整覆盖项目管理的四大阶段：初始、计划、执行、结束"
- "核心技术栈：Spring Boot + Vue3 + LangChain4j"

#### 2. 核心功能演示（2分钟）

**登录演示：**
- 使用pm账号登录
- 展示Dashboard统计数据

**项目管理演示：**
- 进入AI智能办公系统项目
- 展示甘特图视图
- 展示任务看板（拖拽改变状态）

**AI功能演示（亮点）：**
- AI助手对话："这个项目现在进度怎么样？"
- 进度预测功能展示
- AI智能表单填充演示

#### 3. 亮点功能详解（1.5分钟）

**AI智能助手：**
- 基于LangChain4j实现
- 支持自然语言交互
- 可进行项目状态分析、风险识别

**智能进度预测：**
- 基于历史任务数据
- 预测项目交付时间
- 计算按期交付概率

**RAG知识库：**
- 文档向量化存储
- 智能问答

#### 4. 工程质量展示（1分钟）

**缓存优化：**
- Redis缓存热点数据
- 用户权限缓存30分钟
- 项目统计缓存5分钟

**安全加固：**
- XSS防护
- 接口防刷（每分钟100次）
- 操作日志审计

**单元测试：**
- 核心服务测试覆盖
- JUnit5 + Mockito

#### 5. 总结（30秒）
- "这个系统实现了项目管理全生命周期覆盖"
- "深度集成了AI能力，是项目的核心亮点"
- "在性能、安全、质量方面都有充分考虑"

### 常见问题及答案

**Q: 为什么选择LangChain4j？**
A: LangChain4j是Java生态最成熟的LLM集成框架，支持多种大模型，易于扩展。

**Q: Redis缓存的作用是什么？**
A: 缓存用户权限信息、项目统计数据、知识库检索结果，减少数据库查询，提升系统响应速度。

**Q: AI进度预测的原理？**
A: 基于已完成任务的实际耗时，计算平均工作效率，预测剩余任务的完成时间。

**Q: 如何保证数据安全？**
A: 通过JWT认证、RBAC权限控制、XSS过滤、接口防刷等多层防护。

## 开发说明

### 数据库初始化

数据库表结构定义在 `src/main/resources/init.sql`，演示数据在 `demo-data.sql`。

### 添加新的AI功能

在 `AiService.java` 中添加新的AI方法，然后在 `AiController.java` 中暴露对应的API。

## License

MIT License
