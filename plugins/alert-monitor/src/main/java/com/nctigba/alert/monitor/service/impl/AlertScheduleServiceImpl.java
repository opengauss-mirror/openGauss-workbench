
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
 *  AlertScheduleServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/AlertScheduleServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertScheduleDO;
import com.nctigba.alert.monitor.mapper.AlertRuleMapper;
import com.nctigba.alert.monitor.mapper.AlertScheduleMapper;
import com.nctigba.alert.monitor.schedule.AlertLogSchedulingRunnable;
import com.nctigba.alert.monitor.schedule.TaskRegistrar;
import com.nctigba.alert.monitor.service.AlertScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AlertScheduleServiceImpl
 *
 * @since 2023/8/1 10:51
 */
@Service
public class AlertScheduleServiceImpl extends ServiceImpl<AlertScheduleMapper, AlertScheduleDO>
    implements AlertScheduleService {
    @Autowired
    private AlertRuleMapper ruleMapper;
    @Autowired
    private TaskRegistrar taskRegistrar;

    /**
     * add task list by ruleId list
     *
     * @param ruleIds Collection<Long>
     */
    @Override
    @Transactional
    public void addTasks(Collection<Long> ruleIds) {
        if (CollectionUtil.isEmpty(ruleIds)) {
            return;
        }
        List<AlertScheduleDO> alertScheduleDOS = new ArrayList<>();
        for (Long ruleId : ruleIds) {
            Long count = this.baseMapper.selectCount(Wrappers.<AlertScheduleDO>lambdaQuery()
                .eq(AlertScheduleDO::getJobName, CommonConstants.THREAD_NAME_PREFIX + ruleId));
            if (count > 0) {
                continue;
            }
            AlertRuleDO alertRuleDO = ruleMapper.selectById(ruleId);
            Integer frequency = alertRuleDO.getCheckFrequency();
            String unit = alertRuleDO.getCheckFrequencyUnit();
            long fixedDelay = 0L;
            if (unit.equals(CommonConstants.SECOND)) {
                fixedDelay = frequency.longValue() * CommonConstants.SEC_TO_MILL;
            } else if (unit.equals(CommonConstants.MINUTE)) {
                fixedDelay = frequency.longValue() * CommonConstants.MINUTE_TO_SEC * CommonConstants.SEC_TO_MILL;
            } else if (unit.equals(CommonConstants.HOUR)) {
                fixedDelay = frequency.longValue() * CommonConstants.HOUR_TO_MINUTE
                        * CommonConstants.MINUTE_TO_SEC * CommonConstants.SEC_TO_MILL;
            } else {
                fixedDelay = frequency.longValue() * CommonConstants.DAY_TO_HOUR * CommonConstants.HOUR_TO_MINUTE
                    * CommonConstants.MINUTE_TO_SEC * CommonConstants.SEC_TO_MILL;
            }

            AlertScheduleDO alertScheduleDO = new AlertScheduleDO();
            alertScheduleDO.setJobType(CommonConstants.FIXED_DELAY).setJobName(
                    CommonConstants.THREAD_NAME_PREFIX + ruleId)
                .setInitialDelay(fixedDelay).setFixedDelay(fixedDelay).setType(CommonConstants.LOG_RULE)
                .setCreateTime(LocalDateTime.now()).setLastTime(LocalDateTime.now());
            alertScheduleDOS.add(alertScheduleDO);
        }
        if (CollectionUtil.isEmpty(alertScheduleDOS)) {
            return;
        }
        this.saveBatch(alertScheduleDOS);
        for (AlertScheduleDO alertScheduleDO : alertScheduleDOS) {
            AlertLogSchedulingRunnable task = new AlertLogSchedulingRunnable(alertScheduleDO.getJobName());
            Date startTime = Date.from(LocalDateTime.now().plus(alertScheduleDO.getInitialDelay(), ChronoUnit.MILLIS)
                .atZone(ZoneId.systemDefault()).toInstant());
            taskRegistrar.addFixedDelay(task, alertScheduleDO.getFixedDelay(), startTime);
        }
    }

    /**
     * remove task list by ruleId list
     *
     * @param ruleIds Collection<Long>
     */
    public void removeTasks(Collection<Long> ruleIds) {
        if (CollectionUtil.isEmpty(ruleIds)) {
            return;
        }
        List<String> jobNameList = ruleIds.stream().map(item -> CommonConstants.THREAD_NAME_PREFIX + item).collect(
            Collectors.toList());
        List<AlertScheduleDO> scheduleList = this.list(
            Wrappers.<AlertScheduleDO>lambdaQuery().in(AlertScheduleDO::getJobName, jobNameList));
        if (CollectionUtil.isEmpty(scheduleList)) {
            return;
        }
        scheduleList.forEach(item -> {
            AlertLogSchedulingRunnable task = new AlertLogSchedulingRunnable(item.getJobName());
            taskRegistrar.removeTask(task);
            this.removeById(item.getId());
        });
    }
}
