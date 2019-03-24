/**
 * Alipay.com Inc. Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.image.region;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author tritone
 * @version $Id: DataTransferMain.java, v 0.1 2019年03月23日 23:26 tritone Exp $
 */
public class DataTransferMain {

    public static String countryDataTemplate = "{name: \"%s\", value: %s},\n";

    public static void main(String[] argv) throws IOException {
        File file = new File("src/main/resources/region/data.txt");
        DecimalFormat df = new DecimalFormat("#.0000");
        List<String> countrys = getCountryList();
        String dataStr = FileUtils.readFileToString(file);

        List<String> regionList = Arrays.asList(dataStr.split("\n"));
        regionList = regionList.subList(1, regionList.size());
        StringBuilder countryListDataSb = new StringBuilder();

        Double[] valueMax = {0.0};
        final Double[] valueMin = {9999999999.0};

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


            if(!countrys.contains(region)){
                return;
            }


            Double value = Double.valueOf(r.substring(numblerStart + 1, r.length()));

            if (value > valueMax[0]) {
                valueMax[0] = value;
            }

            if (value < valueMin[0]) {
                valueMin[0] = value;
            }
            countryListDataSb.append(String.format(countryDataTemplate, region, value));

        });

        System.out.println("MaxValue = " + df.format(valueMax[0]) + "\n");
        System.out.println("MinValue = " + df.format(valueMin[0]) + "\n");

        String worldHtml = FileUtils.readFileToString(new File(
                "src/main/resources/region/_world.html"));

        worldHtml = worldHtml.replace("\"COUNTRYLIST\"", countryListDataSb.toString());

        FileUtils.writeStringToFile(new File("src/main/resources/region/world.html"), worldHtml);

    }

    public static String countryConvert(String source) {
        if (source.equals("Russian Federation")) {
            return "Russia";
        }

        if(source.equals("South Korea")){
            return "Korea";
        }
        if(source.equals("North Korea")){
            return "Dem. Rep. Korea";
        }

        if(source.equals("Democratic Republic of the Congo")){
            return "Dem. Rep. Congo";
        }

        if(source.equals("South Sudan")){
            return "S. Sudan";
        }

        if(source.equals("Central African Republic")){
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
                String r2 = r.substring(0,r.length()-1);
                Country country = JSONObject.parseObject(r2,Country.class);
                result.add(country.getName());
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


}