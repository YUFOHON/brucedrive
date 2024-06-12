# BruceDrive Project

## Project Description

BruceDrive is a cloud storage project designed for end-users, inspired by Google Drive. It includes features such as user registration, file uploading, chunked uploads, resumable uploads, deduplication, online file previewing (including text, images, videos, audios, Excel, Word, PDF, etc.), and file sharing.

## Technology Stack

- Spring Boot
- MyBatis
- MySQL
- Redis
- FFmpeg

## Responsibilities

1. **User Management**:
    - User registration, login, email verification, and password recovery.

2. **File Management**:
    - Chunked file uploads, deduplication, new folder creation, file previewing, renaming, moving, sharing, and deleting.
    - Shared file list and canceling file sharing.
    - Recycle bin functionality, file restoration, and permanent deletion.

3. **Admin Features**:
    - Super admin role to query all user-uploaded files, with the ability to download and delete files.
    - Super admin user management, including assigning storage space, enabling, and disabling users.
    - System settings configuration, such as email template customization and setting user registration initial storage size.

4. **Shared File Access**:
    - Users can preview and download files shared by others using the share link and code.
    - Users can save shared files to their own cloud storage.

## Key Features and Challenges

1. **Chunked File Uploads and Deduplication**:
    - Implemented chunked file uploads and deduplication using file MD5 checksums.
    - Asynchronously merged file chunks after the upload, and generated video thumbnails using FFmpeg.
    - Cached real-time user storage usage with Redis to avoid frequent database queries.

2. **Multi-level Directory Handling**:
    - Developed a recursive query to retrieve the full path of a directory.
    - Automatically renamed files in the same directory to handle name conflicts.
    - Implemented file moving functionality while preserving the file name.

3. **Permissions and Validation**:
    - Used Spring AOP annotations to implement different access levels for regular users and super admins.
    - Leveraged AOP and Java reflection to perform server-side parameter validation.

4. **Asynchronous Transactions and Circular Dependencies**:
    - Solved the problem of executing asynchronous operations within transactions.
    - Addressed circular dependencies during application development.

## Key Takeaways

- Gained proficiency in using Spring Boot, including the AOP (Aspect-Oriented Programming) approach for implementing role-based access control and server-side validation.
- Utilized Redis for caching system configurations and real-time user storage usage calculations, reducing the load on the database.
- Learned to design the database schema considering future scalability, such as partitioning file data based on user IDs.
- Practiced comprehensive application development, from functional design to database modeling, and from backend implementation to integrating third-party tools (FFmpeg).
- Developed a deeper understanding of Java and database concepts through the practical application of knowledge in a complete project.

The BruceDrive project allowed me to apply my knowledge of Java, databases, and software engineering principles to build a complete cloud storage solution. It was a valuable learning experience that enhanced my skills in backend development, system design, and problem-solving.

## Project Demo
http://34.96.216.102/brucedrive/
# User

```sql
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `user_info` (
`user_id` varchar(15) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'user id',
`nick_name` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'user name',
`password` char(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT 'password',
`salt` char(36) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'salt value',
`phone` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'Phone number',
`email` varchar(30) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'Email address',
`gender` int(0) NULL DEFAULT NULL COMMENT 'Gender: 0-female, 1-male',
`avatar` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'Avatar',
`last_login_time` datetime(0) NULL DEFAULT NULL COMMENT 'Log-last, login, time',
`is_delete` int(0) NULL DEFAULT NULL COMMENT 'Deleted: 0-Not deleted, 1-Deleted',
`use_space` bigint(20) NULL DEFAULT NULL COMMENT 'byte',
`total_space` bigint(20) NULL DEFAULT NULL COMMENT 'total byte available',
`created_user` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'Log-creator',
`create_time` datetime(0) NULL DEFAULT NULL COMMENT 'Log-creation time',
`modified_user` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT 'Log-last modification executor',
`update_time` datetime(0) NULL DEFAULT NULL COMMENT 'Log-last modification time',
PRIMARY KEY (`user_id`),
UNIQUE KEY `key_nick_name`(`nick_name`)
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;
```

```sql

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_email_code
-- ----------------------------
DROP TABLE IF EXISTS `sys_email_code`;
CREATE TABLE `sys_email_code`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `email` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '邮箱',
  `code` varchar(5) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '验证码',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态（0：未使用；1：已使用）',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uni_email_code`(`email`, `code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '邮箱验证码' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_file_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_info`;
CREATE TABLE `sys_file_info` (
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
`file_type` tinyint(1) NULL DEFAULT NULL COMMENT 'File sub-category: 1 video; 2 audio; 3 picture; 4 pdf; 5 doc; 6 excel; 7 txt; 8 code; 9 zip; 10 other',
`status` tinyint(1) NULL DEFAULT 0 COMMENT 'Status: 0 transcoding; 1 transcoding failed; 2 transcoding successful',
`del_flag` tinyint(1) NULL DEFAULT 0 COMMENT 'Delete flag: 0 normal; 1 recycle bin; 2 delete',
`recycle_time` datetime(0) NULL DEFAULT NULL COMMENT 'Enter recycle bin time',
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
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = 'File information table' ROW_FORMAT = Dynamic;
-- ----------------------------
-- Table structure for sys_file_share
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_share`;
CREATE TABLE `sys_file_share` (
`id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'File sharing ID',
`file_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'File ID',
`user_id` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Sharing user ID',
`valid_type` tinyint(1) NULL DEFAULT 0 COMMENT 'Validity period type: 0: 1 day; 1: 7 days; 2: 30 days; 4: Permanent',
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
`role` tinyint(2) NULL DEFAULT 0 COMMENT 'User role (0: ordinary user; 1: administrator)',
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
INSERT INTO `sys_user` VALUES ('4055173124', 'Administrator', 'admin@qq.com', NULL, '', 'dc483e80a7a0bd9ef71d8cf973673924', '2023-12-06 19:00:46', 1595932672, 272378707, 1, 1, '2023-10-21 16:30:58', '2023-12-06 19:01:01');
INSERT INTO `sys_user` VALUES ('4055173125', 'Test user', 'test@qq.com', NULL, NULL, 'dc483e80a7a0bd9ef71d8cf973673924', '2023-12-06 19:01:45', 1595932672, 73337917, 1, 0, '2023-11-30 22:31:46', '2023-12-06 19:03:56');
SET FOREIGN_KEY_CHECKS = 1;

```

# GOOGLE EMAIL PASSWORD

**dlpn bamq hdgs hhjq**

# Deploy

```
sudo docker pull mysql:8.0.37

docker run -d --name myMysql -p 9506:3306 -v /data/mysql:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456 mysql:8.0.37

docker start myMysql

docker exec -it myMysql mysql -u root -p
```

```sql
FROM  openjdk:17
VOLUME /brucedrive
ADD boot-netdisk.jar brucedrive.jar
EXPOSE 8888
ENTRYPOINT ["java","-jar","/boot-netdisk.jar"]
```

connect to database

```sql
sudo docker exec -it myMysql mysql -u root -p
// DOCKER
jdbc:mysql://localhost:9506/brucedrive
 // LOCAL MYSQL
    url: jdbc:mysql://localhost:3306/brucepan?useUnicode=true&allowPublicKeyRetrieval=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false

```

# ffmpeg command

install the ffmpeg

```
sudo apt-get install ffmpeg  # For Ubuntu/Debian
sudo yum install ffmpeg      # For CentOS/RHEL
```

1. **Transfer TS File**:

   The first command, `CMD_TRANSFER_TS`, is used to convert an input file to a Transport Stream (TS) format with the following options:

    - `y`: Overwrites the output file without asking.
    - `i %s`: Specifies the input file.
    - `vcodec copy`: Copies the video stream without re-encoding.
    - `acodec copy`: Copies the audio stream without re-encoding.
    - `vbsf h264_mp4toannexb`: Applies the h264_mp4toannexb video bitstream filter to convert the video stream to Annex B format.
    - `%s`: Specifies the output file.

   You can run this command in a terminal or script as follows:

   Copy

    ```
    ffmpeg -y -i LAYUP_PRATICE.mp4 -vcodec copy -acodec copy -vbsf h264_mp4toannexb LAYUP_PRATICE.ts
    
    ```

2. **Cut TS File**:

   The second command, `CMD_CUT_TS`, is used to split the input TS file into multiple smaller TS files (segments) with the following options:

    - `i %s`: Specifies the input TS file.
    - `c copy`: Copies the streams without re-encoding.
    - `map 0`: Selects all streams from the input.
    - `f segment`: Sets the output format to segmented files.
    - `segment_list %s`: Specifies the output segment list file.
    - `segment_time 30`: Sets the duration of each segment to 30 seconds.
    - `%s/%s_%%4d.ts`: Specifies the output filename pattern for the segmented TS files.

   You can run this command in a terminal or script as follows:

   Copy

    ```
    ffmpeg -i LAYUP_PRATICE.ts -c copy -map 0 -f segment -segment_list LAYUP_PRATICE_index.m3u8 -segment_time 30 LAYUP_PRATICE_%04d.ts
    ```
