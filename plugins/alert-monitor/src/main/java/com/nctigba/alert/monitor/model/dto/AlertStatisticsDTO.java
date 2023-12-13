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
 *  AlertStatisticsDTO.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/model/dto/AlertStatisticsDTO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wuyuebin
 * @date 2023/4/14 14:50
 * @description
 */
@Data
@Accessors(chain = true)
public class AlertStatisticsDTO {
    private Integer totalNum = 0;
    private Integer unReadNum = 0;
    private Integer readNum = 0;
    private Integer firingNum = 0;
    private Integer recoverNum = 0;
    private Integer seriousNum = 0;
    private Integer warnNum = 0;
    private Integer infoNum = 0;
}
