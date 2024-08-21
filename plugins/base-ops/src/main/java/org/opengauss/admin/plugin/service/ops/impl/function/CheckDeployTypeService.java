/*
 * Copyright (c) 2024 Huawei Technologies Co.,Ltd.
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
 * CheckDeployTypeFunctionInstance.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/function/CheckDeployTypeFunctionInstance.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.function;

import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * CheckDeployTypeFunctionInstance
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@Service
public class CheckDeployTypeService {
    private Map<OpenGaussVersionEnum, CheckDeployTypeFunction> checkFunctions = new HashMap<>();

    private final CheckDeployTypeFunction lite = (deployType, size) -> {
        if (Objects.equals(deployType, DeployTypeEnum.SINGLE_NODE)) {
            Assert.isTrue(size == 1, "Lite version single node deployment mode, host node number must be 1");
        } else {
            Assert.isTrue(size == 2, "Lite version cluster deployment mode, host node number must be 2");
        }
    };

    private final CheckDeployTypeFunction minimal = (deployType, size) -> {
        Assert.isTrue(size == 1, "minimal list version,host node number must be 1");
    };

    private final CheckDeployTypeFunction enterprise = (deployType, size) -> {
        if (Objects.equals(deployType, DeployTypeEnum.SINGLE_NODE)) {
            Assert.isTrue(size == 1, "enterprise version single node deployment mode, host node number must be 1");
        } else {
            Assert.isTrue(size > 1 && size < 10, "enterprise version cluster deployment mode ,host node number must be greater than 1 and less than 10");
        }
    };

    /**
     * constructor
     */
    public CheckDeployTypeService() {
        checkFunctions.put(OpenGaussVersionEnum.LITE, lite);
        checkFunctions.put(OpenGaussVersionEnum.MINIMAL_LIST, minimal);
        checkFunctions.put(OpenGaussVersionEnum.ENTERPRISE, enterprise);
    }

    public void check(OpenGaussVersionEnum version, DeployTypeEnum deployType, int size) {
        Assert.isTrue(Objects.nonNull(version), "OpenGaussVersion can not be null");
        Assert.isTrue(Objects.nonNull(deployType), "DeployTypeEnum can not be null");
        checkFunctions.get(version).check(deployType, size);
    }
}
