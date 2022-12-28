package org.opengauss.admin.plugin.service.modeling.operator.factory;

import org.opengauss.admin.plugin.service.modeling.operator.IModelingDataFlowOperatorBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
@Service
public class ModelingDataFlowOperatorFactory {

    @Autowired
    Map<String, IModelingDataFlowOperatorBuilderService> builderServiceMap = new ConcurrentHashMap<>(3);

    public IModelingDataFlowOperatorBuilderService getBuilder(String component) {
        return builderServiceMap.get(component);
    }

}
