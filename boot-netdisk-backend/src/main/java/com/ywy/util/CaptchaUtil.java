package com.ywy.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class CaptchaUtil {
    private int width = 160; // 图片的宽度
    private int height = 40; // 图片的高度
    private int codeCount = 4; // 验证码字符个数
    private int lineCount = 20; // 验证码干扰线数
    private String code; // 验证码

    private BufferedImage buffImg; // 图片Buffer
    Random random = new Random();

    public CaptchaUtil() {
        createCaptcha();
    }

    public CaptchaUtil(int width, int height) {
        this.width = width;
        this.height = height;
        createCaptcha();
    }

    public CaptchaUtil(int width, int height, int codeCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        createCaptcha();
    }

    public CaptchaUtil(int width, int height, int codeCount, int lineCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        this.lineCount = lineCount;
        createCaptcha();
    }

    /**
     * 生成验证码
     */
    private void createCaptcha() {
        int fontWidth = width / codeCount; // 字体的宽度
        int fontHeight = height - 5; // 字体的高度
        int codeY = height - 8;

        buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = buffImg.getGraphics();
        // 设置背景色
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        // 设置字体
        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        g.setFont(font);

        // 绘制干扰线
        for (int i = 0; i < lineCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = xs + random.nextInt(width);
            int ye = ys + random.nextInt(height);
            g.setColor(getRandColor(1, 255));
            g.drawLine(xs, ys, xe, ye);
        }

        // 绘制噪点
        float yawRate = 0.01f; // 噪声率
        int area = (int) (yawRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            buffImg.setRGB(x, y, random.nextInt(255));
        }

        // 绘制验证码
        code = randomStr(codeCount); // 随机生成字符串
        for (int i = 0; i < codeCount; i++) {
            String strRand = code.substring(i, i + 1);
            g.setColor(getRandColor(1, 255));
            g.drawString(strRand, i * fontWidth + 3, codeY);
        }
    }

    /**
     * 随机生成字符串
     *
     * @param n
     * @return
     */
    private String randomStr(int n) {
        String template = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String str = "";
        int len = template.length() - 1;
        double rand;
        for (int i = 0; i < n; i++) {
            rand = (Math.random()) * len;
            str = str + template.charAt((int) rand);
        }
        return str;
    }

    /**
     * 随机生成颜色
     *
     * @param c1
     * @param c2
     * @return
     */
    private Color getRandColor(int c1, int c2) {
        if (c1 > 255) c1 = 255;
        if (c2 > 255) c2 = 255;
        int r = c1 + random.nextInt(c2 - c1);
        int g = c1 + random.nextInt(c2 - c1);
        int b = c1 + random.nextInt(c2 - c1);
        return new Color(r, g, b);
    }

    public void write(OutputStream os) throws IOException {
        ImageIO.write(buffImg, "png", os);
        os.close();
    }

    public String getCode() {
        return code.toLowerCase();
    }
}
