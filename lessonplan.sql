-- =====================================================
-- 教案文件上传 + AI辅助解析模块 建表SQL
-- 数据库: lessonplan
-- 字符集: utf8mb4
-- =====================================================

CREATE DATABASE IF NOT EXISTS lessonplan DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE lessonplan;

-- -----------------------------------------------------
-- 表1: lesson_plan（教案文件信息表）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `lesson_plan`;
CREATE TABLE `lesson_plan` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `stored_name` varchar(255) NOT NULL COMMENT '存储文件名（UUID）',
  `file_type` varchar(10) NOT NULL COMMENT '文件类型：pdf/doc/docx',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `uploader` varchar(100) NOT NULL DEFAULT 'anonymous' COMMENT '上传人',
  `upload_time` datetime NOT NULL COMMENT '上传时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-已上传 1-解析中 2-解析成功 3-解析失败',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_uploader` (`uploader`),
  KEY `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教案文件信息表';

-- -----------------------------------------------------
-- 表2: lesson_plan_parse_result（AI解析结果表）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `lesson_plan_parse_result`;
CREATE TABLE `lesson_plan_parse_result` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lesson_plan_id` int NOT NULL COMMENT '关联的教案文件ID',
  `title` varchar(255) DEFAULT NULL COMMENT 'AI生成的标题',
  `grade_level` varchar(50) DEFAULT NULL COMMENT '年级',
  `subject` varchar(50) DEFAULT NULL COMMENT '学科',
  `teaching_objectives` text DEFAULT NULL COMMENT '教学目标（JSON数组）',
  `keywords` varchar(500) DEFAULT NULL COMMENT '关键词（JSON数组）',
  `summary` varchar(500) DEFAULT NULL COMMENT '100字内摘要',
  `duration` bigint DEFAULT NULL COMMENT '解析耗时（毫秒）',
  `model_name` varchar(100) DEFAULT NULL COMMENT '使用的模型名称',
  `is_mock` tinyint NOT NULL DEFAULT 0 COMMENT '是否为mock结果：0-真实AI 1-mock',
  `fail_reason` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `parse_time` datetime DEFAULT NULL COMMENT '解析时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lesson_plan_id` (`lesson_plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI解析结果表';
