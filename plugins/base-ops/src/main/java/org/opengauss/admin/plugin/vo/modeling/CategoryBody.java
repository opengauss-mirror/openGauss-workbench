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

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class CategoryBody {

    private String field;
    private String categoryName;
    private Integer type;
    private List<String> valueList;

    public CategoryBody(String field, String categoryName, Integer type) {
        this.field = field;
        this.categoryName = categoryName;
        this.type = type;
    }

    public void setField(String field) {
        this.field = field;
    }
    public String getField() {
        return field;
    }

    public void setCategoryName(String field) {
        this.categoryName = field;
    }
    public String getCategoryName() {
        return categoryName;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Integer getType() {
        return type == 0 ? 1 : type;
    }

    public void setValueList(List<String> valueList) {
        this.valueList = valueList;
    }
    public List<String> getValueList() {
        return this.valueList;
    }

}
