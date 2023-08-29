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
