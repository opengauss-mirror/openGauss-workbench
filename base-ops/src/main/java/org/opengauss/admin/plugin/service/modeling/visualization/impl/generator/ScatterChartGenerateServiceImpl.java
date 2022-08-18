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
import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Component("scatter")
public class ScatterChartGenerateServiceImpl extends BaseGenerateServiceImpl {

    private ScatterChartParamsBody scatterChartParamsBody;

    @Autowired
    private IModelingVisualizationGeoFilesService modelingVisualizationGeoFilesService;

    @Override
    public String generate() throws SQLException, ClassNotFoundException {
        parseParams();
        scatterChartParamsBody = JSON.parseObject(JSON.toJSONString(visualizationParams), ScatterChartParamsBody.class);

        ScatterChartBody scatterChartBody = new ScatterChartBody();
        scatterChartBody.setTitle(new Title().setText(scatterChartParamsBody.getTitle()));
        scatterChartBody.setTooltip(new Tooltip().setTrigger("axis"));
        scatterChartBody.setVisualMap(new VisualMap().setMax(1000000).setMin(0).setText(List.of("high","low"))
                .setInRange(new InRange().setColor(List.of("lightskyblue","yellow","orangered"))));

        ModelingVisualizationGeoFilesEntity geoFile = modelingVisualizationGeoFilesService.getById(scatterChartParamsBody.getLocation().getCommonValue());

        scatterChartBody.setGeo(List.of(new Geo().setMap(geoFile.getRegisterName())
                .setZoom(scatterChartParamsBody.getZoom())
                .setCenter(List.of(scatterChartParamsBody.getCenter().get(0), scatterChartParamsBody.getCenter().get(1)))
                .setRoam(true)));
        List<ScatterSeries> series = genSeries(scatterChartParamsBody.getLocation());
        scatterChartBody.setSeries(series);

        return JSONObject.toJSONString(scatterChartBody);
    }

    public List<ScatterSeries> genSeries(Location location){
        ScatterSeriesConstructor scatterSeriesConstructor = new ScatterSeriesConstructor(scatterChartParamsBody.getIndicator(), location, queryResult);
        return scatterSeriesConstructor.getSeriesData();
    }

}
