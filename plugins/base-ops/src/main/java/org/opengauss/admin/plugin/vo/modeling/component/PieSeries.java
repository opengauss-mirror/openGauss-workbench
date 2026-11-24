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
package org.opengauss.admin.plugin.vo.modeling.component;

import java.util.List;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class PieSeries {

    private String name;
    private String type;
    private List<PieData> data;
    private String selectedMode;
    private List<String> radius;
    private Label label;
    private LabelLine labelLine;

    public PieSeries setName(String name) {
        this.name = name;
        return this;
    }
    public String getName() {
        return name;
    }

    public PieSeries setType(String type) {
        this.type = type;
        return this;
    }
    public String getType() {
        return type;
    }

    public void setSelectedMode(String selectedMode) {
        this.selectedMode = selectedMode;
    }
    public String getSelectedMode() {
        return selectedMode;
    }

    public void setRadius(List<String> radius) {
        this.radius = radius;
    }
    public List<String> getRadius() {
        return radius;
    }

    public void setLabel(Label label) {
        this.label = label;
    }
    public Label getLabel() {
        return label;
    }

    public void setLabelLine(LabelLine labelLine) {
        this.labelLine = labelLine;
    }
    public LabelLine getLabelLine() {
        return labelLine;
    }

    public PieSeries setData(List<PieData> data) {
        this.data = data;
        return this;
    }

    public List<PieData> getData() {
        return data;
    }

}
