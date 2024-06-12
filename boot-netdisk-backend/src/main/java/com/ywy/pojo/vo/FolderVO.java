package com.ywy.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件目录信息
 */
@Data
public class FolderVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 文件名称
     */
    private String fileName;
}
