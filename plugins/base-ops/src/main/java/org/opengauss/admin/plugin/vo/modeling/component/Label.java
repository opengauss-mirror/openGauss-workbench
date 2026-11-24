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
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class Label {

    private String position;
    private int fontSize;
    private boolean show;
    private String formatter;

    public Label setPosition(String position) {
        this.position = position;
        return this;
    }
    public String getPosition() {
        return position;
    }

    public Label setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }
    public int getFontSize() {
        return fontSize == 0 ? 10 : fontSize;
    }

    public Label setShow(boolean show) {
        this.show = show;
        return this;
    }
    public boolean getShow() {
        return show;
    }

    public Label setFormatter(String formatter) {
        this.formatter = formatter;
        return this;
    }
    public String getFormatter() {
        return formatter;
    }
}
