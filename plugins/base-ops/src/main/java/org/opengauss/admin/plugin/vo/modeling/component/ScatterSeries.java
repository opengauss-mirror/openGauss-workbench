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
import java.util.Map;

/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/

public class ScatterSeries {

    private String name;
    private String map;
    private String type;
    private boolean roam;

    private String coordinateSystem;
    private List<Map<String,String>> data;
    private String symbolSize;
    private Encode encode;
    private Label label;
    private Emphasis emphasis;
    private ItemStyle itemStyle;
    private int geoIndex;
    private String selectedMode;

    public void setRoam(boolean roam) {
        this.roam = roam;
    }
    public boolean getRoam() {
        return roam;
    }

    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setMap(String map) {
        this.map = map;
    }
    public String getMap() {
        return map;
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

    public void setData(List<Map<String,String>> data) {
         this.data = data;
     }
     public List<Map<String,String>> getData() {
         return data;
     }

    public void setSymbolSize(String symbolSize) {
         this.symbolSize = symbolSize;
     }
     public String getSymbolSize() {
         return symbolSize;
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

    public void setGeoIndex(int geoIndex) {
        this.geoIndex = geoIndex;
    }
    public int getGeoIndex() {
        return geoIndex;
    }

    public void setSelectedMode(String selectedMode) {
        this.selectedMode = selectedMode;
    }
    public String getSelectedMode() {
        return selectedMode;
    }
}
