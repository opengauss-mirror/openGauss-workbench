/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nctigba.alert.monitor.entity.AlertSchedule;

import java.util.Collection;

/**
 * AlertScheduleService
 *
 * @since 2023/8/1 10:51
 */
public interface AlertScheduleService extends IService<AlertSchedule> {
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
