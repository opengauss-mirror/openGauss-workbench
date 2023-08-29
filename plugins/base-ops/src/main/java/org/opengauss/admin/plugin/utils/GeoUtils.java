/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * GeoUtils.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/utils/GeoUtils.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author LZW
 */
public class GeoUtils {

    public static double[] getGeoJsonCenter(String geoJson) {
        JSONObject geoJsonObject = JSONObject.parseObject(geoJson);
        JSONArray features = geoJsonObject.getJSONArray("features");

        double sumX = 0;
        double sumY = 0;
        int count = 0;

        for (int i = 0; i < features.size(); i++) {
            JSONObject feature = features.getJSONObject(i);
            JSONObject geometry = feature.getJSONObject("geometry");
            if (geometry.getString("type").equals("Polygon")) {
                JSONArray coordinates = geometry.getJSONArray("coordinates").getJSONArray(0);
                for (int j = 0; j < coordinates.size(); j++) {
                    JSONArray point = coordinates.getJSONArray(j);
                    sumX += point.getDoubleValue(0);
                    sumY += point.getDoubleValue(1);
                    count++;
                }
            } else if (geometry.getString("type").equals("MultiPolygon")) {
                JSONArray polygons = geometry.getJSONArray("coordinates");
                for (int j = 0; j < polygons.size(); j++) {
                    JSONArray coordinates = polygons.getJSONArray(j).getJSONArray(0);
                    for (int k = 0; k < coordinates.size(); k++) {
                        JSONArray point = coordinates.getJSONArray(k);
                        sumX += point.getDoubleValue(0);
                        sumY += point.getDoubleValue(1);
                        count++;
                    }
                }
            }
        }

        double centerX = sumX / count;
        double centerY = sumY / count;

        return new double[]{centerX, centerY};
    }
}
