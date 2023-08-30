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
 * ModelingDataFlowOperatorFactory.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/modeling/operator/factory/ModelingDataFlowOperatorFactory.java
 *
 * -------------------------------------------------------------------------
 */

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
