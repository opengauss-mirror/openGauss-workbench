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
 * ScatterChartGenerateServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/impl/generator/ScatterChartGenerateServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization.impl.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationGeoFilesEntity;
import org.opengauss.admin.plugin.domain.model.modeling.echart.ScatterChartBody;
import org.opengauss.admin.plugin.domain.model.modeling.echart.constructor.ScatterSeriesConstructor;

import org.opengauss.admin.plugin.service.modeling.IModelingVisualizationGeoFilesService;
import org.opengauss.admin.plugin.utils.GeoUtils;
import org.opengauss.admin.plugin.vo.modeling.ScatterChartParamsBody;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Component("scatter")
public class ScatterChartGenerateServiceImpl extends BaseGenerateServiceImpl {

    private ScatterChartParamsBody scatterChartParamsBody;

    private ScatterSeriesConstructor scatterSeriesConstructor;

    @Autowired
    private IModelingVisualizationGeoFilesService modelingVisualizationGeoFilesService;

    @Override
    public String generate() throws SQLException, ClassNotFoundException {
        parseParams();
        scatterChartParamsBody = JSON.parseObject(JSON.toJSONString(visualizationParams), ScatterChartParamsBody.class);

        ScatterChartBody scatterChartBody = new ScatterChartBody();
        scatterChartBody.setTitle(new Title().setText(scatterChartParamsBody.getTitle()));
        scatterChartBody.setTooltip(new Tooltip().setTrigger("item").setFormatter("{b}<br/>{c}"));

        ModelingVisualizationGeoFilesEntity geoFile = modelingVisualizationGeoFilesService.getById(scatterChartParamsBody.getLocation().getCommonValue());
        //compute geo`s center and config
        double[] center = GeoUtils.getGeoJsonCenter(geoFile.getGeoJson());

        //if roam is config
        double centerX = scatterChartParamsBody.getCenter().get(0) != 0 ? scatterChartParamsBody.getCenter().get(0) : center[0];
        double centerY = scatterChartParamsBody.getCenter().get(1) != 0 ? scatterChartParamsBody.getCenter().get(1) : center[1];
        double zoom = scatterChartParamsBody.getZoom() > 0 ? scatterChartParamsBody.getZoom() : 1.0;

        scatterChartBody.setGeo(List.of(
                new Geo().setMap(geoFile.getRegisterName())
                        .setZoom(zoom)
                        .setCenter(List.of(centerX, centerY))
                        .setRoam(true)));

        List<String> allKeys = new ArrayList<>(queryResult.get(0).keySet());
        if (!allKeys.contains(scatterChartParamsBody.getLocation().getField())) {
            throw new RuntimeException("The field in the parameter is not found in the query result, please check whether the query table or condition has been replaced.");
        }

        List<ScatterSeries> series = genSeries(scatterChartParamsBody.getLocation());
        scatterChartBody.setSeries(series);

        scatterChartBody.setVisualMap(new VisualMap().setMax(scatterSeriesConstructor.maxValue).setMin(0).setText(List.of("high","low"))
                .setInRange(new InRange().setColor(List.of(scatterChartParamsBody.getColorConfig().get(0),scatterChartParamsBody.getColorConfig().get(1),scatterChartParamsBody.getColorConfig().get(2)))));

        return JSONObject.toJSONString(scatterChartBody);
    }

    public List<ScatterSeries> genSeries(Location location){
        scatterSeriesConstructor = new ScatterSeriesConstructor(scatterChartParamsBody.getIndicator(), location, queryResult,scatterChartParamsBody.getLocation().getArea());
        return scatterSeriesConstructor.getSeriesData();
    }

}
