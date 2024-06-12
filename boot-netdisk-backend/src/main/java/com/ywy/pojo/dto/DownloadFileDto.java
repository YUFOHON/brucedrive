package com.ywy.pojo.dto;

import lombok.Data;

/**
 * 文件下载信息
 */
@Data
public class DownloadFileDto {
    /**
     * 下载码
     */
    private String code;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;
}
