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
 * MonitorToolsPromDto.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/beans/MonitorToolsPromDto.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.system.plugin.beans;

import io.prometheus.client.Gauge;
import lombok.Data;

@Data
public class MonitorToolsPromDto {
    private Gauge gauge;

    private String gaugeName;

    public MonitorToolsPromDto() {
    }

    public MonitorToolsPromDto(Gauge gauge, String gaugeName) {
        this.gauge = gauge;
        this.gaugeName = gaugeName;
    }
}
