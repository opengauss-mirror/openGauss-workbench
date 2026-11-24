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
public class DataZoomItem {

    private String type;
    private Integer start;
    private Integer end;

    public DataZoomItem setType(String type) {
        this.type = type;
        return this;
    }
    public String getType() {
        return type;
    }

    public DataZoomItem setStart(Integer start) {
        this.start = start;
        return this;
    }
    public Integer getStart() {
        return start;
    }

    public DataZoomItem setEnd(Integer end) {
        this.end = end;
        return this;
    }
    public Integer getEnd() {
        return end;
    }
}
