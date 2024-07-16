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
 * CheckDeployTypeFunction.java
 *
 * IDENTIFICATION
 * plugins/base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/function/CheckDeployTypeFunction.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl.function;

import org.opengauss.admin.plugin.enums.ops.DeployTypeEnum;

/**
 * CheckDeployTypeFunction
 *
 * @author wangchao
 * @date 2024/6/22 9:41
 **/
@FunctionalInterface
public interface CheckDeployTypeFunction {
    /**
     * check cluster node by deploy type and size
     * @param deployType deploy type
     * @param size size
     */
    void check(DeployTypeEnum deployType, int size);
}