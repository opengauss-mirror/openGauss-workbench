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
