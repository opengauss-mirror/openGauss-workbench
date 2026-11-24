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
public class VisualMap {

    private int min;
    private int max;
    private List<String> text;
    private boolean calculable;
    private String orient;
    private String left;
    private String bottom;
    private InRange inRange;

    public VisualMap setMin(int min) {
        this.min = min;
        return this;
    }
    public int getMin() {
        return min;
    }

    public VisualMap setMax(int max) {
        this.max = max;
        return this;
    }
    public int getMax() {
        return max;
    }

    public VisualMap setCalculable(boolean calculable) {
        this.calculable = calculable;
        return this;
    }
    public boolean getCalculable() {
        return calculable;
    }

    public VisualMap setOrient(String orient) {
        this.orient = orient;
        return this;
    }
    public String getOrient() {
        return orient;
    }

    public VisualMap setLeft(String left) {
        this.left = left;
        return this;
    }
    public String getLeft() {
        return left;
    }

    public VisualMap setBottom(String bottom) {
        this.bottom = bottom;
        return this;
    }
    public String getBottom() {
        return bottom;
    }
    public VisualMap setInRange(InRange inRange) {
        this.inRange = inRange;
        return this;
    }
    public InRange getInRange() {
        return inRange;
    }
    public VisualMap setText(List<String> text) {
        this.text = text;
        return this;
    }
    public List<String> getText() {
        return text;
    }

}
