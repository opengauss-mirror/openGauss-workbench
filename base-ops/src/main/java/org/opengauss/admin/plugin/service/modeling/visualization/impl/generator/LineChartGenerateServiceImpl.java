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
import java.util.ArrayList;
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

        //generate x axis
        Dimension xDimension = genXAxis();
        lineChartBody.setXAxis(new XAxis().setType("category").setBoundaryGap(true).setData(xDimension.getCategoryName()));

        //generate series data
        genSeries(xDimension);

        List<Series> series = new ArrayList<>();
        Map<String, List<Float>> seriesData = lineSeriesConstructor.getSeriesData();
        for(String key : seriesData.keySet()){
            List<Float> value = seriesData.get(key);
            //add y index for each series
            series.add(new Series().setName(key).setType("line").setData(value).setYAxisIndex(0));
        }

        lineChartBody.setSeries(series);

        lineChartBody.setLegend(new Legend("scroll").setData(lineSeriesConstructor.getSeriesKey()).setBottom(10));

        lineChartBody.setYAxis(new YAxis().setType("value").setAxisLabel(new AxisLabel(lineSeriesConstructor.getYUnit())));

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
            //limit size 8
            xDimension.setNum(8);
            //enum value in query data
            queryResult.forEach(item->{
                String filedValue = (String) item.get(xDimensionKey);
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
