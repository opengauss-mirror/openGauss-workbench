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
 *  AlertScheduleService.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/AlertScheduleService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.model.entity.AlertScheduleDO;

import java.util.Collection;

/**
 * AlertScheduleService
 *
 * @since 2023/8/1 10:51
 */
public interface AlertScheduleService extends IService<AlertScheduleDO> {
    /**
     * add task list by ruleId list
     *
     * @param ruleIds Collection<Long>
     */
    void addTasks(Collection<Long> ruleIds);

    /**
     * remove task list by ruleId list
     *
     * @param ruleIds Collection<Long>
     */
    void removeTasks(Collection<Long> ruleIds);
}
