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
public class A {

    private String color;
    private int lineHeight;
    private String align;
    public void setColor(String color) {
         this.color = color;
     }
     public String getColor() {
         return color;
     }

    public void setLineHeight(int lineHeight) {
         this.lineHeight = lineHeight;
     }
     public int getLineHeight() {
         return lineHeight;
     }

    public void setAlign(String align) {
         this.align = align;
     }
     public String getAlign() {
         return align;
     }

}
