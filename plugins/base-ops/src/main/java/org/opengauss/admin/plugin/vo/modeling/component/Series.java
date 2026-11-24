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
public class Series {

    private String name;
    private String type;
    private String stack;
    private List<Float> data;
    private int yAxisIndex;
    private Label label;

    public Series setName(String name) {
        this.name = name;
        return this;
    }
    public String getName() {
        return name;
    }

    public Series setType(String type) {
        this.type = type;
        return this;
    }
    public String getType() {
        return type;
    }

    public Series setStack(String stack) {
        this.stack = stack;
        return this;
    }
    public String getStack() {
        return stack;
    }

    public Series setData(List<Float> data) {
        this.data = data;
        return this;
    }
    public List<Float> getData() {
        return data;
    }

    public Series setYAxisIndex(int yAxisIndex) {
        this.yAxisIndex = yAxisIndex;
        return this;
    }
    public int getYAxisIndex() {
        return yAxisIndex;
    }

    public Series setLabel(Label label) {
        this.label = label;
        return this;
    }
    public Label getLabel() {
        return label;
    }

}
