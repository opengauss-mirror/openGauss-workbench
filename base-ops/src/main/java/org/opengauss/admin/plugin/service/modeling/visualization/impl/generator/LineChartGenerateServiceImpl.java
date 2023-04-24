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
 * LineChartGenerateServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/impl/generator/LineChartGenerateServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization.impl.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationCustomDimensionsEntity;
import org.opengauss.admin.plugin.domain.model.modeling.echart.LineChartBody;
import org.opengauss.admin.plugin.domain.model.modeling.echart.constructor.LineSeriesConstructor;
import org.opengauss.admin.plugin.vo.modeling.Categories;
import org.opengauss.admin.plugin.vo.modeling.Dimension;
import org.opengauss.admin.plugin.vo.modeling.LineChartParamsBody;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Component("line")
public class LineChartGenerateServiceImpl extends BaseGenerateServiceImpl {

    private LineChartParamsBody lineChartParamsBody;
    private LineSeriesConstructor lineSeriesConstructor;

    @Override
    public String generate() throws SQLException, ClassNotFoundException {
        parseParams();
        lineChartParamsBody = JSON.parseObject(JSON.toJSONString(visualizationParams), LineChartParamsBody.class);

        LineChartBody lineChartBody = new LineChartBody();
        lineChartBody.setTitle(new Title().setText(lineChartParamsBody.getTitle()));
        lineChartBody.setTooltip(new Tooltip().setTrigger("axis"));
        lineChartBody.setGrid(new Grid().setBottom("40").setRight("3%").setLeft("3%").setContainLabel(true));

        //set dataZoom
        ArrayList<DataZoomItem> dataZoom = new ArrayList<>();
        dataZoom.add(new DataZoomItem().setStart(0).setEnd(10).setType("inside"));
        dataZoom.add(new DataZoomItem().setStart(0).setEnd(10));
        lineChartBody.setDataZoom(dataZoom);

        List<String> allKeys = new ArrayList<>(queryResult.get(0).keySet());
        if (!allKeys.contains(lineChartParamsBody.getShowType().getDimension())) {
            throw new RuntimeException("The field in the parameter is not found in the query result, please check whether the query table or condition has been replaced.");
        }

        //generate X axis config
        Dimension xDimension;
        if (lineChartParamsBody.getShowType().getKey() == 1) {
            xDimension = genXAxis();
        } else {
            xDimension = genXAxisByTime();
        }

        lineChartBody.setXAxis(new XAxis().setType("category").setBoundaryGap(true).setData(xDimension.getCategoryName()));

        //generate series data
        genSeries(xDimension);

        List<Series> series = new ArrayList<>();
        Map<String, List<Float>> seriesData = lineSeriesConstructor.getSeriesData();
        float minValue = Float.MAX_VALUE;

        for(String key : seriesData.keySet()){
            List<Float> value = seriesData.get(key);
            //add y index for each series
            Series s = new Series().setType("line").setData(value).setYAxisIndex(0);
            if (lineChartParamsBody.getDimension().size() > 0) {
                s.setName(key);
            }
            series.add(s);

            if (fullParams.getJSONObject("paramsData").getInteger("showNumber") == 1) {
                s.setLabel(new Label().setShow(true));
            }

            //get min value for y axis
            float currentMinValue = Collections.min(value);
            if (currentMinValue < minValue) {
                minValue = currentMinValue;
            }
        }

        if (minValue > 0 && minValue < Float.MAX_VALUE)
        {
            minValue = Math.round(minValue * 0.8f);
        } else {
            minValue = 0;
        }

        lineChartBody.setSeries(series);

        lineChartBody.setLegend(new Legend("scroll").setData(lineSeriesConstructor.getSeriesKey()).setTop(10).setWidth("70%"));

        lineChartBody.setYAxis(new YAxis().setMin(minValue).setType("value").setAxisLabel(new AxisLabel(lineSeriesConstructor.getYUnit())));

        return JSONObject.toJSONString(lineChartBody);
    }

    public Dimension genXAxis(){
        Dimension xDimension = new Dimension();
        //get display dimension of X Axis
        String xDimensionKey = lineChartParamsBody.getShowType().getDimension();
        //if dimension is from custom
        if (xDimensionKey.contains("custom|")) {
            //get custom dimension record in database
            String xDimensionId = xDimensionKey.replace("custom|","");
            ModelingVisualizationCustomDimensionsEntity cd = modelingVisualizationCustomDimensionsService.getById(xDimensionId);
            xDimension.setField(cd.getField());
            //enum value by field
            JSONArray categoriesJson = JSONArray.parseArray(cd.getCategoriesJson());
            xDimension.setNum(categoriesJson.size());
            categoriesJson.forEach(categoryJson->{
                Categories category = JSON.parseObject(JSON.toJSONString(categoryJson), Categories.class);
                xDimension.addCategoryBody(category.toBody(cd.getField()));
            });
        } else {
            xDimension.setField(xDimensionKey);
            //enum value in query data
            queryResult.forEach(item->{
                String filedValue = item.get(xDimensionKey) == null ? "null" : item.get(xDimensionKey).toString();
                if (filedValue != null) {
                    Categories category = new Categories();
                    category.setName(filedValue);
                    category.setValue(List.of(filedValue));
                    xDimension.addCategoryBody(category.toBody(xDimensionKey));
                }
            });
        }

        return xDimension;
    }

    public Dimension genXAxisByTime(){
        Dimension xDimension = new Dimension();
        //get display dimension of X Axis
        String xDimensionKey = lineChartParamsBody.getShowType().getDateField();
        Integer particle = lineChartParamsBody.getShowType().getParticle();

        xDimension.setField(xDimensionKey);
        //enum value by field
        queryResult.forEach(item->{
            String filedValue = null;
            if (item.get(xDimensionKey) != null && item.get(xDimensionKey).getClass() == Timestamp.class) {
                filedValue = formatDateTimeDimension((Timestamp)item.get(xDimensionKey),particle);
                item.put(xDimensionKey,filedValue);
            }
            if (filedValue != null) {
                Categories category = new Categories();
                category.setName(filedValue);
                category.setValue(List.of(filedValue));
                xDimension.addCategoryBody(category.toBody(xDimensionKey));
            }
        });

        return xDimension;
    }

    public void genSeries(Dimension xDimension){
        List<Dimension> dimensions = lineChartParamsBody.getDimension();
        dimensions.forEach(dim->{
            if (dim.getField().contains("custom|"))
            {
                String customId = dim.getField().replace("custom|","");
                ModelingVisualizationCustomDimensionsEntity cd = modelingVisualizationCustomDimensionsService.getById(customId);
                dim.setField(cd.getField());
                dim.setCustomFlag(true);

                JSONArray categoriesJson = JSONArray.parseArray(cd.getCategoriesJson());
                dim.setNum(categoriesJson.size());
                categoriesJson.forEach(categoryJson->{
                    Categories category = JSON.parseObject(JSON.toJSONString(categoryJson), Categories.class);
                    dim.addCategoryBody(category.toBody(cd.getField()));
                });
            }
        });

        lineSeriesConstructor = new LineSeriesConstructor(xDimension,
                lineChartParamsBody.getIndicator(),
                dimensions,
                queryResult);

        lineSeriesConstructor.getDimensionValue();
        lineSeriesConstructor.getDescartes();
    }

}
