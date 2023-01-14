/**
 Copyright  (c) 2020 Huawei Technologies Co.,Ltd.
 Copyright  (c) 2021 openGauss Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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
