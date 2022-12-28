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
package org.opengauss.admin.plugin.controller.modeling;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.common.constant.Constants;
import org.opengauss.admin.plugin.base.BaseController;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.utils.http.HttpUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 */
@RestController
@RequestMapping("/modeling/visualization/report/share")
public class ModelingVisualizationShareController extends BaseController {

    @GetMapping("/getMapGeo/{mapName}")
    public AjaxResult getMapGeo(@PathVariable String mapName) {
        AjaxResult ajax = AjaxResult.success();
        try {
            String rspStr = HttpUtils.sendGet( "https://geojson.cn/api/data/china.json", Constants.GBK);
            JSONObject mapInfo = new JSONObject();
            mapInfo.put("name",mapName);
            mapInfo.put("data",JSONObject.parse(rspStr));
            ajax.put("mapData", mapInfo);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
        return ajax;
    }

}

