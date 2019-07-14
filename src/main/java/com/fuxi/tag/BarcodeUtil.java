package com.fuxi.tag;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import javax.imageio.ImageIO;
import org.jbarcode.JBarcode;
import org.jbarcode.encode.EAN8Encoder;
import org.jbarcode.paint.EAN8TextPainter;
import org.jbarcode.paint.WidthCodedPainter;
import org.jbarcode.util.ImageUtil;

/**
 * 支持EAN13, EAN8, UPCA, UPCE, Code 3 of 9, Codabar, Code 11, Code 93, Code 128, MSI/Plessey,
 * Interleaved 2 of PostNet等 利用jbarcode生成各种条形码
 */
public class BarcodeUtil {

    public static void main(String[] paramArrayOfString) {
        try {

            JBarcode localJBarcode = new JBarcode(EAN8Encoder.getInstance(), WidthCodedPainter.getInstance(), EAN8TextPainter.getInstance());
            String str = "2219644";
            BufferedImage image2 = localJBarcode.createBarcode(str);
            BufferedImage image1 = createImage(str, new Font("Times New Roman", Font.ITALIC, 18), 100, 100);
            BufferedImage image = mergeImages(image1, image2);
            saveToFile(image, "EAN8.jpg", ImageUtil.JPEG);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public static BufferedImage mergeImages(BufferedImage image1, BufferedImage image2) {
        BufferedImage combined = new BufferedImage(image1.getWidth() * 2, image1.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = combined.getGraphics();
        g.drawImage(image1, 0, 0, null);
        g.drawImage(image2, image1.getWidth(), 0, null);
        return combined;
    }

    static void saveToJPEG(BufferedImage paramBufferedImage, String paramString) {
        saveToFile(paramBufferedImage, paramString, "jpeg");
    }

    static void saveToFile(BufferedImage paramBufferedImage, String paramString1, String paramString2) {
        try {
            FileOutputStream localFileOutputStream = new FileOutputStream("C:\\Users\\Administrator\\Desktop/" + paramString1);
            ImageUtil.encodeAndWrite(paramBufferedImage, paramString2, localFileOutputStream, 96, 96);
            localFileOutputStream.close();
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public static BufferedImage createImage(String str, Font font, int width, int height) throws Exception {
        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics();
        g.setClip(0, 0, width, height);
        g.setColor(Color.black);
        // g.fillRect(0, 0, width, height);// 先用黑色填充整张图片,也就是背景
        // g.setColor(Color.red);// 在换成黑色
        g.setFont(font);// 设置画笔字体
        /** 用于获得垂直居中y */
        Rectangle clip = g.getClipBounds();
        FontMetrics fm = g.getFontMetrics(font);
        int ascent = fm.getAscent();
        int descent = fm.getDescent();
        int y = (clip.height - (ascent + descent)) / 2 + ascent;
        for (int i = 0; i < 6; i++) {// 256 340 0 680
            g.drawString(str, i * 680, y);// 画出字符串
        }
        g.dispose();
        return image;
    }

}
