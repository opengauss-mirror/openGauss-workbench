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
public class MapStyle {

    private List<StyleJson> styleJson;
    public MapStyle setStyleJson(List<StyleJson> styleJson) {
        this.styleJson = styleJson;
        return this;
    }
    public List<StyleJson> getStyleJson() {
        return styleJson;
    }

    public MapStyle setStyleJsonDefault() {
        this.styleJson = List.of(
                new StyleJson().setFeatureType("water").setElementType("all").setStylers(new Stylers().setColor("#044161")),
                new StyleJson().setFeatureType("land").setElementType("all").setStylers(new Stylers().setColor("#004981")),
                new StyleJson().setFeatureType("boundary").setElementType("geometry").setStylers(new Stylers().setColor("#064f85")),
                new StyleJson().setFeatureType("railway").setElementType("all").setStylers(new Stylers().setColor("#004981")),
                new StyleJson().setFeatureType("highway").setElementType("geometry").setStylers(new Stylers().setColor("#004981")),
                new StyleJson().setFeatureType("highway").setElementType("geometry.fill").setStylers(new Stylers().setColor("##005b96")),
                new StyleJson().setFeatureType("water").setElementType("all").setStylers(new Stylers().setColor("#044161"))
                );
        return this;
    }
}
