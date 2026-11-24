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

import java.util.List;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class BarChartParamsBody {

    private String title;
    private String chartType;
    private String dataFlowId;
    private String operatorId;
    private List<Indicator> indicator;
    private List<Dimension> dimension;
    private ShowType showType;
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

    public void setIndicator(List<Indicator> indicator) {
         this.indicator = indicator;
     }
     public List<Indicator> getIndicator() {
         return indicator;
     }

    public void setDimension(List<Dimension> dimension) {
         this.dimension = dimension;
     }
     public List<Dimension> getDimension() {
         return dimension;
     }

    public void setShowType(ShowType showType) {
         this.showType = showType;
     }
     public ShowType getShowType() {
         return showType;
     }

}
