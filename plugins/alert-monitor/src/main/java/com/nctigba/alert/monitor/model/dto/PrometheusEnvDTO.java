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
 *  PrometheusEnvDTO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/dto/PrometheusEnvDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.dto;

import com.nctigba.alert.monitor.model.entity.AlertConfigDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author wuyuebin
 * @date 2023/6/10 11:08
 * @description
 */
@Data
@Accessors(chain = true)
public class PrometheusEnvDTO {
    private String promIp;
    private Integer hostPort;
    private Integer promPort;
    private String promUsername;
    private String promPasswd;
    private String path;
    private List<AlertConfigDO> configList;
    private List<AlertConfigDO> delConfigList;
}
