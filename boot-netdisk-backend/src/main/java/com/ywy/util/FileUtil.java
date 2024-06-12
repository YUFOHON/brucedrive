package com.ywy.util;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.ywy.core.constants.SystemConstants;
import com.ywy.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Slf4j
public class FileUtil {
    /**
     * 校验文件路径，防止越级读取文件
     * @param path
     * @return
     */
    public static boolean checkPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return true;
        }
        if (path.contains("../") || path.contains("..\\")) {
            return false;
        }
        return true;
    }

    /**
     * read file to stream
     * @param response
     * @param filePath
     */
    public static void readFile(HttpServletResponse response, String filePath) {
        if (!checkPath(filePath)) {
            return;
        }
        OutputStream os = null;
        FileInputStream fis = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            fis = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            os = response.getOutputStream();

            // The front-end word preview only supports .docx, convert .doc to .docx
            if (filePath.endsWith(".doc") ) {
                Document doc = new Document(fis);
                doc.save(os, SaveFormat.DOCX);
            }
            int len = 0;
            while ((len = fis.read(byteData)) != -1) {
                os.write(byteData, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            log.error("File reading exception", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    log.error("IO exception", e);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error("IO exception", e);
                }
            }
        }
    }

    /**
     * Get the file name without suffix
     * @param fileName
     * @return
     */
    public static String getFileNameNoSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    /**
     * Get the suffix of the file
     * @param fileName
     * @return
     */
    public static String getFileSuffix(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        return fileName.substring(index);
    }

    /**
     * 重命名
     * @param fileName
     * @return
     */
    public static String rename(String fileName) {
        String fileNameReal = getFileNameNoSuffix(fileName);
        String suffix = getFileSuffix(fileName);
        return fileNameReal + "_" + RandomStringUtils.random(SystemConstants.LENGTH_5, true, true) + suffix;
    }

    /**
     * Merge chunk files
     * @param dirPath
     * @param toFilePath
     * @param fileName
     * @param delSource
     */
    public static void unionFile(String dirPath, String toFilePath, String fileName, Boolean delSource) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            throw new BusinessException("Directory does not exist");
        }

        File[] fileList = dirFile.listFiles();
        File targetFile = new File(toFilePath);
        RandomAccessFile writeFile = null;
        try {
            writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < fileList.length; i++) {
                int len = -1;
                File chunkFile = new File(dirFile + "/" + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0 , len);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Failed to merge chunks：{}", fileName, e);
                    throw new BusinessException("Failed to merge chunks");
                } finally {
                    if (null != readFile) {
                        try {
                            readFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to merge file fragments: {}", fileName, e);
            throw new BusinessException("Failed to merge file fragments");
        } finally {
            if (null != writeFile) {
                try {
                    writeFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // delete file
            if (delSource && dirFile.exists()) {
                try {
                    FileUtils.deleteDirectory(dirFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
