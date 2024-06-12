/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 50717
 Source Host           : localhost:3306
 Source Schema         : boot-netdisk

 Target Server Type    : MySQL
 Target Server Version : 50717
 File Encoding         : 65001

 Date: 06/12/2023 19:17:34
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_email_code
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_code`;
CREATE TABLE `sys_email_code`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'email',
  `code` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'verification code',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT 'status（0：not use；1：used）',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uni_email_code`(`email`, `code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'email verification code' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_file_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_info`;
CREATE TABLE `sys_file_info`  (
`id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'File ID',
`user_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'User ID',
`file_md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'File MD5 value',
`file_pid` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Parent ID',
`file_size` bigint(20) NULL DEFAULT 0 COMMENT 'File size, unit B',
`file_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'File name',
`file_cover` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Cover',
`file_path` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'File path',
`file_class` tinyint(1) NULL DEFAULT NULL COMMENT 'File type: 0 file; 1 directory',
`file_category` tinyint(1) NULL DEFAULT NULL COMMENT 'File category: 1 video; 2 audio; 3 picture; 4 document; 5 other',
`file_type` tinyint(1) NULL DEFAULT NULL COMMENT 'File sub-categories: 1 video; 2 audio; 3 picture; 4 pdf; 5 doc; 6 excel; 7 txt; 8 code; 9 zip; 10 other',
`status` tinyint(1) NULL DEFAULT 0 COMMENT 'Status: 0 transcoding; 1 transcoding failed; 2 transcoding successful',
`del_flag` tinyint(1) NULL DEFAULT 0 COMMENT 'Delete flag: 0 normal; 1 recycle bin; 2 delete',
`recycle_time` datetime(0) NULL DEFAULT NULL COMMENT 'Entering the recycle bin time',
`create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
 `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT 'Update time',
  PRIMARY KEY (`id`, `user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_md5`(`file_md5`) USING BTREE,
  INDEX `idx_file_pid`(`file_pid`) USING BTREE,
  INDEX `idx_del_flag`(`del_flag`) USING BTREE,
  INDEX `idx_recycle_time`(`recycle_time`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'file information table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_file_share
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_share`;
CREATE TABLE `sys_file_share` (
`id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'File sharing ID',
`file_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'File ID',
`user_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Sharing user ID',
`valid_type` tinyint(1) NULL DEFAULT 0 COMMENT 'Validity type: 0: 1 day; 1: 7 days; 2: 30 days; 4: Permanent',
`expire_time` datetime(0) NULL DEFAULT NULL COMMENT 'Expiration time',
`code` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Extraction code',
`show_count` int(11) NULL DEFAULT 0 COMMENT 'Number of views',
`create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'File sharing' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
`id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'User ID',
`nick_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Nickname',
`email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Registration email',
`qq_open_id` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'QQ login ID',
`qq_avatar` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'QQ avatar',
`password` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'password',
`last_login_time` datetime(0) NULL DEFAULT NULL COMMENT 'last login time',
`total_space` bigint(20) NULL DEFAULT 0 COMMENT 'total storage space, unit byte',
`used_space` bigint(20) NULL DEFAULT 0 COMMENT 'used space, unit byte',
`status` tinyint(1) NULL DEFAULT 1 COMMENT 'status (0: disabled; 1: enabled)',
`role` tinyint(2) NULL DEFAULT 0 COMMENT 'user role (0: ordinary user; 1: administrator)',
`create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
`update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT 'update time',
PRIMARY KEY (`id`) USING BTREE,
UNIQUE INDEX `uni_email`(`email`) USING BTREE,
UNIQUE INDEX `uni_qq_open_id`(`qq_open_id`) USING BTREE,
UNIQUE INDEX `uni_nickname`(`nick_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'user information' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES ('4055173124', '管理员', 'admin@qq.com', NULL, '', 'dc483e80a7a0bd9ef71d8cf973673924', '2023-12-06 19:00:46', 1595932672, 272378707, 1, 1, '2023-10-21 16:30:58', '2023-12-06 19:01:01');
INSERT INTO `sys_user` VALUES ('4055173125', '测试用户', 'test@qq.com', NULL, NULL, 'dc483e80a7a0bd9ef71d8cf973673924', '2023-12-06 19:01:45', 1595932672, 73337917, 1, 0, '2023-11-30 22:31:46', '2023-12-06 19:03:56');

SET FOREIGN_KEY_CHECKS = 1;
