/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.schedule;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.AlertSchedule;
import com.nctigba.alert.monitor.service.AlertScheduleService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TaskRegistrar
 *
 * @since 2023/8/1 11:53
 */
@Component
public class TaskRegistrar implements DisposableBean {
    private final Map<Runnable, ScheduledTask> scheduledTasks = new ConcurrentHashMap<>(16);

    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private AlertScheduleService scheduleService;

    public TaskScheduler getScheduler() {
        return this.taskScheduler;
    }

    /**
     * addFixedRateTask
     *
     * @param task Runnable
     * @param fixedRate Long
     * @param startTime Date
     */
    public void addFixedRateTask(Runnable task, Long fixedRate, Date startTime) {
        if (this.scheduledTasks.containsKey(task)) {
            removeTask(task);
        }
        ScheduledTask scheduledTask = new ScheduledTask();
        if (startTime != null) {
            scheduledTask.future = this.taskScheduler.scheduleAtFixedRate(task, startTime, fixedRate);
        } else {
            scheduledTask.future = this.taskScheduler.scheduleAtFixedRate(task, fixedRate);
        }
        this.scheduledTasks.put(task, scheduledTask);
    }

    /**
     * addFixedDelay
     *
     * @param task Runnable
     * @param fixedRate Long
     * @param startTime Date
     */
    public void addFixedDelay(Runnable task, Long fixedRate, Date startTime) {
        if (this.scheduledTasks.containsKey(task)) {
            removeTask(task);
        }
        ScheduledTask scheduledTask = new ScheduledTask();
        if (startTime != null) {
            scheduledTask.future = this.taskScheduler.scheduleWithFixedDelay(task, startTime, fixedRate);
        } else {
            scheduledTask.future = this.taskScheduler.scheduleWithFixedDelay(task, fixedRate);
        }
        this.scheduledTasks.put(task, scheduledTask);
    }

    /**
     * addCronTask
     *
     * @param task Runnable
     * @param cronExpression String
     */
    public void addCronTask(Runnable task, String cronExpression) {
        CronTask cronTask = new CronTask(task, cronExpression);
        if (this.scheduledTasks.containsKey(task)) {
            removeTask(task);
        }
        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.future = this.taskScheduler.schedule(task, cronTask.getTrigger());
        this.scheduledTasks.put(task, scheduledTask);
    }

    /**
     * removeTask
     *
     * @param task Runnable
     */
    public void removeTask(Runnable task) {
        ScheduledTask scheduledTask = this.scheduledTasks.remove(task);
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
    }

    /**
     * destroy
     */
    @Override
    public void destroy() {
        for (ScheduledTask task : this.scheduledTasks.values()) {
            task.cancel();
        }
        // change locked to unlocked
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<AlertSchedule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(AlertSchedule::getIsLocked, CommonConstants.UNLOCK).set(AlertSchedule::getLastTime, now)
            .set(AlertSchedule::getUpdateTime, now).eq(AlertSchedule::getIsLocked, CommonConstants.LOCKED);
        scheduleService.update(updateWrapper);
        this.scheduledTasks.clear();
    }
}
