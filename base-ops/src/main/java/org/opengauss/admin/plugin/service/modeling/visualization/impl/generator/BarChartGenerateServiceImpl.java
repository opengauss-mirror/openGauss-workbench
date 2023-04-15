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
 * BarChartGenerateServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/impl/generator/BarChartGenerateServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization.impl.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationCustomDimensionsEntity;

import org.opengauss.admin.plugin.domain.model.modeling.echart.BarChartBody;
import org.opengauss.admin.plugin.domain.model.modeling.echart.constructor.BarSeriesConstructor;
import org.opengauss.admin.plugin.vo.modeling.BarChartParamsBody;
import org.opengauss.admin.plugin.vo.modeling.*;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.opengauss.admin.plugin.vo.modeling.Categories;
import org.opengauss.admin.plugin.vo.modeling.Dimension;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Component("bar")
public class BarChartGenerateServiceImpl extends BaseGenerateServiceImpl {

    private BarChartParamsBody barChartParamsBody;
    private BarSeriesConstructor barSeriesConstructor;

    @Override
    public String generate() throws SQLException, ClassNotFoundException {
        parseParams();
        barChartParamsBody = JSON.parseObject(JSON.toJSONString(visualizationParams), BarChartParamsBody.class);

        BarChartBody barChartBody = new BarChartBody();
        barChartBody.setTitle(new Title().setText(barChartParamsBody.getTitle()));
        barChartBody.setTooltip(new Tooltip().setTrigger("axis"));
        barChartBody.setGrid(new Grid().setBottom("40").setRight("3%").setLeft("3%").setContainLabel(true));

        //set dataZoom
        ArrayList<DataZoomItem> dataZoom = new ArrayList<>();
        dataZoom.add(new DataZoomItem().setStart(0).setEnd(10).setType("inside"));
        dataZoom.add(new DataZoomItem().setStart(0).setEnd(10));
        barChartBody.setDataZoom(dataZoom);

        List<String> allKeys = new ArrayList<>(queryResult.get(0).keySet());
        if (!allKeys.contains(barChartParamsBody.getShowType().getDimension())) {
            throw new RuntimeException("The field in the parameter is not found in the query result, please check whether the query table or condition has been replaced.");
        }
        //generate X axis config
        Dimension xDimension;
        if (barChartParamsBody.getShowType().getKey() == 1) {
            xDimension = genXAxis();
        } else {
            xDimension = genXAxisByTime();
        }

        barChartBody.setXAxis(new XAxis().setType("category").setBoundaryGap(true).setData(xDimension.getCategoryName()));

        //generate series data
        genSeries(xDimension);

        List<Series> series = new ArrayList<>();
        Map<String, List<Float>> seriesData = barSeriesConstructor.getSeriesData();
        for(String key : seriesData.keySet()){
            List<Float> value = seriesData.get(key);
            //add type and yAxis index
            Series s = new Series().setType("bar").setData(value).setYAxisIndex(0);
            if (barChartParamsBody.getDimension().size() > 0) {
                s.setName(key);
            }
            if (fullParams.getJSONObject("paramsData").getInteger("showNumber") == 1) {
                s.setLabel(new Label().setShow(true));
            }
            series.add(s);
        }

        barChartBody.setSeries(series);

        barChartBody.setLegend(new Legend("scroll").setData(barSeriesConstructor.getSeriesKey()).setTop(10).setWidth("70%"));

        //set y unit
        barChartBody.setYAxis(new YAxis().setType("value").setAxisLabel(new AxisLabel(barSeriesConstructor.getYUnit())));

        return JSONObject.toJSONString(barChartBody);
    }

    public Dimension genXAxis(){
        Dimension xDimension = new Dimension();
        //get display dimension of X Axis
        String xDimensionKey = barChartParamsBody.getShowType().getDimension();
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
            //enum value by field
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
        String xDimensionKey = barChartParamsBody.getShowType().getDateField();
        Integer particle = barChartParamsBody.getShowType().getParticle();

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
        List<Dimension> dimensions = barChartParamsBody.getDimension();
        dimensions.forEach(dim->{
            //prepare dimensions
            if (dim.getField().contains("custom|"))
            {
                //get custom dimension record in database
                String customId = dim.getField().replace("custom|","");
                ModelingVisualizationCustomDimensionsEntity cd = modelingVisualizationCustomDimensionsService.getById(customId);
                dim.setField(cd.getField());
                dim.setCustomFlag(true);
                //enum value by field
                JSONArray categoriesJson = JSONArray.parseArray(cd.getCategoriesJson());
                categoriesJson.forEach(categoryJson->{
                    Categories category = JSON.parseObject(JSON.toJSONString(categoryJson), Categories.class);
                    dim.addCategoryBody(category.toBody(cd.getField()));
                });
                dim.setNum(categoriesJson.size());
            }
        });

        barSeriesConstructor = new BarSeriesConstructor(xDimension,
                barChartParamsBody.getIndicator(),
                barChartParamsBody.getDimension(),
                queryResult);

        barSeriesConstructor.getDimensionValue();
        barSeriesConstructor.getDescartes();
    }

}
