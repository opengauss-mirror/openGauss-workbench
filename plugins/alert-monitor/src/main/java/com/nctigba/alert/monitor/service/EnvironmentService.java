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
 *  EnvironmentService.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/EnvironmentService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import java.util.List;

/**
 * EnvironmentService
 *
 * @author wuyuebin
 * @since  2023/6/16 15:42
 */
public interface EnvironmentService {
    /**
     * get clusters
     *
     * @return List
     */
    List cluster();

    /**
     * checkPrometheus
     */
    void checkPrometheus();
}
