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
package org.opengauss.admin.plugin.vo.modeling;

import org.opengauss.admin.plugin.vo.modeling.component.Node;
import org.opengauss.admin.plugin.vo.modeling.component.Relation;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class GraphChartParamsBody {

    private String title;
    private String chartType;
    private boolean loading;
    private String dataFlowId;
    private String operatorId;
    private Indicator indicator;
    private Node node;
    private Relation relation;

    public void setTitle(String title) {
         this.title = title;
     }
     public String getTitle() {
         return title;
     }

    public void setChartType(String chartType) {
         this.chartType = chartType;
     }
     public String getChartType() {
         return chartType;
     }

    public void setLoading(boolean loading) {
         this.loading = loading;
     }
     public boolean getLoading() {
         return loading;
     }

    public void setDataFlowId(String dataFlowId) {
         this.dataFlowId = dataFlowId;
     }
     public String getDataFlowId() {
         return dataFlowId;
     }

    public void setOperatorId(String operatorId) {
         this.operatorId = operatorId;
     }
     public String getOperatorId() {
         return operatorId;
     }

    public void setIndicator(Indicator indicator) {
         this.indicator = indicator;
     }
     public Indicator getIndicator() {
         return indicator;
     }

    public void setNode(Node node) {
         this.node = node;
     }
     public Node getNode() {
         return node;
     }

    public void setRelation(Relation relation) {
         this.relation = relation;
     }
     public Relation getRelation() {
         return relation;
     }

}
