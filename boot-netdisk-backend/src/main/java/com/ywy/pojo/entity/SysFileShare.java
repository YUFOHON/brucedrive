package com.ywy.pojo.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ywy.core.base.BaseEntity;
import lombok.Data;

/**
 * 文件分享
 * @TableName sys_file_share
 */
@Data
public class SysFileShare extends BaseEntity {
    /**
     * 分享ID
     */
    private String shareId;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 分享用户ID
     */
    private String userId;

    /**
     * 有效期类型：0：1天；1：7天；2：30天；4：永久
     */
    private Integer validType;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 提取码
     */
    private String code;

    /**
     * 浏览次数
     */
    private Integer showCount;


    /******************** 联表查询 sys_file_info ********************/
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

    @Override
    public void setId(String id) {
        super.setId(id);
        this.shareId = id;
    }
}