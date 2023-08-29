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
public class B {

    private String color;
    private int fontSize;
    private String fontWeight;
    private int lineHeight;
    public void setColor(String color) {
         this.color = color;
     }
     public String getColor() {
         return color;
     }

    public void setFontSize(int fontSize) {
         this.fontSize = fontSize;
     }
     public int getFontSize() {
         return fontSize;
     }

    public void setFontWeight(String fontWeight) {
         this.fontWeight = fontWeight;
     }
     public String getFontWeight() {
         return fontWeight;
     }

    public void setLineHeight(int lineHeight) {
         this.lineHeight = lineHeight;
     }
     public int getLineHeight() {
         return lineHeight;
     }

}
