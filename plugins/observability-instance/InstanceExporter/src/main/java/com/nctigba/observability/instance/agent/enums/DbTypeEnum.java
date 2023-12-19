/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  DbTypeEnum.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/enums/DbTypeEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DbTypeEnum
 *
 * @author liupengfei
 * @since 2023/9/6
 */
@AllArgsConstructor
@Getter
public enum DbTypeEnum {
    OPENGAUSS("org.opengauss.Driver",
        "jdbc:opengauss://{}:{}/postgres?" + "TimeZone=UTC&ApplicationName=DataKit Instance Monitoring Agent");
    private final String driverClass;
    private final String urlPattern;
}
