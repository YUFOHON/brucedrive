package com.ywy.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 外部分享信息
 */
@Data
public class WebShareVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 分享文件名称
     */
    private String fileName;

    /**
     * 分享用户昵称
     */
    private String nickName;

    /**
     * 分享用户头像
     */
    private String avatar;

    /**
     * 是否为当前用户
     */
    private Boolean currentUser;
}
