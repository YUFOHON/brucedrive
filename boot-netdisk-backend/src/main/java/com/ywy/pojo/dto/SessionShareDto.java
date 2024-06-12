package com.ywy.pojo.dto;

import lombok.Data;

import java.util.Date;

/**
 * 文件分享session信息
 */
@Data
public class SessionShareDto {
    /**
     * 分享ID
     */
    private String shareId;

    /**
     * 分享用户ID
     */
    private String shareUserId;

    /**
     * 分享文件ID
     */
    private String fileId;

    /**
     * 分享过期时间
     */
    private Date expireTime;
}
