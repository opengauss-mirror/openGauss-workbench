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
