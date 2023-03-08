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
import org.opengauss.admin.plugin.vo.modeling.CategoryBody;
import org.opengauss.admin.plugin.vo.modeling.Dimension;
import org.opengauss.admin.plugin.vo.modeling.Indicator;

import java.util.*;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Data
@EqualsAndHashCode(callSuper=true)
public class BarSeriesConstructor extends BaseSeriesConstructor {
    private final Dimension xDimension;

    private final List<Indicator> indicators;
    private final List<Dimension> dimensions;

    private final List<Map<String, Object>> queryData;

    private String summaryType;
    private String yUnit;

    private List<String> indicatorFieldList;
    private Map<String,String> categoryDimensionValueMapper;

    private Map<String,List<List<Float>>> seriesData;
    public ArrayList<String> seriesKey;

    public BarSeriesConstructor(Dimension xDimension, List<Indicator> indicators, List<Dimension> dimensions, List<Map<String, Object>> queryData) {
        this.indicators = indicators;
        this.dimensions = dimensions;
        this.xDimension = xDimension;
        this.queryData = queryData;
    }

    public void getDimensionValue() {
        //Analyze the specific values that need to be counted in different dimensions, controlled by the limit number num
        queryData.forEach(item->{
            dimensions.forEach(dim->{
                String dimField = dim.getField();
                String dimCategory = (String) item.get(dimField);
                if( !dim.getCustomFlag() && !Objects.equals(dimField, xDimension.getField())) {
                    //Generate a new category object, here is a non-custom dimension, and the category name is consistent with the value
                    CategoryBody cb = new CategoryBody(dimField, dimCategory, 1);
                    List<String> valueList = new ArrayList<>();
                    valueList.add(dimCategory);
                    cb.setValueList(valueList);
                    dim.addCategoryBody(cb);
                }
            });
        });

        //enumeration mapping relationship
        categoryDimensionValueMapper = new HashMap<>();
        dimensions.forEach(dim -> {
            dim.getCategoryBodyList().forEach(body -> {
                body.getValueList().forEach(value->{
                    categoryDimensionValueMapper.put( body.getField()+value,body.getCategoryName());
                });
            });
        });

        indicatorFieldList = new ArrayList<>();
        //a single graph only supports a single indicator
        indicatorFieldList.add(indicators.get(0).getField());

    }

    public void getDescartes() {
        seriesKey = new ArrayList<>();
        List<List<String>> dimData = new ArrayList<>();
        dimensions.forEach(dim -> {
            List<String> categoryList = new ArrayList<>();
            dim.getCategoryBodyList().forEach(categoryBody -> {
                categoryList.add(categoryBody.getCategoryName());
            });
            dimData.add(categoryList);
        });

        //computer the Cartesian results between various categories of different statistical dimensions
        culDescartes(dimData, seriesKey, 0, "");

        seriesData = new HashMap<>(seriesKey.size());

        if (seriesKey.size() == 0 ) {
            seriesKey.add("Total");
        }

        seriesKey.forEach(key->{
            List<List<Float>> dataArr = new ArrayList<>();
            for (int i = 0; i < xDimension.getCategoryName().size();i++) {
                dataArr.add(new ArrayList<>());
            }
            seriesData.put(key, dataArr);
        });
    }

    public Map<String,List<Float>> getSeriesData() {
        queryData.forEach(item->{
            String xValue = item.get(xDimension.getField()) == null ? "null" : item.get(xDimension.getField()).toString();
            List<Integer> xIndexList = xDimension.indexListByValue(xValue);
            if (xIndexList.size() > 0) {
                //Temporarily only counting single indicators
                Float yValue = toFloat(String.valueOf(item.get(indicatorFieldList.get(0))));

                List<String> categoryGroup = new ArrayList<>();
                //The field corresponding to the statistical dimension is evaluated
                dimensions.forEach(dim->{
                    String categoryValue = item.get(dim.getField()) == null ? "null" : item.get(dim.getField()) .toString();
                    String mapperKey = dim.getField()+categoryValue;
                    //Find the mapped category name
                    if (categoryDimensionValueMapper.containsKey(mapperKey)) {
                        categoryGroup.add(categoryDimensionValueMapper.get(mapperKey));
                    }
                });

                //Concat all found category names as key
                String groupKey = String.join("", categoryGroup);

                if (dimensions.size() == 0) {
                    xIndexList.forEach(xIndex -> {
                        seriesData.get("Total").get(xIndex).add(yValue);
                    });
                } else if (seriesData.containsKey(groupKey))
                {
                    xIndexList.forEach(xIndex -> {
                        seriesData.get(groupKey).get(xIndex).add(yValue);
                    });
                }
            }
        });

        Map<String,List<Float>> resultSeries = new HashMap<>();
        seriesData.forEach((k,v) -> {
            List<Float> summaryValue = new ArrayList<>();
            v.forEach(value -> {
                Float s = culSummaryValue(value,indicators.get(0).getType());
                summaryValue.add(s);
            });
            resultSeries.put(k,summaryValue);
        });
        return resultSeries;
    }

    public String getYUnit() {
        yUnit = indicators.get(0).getUnit();
        return "{value}" + yUnit;
    }
}
