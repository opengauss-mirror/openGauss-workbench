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
public class Categories {

    private String name;
    private int type;
    private List<String> value;
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }
    public List<String> getValue() {
        return value;
    }

    public CategoryBody toBody(String field)
    {
        CategoryBody body = new CategoryBody(field,this.name, this.type);
        body.setValueList(this.getValue());
        return body;
    }

}
