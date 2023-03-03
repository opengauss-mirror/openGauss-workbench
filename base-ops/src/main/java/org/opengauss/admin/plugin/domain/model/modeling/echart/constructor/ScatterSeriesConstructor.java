/**
 Copyright  (c) 2020 Huawei Technologies Co.,Ltd.
 Copyright  (c) 2021 openGauss Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.opengauss.admin.plugin.domain.model.modeling.echart.constructor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.plugin.vo.modeling.Indicator;
import org.opengauss.admin.plugin.vo.modeling.component.Encode;
import org.opengauss.admin.plugin.vo.modeling.component.Location;
import org.opengauss.admin.plugin.vo.modeling.component.ScatterSeries;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Data
@EqualsAndHashCode(callSuper=true)
public class ScatterSeriesConstructor extends BaseSeriesConstructor {

    private final Indicator indicator;
    private final Location location;
    private final int highLight;

    private final List<Map<String, Object>> queryData;

    private String summaryType;

    private Map<String,List<Float>> seriesData;
    public ArrayList<String> seriesKey;
    public int maxValue;

    public ScatterSeriesConstructor(Indicator indicator, Location location, List<Map<String, Object>> queryData, int highLight) {
        this.indicator = indicator;
        this.location = location;
        this.queryData = queryData;
        this.highLight = highLight;
    }

    public List<ScatterSeries> getSeriesData()  {

        Map<String,Float> seriesTemp = new HashMap<>();
        queryData.forEach(item->{
            String loc = "";
            Object field = item.get(location.getField());
            if (field != null) {
                loc = ((String) field).trim();
            }
            float yValue = toFloat(String.valueOf(item.get(indicator.getField())));
            if (seriesTemp.containsKey(loc)) {
                seriesTemp.replace(loc,seriesTemp.get(loc) + yValue);
            } else {
                seriesTemp.put(loc,yValue);
            }
        });

        List<Map.Entry<String, Float>> list = new ArrayList<>(seriesTemp.entrySet());

        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        List<String> topLoc = list.stream()
                .limit(highLight)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        maxValue = (int) Math.ceil(list.get(0).getValue());

        //Format
        List<Map<String,String>> seriesResult = new ArrayList<>();
        seriesTemp.forEach( (key,var) -> {
            if (key != null) {
                if (topLoc.contains(key)) {
                    seriesResult.add(Map.of("name", key, "value", var.toString(),"selected","true"));
                } else {
                    seriesResult.add(Map.of("name", key, "value", var.toString()));
                }
            }
        });

        ScatterSeries scatterSeries = new ScatterSeries();
        scatterSeries.setMap("china");
        scatterSeries.setType("map");
        scatterSeries.setRoam(true);
        scatterSeries.setData(seriesResult);
        scatterSeries.setEncode(new Encode().setValue(2));
        scatterSeries.setGeoIndex(0);
        scatterSeries.setSelectedMode("multiple");

        return List.of(scatterSeries);
    }

}
