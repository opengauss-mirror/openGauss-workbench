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
 * GraphChartGenerateServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/impl/generator/GraphChartGenerateServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization.impl.generator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.domain.model.modeling.echart.*;
import org.opengauss.admin.plugin.domain.model.modeling.echart.constructor.GraphSeriesConstructor;
import org.opengauss.admin.plugin.vo.modeling.*;
import org.opengauss.admin.plugin.vo.modeling.component.*;
import org.opengauss.admin.plugin.domain.model.modeling.echart.GraphChartBody;
import org.opengauss.admin.plugin.vo.modeling.GraphChartParamsBody;
import org.opengauss.admin.plugin.vo.modeling.component.GraphSeries;
import org.opengauss.admin.plugin.vo.modeling.component.Title;
import org.opengauss.admin.plugin.vo.modeling.component.Tooltip;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Component("graph")
public class GraphChartGenerateServiceImpl extends BaseGenerateServiceImpl {

    private GraphChartParamsBody graphChartParamsBody;

    @Override
    public String generate() throws SQLException, ClassNotFoundException {
        parseParams();
        graphChartParamsBody = JSON.parseObject(JSON.toJSONString(visualizationParams), GraphChartParamsBody.class);

        GraphChartBody graphChartBody = new GraphChartBody();
        graphChartBody.setTitle(new Title().setText(graphChartParamsBody.getTitle()));
        graphChartBody.setTooltip(new Tooltip().setTrigger("axis"));
        List<GraphSeries> series = genSeries();
        graphChartBody.setSeries(series);
        return JSONObject.toJSONString(graphChartBody);
    }

    public List<GraphSeries> genSeries(){
        GraphSeriesConstructor graphSeriesConstructor = new GraphSeriesConstructor(graphChartParamsBody.getIndicator(), graphChartParamsBody.getNode(), graphChartParamsBody.getRelation(), queryResult);
        return graphSeriesConstructor.getSeriesData();
    }

}
