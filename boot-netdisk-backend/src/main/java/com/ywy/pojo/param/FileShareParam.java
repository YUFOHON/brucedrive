package com.ywy.pojo.param;

import com.ywy.core.base.BaseParam;
import lombok.Data;

@Data
public class FileShareParam extends BaseParam {
    /**
     * 文件分享ID
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
     * 是否关联查询文件名
     */
    private Boolean queryFileName;
}
