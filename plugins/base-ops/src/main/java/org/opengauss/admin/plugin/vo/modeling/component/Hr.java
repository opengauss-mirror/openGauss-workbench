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
public class Hr {

    private String borderColor;
    private Date width;
    private int borderWidth;
    private int height;
    public void setBorderColor(String borderColor) {
         this.borderColor = borderColor;
     }
     public String getBorderColor() {
         return borderColor;
     }

    public void setWidth(Date width) {
         this.width = width;
     }
     public Date getWidth() {
         return width;
     }

    public void setBorderWidth(int borderWidth) {
         this.borderWidth = borderWidth;
     }
     public int getBorderWidth() {
         return borderWidth;
     }

    public void setHeight(int height) {
         this.height = height;
     }
     public int getHeight() {
         return height;
     }

}
