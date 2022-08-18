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
package org.opengauss.admin.plugin.vo.modeling.component;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class Grid {

    private String left;
    private String right;
    private String bottom;
    private boolean containLabel;
    public Grid setLeft(String left) {
        this.left = left;
        return this;
    }
    public String getLeft() {
        return left;
    }

    public Grid setRight(String right) {
        this.right = right;
        return this;
    }
    public String getRight() {
        return right;
    }

    public Grid setBottom(String bottom) {
        this.bottom = bottom;
        return this;
    }
    public String getBottom() {
        return bottom;
    }

    public Grid setContainLabel(boolean containLabel) {
        this.containLabel = containLabel;
        return this;
    }
    public boolean getContainLabel() {
        return containLabel;
    }

}
