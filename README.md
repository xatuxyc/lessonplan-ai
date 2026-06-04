# 教案文件上传 + AI 辅助解析模块

## 项目简介

基于 Spring Boot 3.5 + MyBatis + Spring AI Alibaba 构建的教案文件上传与AI辅助解析系统。支持 PDF/DOC/DOCX 文件上传，通过本地大模型（Ollama）自动解析教案内容，生成标题、年级学科、教学目标、关键词和摘要。

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.5.14 | 应用框架 |
| MyBatis | 3.0.5 | ORM 持久层 |
| MySQL | 8.0 | 数据库 |
| Spring AI Alibaba | 1.0.0.2 | AI 模型接入 |
| Apache POI | 5.2.5 | DOC/DOCX 文件解析 |
| Apache PDFBox | 3.0.1 | PDF 文件解析 |
| Ollama | - | 本地大模型服务 |

## 项目结构

```
lessonplan-ai/
├── pom.xml
├── lessonplan.sql                          # 建表SQL
├── README.md
└── src/main/
    ├── java/com/lessonplan/
    │   ├── LessonPlanApplication.java      # 启动类
    │   ├── ai/                             # AI解析服务
    │   │   ├── LessonPlanAiService.java    # AI接口
    │   │   ├── OllamaAiServiceImpl.java    # Ollama本地模型实现
    │   │   └── MockAiServiceImpl.java      # Mock实现（开发测试用）
    │   ├── annotation/                     # 自定义注解
    │   ├── controller/
    │   │   ├── LessonPlanController.java   # 教案文件Controller
    │   │   └── base/
    │   │       ├── BaseController.java     # 基类
    │   │       └── GlobalExceptionHandlerController.java
    │   ├── entity/
    │   │   ├── config/AppConfig.java       # 配置类
    │   │   ├── dto/LessonPlanParseDto.java # 解析结果DTO
    │   │   ├── enums/                      # 枚举
    │   │   ├── po/                         # 持久化对象
    │   │   ├── query/                      # 查询参数
    │   │   └── vo/                         # 视图对象
    │   ├── exception/BusinessException.java
    │   ├── mappers/                        # MyBatis Mapper接口
    │   └── service/                        # Service层
    │       └── impl/
    └── resources/
        ├── application.properties
        └── com/lessonplan/mappers/         # MyBatis XML
```

## 快速开始

### 1. 创建数据库

```bash
mysql -u root -p < lessonplan.sql
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.properties`，修改数据库连接信息：

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/lessonplan?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. 配置AI服务

#### 方式一：使用 Mock 模式（无需安装大模型）

```properties
ai.service.mode=mock
```

#### 方式二：使用 Ollama 本地大模型

1. 安装 Ollama：https://ollama.com/download
2. 拉取模型：
   ```bash
   ollama pull qwen2.5:7b
   ```
3. 配置：
   ```properties
   ai.service.mode=ollama
   ai.ollama.base-url=http://localhost:11434
   ai.ollama.model=qwen2.5:7b
   ```

### 4. 构建运行

```bash
cd lessonplan-ai
mvn clean install
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

响应:
{
  "status": "success",
  "code": 200,
  "data": {
    "id": 1,
    "fileName": "数学教案.docx",
    "fileType": "docx",
    "fileSize": 102400,
    "uploader": "张老师",
    "uploadTime": "2026-06-03 14:00:00",
    "status": 0
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
  "pageSize": 15,
  "fileNameFuzzy": "数学",
  "fileType": "docx",
  "uploaderFuzzy": "张",
  "status": 0,
  "uploadTimeStart": "2026-01-01 00:00:00",
  "uploadTimeEnd": "2026-12-31 23:59:59"
}

响应:
{
  "status": "success",
  "code": 200,
  "data": {
    "totalCount": 10,
    "pageSize": 15,
    "pageNo": 1,
    "pageTotal": 1,
    "list": [...]
  }
}
```

### 3. 查询教案文件详情

```
GET /api/lessonplan/detail/{id}

响应:
{
  "status": "success",
  "code": 200,
  "data": {
    "lessonPlan": { ... },
    "parseResult": {
      "id": 1,
      "lessonPlanId": 1,
      "title": "小学五年级数学——分数的加减法",
      "gradeLevel": "小学五年级",
      "subject": "数学",
      "teachingObjectives": "[\"掌握分数加减法的运算法则\",\"...\",\"...\"]",
      "keywords": "[\"分数\",\"加减法\",\"通分\",\"约分\"]",
      "summary": "本教案围绕分数加减法展开...",
      "duration": 3200,
      "modelName": "ollama/qwen2.5:7b",
      "isMock": 0,
      "parseTime": "2026-06-03 14:01:30"
    }
  }
}
```

### 4. 触发AI解析

```
POST /api/lessonplan/parse/{id}

响应:
{
  "status": "success",
  "code": 200,
  "data": "解析任务已提交"
}
```

### 5. 查询解析状态

```
GET /api/lessonplan/parseStatus/{id}

响应:
{
  "status": "success",
  "code": 200,
  "data": {
    "id": 1,
    "status": 2,
    "parseResult": { ... }
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

响应:
{
  "status": "success",
  "code": 200,
  "data": null
}
```

---

## 自测说明

### 测试1：文件上传

```bash
# 上传PDF文件
curl -X POST http://localhost:7072/api/lessonplan/upload \
  -F "file=@/path/to/test.pdf" \
  -F "uploader=测试用户"

# 上传DOCX文件
curl -X POST http://localhost:7072/api/lessonplan/upload \
  -F "file=@/path/to/test.docx" \
  -F "uploader=测试用户"

# 上传不支持的文件类型（应返回603错误）
curl -X POST http://localhost:7072/api/lessonplan/upload \
  -F "file=@/path/to/test.txt" \
  -F "uploader=测试用户"
```

### 测试2：文件列表查询

```bash
curl -X POST http://localhost:7072/api/lessonplan/list \
  -H "Content-Type: application/json" \
  -d '{"pageNo":1,"pageSize":10}'
```

### 测试3：AI解析

```bash
# 触发解析（异步）
curl -X POST http://localhost:7072/api/lessonplan/parse/1

# 查询解析状态（等待几秒后查询）
curl http://localhost:7072/api/lessonplan/parseStatus/1
```

### 测试4：文件详情

```bash
curl http://localhost:7072/api/lessonplan/detail/1
```

### 测试5：删除文件

```bash
curl -X DELETE http://localhost:7072/api/lessonplan/delete/1
```

---

