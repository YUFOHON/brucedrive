package com.ywy.pojo.enums;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 文件子分类枚举值
 */
public enum FileTypeEnum {
    VIDEO(FileCategoryEnum.VIDEO, 1, new String[] {".mp4", ".avi", ".rmvb", ".mkv", ".mov"}, "视频"),
    AUDIO(FileCategoryEnum.AUDIO, 2, new String[] {".mp3", ".wav", ".wma", ".mp2", ".flac", ".midi", ".ra", ".ape", ".acc", ".cda"}, "音频"),
    IMAGE(FileCategoryEnum.IMAGE, 3, new String[] {".jpeg", ".jpg", ".png", ".gif", ".bmp", ".dds", ".psd", ".pst", ".webp", ".xmp", ".svg", ".tiff"}, "图片"),
    PDF(FileCategoryEnum.DOC, 4, new String[] {".pdf"}, "pdf"),
    WORD(FileCategoryEnum.DOC, 5, new String[] {".doc", ".docx"}, "word"),
    EXCEL(FileCategoryEnum.DOC, 6, new String[] {".xls", ".xlsx"}, "excel"),
    TXT(FileCategoryEnum.DOC, 7, new String[] {".txt"}, "txt文本"),
    PROGRAM(FileCategoryEnum.DOC, 8, new String[] {".h", ".c", ".hpp", ".cpp", ".c++", ".dll", ".java", ".class", ".jsp", ".html", ".htm", ".js", ".ts", ".css", ".scss", "json", "vue", ".xml", ".sql"}, "程序"),
    ZIP(FileCategoryEnum.OTHERS, 9, new String[] {".rar", ".zip", ".7z", ".tar", ".gz", ".war", ".jar", ".iso"}, "压缩文件"),
    OTHERS(FileCategoryEnum.OTHERS, 10, new String[] {}, "其他"),
    ;

    private FileCategoryEnum category;
    private Integer type;
    private String[] suffixs;
    private String desc;

    FileTypeEnum(FileCategoryEnum category, Integer type, String[] suffixs, String desc) {
        this.category = category;
        this.type = type;
        this.suffixs = suffixs;
        this.desc = desc;
    }

    public FileCategoryEnum getCategory() {
        return category;
    }

    public Integer getType() {
        return type;
    }

    public String[] getSuffixs() {
        return suffixs;
    }

    public String getDesc() {
        return desc;
    }

    public static FileTypeEnum getFileTypeBySuffix(String suffix) {
        for (FileTypeEnum item : FileTypeEnum.values()) {
            if (ArrayUtils.contains(item.getSuffixs(), suffix)) {
                return item;
            }
        }
        return FileTypeEnum.OTHERS;
    }

    public static FileTypeEnum getByType(String type) {
        for (FileTypeEnum item : FileTypeEnum.values()) {
            if (item.getType().equals(type)) {
                return item;
            }
        }
        return null;
    }
}
