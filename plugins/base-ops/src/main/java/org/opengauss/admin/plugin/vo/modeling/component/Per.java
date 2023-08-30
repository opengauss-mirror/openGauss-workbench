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
import java.util.Date;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class Per {

    private String color;
    private String backgroundColor;
    private Date padding;
    private int borderRadius;
    public void setColor(String color) {
         this.color = color;
     }
     public String getColor() {
         return color;
     }

    public void setBackgroundColor(String backgroundColor) {
         this.backgroundColor = backgroundColor;
     }
     public String getBackgroundColor() {
         return backgroundColor;
     }

    public void setPadding(Date padding) {
         this.padding = padding;
     }
     public Date getPadding() {
         return padding;
     }

    public void setBorderRadius(int borderRadius) {
         this.borderRadius = borderRadius;
     }
     public int getBorderRadius() {
         return borderRadius;
     }

}
