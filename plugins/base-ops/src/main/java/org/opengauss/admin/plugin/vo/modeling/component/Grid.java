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
