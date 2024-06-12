package com.ywy.pojo.param;

import com.ywy.core.base.BaseParam;
import lombok.Data;

@Data
public class FileInfoParam extends BaseParam {
    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 使用者ID
     */
    private String userId;

    /**
     * 文件MD5值
     */
    private String fileMd5;

    /**
     * 父級ID
     */
    private String filePid;

    /**
     * 檔案名稱
     */
    private String fileName;

    /**
     * 檔案類型：0檔；1目錄
     */
    private Integer fileClass;

    /**
     * 文件分類：1影片；2音訊；3圖片；4文件；5其他
     */
    private Integer fileCategory;

    /**
     * 檔案子分類：1影片；2音訊；3圖片；4pdf；5doc；6excel；7txt；8code；9zip；10其他
     */
    private Integer fileType;

    /**
     * 狀態：0轉碼中；1轉碼失敗；2轉碼成功
     */
    private Integer status;

    /**
     * 刪除標記：0正常；1回收站；2刪除
     */
    private Integer delFlag;


    /**
     * 檔案名稱模糊查詢
     */
    private String fileNameFuzzy;

    /**
     * 包含文件ID數組
     */
    private String[] fileIdArrIn;

    /**
     * 不包含文件ID數組
     */
    private String[] fileIdArrNotIn;

    /**
     * 是否關聯查詢使用者暱稱
     */
    private Boolean queryNickName;

    /**
     * 是否關聯查詢過期時間
     */
    private Boolean queryExpireTime;
}