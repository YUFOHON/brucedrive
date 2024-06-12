package com.ywy.core.base;

import lombok.Data;

/**
 * Base query parameter
 */
@Data
public class BaseParam {
    private Integer pageNo = 1;

    private Integer pageSize = 15;

    private String orderBy;

    public void setPageNo(Integer pageNo) {
        if (pageNo == null || pageNo <= 0) {
            pageNo = 1;
        }
        this.pageNo = pageNo;
    }

    public void setPageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            pageSize = 15;
        }

        this.pageSize = pageSize;
    }
}
