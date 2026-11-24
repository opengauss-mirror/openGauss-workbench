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
public class Legend {

    private List<String> data;
    private String type;
    private String width;

    private int bottom;
    private int top;

    public Legend(String type) {
        this.type = type;
    }

    public Legend setData(List<String> data) {
        this.data = data;
        return this;
    }
    public List<String> getData() {
        return data;
    }

    public String getType() {
        return this.type;
    }

    public Legend setBottom(int bottom) {
        this.bottom = bottom;
        return this;
    }

    public int getBottom() {
        return this.bottom;
    }

    public Legend setTop(int top) {
        this.top = top;
        return this;
    }

    public int getTop() {
        return this.top;
    }

    public Legend setWidth(String width) {
        this.width = width;
        return this;
    }

    public String getWidth() {
        return this.width;
    }

}
