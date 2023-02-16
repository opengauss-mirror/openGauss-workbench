package org.opengauss.admin.plugin.service.modeling.visualization.impl.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingVisualizationCustomDimensionsEntity;
import org.opengauss.admin.plugin.domain.model.modeling.echart.MixChartBody;
import org.opengauss.admin.plugin.domain.model.modeling.echart.constructor.BarSeriesConstructor;
import org.opengauss.admin.plugin.domain.model.modeling.echart.constructor.LineSeriesConstructor;
import org.opengauss.admin.plugin.vo.modeling.Categories;
import org.opengauss.admin.plugin.vo.modeling.Dimension;
import org.opengauss.admin.plugin.vo.modeling.MixChartParamsBody;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Component("mix")
public class MixChartGenerateServiceImpl extends BaseGenerateServiceImpl {

    private MixChartParamsBody mixChartParamsBody;
    private List<BarSeriesConstructor> barSeriesConstructors;
    private List<LineSeriesConstructor> lineSeriesConstructors;
    private LinkedHashSet<String> yUnits;

    @Override
    public String generate() throws SQLException, ClassNotFoundException {
        parseParams();
        mixChartParamsBody = JSON.parseObject(JSON.toJSONString(visualizationParams), MixChartParamsBody.class);

        MixChartBody mixChartBody = new MixChartBody();
        mixChartBody.setTitle(new Title().setText(mixChartParamsBody.getTitle()));
        mixChartBody.setTooltip(new Tooltip().setTrigger("axis"));
        mixChartBody.setGrid(new Grid().setBottom("40").setRight("3%").setLeft("3%").setContainLabel(true));

        //generate X axis config
        Dimension xDimension;
        if (mixChartParamsBody.getShowType().getKey() == 1) {
            xDimension = genXAxis();
        } else {
            xDimension = genXAxisByTime();
        }

        mixChartBody.setXAxis(new XAxis().setType("category").setBoundaryGap(true).setData(xDimension.getCategoryName()));

        genSeries(xDimension);

        List<Series> series = new ArrayList<>();
        List<String> seriesKeys = new ArrayList<>();

        ArrayList<String> yUnitsList = new ArrayList<>(yUnits);

        AtomicInteger chartColorIndex = new AtomicInteger(0);

        //process bar chart info
        barSeriesConstructors.forEach(barSeriesConstructor -> {
            Map<String, List<Float>> seriesData = barSeriesConstructor.getSeriesData();
            for(String key : seriesData.keySet()){
                List<Float> value = seriesData.get(key);
                //get index of Y Axis
                int yIndex = yUnitsList.indexOf(barSeriesConstructor.getYUnit());
                if (Objects.equals(key, "Total")) {
                    chartColorIndex.addAndGet(1);
                    series.add(new Series().setName(key + "-" + chartColorIndex).setType("bar").setData(value).setYAxisIndex(yIndex));
                } else {
                    series.add(new Series().setName(key).setType("bar").setData(value).setYAxisIndex(yIndex));
                }
                //merge categories
                seriesKeys.addAll(barSeriesConstructor.getSeriesKey());
            }
        });

        //process line chart info
        lineSeriesConstructors.forEach(lineSeriesConstructor -> {
            Map<String, List<Float>> seriesData = lineSeriesConstructor.getSeriesData();
            for(String key : seriesData.keySet()){
                List<Float> value = seriesData.get(key);
                //get index of Y Axis
                int yIndex = yUnitsList.indexOf(lineSeriesConstructor.getYUnit());
                if (Objects.equals(key, "Total")) {
                    chartColorIndex.addAndGet(1);
                    series.add(new Series().setName(key + "-" + chartColorIndex).setType("line").setData(value).setYAxisIndex(yIndex));
                } else {
                    series.add(new Series().setName(key).setType("line").setData(value).setYAxisIndex(yIndex));
                }
                //merge categories
                seriesKeys.addAll(lineSeriesConstructor.getSeriesKey());
            }
        });


        mixChartBody.setSeries(series);

        mixChartBody.setLegend(new Legend("scroll").setData(seriesKeys).setBottom(10));

        //add Y Axis index , first y Axis at left and others place at right
        List<YAxis> yAxes = new ArrayList<>();
        yUnitsList.forEach(yUnit->{
            if (yUnitsList.indexOf(yUnit) == 0) {
                yAxes.add(new YAxis().setType("value").setAxisLabel(new AxisLabel(yUnit)).setPosition("left"));
            } else {
                yAxes.add(new YAxis().setType("value").setAxisLabel(new AxisLabel(yUnit)).setPosition("right"));
            }
        });
        mixChartBody.setYAxis(yAxes);

        return JSONObject.toJSONString(mixChartBody);
    }

    public Dimension genXAxis(){
        Dimension xDimension = new Dimension();
        String xDimensionKey = mixChartParamsBody.getShowType().getDimension();
        if (xDimensionKey.contains("custom|")) {
            String xDimensionId = xDimensionKey.replace("custom|","");
            ModelingVisualizationCustomDimensionsEntity cd = modelingVisualizationCustomDimensionsService.getById(xDimensionId);
            xDimension.setField(cd.getField());
            JSONArray categoriesJson = JSONArray.parseArray(cd.getCategoriesJson());
            xDimension.setNum(categoriesJson.size());
            categoriesJson.forEach(categoryJson->{
                Categories category = JSON.parseObject(JSON.toJSONString(categoryJson), Categories.class);
                xDimension.addCategoryBody(category.toBody(cd.getField()));
            });
        } else {
            xDimension.setField(xDimensionKey);
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
        String xDimensionKey = mixChartParamsBody.getShowType().getDateField();
        Integer particle = mixChartParamsBody.getShowType().getParticle();

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
        barSeriesConstructors = new ArrayList<>();
        lineSeriesConstructors = new ArrayList<>();
        yUnits = new LinkedHashSet<>();

        mixChartParamsBody.getSubCharts().forEach(subCharts -> {
            List<Dimension> dimensions = subCharts.getDimension();
            dimensions.forEach(dim->{
                if (dim.getField().contains("custom|"))
                {
                    String customId = dim.getField().replace("custom|","");
                    ModelingVisualizationCustomDimensionsEntity cd = modelingVisualizationCustomDimensionsService.getById(customId);
                    dim.setField(cd.getField());
                    dim.setCustomFlag(true);

                    JSONArray categoriesJson = JSONArray.parseArray(cd.getCategoriesJson());
                    categoriesJson.forEach(categoryJson->{
                        Categories category = JSON.parseObject(JSON.toJSONString(categoryJson), Categories.class);
                        dim.addCategoryBody(category.toBody(cd.getField()));
                    });
                    dim.setNum(categoriesJson.size());
                }
            });

            if (Objects.equals(subCharts.getChartType(), "bar")) {
                BarSeriesConstructor barSeriesConstructor = new BarSeriesConstructor(xDimension,
                        subCharts.getIndicator(),
                        dimensions,
                        queryResult);
                barSeriesConstructor.getDimensionValue();
                barSeriesConstructor.getDescartes();

                yUnits.add(barSeriesConstructor.getYUnit());
                barSeriesConstructors.add(barSeriesConstructor);
            }

            if (Objects.equals(subCharts.getChartType(), "line")) {
                LineSeriesConstructor lineSeriesConstructor = new LineSeriesConstructor(xDimension,
                        subCharts.getIndicator(),
                        dimensions,
                        queryResult);
                lineSeriesConstructor.getDimensionValue();
                lineSeriesConstructor.getDescartes();

                yUnits.add(lineSeriesConstructor.getYUnit());
                lineSeriesConstructors.add(lineSeriesConstructor);
            }
        });

    }

}




