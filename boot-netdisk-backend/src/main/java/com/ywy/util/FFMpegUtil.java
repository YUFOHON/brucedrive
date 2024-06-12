package com.ywy.util;

import com.ywy.core.constants.SystemConstants;
import com.ywy.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

@Slf4j
public class FFMpegUtil {
    /**
     * 视频切片
     * @param fileId
     * @param videoFilePath
     */
    public static void cutFile4Video(String fileId, String videoFilePath) {
        // 创建同名切片目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }
        String CMD_TRANSFER_TS = "ffmpeg -y -i %s -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
        String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";

        String tsPath = tsFolder + "/" + SystemConstants.TS_NAME;
        // Generate .ts
        String cmd = String.format(CMD_TRANSFER_TS, videoFilePath, tsPath);
        exec(cmd, false);
        // Generate index file .m3u8 and slice .ts
        //public static final String M3U8_NAME = "index.m3u8"
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + SystemConstants.M3U8_NAME, tsFolder.getPath(), fileId);
        exec(cmd, false);

        // Delete index.ts
        new File(tsPath).delete();
    }

    /**
     * 生成视频封面
     * @param sourceFile
     * @param width
     * @param targetFile
     */
    public static void createVideoCover(File sourceFile, Integer width, File targetFile) {
        try {
            String cmd = "ffmpeg -i %s -y -vframes 1 -vf scale=%d:%d/a %s";
            exec(String.format(cmd, sourceFile.getAbsoluteFile(), width, width, targetFile.getAbsoluteFile()), false);
        } catch (Exception e) {
            log.error("Create video cover fail", e);
        }
    }

    /**
     * 生成图片缩略图
     * @param sourceFile
     * @param thumbnailWidth
     * @param targetFile
     * @param delSource
     * @return
     */
    public static Boolean createThumbnail(File sourceFile, Integer thumbnailWidth, File targetFile, Boolean delSource) {
        try {
            BufferedImage bufferedImage = ImageIO.read(sourceFile);
            int w = bufferedImage.getWidth();
            int h = bufferedImage.getHeight();
            // 如果图片宽度小于指定宽度则不进行压缩
            if (w <= thumbnailWidth) {
                return false;
            }
            compressImage(sourceFile, thumbnailWidth, targetFile, delSource);
            return true;
        } catch (Exception e) {
            log.error("生成图片缩略图失败", e);
        }
        return false;
    }

    /**
     * 图片压缩
     * @param sourceFile
     * @param width
     * @param targetFile
     * @param delSource
     */
    public static void compressImage(File sourceFile, Integer width, File targetFile, Boolean delSource) {
        try {
            String cmd = "ffmpeg -i %s -vf scale=%d:-1 %s -y";
            exec(String.format(cmd, sourceFile.getAbsoluteFile(), width, targetFile.getAbsoluteFile()), false);
            if (delSource) {
                FileUtils.forceDelete(sourceFile);
            }
        } catch (Exception e) {
            log.error("压缩图片失败", e);
        }
    }

    public static String exec(String cmd, Boolean out) {
        if (StringUtils.isEmpty(cmd)) {
            log.error("命令为空执行失败");
            return null;
        }
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            // 执行命令
            process = Runtime.getRuntime().exec(cmd);

            // 取出输出流和错误流的信息
            PrintStream errStream = new PrintStream(process.getErrorStream());
            PrintStream infoStream = new PrintStream(process.getInputStream());
            errStream.start();
            infoStream.start();

            // 等待命令执行完成
            process.waitFor();
            String result = errStream.stringBuffer.append(infoStream.stringBuffer + "\n").toString();

            // 输出执行命令结果
            if (out) {
                log.info("执行命令：{}完毕，执行结果：{}", cmd, result);
            } else {
                log.info("执行命令：{}完毕", cmd);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("执行命令失败：{}", e.getMessage());
            throw new BusinessException("命令执行失败");
        } finally {
            if (null != process) {
                ProcessKiller processKiller = new ProcessKiller(process);
                runtime.addShutdownHook(processKiller);
            }
        }
    }

    /**
     * 查询推出前结束已有的进程
     */
    private static class ProcessKiller extends Thread {
        private Process process;

        public ProcessKiller(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            this.process.destroy();
        }
    }

    /**
     * 获取线程执行过程中产生的输出流和错误流信息
     */
    private static class PrintStream extends Thread {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();

        public PrintStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                if (null == inputStream) {
                    return;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("读取输入流报错：{}", e.getMessage());
            } finally {
                try {
                    if (null != bufferedReader) {
                        bufferedReader.close();
                    }
                    if (null != inputStream) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    log.error("关闭流报错：{}", e.getMessage());
                }
            }
        }
    }
}
