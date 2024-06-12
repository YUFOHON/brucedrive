package com.ywy.pojo.dto;

import lombok.Data;

/**
 * 文件上传返回信息
 */
@Data
public class UploadResultDto {
    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 上传状态
     */
    private String status;
}
