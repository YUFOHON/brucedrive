package com.ywy.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {
    public static final String UTF_8 = "UTF-8";
    // 算法名称
    private static final String KEY_ALGORITHM = "AES";
    // 算法/模式/填充
    private static final String CIPHER_MODE = "AES/ECB/PKCS5Padding";

    /**
     * AES 加密操作
     *
     * @param src  待加密内容
     * @param key 加密密码
     * @return 返回Base64转码后的加密数据
     */
    public static String encrypt(String src, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(UTF_8), KEY_ALGORITHM);
            // 实例化
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            byte[] byteContent = src.getBytes("utf-8");
            // 使用密钥初始化，设置为加密模式
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // 执行加密操作
            byte[] result = cipher.doFinal(byteContent);
            return byte2hex(result);
        } catch (Exception ex) {
           ex.printStackTrace();
        }
        return null;
    }

    /**
     * AES 解密操作
     *
     * @param content
     * @param key
     * @return
     */
    public static String decrypt(String content, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(UTF_8), KEY_ALGORITHM);
            // 实例化
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            // 使用密钥初始化，设置为解密模式
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            // 执行解密操作
            byte[] result = cipher.doFinal(hex2byte(content));
            return new String(result, UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * 字节数组转成16进制字符串
     */
    public static String byte2hex(byte[] bytes) {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        String tmp = "";
        for (int n = 0; n < bytes.length; n++) {
            // 整数转成十六进制表示
            tmp = (Integer.toHexString(bytes[n] & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString().toUpperCase(); // 转成大写
    }

    /**
     * 将hex字符串转换成字节数组
     */
    private static byte[] hex2byte(String hexStr) {
        if (hexStr == null || hexStr.length() < 2) {
            return new byte[0];
        }
        hexStr = hexStr.toLowerCase();
        int l = hexStr.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = hexStr.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(encrypt("打开阀门", "4d829921740ceb089d01985dd5eced13"));
    }
}
