# 仿百度网盘项目

基于SpringBoot、Vue.js、Element UI等主流前后端技术，采用前后端分离架构实现的网盘项目。

参考项目：easypan仿百度网盘  
前端：https://www.bilibili.com/video/BV1Qs4y1z7Km  
后端：https://www.bilibili.com/video/BV1qV4y1d7zY


## 功能介绍
* 登录注册：注册、登录、QQ登录、找回密码、发送邮箱验证码
* 文件上传：分片上传、秒传、断点续传、取消上传
* 文件列表：新建文件夹、删除、重命名、移动、文件下载
* 文件在线预览：视频播放、音乐播放、图片预览、excel、doc、pdf预览、文本预览
* 文件分享：分享、复制享链接、取消分享
* 回收站：还原、彻底删除
* 设置（仅管理员）：  
  用户文件（查看所有用户上传文件、删除、预览、下载）  
  用户管理（分配空间、启用、禁用）
  系统设置（注册注册邮箱验证码邮件模板、设置用户网盘初始化空间大小）
* 外部分享：通过提取码查看文件列表、预览文件、保存到我的网盘、当前用户取消分享


## 项目展示
1.登录
![登录](doc/登录.png)

2.注册
![注册](doc/注册.png)

3.主界面
![主界面](doc/主界面.png)

4.上传文件
![上传文件](doc/上传文件.png)

5.文件预览  
文档预览
![文档预览](doc/文档预览.png)
视频预览
![视频预览](doc/视频预览.png)

6.文件分享  
分享文件
![分享文件](doc/分享文件.png)
分享结果
![分享结果](doc/分享结果.png)
分享记录
![分享记录](doc/分享记录.png)

7.访问分享链接  
分享校验
![分享校验](doc/分享校验.png)
分享文件列表
![分享文件列表](doc/分享文件列表.png)
保存到我的网盘
![保存到我的网盘](doc/保存到我的网盘.png)

8.回收站
![回收站](doc/回收站.png)

9.管理员设置  
用户文件
![用户文件](doc/管理员设置-用户文件.png)
用户管理
![用户管理](doc/管理员设置-用户管理.png)
系统设置
![系统设置](doc/管理员设置-系统设置.png)


## 前端项目介绍
### 开发环境
* Visual Studio Code
* [node v16.20.0](https://nodejs.org/download/release/v16.20.0/)

### 技术选型
* 前端框架：Vue3
* 构建工具：Vite
* UI框架：Element Plus

### 项目部署
1. 下载源码
2. 编译源码  
执行 `npm install`命令，下载依赖包。
3. 启动项目  
执行`npm run dev`命令启动项目，启动后通过http://localhost:6088访问项目。
4. 项目打包  
使用nginx访服务访问项目，nginx配置文件如下：
```
 server {
    listen       80;
    server_name  test.pan.com;

    charset utf-8;

    location /netdisk {
        alias /root/app/boot-netdisk/dist/;
        try_files $uri $uri/ /netdisk/index.html;
        index  index.html;
    }
    location /api {
        proxy_pass http://localhost:8003/api;
        proxy_set_header x-forwarded-for  $remote_addr;
    }
 }
```
注意： `server_name`为自己的域名或IP，`/netdisk`为项目前缀必须与`vite.config.js`中的base对应：
```
export default defineConfig({
  plugins: [
    vue(),
  ],
  base: '/netdisk',
  ......
```
如果不想要前缀，nginx和vite都需要去掉。  
执行`npm run build`命令，进行打包，打包后会生成dist目录。放到服务器指定目录下，配置nginx，启动nginx即可访问。

访问地址：http://IP:port/netdisk/，管理员账号：admin@qq.com/a123456，测试账号：test@qq.com/a123456

## 后端项目介绍
### 开发环境
* JDK1.8
* 数据库：MySQL5.7
* 非关系型数据库：Redis
* 多媒体框架：[ffmpeg](https://ffmpeg.org/download.html)  
windows安装ffmpeg：下载安装包https://github.com/BtbN/FFmpeg-Builds/releases，解压后配置环境变量到bin即可。  
centos7安装ffmpeg：  
（1）通过安装rpm包启用RPM Fusion存储库  
`yum install https://download1.rpmfusion.org/free/el/rpmfusion-free-release-8.noarch.rpm`  
（2）添加SDL仓库  
`yum install http://rpmfind.net/linux/epel/7/x86_64/Packages/s/SDL2-2.0.14-2.el7.x86_64.rpm`  
（3）安装FFmpeg  
`yum install ffmpeg`  
（4）通过检查其版本来验证FFmpeg安装  
`ffmpeg -version`

### 技术选型
* 基础框架：SpringBoot、MyBatis
* 数据库：MySQL
* 非关系型数据库：Redis
* 多媒体框架：ffmpeg

### 项目部署
1. 下载源码
2. 编译源码  
在工程下执行`maven clean install`命令下载依赖。
3. 导入数据库  
使用数据库脚本`doc/boot-netdisk.sql`，初始化数据库。同时修改工程的application.yml文件中的数据源等配置信息。
4. 服务器运行项目  
执行`maven clean package`命令进行打包，将打包后的jar文件上传服务器，执行命令运行项目：  
`nohup java -jar boot-netdisk..jar >run.log 2>&1 &`#   b r u c e d r i v e  
 