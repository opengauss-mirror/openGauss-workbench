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
