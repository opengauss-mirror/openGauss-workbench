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
 * HeatmapChartGenerateServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/impl/generator/HeatmapChartGenerateServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization.impl.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.echart.HeatmapChartBody;
import org.opengauss.admin.plugin.domain.model.modeling.echart.constructor.HeatmapSeriesConstructor;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.opengauss.admin.plugin.vo.modeling.Dimension;
import org.opengauss.admin.plugin.vo.modeling.HeatmapChartParamsBody;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Component("heatmap")
public class HeatmapChartGenerateServiceImpl extends BaseGenerateServiceImpl {

    private HeatmapChartParamsBody heatmapChartParamsBody;

    @Override
    public String generate() throws SQLException, ClassNotFoundException {
        parseParams();
        heatmapChartParamsBody = JSON.parseObject(JSON.toJSONString(visualizationParams), HeatmapChartParamsBody.class);

        //prepare dimension data
        Dimension xDimension = formatDimension(heatmapChartParamsBody.getX());
        Dimension yDimension = formatDimension(heatmapChartParamsBody.getY());

        List<String> allKeys = new ArrayList<>(queryResult.get(0).keySet());
        if (!allKeys.contains(heatmapChartParamsBody.getX().getField())) {
            throw new RuntimeException("The field in the parameter is not found in the query result, please check whether the query table or condition has been replaced.");
        }

        HeatmapChartBody heatmapChartBody = new HeatmapChartBody();
        heatmapChartBody.setTitle(new Title().setText(heatmapChartParamsBody.getTitle()));
        heatmapChartBody.setTooltip(new Tooltip().setTrigger("axis"));
        heatmapChartBody.setXAxis(new XAxis().setBoundaryGap(true).setData(xDimension.getCategoryName()).setType("category").setSplitArea(new SplitArea().setShow(true)));
        heatmapChartBody.setYAxis(new XAxis().setBoundaryGap(true).setData(yDimension.getCategoryName()).setType("category").setSplitArea(new SplitArea().setShow(true)));
        int heatMin = heatmapChartParamsBody.getRange().stream()
                .findFirst().orElse(0);
        int heatMax = heatmapChartParamsBody.getRange().stream()
                .skip(1).findFirst().orElse(100);
        heatmapChartBody.setVisualMap(new VisualMap().setBottom("5").setCalculable(true).setMin(heatMin).setMax(heatMax).setLeft("center").setOrient("horizontal"));
        heatmapChartBody.setSeries(genSeries(xDimension,yDimension));

        return JSONObject.toJSONString(heatmapChartBody);
    }

    public List<HeatmapSeries> genSeries(Dimension xDimension, Dimension yDimension){
        HeatmapSeriesConstructor heatmapSeriesConstructor = new HeatmapSeriesConstructor(heatmapChartParamsBody.getIndicator(), xDimension, yDimension, queryResult);
        return heatmapSeriesConstructor.getSeriesData();
    }


}
