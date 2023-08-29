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
