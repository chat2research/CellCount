/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.image;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import org.apache.commons.io.FileUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ij.measure.Measurements.CENTER_OF_MASS;
import static ij.plugin.filter.ParticleAnalyzer.DISPLAY_SUMMARY;
import static ij.plugin.filter.ParticleAnalyzer.SHOW_OUTLINES;
import static ij.process.ImageProcessor.RED_LUT;

/**
 * @author tritone
 * @version $Id: ImageMain.java, v 0.1 2018年12月26日 23:36 tritone Exp $
 */
public class ImageMain {
    public static void main(String[] argv) throws IOException {

        //调用静态函数 work，输入为 待处理的文件夹

        work("src/main/resources/T1 100ng");
    }

    public static ArrayList<File> getFiles(String directory) {
        ArrayList<File> files = new ArrayList<File>();
        File file = new File(directory);
        File[] tempList = file.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                if (tempList[i].getName().endsWith("DS_Store")) {
                    continue;
                }
                files.add(tempList[i]);
            }
            if (tempList[i].isDirectory()) {
                files.addAll(getFiles(tempList[i].getPath()));
            }
        }
        return files;
    }

    /**
     * 处理的主要逻辑
     * 1.图片->转为灰度 -> 做gama灰度拉伸(ImageUtil.gammaProcess) -> 确定阈值 ->
     * 使用IJ 组件的 ParticleAnalyzer 进行细胞个数获取
     *
     * @param filePath
     * @param display
     * @return
     */
    public static Integer getCellCount(String filePath,boolean display) {
        ResultsTable resultsTable = new ResultsTable();

        ImagePlus imp = IJ.openImage(filePath);
        File file = new File(filePath);
        ImageConverter ic = new ImageConverter(imp);
        ic.convertToGray8();
        imp.updateAndDraw();

        BufferedImage bufferedImage = imp.getBufferedImage();
        bufferedImage =  ImageUtil.gammaProcess(bufferedImage,0.9);

        imp.setImage(bufferedImage);
        ic.convertToGray8();
        imp.updateAndDraw();
        IJ.save(imp,file.getParent() + "/handle_1_"+file.getName());
        //imp.show();

        ImageProcessor imageProcessor = imp.getProcessor();
        imageProcessor.invert();
        imageProcessor.setThreshold(0, 235, RED_LUT);
        imageProcessor.autoThreshold();
        //imp.updateAndDraw();

        ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(display?SHOW_OUTLINES:DISPLAY_SUMMARY, CENTER_OF_MASS, resultsTable, 10, 200,0.5,1.0);
        particleAnalyzer.analyze(imp);

        IJ.save(IJ.getImage(), file.getParent() +"/handle_2_"+file.getName());
        IJ.save(imp, file.getParent() +"/handle_"+file.getName());
        IJ.getImage().close();

        return resultsTable.getCounter();
    }

    public static void work(String directory) throws IOException {
        List<File> files = getFiles(directory);
        String resultFileName =  directory.split("\\/")[directory.split("\\/").length-1]+".csv";
        StringBuilder sb = new StringBuilder();
        sb.append("组名,文件名,细胞个数\n");
        files.stream().forEach(file -> {
            Integer count = getCellCount(file.getAbsolutePath(),true);
            String group = getGroupName(file.getName());
            System.out.println(group + "," + file.getName() + "," + count);
            sb.append(group + "," + file.getName() + "," + count);
            sb.append("\n");
        });
        String outFilePath = "src/main/resources/"+resultFileName;
        FileUtils.deleteQuietly(new File(outFilePath));
        FileUtils.writeStringToFile(new File(outFilePath), sb.toString());
    }

    public static String getGroupName(String fileName) {
        return getGroupName2(fileName);
    }
    public static String getGroupName3(String fileName){
        return fileName.split("-")[0] + " " + fileName.split("-")[2];
    }
    public static String getGroupName2(String fileName) {
        return fileName.split("-")[0];
    }

}