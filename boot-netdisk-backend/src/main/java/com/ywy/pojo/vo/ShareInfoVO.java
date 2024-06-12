package com.ywy.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件分享信息
 */
@Data
public class ShareInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件分享ID
     */
    private String shareId;

    /**
     * 提取码
     */
    private String code;
}
