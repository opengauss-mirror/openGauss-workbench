package org.opengauss.admin.plugin.service.modeling.visualization.impl.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationGeoFilesEntity;
import org.opengauss.admin.plugin.domain.model.modeling.echart.ScatterChartBody;
import org.opengauss.admin.plugin.domain.model.modeling.echart.constructor.ScatterSeriesConstructor;

import org.opengauss.admin.plugin.service.modeling.IModelingVisualizationGeoFilesService;
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

        scatterChartBody.setGeo(List.of(new Geo().setMap(geoFile.getRegisterName())
                .setZoom(scatterChartParamsBody.getZoom())
                .setCenter(List.of(scatterChartParamsBody.getCenter().get(0), scatterChartParamsBody.getCenter().get(1)))
                .setRoam(true)));

        List<String> allKeys = new ArrayList<>(queryResult.get(0).keySet());
        if (!allKeys.contains(scatterChartParamsBody.getLocation().getField())) {
            throw new RuntimeException("The field in the parameter is not found in the query result, please check whether the query table or condition has been replaced.");
        }

        List<ScatterSeries> series = genSeries(scatterChartParamsBody.getLocation());
        scatterChartBody.setSeries(series);

        scatterChartBody.setVisualMap(new VisualMap().setMax(scatterSeriesConstructor.maxValue).setMin(0).setText(List.of("high","low"))
                .setInRange(new InRange().setColor(List.of("lightskyblue","yellow","orangered"))));

        return JSONObject.toJSONString(scatterChartBody);
    }

    public List<ScatterSeries> genSeries(Location location){
        scatterSeriesConstructor = new ScatterSeriesConstructor(scatterChartParamsBody.getIndicator(), location, queryResult,scatterChartParamsBody.getLocation().getArea());
        return scatterSeriesConstructor.getSeriesData();
    }

}
