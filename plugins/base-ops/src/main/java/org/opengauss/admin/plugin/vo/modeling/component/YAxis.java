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
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class YAxis {

    private String type;
    private String name;
    private String position;
    private boolean alignTicks;
    private AxisLine axisLine;
    private AxisLabel axisLabel;
    private float min;

    public YAxis setType(String type) {
        this.type = type;
        return this;
    }
    public String getType() {
        return type;
    }

    public YAxis setName(String name) {
        this.name = name;
        return this;
    }
    public String getName() {
        return name;
    }

    public YAxis setPosition(String position) {
        this.position = position;
        return this;
    }
    public String getPosition() {
        return position;
    }

    public YAxis setAlignTicks(boolean alignTicks) {
        this.alignTicks = alignTicks;
        return this;
    }
    public boolean getAlignTicks() {
        return alignTicks;
    }

    public YAxis setAxisLine(AxisLine axisLine) {
        this.axisLine = axisLine;
        return this;
    }
    public AxisLine getAxisLine() {
        return axisLine;
    }

    public YAxis setAxisLabel(AxisLabel axisLabel) {
        this.axisLabel = axisLabel;
        return this;
    }
    public AxisLabel getAxisLabel() {
        return axisLabel;
    }

    public YAxis setMin(float min) {
        this.min = min;
        return this;
    }
    public float getMin() {
        return min;
    }

}
