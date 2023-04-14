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
 * DeployTypeEnum.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/ops/DeployTypeEnum.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.enums.ops;

/**
 * deploy type
 *
 * @author lhf
 * @date 2022/8/6 17:08
 **/
public enum DeployTypeEnum {
    /**
     * cluster
     */
    CLUSTER,
    /**
     * single node
     */
    SINGLE_NODE;

    public static DeployTypeEnum nameOf(String deployType) {
        DeployTypeEnum[] enumConstants = DeployTypeEnum.class.getEnumConstants();
        for (DeployTypeEnum enumConstant : enumConstants) {
            if (enumConstant.name().equalsIgnoreCase(deployType)) {
                return enumConstant;
            }
        }
        return null;
    }
}
