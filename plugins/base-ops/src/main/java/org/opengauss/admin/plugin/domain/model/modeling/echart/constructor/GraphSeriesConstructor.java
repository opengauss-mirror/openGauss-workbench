/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.opengauss.admin.plugin.domain.model.modeling.echart.constructor;

import org.opengauss.admin.plugin.vo.modeling.component.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.opengauss.admin.plugin.vo.modeling.Indicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Data
@EqualsAndHashCode(callSuper=true)
public class GraphSeriesConstructor extends BaseSeriesConstructor {

    private final Indicator indicator;
    private final Node nodeParams;
    private final Relation relationParams;

    private final List<Map<String, Object>> queryData;

    private String summaryType;

    private Map<String,List<Float>> seriesData;
    public ArrayList<String> seriesKey;


    public GraphSeriesConstructor(Indicator indicator, Node nodeParams, Relation relationParams, List<Map<String, Object>> queryData) {
        this.indicator = indicator;
        this.nodeParams = nodeParams;
        this.relationParams = relationParams;
        this.queryData = queryData;
    }

    public List<GraphSeries> getSeriesData()  {
        GraphSeries mainSeries = new GraphSeries();
        mainSeries.setName(nodeParams.getName());
        mainSeries.setType("graph");
        mainSeries.setLayout("none");
        mainSeries.setRoam(true);
        mainSeries.setLabel(new Label().setPosition("right"));
        mainSeries.setLineStyle(new LineStyle().setCurveness(0.3f).setColor("source"));

        List<GraphNode> nodeList = new ArrayList<>();
        List<GraphLink> linkList = new ArrayList<>();
        queryData.forEach(item->{
            GraphNode node = new GraphNode();
            node.setId((String) item.get(nodeParams.getId()));
            float xPosition = toFloat(item.get(nodeParams.getX()));
            float yPosition = toFloat(item.get(nodeParams.getY()));
            if (xPosition > 0 && yPosition > 0) {
                node.setX(xPosition);
                node.setY(yPosition);
            }
            node.setValue(toFloat(item.get(relationParams.getValue())));
            node.setCategory(1);
            node.setName((String) item.get(nodeParams.getName()));
            node.setSymbolSize(toFloat(item.get(nodeParams.getSize())));
            node.setLabel(new Label().setShow(true));
            nodeList.add(node);

            GraphLink link = new GraphLink();
            link.setTarget((String) item.get(relationParams.getOut()));
            link.setSource((String) item.get(relationParams.getIn()));
            linkList.add(link);
        });

        mainSeries.setData(nodeList);
        mainSeries.setLinks(linkList);

        return List.of(mainSeries);
    }

}
