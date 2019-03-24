/**
 * Alipay.com Inc. Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.image.region;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author tritone
 * @version $Id: DataTransferMain.java, v 0.1 2019年03月23日 23:26 tritone Exp $
 */
public class DataTransferV2Main {

    // 下面这个值代表组数，比如 =8，就是160个国家分成8组，每组20个国家一个颜色
    public static int groupNum = 20;

    public static Double percentRed = 0.10;

    public static String countryDataTemplate = "{name: \"%s\", value: %s},\n";

    public static String colorTemplate = "{start: %s, end:%s}";

    public static void main(String[] argv) throws IOException {
        File file = new File("src/main/resources/region/data.txt");
        DecimalFormat df = new DecimalFormat("#.0000");
        List<String> countrys = getCountryList();
        String dataStr = FileUtils.readFileToString(file);

        List<String> regionList = Arrays.asList(dataStr.split("\n"));
        regionList = regionList.subList(1, regionList.size());
        StringBuilder countryListDataSb = new StringBuilder();

        StringBuilder colorSb = new StringBuilder();

        Double[] valueMax = {0.0};
        List<Double> values = new ArrayList<>();

        final Double[] valueMin = {9999999999.0};

        List<Double> finalValues = values;
        regionList.stream().forEach(r -> {

            int numblerStart = r.lastIndexOf(",");
            String region = r.substring(0, numblerStart);

            if (region.charAt(0) == '\"') {
                region = region.substring(1, region.length());
            }
            if (region.charAt(region.length() - 1) == '\"') {
                region = region.substring(0, region.length() - 1);
            }

            region = countryConvert(region);

            if (!countrys.contains(region)) {
                return;
            }

            Double value = Double.valueOf(r.substring(numblerStart + 1, r.length()));

            finalValues.add(value);
            if (value > valueMax[0]) {
                valueMax[0] = value;
            }
            if (value < valueMin[0]) {
                valueMin[0] = value;
            }
            countryListDataSb.append(String.format(countryDataTemplate, region, value));
        });

        values.sort(new Comparator<Double>() {
            @Override
            public int compare(Double r1, Double r2) {
                if (r1 > r2) {
                    return 1;
                }
                if (r2 > r1) {
                    return -1;
                }
                return 0;
            }
        });

        for (int i = 0; i < groupNum; i++) {
            int index = values.size() / groupNum * i;
            int indexNext = values.size() / groupNum * (i + 1);

            Integer indexValue = i == 0 ? 0 : values.get(index).intValue() / 100 * 100;
            Integer indexNextValue = i == groupNum - 1 ? values.get(values.size() - 1).intValue() / 100 * 100 + 100 : values.get(indexNext)
                    .intValue()
                    / 100 * 100;

            colorSb.append(String.format(colorTemplate, indexValue, indexNextValue));
            colorSb.append(",");
        }

        Double percentValue = values.get(Double.valueOf(1.0 * percentRed * 169).intValue());

        System.out.println("MaxValue = " + df.format(valueMax[0]) + "\n");
        System.out.println("MinValue = " + df.format(valueMin[0]) + "\n");

        String worldHtml = FileUtils.readFileToString(new File(
                "src/main/resources/region/_V2world.html"));

        //worldHtml = worldHtml.replace("9999999999",String.valueOf(percentV2Value.intValue()));

        worldHtml = worldHtml.replace("\"COLORLIST\"", colorSb.toString());
        worldHtml = worldHtml.replace("\"COUNTRYLIST\"", countryListDataSb.toString());

        FileUtils.writeStringToFile(new File("src/main/resources/region/world.html"), worldHtml);

    }

    public static String countryConvert(String source) {
        if (source.equals("Russian Federation")) {
            return "Russia";
        }

        if (source.equals("South Korea")) {
            return "Korea";
        }
        if (source.equals("North Korea")) {
            return "Dem. Rep. Korea";
        }

        if (source.equals("Democratic Republic of the Congo")) {
            return "Dem. Rep. Congo";
        }

        if (source.equals("South Sudan")) {
            return "S. Sudan";
        }

        if (source.equals("Central African Republic")) {
            return "Central African Rep.";
        }

        //if(source.equals("United States")){
        //    return "United States of America";
        //}

        return source;
    }

    public static List<String> getCountryList() {
        List<String> result = new ArrayList<>();
        try {
            String countryStr = FileUtils.readFileToString(new File("src/main/resources/region/country.txt"));

            //List<Country> countries = JSONArray.parseArray(countryStr,Country.class);

            Arrays.asList(countryStr.split("\n")).stream().forEach(r -> {
                String r2 = r.substring(0, r.length() - 1);
                Country country = JSONObject.parseObject(r2, Country.class);
                result.add(country.getName());
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

}