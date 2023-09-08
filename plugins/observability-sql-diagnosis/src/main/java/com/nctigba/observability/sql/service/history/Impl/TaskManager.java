/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.result.TaskState;
import com.nctigba.observability.sql.service.history.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * TaskManager
 *
 * @author luomeng
 * @since 2023/7/27
 */
@Service
@RequiredArgsConstructor
public class TaskManager {
    private final TaskService taskService;
    private final HisDiagnosisTaskMapper taskMapper;

    private final ExecutorService executorService = ThreadUtil.newExecutor(CommonConstants.MAX_RUN_NODE_NUM);

    /**
     * Run diagnosis task
     *
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void run() {
        List<HisDiagnosisTask> runTask = taskMapper.selectList(
                Wrappers.lambdaQuery(HisDiagnosisTask.class).in(HisDiagnosisTask::getState, TaskState.WAITING,
                                TaskState.SQL_RUNNING, TaskState.RECEIVING)
                        .eq(HisDiagnosisTask::getDiagnosisType, "sql"));
        if (!CollectionUtils.isEmpty(runTask)) {
            runTask.forEach(f -> {
                Duration duration = Duration.between(f.getTaskStartTime().toInstant(), new Date().toInstant());
                if (duration.toHours() >= 1) {
                    f.setState(TaskState.ERROR);
                    taskMapper.updateById(f);
                }
            });
        }
        if (runTask.size() >= CommonConstants.MAX_RUN_NODE_NUM) {
            return;
        }
        List<String> nodeList = new ArrayList<>();
        for (HisDiagnosisTask diagnosisTask : runTask) {
            nodeList.add(diagnosisTask.getNodeId());
        }
        List<HisDiagnosisTask> createTask = taskMapper.selectList(
                Wrappers.lambdaQuery(HisDiagnosisTask.class).eq(HisDiagnosisTask::getState, TaskState.CREATE)
                        .eq(HisDiagnosisTask::getDiagnosisType, "sql")
                        .notIn(HisDiagnosisTask::getNodeId, nodeList)
                        .orderByAsc(HisDiagnosisTask::getId));
        for (HisDiagnosisTask diagnosisTask : createTask) {
            if (nodeList.contains(diagnosisTask.getNodeId())) {
                continue;
            }
            if (nodeList.size() > CommonConstants.MAX_RUN_NODE_NUM) {
                break;
            }
            nodeList.add(diagnosisTask.getNodeId());
            diagnosisTask.setState(TaskState.WAITING);
            taskMapper.updateById(diagnosisTask);
            executorService.execute(() -> {
                taskService.start(diagnosisTask.getId());
            });
        }
    }
}