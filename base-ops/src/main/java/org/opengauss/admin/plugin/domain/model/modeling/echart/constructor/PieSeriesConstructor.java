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
import org.opengauss.admin.plugin.vo.modeling.component.Label;
import org.opengauss.admin.plugin.vo.modeling.component.LabelLine;
import org.opengauss.admin.plugin.vo.modeling.component.PieData;
import org.opengauss.admin.plugin.vo.modeling.component.PieSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Data
@EqualsAndHashCode(callSuper=true)
public class PieSeriesConstructor extends BaseSeriesConstructor {

    private final Indicator indicator;
    private final Dimension dimension;
    private final Dimension subDimension;

    private final List<Map<String, Object>> queryData;

    private String summaryType;
    private Boolean displayPercentage;

    private Map<String,List<Float>> seriesData;
    public ArrayList<String> seriesKey;

    public PieSeriesConstructor(Indicator indicator, Dimension dimension, Dimension subDimension, List<Map<String, Object>> queryData, Boolean displayPercentage) {
        this.indicator = indicator;
        this.dimension = dimension;
        this.subDimension = subDimension;
        this.displayPercentage = displayPercentage;

        this.queryData = queryData;
    }

    public List<PieSeries> getSeriesData() {

        PieSeries mainPieSeries = new PieSeries();

        //Initialize outer circle parameters
        mainPieSeries.setRadius(List.of("0%","60%"));
        mainPieSeries.setSelectedMode("single");
        mainPieSeries.setType("pie");
        mainPieSeries.setLabelLine(new LabelLine().setLength(30).setShow(true));
        if (displayPercentage != null && displayPercentage) {
            mainPieSeries.setLabel(new Label().setFormatter("{b}\n\n占比{d}%").setFontSize(12).setShow(true));
        }
        List<PieData> pieDataList = new ArrayList<>();
        List<PieData> subPieDataList = new ArrayList<>();

        setSeriesKey(new ArrayList<>());

        dimension.getCategoryBodyList().forEach(categoryBody -> {
            PieData pieData = new PieData();
            pieData.setName(categoryBody.getCategoryName());
            pieData.setValue(0f);
            pieDataList.add(dimension.getCategoryBodyList().indexOf(categoryBody),pieData);
            seriesKey.add(categoryBody.getCategoryName());
        });

        subDimension.getCategoryBodyList().forEach(categoryBody -> {
            PieData pieData = new PieData();
            pieData.setName(categoryBody.getCategoryName());
            pieData.setValue(0f);
            subPieDataList.add(subDimension.getCategoryBodyList().indexOf(categoryBody),pieData);
            seriesKey.add(categoryBody.getCategoryName());
        });

        queryData.forEach(item->{
            //Outer circle data
            String xValue = (String) item.get(dimension.getField());
            List<Integer> xIndexList = dimension.indexListByValue(xValue);
            float yValue = toFloat(String.valueOf(item.get(indicator.getField())));

            if (xIndexList.size() > 0)
            {
                //If the custom dimension contains multiple categories that overlap, the total will be greater than 100%, which needs to be considered
                xIndexList.forEach(xIndex -> {
                    pieDataList.get(xIndex).addValue(yValue);
                });
            }
            //Inner circle data
            String subValue = (String) item.get(subDimension.getField());
            List<Integer> subIndexList = subDimension.indexListByValue(subValue);
            if (subIndexList.size() > 0)
            {
                subIndexList.forEach(subIndex -> {
                    subPieDataList.get(subIndex).addValue(yValue);
                });
            }
        });
        mainPieSeries.setData(pieDataList);

        List<PieSeries> result = new ArrayList<>();

        if (subPieDataList.size() > 0) {
            mainPieSeries.setRadius(List.of("45%","60%"));
            PieSeries subPieSeries = new PieSeries();

            Label nl = new Label().setPosition("inner").setFontSize(11).setShow(true);
            if (displayPercentage != null && displayPercentage) {
                nl.setFormatter("{b}\n\n占比{d}%");
            }
            subPieSeries.setLabel(nl);
            subPieSeries.setRadius(List.of("0","30%"));
            subPieSeries.setSelectedMode("single");
            subPieSeries.setType("pie");
            subPieSeries.setLabelLine(new LabelLine().setShow(false));
            subPieSeries.setData(subPieDataList);
            result.add(subPieSeries);
        }
        result.add(mainPieSeries);

        return result;
    }

}
