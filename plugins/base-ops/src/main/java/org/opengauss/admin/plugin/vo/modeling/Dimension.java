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

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Data
public class Dimension {

    private String field;
    private Boolean customFlag = false;
    private int num;
    private List<CategoryBody> categoryBodyList = new ArrayList<>();
    private List<String> categoryName = new ArrayList<>();

    public void setField(String field) {
        this.field = field;
    }
    public String getField() {
        return field;
    }

    public void setNum(int num) {
        this.num = num;
    }
    public int getNum() {
        return this.num == 0 ? 100 : this.num;
    }

    public void addCategoryBody(CategoryBody categoryBody)
    {
        if (!categoryName.contains(categoryBody.getCategoryName()) && categoryBodyList.size() < this.getNum()) {
            categoryBodyList.add(categoryBody);
            categoryName.add(categoryBody.getCategoryName());
        }
    }

    public List<Integer> indexListByValue(String value)
    {
        if (value == null) {
            return Collections.emptyList();
        }

        List<Integer> indexList = new ArrayList<>();
        categoryBodyList.forEach(categoryBody -> {
            if (categoryBody.getType() == 1 && categoryBody.getValueList().contains(value)) {
                indexList.add(categoryBodyList.indexOf(categoryBody));
            }
            if (categoryBody.getType() == 2 && !categoryBody.getValueList().contains(value)) {
                indexList.add(categoryBodyList.indexOf(categoryBody));
            }
        });
        return indexList;
    }
}
