-- ============================================================
-- 培训机构后台管理系统 —— 数据库结构
-- 数据库: training_admin
-- 说明: 本文件可重复执行(先删后建), 请先执行本文件再执行 inser_data.sql
-- ============================================================

DROP DATABASE IF EXISTS `training_admin`;
CREATE DATABASE `training_admin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `training_admin`;

-- ------------------------------------------------------------
-- 部门表
-- ------------------------------------------------------------
CREATE TABLE `department` (
  `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
  `name`        varchar(20)  NOT NULL COMMENT '部门名称',
  `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_department_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

-- ------------------------------------------------------------
-- 员工表
-- ------------------------------------------------------------
CREATE TABLE `employee` (
  `id`            int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
  `username`      varchar(20)  NOT NULL COMMENT '登录用户名',
  -- NOT NULL + 默认值: 从数据库层面杜绝"新增员工密码为 NULL 导致无法登录"
  `password`      varchar(32)  NOT NULL DEFAULT '123456' COMMENT '登录密码',
  `name`          varchar(20)  NOT NULL COMMENT '姓名',
  `gender`        tinyint unsigned NOT NULL COMMENT '性别, 1:男, 2:女',
  `phone`         char(11)     NOT NULL COMMENT '手机号',
  `position`      tinyint unsigned DEFAULT NULL COMMENT '职位, 1:班主任, 2:讲师, 3:学工主管, 4:教研主管, 5:咨询师, 6:其他',
  `salary`        int unsigned DEFAULT NULL COMMENT '薪资',
  `avatar`        varchar(255) DEFAULT NULL COMMENT '头像访问路径',
  `entry_date`    date         DEFAULT NULL COMMENT '入职日期',
  `department_id` int unsigned DEFAULT NULL COMMENT '所属部门ID',
  `create_time`   datetime     DEFAULT NULL COMMENT '创建时间',
  `update_time`   datetime     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_employee_username` (`username`),
  UNIQUE KEY `uk_employee_phone` (`phone`),
  KEY `idx_employee_department_id` (`department_id`),
  CONSTRAINT `fk_employee_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表';

-- ------------------------------------------------------------
-- 员工工作经历表
-- ------------------------------------------------------------
CREATE TABLE `employee_experience` (
  `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
  `employee_id` int unsigned NOT NULL COMMENT '员工ID',
  `begin_date`  date         DEFAULT NULL COMMENT '开始时间',
  `end_date`    date         DEFAULT NULL COMMENT '结束时间',
  `company`     varchar(50)  DEFAULT NULL COMMENT '公司名称',
  `position`    varchar(50)  DEFAULT NULL COMMENT '职位',
  PRIMARY KEY (`id`),
  KEY `idx_employee_experience_employee_id` (`employee_id`),
  CONSTRAINT `fk_employee_experience_employee` FOREIGN KEY (`employee_id`)
      REFERENCES `employee` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工工作经历表';

-- ------------------------------------------------------------
-- 班级表
-- ------------------------------------------------------------
CREATE TABLE `course_class` (
  `id`              int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
  `name`            varchar(30)  NOT NULL COMMENT '班级名称',
  `room`            varchar(20)  DEFAULT NULL COMMENT '班级教室',
  `begin_date`      date         NOT NULL COMMENT '开课时间',
  `end_date`        date         NOT NULL COMMENT '结课时间',
  `head_teacher_id` int unsigned DEFAULT NULL COMMENT '班主任ID',
  `subject`         tinyint unsigned NOT NULL COMMENT '学科, 1:Java, 2:前端, 3:大数据, 4:Python, 5:Go, 6:嵌入式',
  `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
  `update_time`     datetime     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_class_name` (`name`),
  KEY `idx_course_class_head_teacher_id` (`head_teacher_id`),
  -- 从数据库层面杜绝"结课时间早于开课时间"的脏数据(MySQL 8.0.16+ 才会真正生效)
  CONSTRAINT `chk_course_class_date` CHECK (`end_date` > `begin_date`),
  CONSTRAINT `fk_course_class_head_teacher` FOREIGN KEY (`head_teacher_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='班级表';

-- ------------------------------------------------------------
-- 学员表
-- ------------------------------------------------------------
CREATE TABLE `student` (
  `id`              int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
  `name`            varchar(20)  NOT NULL COMMENT '姓名',
  `student_no`      char(10)     NOT NULL COMMENT '学号',
  `gender`          tinyint unsigned NOT NULL COMMENT '性别, 1:男, 2:女',
  `phone`           varchar(11)  NOT NULL COMMENT '手机号',
  `id_card`         char(18)     NOT NULL COMMENT '身份证号',
  `is_college`      tinyint unsigned NOT NULL COMMENT '是否院校学员, 1:是, 0:否',
  `address`         varchar(100) DEFAULT NULL COMMENT '联系地址',
  `degree`          tinyint unsigned DEFAULT NULL COMMENT '最高学历, 1:初中, 2:高中, 3:大专, 4:本科, 5:硕士, 6:博士',
  `graduation_date` date         DEFAULT NULL COMMENT '毕业时间',
  `class_id`        int unsigned NOT NULL COMMENT '所属班级ID',
  `violation_count` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '违纪次数',
  `violation_score` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '违纪扣分',
  `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
  `update_time`     datetime     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_student_no` (`student_no`),
  UNIQUE KEY `uk_student_phone` (`phone`),
  UNIQUE KEY `uk_student_id_card` (`id_card`),
  KEY `idx_student_class_id` (`class_id`),
  CONSTRAINT `fk_student_course_class` FOREIGN KEY (`class_id`) REFERENCES `course_class` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学员表';

-- ------------------------------------------------------------
-- 操作日志表
-- 说明: 刻意不对 operator_id 建外键 —— 员工被删除后, 历史操作日志必须保留下来用于追溯。
--       method_params / return_value 必须 NOT NULL DEFAULT '',
--       因为前端日志页会直接读 row.methodParams.length, 为 NULL 会导致页面报错。
-- ------------------------------------------------------------
CREATE TABLE `operation_log` (
  `id`             int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
  `operator_id`    int unsigned          DEFAULT NULL COMMENT '操作人ID',
  `operator_name`  varchar(20)  NOT NULL DEFAULT '' COMMENT '操作人姓名',
  `operation_time` datetime     NOT NULL COMMENT '操作时间',
  `class_name`     varchar(200) NOT NULL DEFAULT '' COMMENT '类名',
  `method_name`    varchar(100) NOT NULL DEFAULT '' COMMENT '方法名',
  `method_params`  varchar(2000) NOT NULL DEFAULT '' COMMENT '请求参数(密码已掩码)',
  `return_value`   varchar(2000) NOT NULL DEFAULT '' COMMENT '返回值',
  `cost_time`      bigint       NOT NULL DEFAULT 0  COMMENT '操作耗时(ms)',
  `operation_ip`   varchar(64)  NOT NULL DEFAULT '' COMMENT '操作IP',
  PRIMARY KEY (`id`),
  KEY `idx_operation_log_operation_time` (`operation_time`),
  KEY `idx_operation_log_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- ============================================================
-- RBAC 权限模型: 员工 --< 员工角色 >-- 角色 --< 角色菜单 >-- 菜单/权限
-- 说明:
--   1. menu 表同时承载"菜单"(type=1)与"按钮/接口权限"(type=2),
--      两者都通过 permission_code 作为权限标识, 例如 class:delete。
--   2. 只有菜单(type=1)才有 path, 用于前端路由与动态菜单。
--   3. 父级菜单(type=1 且无 permission_code)只做分组, 不参与鉴权。
--   4. 权限校验在服务端完成(见 PermissionInterceptor),
--      前端隐藏菜单只是体验优化, 不能当作安全边界。
-- ============================================================

-- ------------------------------------------------------------
-- 角色表
-- ------------------------------------------------------------
CREATE TABLE `role` (
  `id`          int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
  `name`        varchar(20)  NOT NULL COMMENT '角色名称',
  `code`        varchar(30)  NOT NULL COMMENT '角色标识, 如 ADMIN',
  `remark`      varchar(100) DEFAULT NULL COMMENT '备注',
  `create_time` datetime     DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_name` (`name`),
  UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ------------------------------------------------------------
-- 菜单/权限表
-- ------------------------------------------------------------
CREATE TABLE `menu` (
  `id`              int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID, 主键',
  `parent_id`       int unsigned NOT NULL DEFAULT 0 COMMENT '父级ID, 0 表示顶级',
  `name`            varchar(20)  NOT NULL COMMENT '名称',
  `type`            tinyint unsigned NOT NULL COMMENT '类型, 1:菜单, 2:按钮/接口权限',
  `permission_code` varchar(50)  DEFAULT NULL COMMENT '权限标识, 如 class:delete; 分组菜单为空',
  `path`            varchar(100) DEFAULT NULL COMMENT '前端路由, 仅菜单有值',
  `sort`            int          NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time`     datetime     DEFAULT NULL COMMENT '创建时间',
  `update_time`     datetime     DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_permission_code` (`permission_code`),
  KEY `idx_menu_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单与权限表';

-- ------------------------------------------------------------
-- 员工-角色关联表
-- ------------------------------------------------------------
CREATE TABLE `employee_role` (
  `employee_id` int unsigned NOT NULL COMMENT '员工ID',
  `role_id`     int unsigned NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`employee_id`, `role_id`),
  KEY `idx_employee_role_role_id` (`role_id`),
  CONSTRAINT `fk_employee_role_employee` FOREIGN KEY (`employee_id`)
      REFERENCES `employee` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_employee_role_role` FOREIGN KEY (`role_id`)
      REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工角色关联表';

-- ------------------------------------------------------------
-- 角色-菜单关联表
-- ------------------------------------------------------------
CREATE TABLE `role_menu` (
  `role_id` int unsigned NOT NULL COMMENT '角色ID',
  `menu_id` int unsigned NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`),
  KEY `idx_role_menu_menu_id` (`menu_id`),
  CONSTRAINT `fk_role_menu_role` FOREIGN KEY (`role_id`)
      REFERENCES `role` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_menu_menu` FOREIGN KEY (`menu_id`)
      REFERENCES `menu` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';
