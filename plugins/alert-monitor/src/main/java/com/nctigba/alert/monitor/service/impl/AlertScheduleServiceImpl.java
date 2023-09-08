
/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.AlertRule;
import com.nctigba.alert.monitor.entity.AlertSchedule;
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
public class AlertScheduleServiceImpl extends ServiceImpl<AlertScheduleMapper, AlertSchedule>
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
        List<AlertSchedule> alertSchedules = new ArrayList<>();
        for (Long ruleId : ruleIds) {
            Long count = this.baseMapper.selectCount(Wrappers.<AlertSchedule>lambdaQuery().eq(AlertSchedule::getJobName,
                CommonConstants.THREAD_NAME_PREFIX + ruleId));
            if (count > 0) {
                continue;
            }
            AlertRule alertRule = ruleMapper.selectById(ruleId);
            Integer frequency = alertRule.getCheckFrequency();
            String unit = alertRule.getCheckFrequencyUnit();
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

            AlertSchedule alertSchedule = new AlertSchedule();
            alertSchedule.setJobType(CommonConstants.FIXED_DELAY).setJobName(
                    CommonConstants.THREAD_NAME_PREFIX + ruleId)
                .setInitialDelay(fixedDelay).setFixedDelay(fixedDelay).setType(CommonConstants.LOG_RULE)
                .setCreateTime(LocalDateTime.now()).setLastTime(LocalDateTime.now());
            alertSchedules.add(alertSchedule);
        }
        if (CollectionUtil.isEmpty(alertSchedules)) {
            return;
        }
        this.saveBatch(alertSchedules);
        for (AlertSchedule alertSchedule : alertSchedules) {
            AlertLogSchedulingRunnable task = new AlertLogSchedulingRunnable(alertSchedule.getJobName());
            Date startTime = Date.from(LocalDateTime.now().plus(alertSchedule.getInitialDelay(), ChronoUnit.MILLIS)
                .atZone(ZoneId.systemDefault()).toInstant());
            taskRegistrar.addFixedDelay(task, alertSchedule.getFixedDelay(), startTime);
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
        List<AlertSchedule> scheduleList = this.list(
            Wrappers.<AlertSchedule>lambdaQuery().in(AlertSchedule::getJobName, jobNameList));
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
