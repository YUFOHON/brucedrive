package com.ywy.core.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageResultVO<T> {
    private Integer totalCount;
    private Integer pageSize;
    private Integer pageNo;
    private Integer pageTotal;
    private List<T> list = new ArrayList<T>();

    public PageResultVO() {

    }

    public PageResultVO(Integer totalCount, Integer pageSize, Integer pageNo, List<T> list) {
        this.totalCount = totalCount;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.list = list;

        if (this.pageSize <= 0) {
            this.pageSize = 15;
        }

        if (this.totalCount > 0) {
            this.pageTotal = this.totalCount % this.pageSize == 0 ? this.totalCount / this.pageSize : this.totalCount / this.pageSize + 1;
        } else {
            pageTotal = 1;
        }

        if (this.pageNo <= 1) {
            this.pageNo = 1;
        }

        if (this.pageNo > this.pageTotal) {
            this.pageNo = this.pageTotal;
        }
    }
}
