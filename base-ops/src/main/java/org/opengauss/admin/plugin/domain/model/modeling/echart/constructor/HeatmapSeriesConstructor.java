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
import org.opengauss.admin.plugin.vo.modeling.Dimension;
import org.opengauss.admin.plugin.vo.modeling.Indicator;
import org.opengauss.admin.plugin.vo.modeling.component.Emphasis;
import org.opengauss.admin.plugin.vo.modeling.component.HeatmapSeries;
import org.opengauss.admin.plugin.vo.modeling.component.ItemStyle;
import org.opengauss.admin.plugin.vo.modeling.component.Label;

import java.util.*;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Data
@EqualsAndHashCode(callSuper=true)
public class HeatmapSeriesConstructor extends BaseSeriesConstructor {

    private final Indicator indicator;
    private final Dimension xDimension;
    private final Dimension yDimension;

    private final List<Map<String, Object>> queryData;

    private String summaryType;

    private Map<String,List<Float>> seriesData;
    public ArrayList<String> seriesKey;

    public HeatmapSeriesConstructor(Indicator indicator, Dimension xDimension,Dimension yDimension, List<Map<String, Object>> queryData) {
        this.indicator = indicator;
        this.xDimension = xDimension;
        this.yDimension = yDimension;
        this.queryData = queryData;
    }

    public List<HeatmapSeries> getSeriesData() {

        HeatmapSeries heatmapSeries = new HeatmapSeries();
        heatmapSeries.setType("heatmap");
        heatmapSeries.setLabel(new Label().setShow(true));
        heatmapSeries.setEmphasis(new Emphasis().setItemStyle(new ItemStyle().setShadowBlur(10).setShadowColor("rgba(0, 0, 0, 0.5)")));

        HashMap<List<Integer>,Float> result = new HashMap<>();

        queryData.forEach(item->{
            //X axis
            String xValue = (String) item.get(xDimension.getField());
            List<Integer> xIndexList = xDimension.indexListByValue(xValue);
            //Y axis
            String yValue = (String) item.get(yDimension.getField());
            List<Integer> yIndexList = yDimension.indexListByValue(yValue);

            if (xIndexList.size() > 0 && yIndexList.size() > 0)
            {
                xIndexList.forEach(xIndex->{
                    yIndexList.forEach(yIndex->{
                        List<Integer> target = List.of(xIndex,yIndex);
                        float heatValue = toFloat(String.valueOf(item.get(indicator.getField())));

                        if (result.containsKey(List.of(xIndex,yIndex))) {
                            result.replace(target,result.get(target) + heatValue);
                        } else {
                            result.put(target,heatValue);
                        }
                    });
                });
            }

        });

        Iterator<Map.Entry<List<Integer>, Float>> entries = result.entrySet().iterator();
        List<List<Object>> finalResult = new ArrayList<>();

        while (entries.hasNext()) {
            Map.Entry<List<Integer>, Float> entry = entries.next();
            finalResult.add(List.of(entry.getKey().get(0),entry.getKey().get(1),entry.getValue()));
        }

        heatmapSeries.setData(finalResult);

        return List.of(heatmapSeries);
    }

}
