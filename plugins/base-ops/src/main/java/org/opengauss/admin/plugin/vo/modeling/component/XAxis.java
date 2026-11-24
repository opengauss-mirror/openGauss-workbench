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
public class XAxis {

    private String type;
    private boolean boundaryGap;
    private SplitArea splitArea;
    private List<String> data;

    public XAxis setType(String type) {
        this.type = type;
        return this;
    }
    public String getType() {
        return type;
    }

    public XAxis setBoundaryGap(boolean boundaryGap) {
        this.boundaryGap = boundaryGap;
        return this;
    }
    public boolean getBoundaryGap() {
        return boundaryGap;
    }

    public XAxis setSplitArea(SplitArea splitArea) {
        this.splitArea = splitArea;
        return this;
    }
    public SplitArea getSplitArea() {
        return splitArea;
    }

    public XAxis setData(List<String> data) {
        this.data = data;
        return this;
    }
    public List<String> getData() {
        return data;
    }

}
