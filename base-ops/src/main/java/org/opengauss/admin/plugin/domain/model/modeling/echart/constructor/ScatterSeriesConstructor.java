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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Data
@EqualsAndHashCode(callSuper=true)
public class ScatterSeriesConstructor extends BaseSeriesConstructor {

    private final Indicator indicator;
    private final Location location;

    private final List<Map<String, Object>> queryData;

    private String summaryType;

    private Map<String,List<Float>> seriesData;
    public ArrayList<String> seriesKey;

    public ScatterSeriesConstructor(Indicator indicator, Location location, List<Map<String, Object>> queryData) {
        this.indicator = indicator;
        this.location = location;
        this.queryData = queryData;
    }

    public List<ScatterSeries> getSeriesData()  {

        Map<String,Float> seriesTemp = new HashMap<>();
        queryData.forEach(item->{
            String loc = (String) item.get(location.getField());
            float yValue = Float.parseFloat(String.valueOf(item.get(indicator.getField())));
            if (seriesTemp.containsKey(loc)) {
                seriesTemp.replace(loc,seriesTemp.get(loc) + yValue);
            } else {
                seriesTemp.put(loc,yValue);
            }
        });
        System.out.println(seriesTemp);

        //Format
        List<Map<String,String>> seriesResult = new ArrayList<>();
        seriesTemp.forEach( (key,var) -> {
            if (key != null) {
                seriesResult.add(Map.of("name", key, "value", var.toString()));
            }
        });

        ScatterSeries scatterSeries = new ScatterSeries();
        scatterSeries.setMap("china");
        scatterSeries.setType("map");
        scatterSeries.setRoam(true);
        scatterSeries.setData(seriesResult);
        scatterSeries.setEncode(new Encode().setValue(2));
        scatterSeries.setGeoIndex(0);

        return List.of(scatterSeries);
    }

}
