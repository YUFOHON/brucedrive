package com.ywy.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * File information
 */
@Data
public class FileInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * File ID
     */
    private String fileId;

    /**
     * Parent ID
     */
    private String filePid;

    /**
     * File size
     */
    private Long fileSize;

    /**
     * File name
     */
    private String fileName;

    /**
     * Cover
     */
    private String fileCover;

    /**
     * File type: 0 file; 1 directory
     */
    private Integer fileClass;

    /**
     * File category: 1 video; 2 audio; 3 picture; 4 document; 5 other
     */
    private Integer fileCategory;

    /**
     * File sub-category: 1 video; 2 audio; 3 picture; 4 pdf; 5 doc; 6 excel; 7 txt; 8 code; 9 zip; 10 other
     */
    private Integer fileType;

    /**
     * Status: 0 transcoding; 1 transcoding failed; 2 transcoding successful
     */
    private Integer status;

    /**
     * Modification time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * Time to enter the recycle bin
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date recycleTime;
}