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

