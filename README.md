# 教案文件上传 + AI 辅助解析模块

## 项目简介

基于 Spring Boot 3.4 + MyBatis + MySQL 构建的教案文件上传与AI辅助解析系统。支持 PDF/DOC/DOCX 文件上传，通过 AI 自动解析教案内容，生成标题、年级学科、教学目标、关键词和摘要。当前使用 Mock 模式，预留 llama.cpp 接入实现。

## 环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | **17** | 必须，不支持 JDK 8/11/21+ |
| Maven | 3.6+ | 构建工具 |
| MySQL | 8.0+ | 数据库 |

> **注意**：本项目基于 Spring Boot 3.4，要求 JDK 17。JDK 21 也可编译运行，但 JDK 8/11 不支持。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.5 | 应用框架 |
| MyBatis | 3.0.5 | ORM 持久层 |
| MySQL | 8.0 | 数据库 |
| Apache POI | 5.2.5 | DOC/DOCX 文件解析 |
| Apache PDFBox | 3.0.4 | PDF 文件解析 |
| FastJSON | 1.2.83 | JSON 序列化 |
| Mock AI | - | AI 解析模拟（可替换为真实模型） |

## 项目结构

```
lessonplan-ai/
├── pom.xml
├── lessonplan.sql                              # 建表SQL
├── README.md
└── src/main/
    ├── java/com/lessonplan/
    │   ├── LessonPlanApplication.java           # 启动类
    │   ├── ai/                                  # AI解析服务
    │   │   ├── LessonPlanAiService.java         # AI接口
    │   │   ├── MockAiServiceImpl.java           # Mock实现（默认启用）
    │   │   └── LlamaCppAiServiceImpl.java       # llama.cpp实现（备份，未启用）
    │   ├── config/
    │   │   └── CorsConfig.java                  # 跨域配置
    │   ├── controller/
    │   │   └── LessonPlanController.java         # 教案文件Controller
    │   ├── entity/
    │   │   ├── dto/LessonPlanParseDto.java       # 解析结果DTO
    │   │   ├── enums/                            # 枚举（FileType/Status）
    │   │   ├── po/                               # 持久化对象
    │   │   ├── query/                            # 查询参数
    │   │   └── vo/                               # 视图对象（ResponseVO/PaginationResultVO）
    │   ├── mappers/                              # MyBatis Mapper接口
    │   └── service/                              # Service层
    │       └── impl/
    └── resources/
        ├── application.properties
        └── com/lessonplan/mappers/               # MyBatis XML
```

## 快速开始

### 1. 创建数据库

```bash
mysql -u root -p < lessonplan.sql
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.properties`，或通过环境变量覆盖：

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|----------|--------|------|
| spring.datasource.username | DB_USERNAME | root | 数据库用户名 |
| spring.datasource.password | DB_PASSWORD | (空) | 数据库密码 |
| project.folder | FILE_STORAGE_PATH | e:/code/files/lessonplan/ | 文件存储目录 |

**环境变量方式**（推荐，避免敏感信息入库）：

```bash
# Linux/Mac
export DB_PASSWORD=your_password

# Windows PowerShell
$env:DB_PASSWORD = "your_password"
```

### 3. 构建运行

```bash
mvn clean package -DskipTests
java -jar target/lessonplan-ai-1.0.jar
```

或使用 Maven 直接运行：

```bash
mvn spring-boot:run
```

服务启动在 `http://localhost:7072/api`

---

## 接口说明

### 1. 上传教案文件

```
POST /api/lessonplan/upload
Content-Type: multipart/form-data

参数:
  file: 文件（必填，支持 PDF/DOC/DOCX，最大50MB）
  uploader: 上传人（选填，默认 anonymous）

响应示例:
{
  "status": "success",
  "code": 200,
  "info": null,
  "data": {
    "id": 1,
    "fileName": "数学教案.docx",
    "storedName": "a1b2c3d4e5f6.docx",
    "fileType": "docx",
    "fileSize": 102400,
    "uploader": "张老师",
    "uploadTime": "2026-06-03 14:00:00",
    "status": 0,
    "filePath": "e:/code/files/lessonplan/lessonplan/a1b2c3d4e5f6.docx"
  }
}
```

### 2. 查询教案文件列表

```
POST /api/lessonplan/list
Content-Type: application/json

请求体:
{
  "pageNo": 1,
  "pageSize": 20
}

响应示例:
{
  "status": "success",
  "code": 200,
  "data": {
    "totalCount": 5,
    "pageSize": 20,
    "pageNo": 1,
    "pageTotal": 1,
    "list": [
      {
        "id": 1,
        "fileName": "语文教案.pdf",
        "fileType": "pdf",
        "fileSize": 204800,
        "uploader": "张老师",
        "uploadTime": "2026-06-03 14:00:00",
        "status": 2
      }
    ]
  }
}
```

### 3. 查询教案文件详情（含解析结果）

```
GET /api/lessonplan/detail/{id}

响应示例:
{
  "status": "success",
  "code": 200,
  "data": {
    "lessonPlan": {
      "id": 1,
      "fileName": "语文教案.pdf",
      "fileType": "pdf",
      "fileSize": 204800,
      "uploader": "张老师",
      "uploadTime": "2026-06-03 14:00:00",
      "status": 2
    },
    "parseResult": {
      "id": 1,
      "lessonPlanId": 1,
      "title": "【Mock】教案标题 - 基于内容的模拟解析",
      "gradeLevel": "小学五年级",
      "subject": "语文",
      "teachingObjectives": "[\"理解课文主要内容\",\"掌握生字新词\",\"学习写作手法\",\"培养阅读习惯\"]",
      "keywords": "[\"阅读理解\",\"写作手法\",\"生字新词\",\"思想感情\",\"阅读习惯\"]",
      "summary": "本教案围绕课文阅读理解展开，帮助学生深入理解课文，提升语文综合素养。",
      "duration": 520,
      "modelName": "mock/qwen2.5-mock",
      "isMock": 1,
      "failReason": null,
      "parseTime": "2026-06-03 14:01:00"
    }
  }
}
```

### 4. 触发AI解析

```
POST /api/lessonplan/parse/{id}

响应示例:
{
  "status": "success",
  "code": 200,
  "info": "解析任务已提交",
  "data": null
}
```

### 5. 查询解析状态

```
GET /api/lessonplan/parseStatus/{id}

响应示例:
{
  "status": "success",
  "code": 200,
  "data": {
    "id": 1,
    "status": 2,
    "parseResult": {
      "id": 1,
      "lessonPlanId": 1,
      "title": "【Mock】教案标题 - 基于内容的模拟解析",
      "gradeLevel": "小学五年级",
      "subject": "语文",
      "isMock": 1,
      "duration": 520,
      "modelName": "mock/qwen2.5-mock"
    }
  }
}
```

状态码说明：
- 0 = 已上传
- 1 = 解析中
- 2 = 解析成功
- 3 = 解析失败

### 6. 删除教案文件

```
DELETE /api/lessonplan/delete/{id}

响应示例:
{
  "status": "success",
  "code": 200,
  "info": null,
  "data": null
}
```

---

## 自测说明

以下为完整自测流程，使用 curl 命令逐步验证所有接口。

### 前置条件

- MySQL 已启动，`lessonplan` 数据库已创建
- 应用已启动在 `http://localhost:7072`

### 测试1：文件上传

```bash
# 上传PDF文件
curl -X POST http://localhost:7072/api/lessonplan/upload \
  -F "file=@test.pdf" \
  -F "uploader=测试用户"

# 上传DOCX文件
curl -X POST http://localhost:7072/api/lessonplan/upload \
  -F "file=@test.docx" \
  -F "uploader=测试用户"

# 上传不支持的文件类型（应返回错误）
curl -X POST http://localhost:7072/api/lessonplan/upload \
  -F "file=@test.txt" \
  -F "uploader=测试用户"
```

预期结果：PDF/DOCX 上传成功返回200和文件信息；TXT 返回400错误。

### 测试2：文件列表查询

```bash
curl -X POST http://localhost:7072/api/lessonplan/list \
  -H "Content-Type: application/json" \
  -d '{"pageNo":1,"pageSize":10}'
```

预期结果：返回分页列表，包含之前上传的文件。

### 测试3：文件详情查询

```bash
curl http://localhost:7072/api/lessonplan/detail/1
```

预期结果：返回文件详情，parseResult 为 null（尚未解析）。

### 测试4：触发AI解析

```bash
curl -X POST http://localhost:7072/api/lessonplan/parse/1
```

预期结果：返回"解析任务已提交"。

### 测试5：查询解析状态

```bash
# 等待1-2秒后查询
curl http://localhost:7072/api/parseStatus/1
```

预期结果：status=2（解析成功），parseResult 包含 Mock 生成的标题、教学目标、关键词、摘要。

### 测试6：删除文件

```bash
curl -X DELETE http://localhost:7072/api/lessonplan/delete/1
```

预期结果：返回200，再次查询 detail 应返回404。

---

## AI 工具使用说明

### 使用了哪些 AI 工具

| AI 工具 | 用途 |
|---------|------|
| Trae IDE (内置 AI) | 代码生成、项目结构设计、代码规范检查、文档编写 |

### 让 AI 做了什么

1. **项目结构设计**：让 Trae 分析了参考项目的代码规范（包命名、类命名、注解使用、Mapper XML 结构等），并按照相同规范生成了新项目的完整目录结构。

2. **实体类生成**：让 Trae 根据需求描述生成了 PO/DTO/VO/Query/Enum 等实体类，遵循手写 getter/setter、Serializable、@JsonFormat 等规范。

3. **Mapper XML 生成**：让 Trae 按照 MyBatis XML 模板（resultMap + base_column_list + 动态SQL）生成了完整的 Mapper XML。

4. **Service 层实现**：让 Trae 实现了文件上传、文本提取（PDFBox/POI）、AI 解析调度等核心业务逻辑。

5. **AI 解析服务**：让 Trae 设计了 LessonPlanAiService 接口和 Mock 实现，支持后续扩展接入真实模型。

6. **代码规范检查**：让 Trae 检查代码是否符合 Java 编码规范、Spring Boot 最佳实践、MyBatis 使用规范。

7. **文档生成**：让 Trae 生成了建表 SQL、README、接口说明和自测说明。

### 我检查并修改了什么

1. **JDK 版本修复**：AI 初始生成使用 JDK 21 + Spring Boot 3.5，我发现评估环境为 JDK 17，手动降级为 JDK 17 + Spring Boot 3.4，确保兼容性。

2. **依赖清理**：AI 引入了 `spring-ai-alibaba-starter` 依赖，该依赖在 Maven 仓库中找不到（1.0.0.2 版本不存在），我手动移除了该依赖及相关 OkHttp、Spring AI 仓库配置。

3. **Ollama 移除**：AI 初始实现包含 Ollama 本地模型调用，按要求移除了 Ollama 相关代码和配置，仅保留 Mock 模式，并创建了 LlamaCppAiServiceImpl 作为备份参考。

4. **敏感配置处理**：AI 初始将数据库密码硬编码为 `123456`，我改为使用 `${DB_PASSWORD:}` 环境变量占位符，避免敏感信息入库。

5. **接口设计审查**：检查了所有接口的请求/响应格式，确认文件上传支持 PDF/DOC/DOCX 三种类型，大小限制 50MB，解析任务为异步执行。

6. **异常处理**：检查了文件类型校验、大小校验、空文件校验、解析失败时的状态回滚和失败原因记录。

7. **AI 提示词**：检查并优化了发送给大模型的 prompt，确保返回格式为严格 JSON。

8. **异步解析**：确认了 @Async 注解的使用方式，解析失败时正确保存失败原因和更新状态。

---

## 敏感配置说明

本项目 `application.properties` 中不包含任何硬编码密码或 API Key。所有敏感配置均通过环境变量注入：

| 配置项 | 环境变量 | 说明 |
|--------|----------|------|
| 数据库密码 | `DB_PASSWORD` | 默认为空，需在运行环境设置 |
| 数据库用户名 | `DB_USERNAME` | 默认 root |
| 文件存储路径 | `FILE_STORAGE_PATH` | 默认 e:/code/files/lessonplan/ |

**切勿将真实密码提交到 Git 仓库。**

---

## AI 模型扩展说明

当前项目使用 Mock 模式进行 AI 解析。如需接入真实模型，可参考 `LlamaCppAiServiceImpl.java`（备份实现），步骤如下：

1. 启动 llama.cpp server：`./llama-server -m model.gguf --port 8080`
2. 在 `LlamaCppAiServiceImpl` 类上添加 `@Component` 注解
3. 移除 `MockAiServiceImpl` 的 `@Component` 注解
4. 重启应用

llama.cpp 兼容 OpenAI API 格式，调用 `/v1/chat/completions` 接口，无需额外 SDK 依赖。
