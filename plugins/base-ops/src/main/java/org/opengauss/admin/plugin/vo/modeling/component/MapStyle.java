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
