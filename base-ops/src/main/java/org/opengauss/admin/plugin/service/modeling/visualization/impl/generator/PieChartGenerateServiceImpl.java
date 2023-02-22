package org.opengauss.admin.plugin.service.modeling.visualization.impl.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.opengauss.admin.plugin.domain.model.modeling.echart.PieChartBody;
import org.opengauss.admin.plugin.domain.model.modeling.echart.constructor.PieSeriesConstructor;
import org.opengauss.admin.plugin.vo.modeling.Dimension;
import org.opengauss.admin.plugin.vo.modeling.PieChartParamsBody;
import org.opengauss.admin.plugin.vo.modeling.component.Legend;
import org.opengauss.admin.plugin.vo.modeling.component.Title;
import org.opengauss.admin.plugin.vo.modeling.component.Tooltip;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Component("pie")
public class PieChartGenerateServiceImpl extends BaseGenerateServiceImpl {

    private PieChartParamsBody pieChartParamsBody;
    private PieSeriesConstructor pieSeriesConstructor;

    @Override
    public String generate() throws SQLException, ClassNotFoundException {
        parseParams();
        pieChartParamsBody = JSON.parseObject(JSON.toJSONString(visualizationParams), PieChartParamsBody.class);

        PieChartBody pieChartBody = new PieChartBody();
        pieChartBody.setTitle(new Title().setText(pieChartParamsBody.getTitle()));
        pieChartBody.setTooltip(new Tooltip().setTrigger("axis"));

        genSeries();

        pieChartBody.setSeries(pieSeriesConstructor.getSeriesData());

        pieChartBody.setLegend(new Legend("scroll").setData(pieSeriesConstructor.getSeriesKey()).setBottom(10));

        System.out.println(JSONObject.toJSONString(pieChartBody));
        return JSONObject.toJSONString(pieChartBody);
    }

    public void genSeries(){
        //format dimension data
        Dimension dimension = formatDimension(pieChartParamsBody.getDimension());
        Dimension subDimension = null;
        //if inner circle is config
        if (pieChartParamsBody.getShowType().getField() != null) {
            subDimension = new Dimension();
            subDimension.setNum(10);
            subDimension.setField(pieChartParamsBody.getShowType().getField());
            subDimension = formatDimension(subDimension);
        }

        pieSeriesConstructor = new PieSeriesConstructor( pieChartParamsBody.getIndicator(), dimension, subDimension, queryResult,pieChartParamsBody.getShowType().getPercentage());

    }

}
