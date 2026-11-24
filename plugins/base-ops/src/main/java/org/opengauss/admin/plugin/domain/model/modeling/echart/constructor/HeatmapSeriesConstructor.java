/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
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
