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

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class MapSeries {

    private String name;
    private String type;
    private String coordinateSystem;
    private List<MapData> data;
    private Encode encode;
    private Label label;
    private Emphasis emphasis;
    private ItemStyle itemStyle;
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public void setCoordinateSystem(String coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }
    public String getCoordinateSystem() {
        return coordinateSystem;
    }

    public void setData(List<MapData> data) {
        this.data = data;
    }
    public List<MapData> getData() {
        return data;
    }

    public void setEncode(Encode encode) {
        this.encode = encode;
    }
    public Encode getEncode() {
        return encode;
    }

    public void setLabel(Label label) {
        this.label = label;
    }
    public Label getLabel() {
        return label;
    }

    public void setEmphasis(Emphasis emphasis) {
        this.emphasis = emphasis;
    }
    public Emphasis getEmphasis() {
        return emphasis;
    }

    public void setItemStyle(ItemStyle itemStyle) {
        this.itemStyle = itemStyle;
    }
    public ItemStyle getItemStyle() {
        return itemStyle;
    }

}
