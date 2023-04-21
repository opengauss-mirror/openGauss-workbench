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
 * PieChartGenerateServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/impl/generator/PieChartGenerateServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

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
import java.util.ArrayList;
import java.util.List;

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
        pieChartBody.setTooltip(new Tooltip().setTrigger("item").setFormatter("{b} {c}"));

        List<String> allKeys = new ArrayList<>(queryResult.get(0).keySet());
        if (!allKeys.contains(pieChartParamsBody.getDimension().getField())) {
            throw new RuntimeException("The field in the parameter is not found in the query result, please check whether the query table or condition has been replaced.");
        }

        genSeries();

        pieChartBody.setSeries(pieSeriesConstructor.getSeriesData());

        pieChartBody.setLegend(new Legend("scroll").setData(pieSeriesConstructor.getSeriesKey()).setBottom(10));

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
