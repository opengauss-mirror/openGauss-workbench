/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * ModelingVisualizationChartFactory.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/visualization/factory/ModelingVisualizationChartFactory.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.modeling.visualization.factory;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.plugin.service.modeling.visualization.IModelingVisualizationChartGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Service
public class ModelingVisualizationChartFactory {

    @Autowired
    Map<String, IModelingVisualizationChartGenerateService> generateServiceMap = new ConcurrentHashMap<>(3);

    public IModelingVisualizationChartGenerateService getGenerateServiceByJsonParams(JSONObject params) throws Exception{
        String component = params.getJSONObject("paramsData").getString("chartType");
        IModelingVisualizationChartGenerateService generator = getGenerateService(component);
        if (component == null || generator == null) {
            throw new RuntimeException("chart type not found!");
        }
        generator.setFullParams(params);
        return generator;
    }

    public IModelingVisualizationChartGenerateService getGenerateService(String component) throws Exception{
        return generateServiceMap.get(component);
    }
}
