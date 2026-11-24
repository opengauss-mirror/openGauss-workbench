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
public class Location {

    private String field;
    private int type;
    private Long commonValue;
    private String diyValue;
    private int area;
    public void setField(String field) {
        this.field = field;
    }
    public String getField() {
        return field;
    }

    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }

    public void setCommonValue(Long commonValue) {
        this.commonValue = commonValue;
    }
    public Long getCommonValue() {
        return commonValue;
    }

    public void setDiyValue(String diyValue) {
        this.diyValue = diyValue;
    }
    public String getDiyValue() {
        return diyValue;
    }

    public void setArea(int area) {
        this.area = area;
    }
    public int getArea() {
        return area;
    }

}
