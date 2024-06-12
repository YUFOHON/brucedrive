package com.ywy.core.constants;

/**
 * 常量定义
 */
public class SystemConstants {
    public static final Integer ZERO = 0;
    public static final String ZERO_STR = "0";

    public static final Integer LENGTH_5 = 5;
    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_15 = 15;
    public static final Integer LENGTH_20 = 20;
    public static final Integer LENGTH_30 = 30;
    public static final Integer LENGTH_50 = 50;
    public static final Integer LENGTH_150 = 150;

    public static final Long MB = 1024 * 1024L;

    /**
     * 用户头像目录
     */
    public static final String USER_AVATAR_FOLDER = "avatar/";

    /**
     * 用户头像后缀名
     */
    public static final String USER_AVATAR_SUFFIX = ".jpg";
    /**
     * default user avatar
     */
    public static final String USER_AVATAR_DEFAULT = "default_avatar.jpg";
    /**
     * 视频缩略图后缀名
     */
    public static final String VIDEO_COVER_SUFFIX = ".png";

    /**
     * 文件临时目录
     */
    public static final String FILE_TEMP_FOLDER = "temp/";
    /**
     * 文件真实目录
     */
    public static final String FILE_FOLDER = "file/";

    public static final Object TS_NAME = "index.ts";
    public static final String M3U8_NAME = "index.m3u8";


    /******************* session ********************/

    /**
     * Verification code type: Login registration
     */
    public static final String SESSION_CAPTCHA_CODE_CHECK = "session_captcha_code_check";

    /**
     * Verification code type: Send email verification code
     */
    public static final String SESSION_CAPTCHA_CODE_EMAIL = "session_captcha_code_email";

    /**
     * Session login user
     */
    public static final String SESSION_LOGIN_USER = "session_login_user";

    /**
     * Session file sharing
     */
    public static final String SESSION_SHARE = "session_share_";


    /******************* redis key ********************/

    /**
     * Expiration time 1 minute
     */
    public static final Integer REDIS_KEY_EXPIRES_ONE_MIN = 60;

    /**
     * Expiration time 5 minutes
     */
    public static final Integer REDIS_KEY_EXPIRES_FIVE_MIN = REDIS_KEY_EXPIRES_ONE_MIN * 5;

    /**
     * Expiration time 1 hour
     */
    public static final Integer REDIS_KEY_EXPIRES_ONE_HOUR = REDIS_KEY_EXPIRES_ONE_MIN * 60;

    /**
     * Expiration time 1 day
     */
    public static final Integer REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;

    /**
     * System settings
     */
    public static final String REDIS_KEY_SYSTEM_SETTINGS = "netdisk:system_settings";

    /**
     * User network disk space
     */
    public static final String REDIS_KEY_USER_SPACE = "netdisk:user_space:";

    /**
     * User temporary file size
     */
    public static final String REDIS_KEY_USER_FILE_TEMP_SIZE = "netdisk:user_file_temp_size:";

    /**
     * Download code
     */
    public static final String REDIS_KEY_DOWNLOAD_CODE = "netdisk:download_code:";

}
