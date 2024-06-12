package com.ywy.pojo.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ywy.core.base.BaseEntity;
import lombok.Data;

/**
 * 文件信息表
 * @TableName sys_file_info
 */
@Data
public class SysFileInfo extends BaseEntity {
    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 文件MD5值
     */
    private String fileMd5;

    /**
     * 父级ID
     */
    private String filePid;

    /**
     * 文件大小，单位B
     */
    private Long fileSize;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 封面
     */
    private String fileCover;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件类型：0文件；1目录
     */
    private Integer fileClass;

    /**
     * 文件分类：1视频；2音频；3图片；4文档；5其他
     */
    private Integer fileCategory;

    /**
     * 文件子分类：1视频；2音频；3图片；4pdf；5doc；6excel；7txt；8code；9zip；10其他
     */
    private Integer fileType;

    /**
     * 状态：0转码中；1转码失败；2转码成功
     */
    private Integer status;

    /**
     * 删除标记：0正常；1回收站；2删除
     */
    private Integer delFlag;

    /**
     * 进入回收站时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recycleTime;

    /******************** 联表查询 sys_user ********************/
    /**
     * 用户昵称
     */
    private String nickName;

    @Override
    public void setId(String id) {
        super.setId(id);
        this.fileId = id;
    }
}