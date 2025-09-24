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
 * openGauss-visualtool/visualtool-service/src/main/java/org/opengauss/admin/system/plugin/beans/MonitorToolsPromDto
 * .java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.system.plugin.beans;

import io.prometheus.metrics.core.metrics.Gauge;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MonitorToolsPromDto
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonitorToolsPromDto {
    private Gauge gauge;
    private String gaugeName;
}
