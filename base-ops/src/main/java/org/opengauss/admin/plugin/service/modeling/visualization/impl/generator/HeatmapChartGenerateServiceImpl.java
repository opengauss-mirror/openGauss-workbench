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

        HeatmapChartBody heatmapChartBody = new HeatmapChartBody();
        heatmapChartBody.setTitle(new Title().setText(heatmapChartParamsBody.getTitle()));
        heatmapChartBody.setTooltip(new Tooltip().setTrigger("axis"));
        heatmapChartBody.setXAxis(new XAxis().setBoundaryGap(true).setData(xDimension.getCategoryName()).setType("category").setSplitArea(new SplitArea().setShow(true)));
        heatmapChartBody.setYAxis(new XAxis().setBoundaryGap(true).setData(yDimension.getCategoryName()).setType("category").setSplitArea(new SplitArea().setShow(true)));
        heatmapChartBody.setVisualMap(new VisualMap().setBottom("5").setCalculable(true).setMin(0).setMax(100).setLeft("center").setOrient("horizontal"));
        heatmapChartBody.setSeries(genSeries(xDimension,yDimension));

        return JSONObject.toJSONString(heatmapChartBody);
    }

    public List<HeatmapSeries> genSeries(Dimension xDimension, Dimension yDimension){
        HeatmapSeriesConstructor heatmapSeriesConstructor = new HeatmapSeriesConstructor(heatmapChartParamsBody.getIndicator(), xDimension, yDimension, queryResult);
        return heatmapSeriesConstructor.getSeriesData();
    }


}
