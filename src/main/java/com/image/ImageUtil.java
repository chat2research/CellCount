/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.image;

import java.awt.image.BufferedImage;

/**
 * @author tritone
 * @version $Id: ImageUtil.java, v 0.1 2019年01月02日 11:40 tritone Exp $
 */
public class ImageUtil {

    /** 拉普拉斯叠加原图像 **/
    public  static BufferedImage laplaceAddProcess(BufferedImage src) {

        // 拉普拉斯算子
        int[] LAPLACE = new int[] { 0, -1, 0, -1, 4, -1, 0, -1, 0 };

        int width = src.getWidth();
        int height = src.getHeight();

        int[] pixels = new int[width * height];
        int[] outPixels = new int[width * height];

        int type = src.getType();
        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            src.getRaster().getDataElements(0, 0, width, height, pixels);
        }
        src.getRGB(0, 0, width, height, pixels, 0, width);

        int k0 = 0, k1 = 0, k2 = 0;
        int k3 = 0, k4 = 0, k5 = 0;
        int k6 = 0, k7 = 0, k8 = 0;

        k0 = LAPLACE[0];
        k1 = LAPLACE[1];
        k2 = LAPLACE[2];
        k3 = LAPLACE[3];
        k4 = LAPLACE[4];
        k5 = LAPLACE[5];
        k6 = LAPLACE[6];
        k7 = LAPLACE[7];
        k8 = LAPLACE[8];
        int offset = 0;

        int sr = 0, sg = 0, sb = 0;
        int r = 0, g = 0, b = 0;
        for (int row = 1; row < height - 1; row++) {
            offset = row * width;
            for (int col = 1; col < width - 1; col++) {

                r = (pixels[offset + col] >> 16) & 0xff;
                g = (pixels[offset + col] >> 8) & 0xff;
                b = (pixels[offset + col]) & 0xff;
                // red
                sr = k0 * ((pixels[offset - width + col - 1] >> 16) & 0xff)
                        + k1 * ((pixels[offset - width + col] >> 16) & 0xff)
                        + k2
                        * ((pixels[offset - width + col + 1] >> 16) & 0xff)
                        + k3 * ((pixels[offset + col - 1] >> 16) & 0xff) + k4
                        * ((pixels[offset + col] >> 16) & 0xff) + k5
                        * ((pixels[offset + col + 1] >> 16) & 0xff) + k6
                        * ((pixels[offset + width + col - 1] >> 16) & 0xff)
                        + k7 * ((pixels[offset + width + col] >> 16) & 0xff)
                        + k8
                        * ((pixels[offset + width + col + 1] >> 16) & 0xff);
                // green
                sg = k0 * ((pixels[offset - width + col - 1] >> 8) & 0xff) + k1
                        * ((pixels[offset - width + col] >> 8) & 0xff) + k2
                        * ((pixels[offset - width + col + 1] >> 8) & 0xff) + k3
                        * ((pixels[offset + col - 1] >> 8) & 0xff) + k4
                        * ((pixels[offset + col] >> 8) & 0xff) + k5
                        * ((pixels[offset + col + 1] >> 8) & 0xff) + k6
                        * ((pixels[offset + width + col - 1] >> 8) & 0xff) + k7
                        * ((pixels[offset + width + col] >> 8) & 0xff) + k8
                        * ((pixels[offset + width + col + 1] >> 8) & 0xff);
                // blue
                sb = k0 * (pixels[offset - width + col - 1] & 0xff) + k1
                        * (pixels[offset - width + col] & 0xff) + k2
                        * (pixels[offset - width + col + 1] & 0xff) + k3
                        * (pixels[offset + col - 1] & 0xff) + k4
                        * (pixels[offset + col] & 0xff) + k5
                        * (pixels[offset + col + 1] & 0xff) + k6
                        * (pixels[offset + width + col - 1] & 0xff) + k7
                        * (pixels[offset + width + col] & 0xff) + k8
                        * (pixels[offset + width + col + 1] & 0xff);
                // 运算后的像素值和原图像素叠加
                r += sr;
                g += sg;
                b += sb;
                outPixels[offset + col] = (0xff << 24) | (clamp(r) << 16)
                        | (clamp(g) << 8) | clamp(b);

                // next pixel
                r = 0;
                g = 0;
                b = 0;
            }
        }

        BufferedImage dest = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            dest.getRaster().setDataElements(0, 0, width, height, outPixels);
        } else {
            dest.setRGB(0, 0, width, height, outPixels, 0, width);
        }
        return dest;
    }

    /** 伽马变化 */
    public  static BufferedImage gammaProcess(BufferedImage src,Double gamma) {

        BufferedImage image = mathProcess(src);

        if(gamma==null){
            gamma = 0.5;
        }

        int type = image.getType();
        int width = src.getWidth();
        int height = src.getHeight();

        // 经过数学变换后的像素信息
        int[] pixels = new int[width * height];
        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            image.getRaster().getDataElements(0, 0, width, height, pixels);
        }
        image.getRGB(0, 0, width, height, pixels, 0, width);

        int[] outPixels = new int[width * height];

        // 建立LUT查找表
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {

            float f = (float) (i / 255.0);
            f = (float) Math.pow(f, gamma);

            lut[i] = (int) (f * 255.0);
        }

        int r = 0, g = 0, b = 0;
        int or = 0, og = 0, ob = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {

                int pixel = pixels[row * width + col];

                r = (pixel >> 16) & 0XFF;
                g = (pixel >> 8) & 0XFF;
                b = (pixel) & 0XFF;

                or = lut[r];
                og = lut[g];
                ob = lut[b];

                outPixels[row * width + col] = (255 << 24) | (clamp(or) << 16)
                        | (clamp(og) << 8) | (clamp(ob));

            }
        }

        BufferedImage dest = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            dest.getRaster().setDataElements(0, 0, width, height, outPixels);
        } else {
            dest.setRGB(0, 0, width, height, outPixels, 0, width);
        }

        return dest;
    }


    public static  int clamp(int value) {
        return value > 255 ? 255 : (value < 0 ? 0 : value);
    }

    /** 数学运算*/
    public  static BufferedImage mathProcess(BufferedImage src) {

        // 获取经拉普拉斯运算后与原图叠加的图片
        BufferedImage lapsImage = laplaceAddProcess(src);

        // 获取索贝尔5*5均值滤波后的图像
        BufferedImage meanImage = meanValueProcess(src);

        int type = src.getType();
        int width = src.getWidth();
        int height = src.getHeight();

        // 原始图像的像素信息
        int[] pixels = new int[width * height];
        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            src.getRaster().getDataElements(0, 0, width, height, pixels);
        }
        src.getRGB(0, 0, width, height, pixels, 0, width);

        // 拉普拉斯锐化后的像素信息
        int[] lapsPixels = new int[width * height];
        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            lapsImage.getRaster().getDataElements(0, 0, width, height,
                    lapsPixels);
        }
        lapsImage.getRGB(0, 0, width, height, lapsPixels, 0, width);

        // Sobel和均值滤波后的像素信息
        int[] meanPixels = new int[width * height];
        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            meanImage.getRaster().getDataElements(0, 0, width, height,
                    meanPixels);
        }
        meanImage.getRGB(0, 0, width, height, meanPixels, 0, width);

        int[] outPixels = new int[width * height];

        // 图像相乘
        int lr = 0, lg = 0, lb = 0;
        int mr = 0, mg = 0, mb = 0;
        int or = 0, og = 0, ob = 0;
        int r = 0, g = 0, b = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int lpixel = lapsPixels[row * width + col];
                int mpixel = meanPixels[row * width + col];

                // 原始图像
                int opixel = pixels[row * width + col];

                lr = (lpixel >> 16) & 0XFF;
                mr = (mpixel >> 16) & 0XFF;
                or = (opixel >> 16) & 0XFF;

                lg = (lpixel >> 8) & 0XFF;
                mg = (mpixel >> 8) & 0XFF;
                og = (opixel >> 8) & 0XFF;

                lb = (lpixel) & 0XFF;
                mb = (mpixel) & 0XFF;
                ob = (opixel) & 0XFF;

                /** 图像相乘 标定到0~255 */
                r = (lr * mr) / 255;
                g = (lg * mg) / 255;
                b = (lb * mb) / 255;

                // 相乘后图像与原图相加
                r = r + or;
                g = g + og;
                b = b + ob;

                outPixels[row * width + col] = (255 << 24) | (clamp(r) << 16)
                        | (clamp(g) << 8) | (clamp(b));
            }
        }

        BufferedImage dest = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            dest.getRaster().setDataElements(0, 0, width, height, outPixels);
        } else {
            dest.setRGB(0, 0, width, height, outPixels, 0, width);
        }


        return dest;
    }

    /** 均值滤波 **/
    public static  BufferedImage meanValueProcess(BufferedImage src) {

        BufferedImage image = sobelProcess(src);// 已经索贝尔处理的图像

        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        int[] outPixels = new int[width * height];

        int type = image.getType();
        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            image.getRaster().getDataElements(0, 0, width, height, pixels);
        }
        image.getRGB(0, 0, width, height, pixels, 0, width);

        // 均值滤波使用的卷积模板半径，这里使用5*5均值，所以半径使用2
        int radius = 2;
        int total = (2 * radius + 1) * (2 * radius + 1);

        int r = 0, g = 0, b = 0;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int sum = 0;
                for (int i = -radius; i <= radius; i++) {
                    int roffset = row + i;
                    roffset = (roffset < 0) ? 0
                            : (roffset >= height ? height - 1 : roffset);

                    for (int j = -radius; j <= radius; j++) {

                        int coffset = col + j;
                        coffset = (coffset < 0) ? 0
                                : (coffset >= width ? width - 1 : coffset);

                        int pixel = pixels[roffset * width + coffset];

                        r = (pixel >> 16) & 0XFF;

                        sum += r;
                    }
                }

                r = sum / total;
                g = sum / total;
                b = sum / total;

                outPixels[row * width + col] = (255 << 24) | (clamp(r) << 16)
                        | (clamp(g) << 8) | clamp(b);
            }
        }

        BufferedImage dest = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            dest.getRaster().setDataElements(0, 0, width, height, outPixels);
        } else {
            dest.setRGB(0, 0, width, height, outPixels, 0, width);
        }

        return dest;
    }

    public static BufferedImage sobelProcess(BufferedImage src) {

        // Sobel算子
        int[] sobel_y = new int[] { -1, -2, -1, 0, 0, 0, 1, 2, 1 };
        int[] sobel_x = new int[] { -1, 0, 1, -2, 0, 2, -1, 0, 1 };

        int width = src.getWidth();
        int height = src.getHeight();

        int[] pixels = new int[width * height];
        int[] outPixels = new int[width * height];

        int type = src.getType();
        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            src.getRaster().getDataElements(0, 0, width, height, pixels);
        }
        src.getRGB(0, 0, width, height, pixels, 0, width);

        int offset = 0;
        int x0 = sobel_x[0];
        int x1 = sobel_x[1];
        int x2 = sobel_x[2];
        int x3 = sobel_x[3];
        int x4 = sobel_x[4];
        int x5 = sobel_x[5];
        int x6 = sobel_x[6];
        int x7 = sobel_x[7];
        int x8 = sobel_x[8];

        int k0 = sobel_y[0];
        int k1 = sobel_y[1];
        int k2 = sobel_y[2];
        int k3 = sobel_y[3];
        int k4 = sobel_y[4];
        int k5 = sobel_y[5];
        int k6 = sobel_y[6];
        int k7 = sobel_y[7];
        int k8 = sobel_y[8];

        int yr = 0, yg = 0, yb = 0;
        int xr = 0, xg = 0, xb = 0;
        int r = 0, g = 0, b = 0;

        for (int row = 1; row < height - 1; row++) {
            offset = row * width;
            for (int col = 1; col < width - 1; col++) {

                // red
                yr = k0 * ((pixels[offset - width + col - 1] >> 16) & 0xff)
                        + k1 * ((pixels[offset - width + col] >> 16) & 0xff)
                        + k2
                        * ((pixels[offset - width + col + 1] >> 16) & 0xff)
                        + k3 * ((pixels[offset + col - 1] >> 16) & 0xff) + k4
                        * ((pixels[offset + col] >> 16) & 0xff) + k5
                        * ((pixels[offset + col + 1] >> 16) & 0xff) + k6
                        * ((pixels[offset + width + col - 1] >> 16) & 0xff)
                        + k7 * ((pixels[offset + width + col] >> 16) & 0xff)
                        + k8
                        * ((pixels[offset + width + col + 1] >> 16) & 0xff);

                xr = x0 * ((pixels[offset - width + col - 1] >> 16) & 0xff)
                        + x1 * ((pixels[offset - width + col] >> 16) & 0xff)
                        + x2
                        * ((pixels[offset - width + col + 1] >> 16) & 0xff)
                        + x3 * ((pixels[offset + col - 1] >> 16) & 0xff) + x4
                        * ((pixels[offset + col] >> 16) & 0xff) + x5
                        * ((pixels[offset + col + 1] >> 16) & 0xff) + x6
                        * ((pixels[offset + width + col - 1] >> 16) & 0xff)
                        + x7 * ((pixels[offset + width + col] >> 16) & 0xff)
                        + x8
                        * ((pixels[offset + width + col + 1] >> 16) & 0xff);

                // green
                yg = k0 * ((pixels[offset - width + col - 1] >> 8) & 0xff) + k1
                        * ((pixels[offset - width + col] >> 8) & 0xff) + k2
                        * ((pixels[offset - width + col + 1] >> 8) & 0xff) + k3
                        * ((pixels[offset + col - 1] >> 8) & 0xff) + k4
                        * ((pixels[offset + col] >> 8) & 0xff) + k5
                        * ((pixels[offset + col + 1] >> 8) & 0xff) + k6
                        * ((pixels[offset + width + col - 1] >> 8) & 0xff) + k7
                        * ((pixels[offset + width + col] >> 8) & 0xff) + k8
                        * ((pixels[offset + width + col + 1] >> 8) & 0xff);

                xg = x0 * ((pixels[offset - width + col - 1] >> 8) & 0xff) + x1
                        * ((pixels[offset - width + col] >> 8) & 0xff) + x2
                        * ((pixels[offset - width + col + 1] >> 8) & 0xff) + x3
                        * ((pixels[offset + col - 1] >> 8) & 0xff) + x4
                        * ((pixels[offset + col] >> 8) & 0xff) + x5
                        * ((pixels[offset + col + 1] >> 8) & 0xff) + x6
                        * ((pixels[offset + width + col - 1] >> 8) & 0xff) + x7
                        * ((pixels[offset + width + col] >> 8) & 0xff) + x8
                        * ((pixels[offset + width + col + 1] >> 8) & 0xff);
                // blue
                yb = k0 * (pixels[offset - width + col - 1] & 0xff) + k1
                        * (pixels[offset - width + col] & 0xff) + k2
                        * (pixels[offset - width + col + 1] & 0xff) + k3
                        * (pixels[offset + col - 1] & 0xff) + k4
                        * (pixels[offset + col] & 0xff) + k5
                        * (pixels[offset + col + 1] & 0xff) + k6
                        * (pixels[offset + width + col - 1] & 0xff) + k7
                        * (pixels[offset + width + col] & 0xff) + k8
                        * (pixels[offset + width + col + 1] & 0xff);

                xb = x0 * (pixels[offset - width + col - 1] & 0xff) + x1
                        * (pixels[offset - width + col] & 0xff) + x2
                        * (pixels[offset - width + col + 1] & 0xff) + x3
                        * (pixels[offset + col - 1] & 0xff) + x4
                        * (pixels[offset + col] & 0xff) + x5
                        * (pixels[offset + col + 1] & 0xff) + x6
                        * (pixels[offset + width + col - 1] & 0xff) + x7
                        * (pixels[offset + width + col] & 0xff) + x8
                        * (pixels[offset + width + col + 1] & 0xff);

                // 索贝尔梯度
                r = (int) Math.sqrt(yr * yr + xr * xr);
                g = (int) Math.sqrt(yg * yg + xg * xg);
                b = (int) Math.sqrt(yb * yb + xb * xb);

                outPixels[offset + col] = (0xff << 24) | (clamp(r) << 16)
                        | (clamp(g) << 8) | clamp(b);
            }
        }

        BufferedImage dest = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);

        if (type == BufferedImage.TYPE_INT_ARGB
                || type == BufferedImage.TYPE_INT_RGB) {
            dest.getRaster().setDataElements(0, 0, width, height, outPixels);
        } else {
            dest.setRGB(0, 0, width, height, outPixels, 0, width);
        }
        return dest;

    }

}