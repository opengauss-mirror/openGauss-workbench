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
public class HeatmapSeries {

    private String name;
    private String type;
    private List<List<Object>> data;
    private Label label;
    private Emphasis emphasis;
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public HeatmapSeries setData(List<List<Object>> data) {
        this.data = data;
        return this;
    }
    public List<List<Object>> getData() {
        return data;
    }

    public void setLabel(Label label) {
        this.label = label;
    }
    public Label getLabel() {
        return label;
    }

    public void setEmphasis(Emphasis emphasis) {
        this.emphasis = emphasis;
    }
    public Emphasis getEmphasis() {
        return emphasis;
    }

}
