package com.ywy.pojo.dto;

import lombok.Data;

/**
 * 用户网盘空间信息
 */
@Data
public class UserSpaceDto {
    /**
     * 已使用空间
     */
    private Long usedSpace;

    /**
     * 总空间
     */
    private Long totalSpace;
}
