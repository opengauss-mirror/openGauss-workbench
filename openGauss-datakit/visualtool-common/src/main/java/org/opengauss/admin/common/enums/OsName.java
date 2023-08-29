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
 * OsName.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/enums/OsName.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author wangyl
 * @date 203/03/08 13:38
 **/
@Getter
@AllArgsConstructor
public enum OsName {
    CENTOS("centos"),
    OPEN_EULER("openEuler"),
    ALL("all");

    private String osName;
}
