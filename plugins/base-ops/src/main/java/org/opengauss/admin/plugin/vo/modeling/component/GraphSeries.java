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
import java.util.Map;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class GraphSeries {

    private String name;
    private String type;
    private String layout;
    private List<GraphNode> data;
    private List<GraphLink> links;
    private List<Map<String,String>> categories;

    private boolean roam;
    private Label label;
    private LineStyle lineStyle;
    private Emphasis emphasis;
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

    public void setLayout(String layout) {
        this.layout = layout;
    }
    public String getLayout() {
        return layout;
    }

    public void setData(List<GraphNode> data) {
        this.data = data;
    }
    public List<GraphNode> getData() {
        return data;
    }

    public void setLinks(List<GraphLink> links) {
        this.links = links;
    }
    public List<GraphLink> getLinks() {
        return links;
    }

    public void setCategories(List<Map<String,String>> categories) {
        this.categories = categories;
    }
    public List<Map<String,String>> getCategories() {
        return categories;
    }

    public void setRoam(boolean roam) {
        this.roam = roam;
    }
    public boolean getRoam() {
        return roam;
    }

    public void setLabel(Label label) {
        this.label = label;
    }
    public Label getLabel() {
        return label;
    }

    public void setLineStyle(LineStyle lineStyle) {
        this.lineStyle = lineStyle;
    }
    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public void setEmphasis(Emphasis emphasis) {
        this.emphasis = emphasis;
    }
    public Emphasis getEmphasis() {
        return emphasis;
    }

}
