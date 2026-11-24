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
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class ShowType {

    private int key;
    private String dimension;
    private String field;
    private Boolean percentage;
    private String dateField;
    private Integer particle;
    private Integer compareWay;

    public void setKey(int key) {
        this.key = key;
    }
    public int getKey() {
        return key;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
    public String getDimension() {
        return dimension;
    }

    public void setField(String field) {
        this.field = field;
    }
    public String getField() {
        return field;
    }

    public void setPercentage(Boolean percentage) {
        this.percentage = percentage;
    }
    public Boolean getPercentage() {
        return percentage;
    }

    public void setDateField(String dateField) {
        this.dateField = dateField;
    }
    public String getDateField() {
        return dateField;
    }

    public void setParticle(Integer particle) {
        this.particle = particle;
    }
    public Integer getParticle() {
        return particle;
    }

    public void setCompareWay(Integer compareWay) {
        this.compareWay = compareWay;
    }
    public Integer getCompareWay() {
        return compareWay;
    }

}
