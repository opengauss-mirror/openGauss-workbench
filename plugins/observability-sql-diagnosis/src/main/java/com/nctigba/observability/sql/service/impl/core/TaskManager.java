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
 *  TaskManager.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/core/TaskManager.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.core;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.enums.TaskStateEnum;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.service.TaskService;
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
    private final DiagnosisTaskMapper taskMapper;

    private final ExecutorService executorService = ThreadUtil.newExecutor(CommonConstants.MAX_RUN_NODE_NUM);

    /**
     * Run diagnosis task
     *
     */
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void run() {
        List<DiagnosisTaskDO> runTask = taskMapper.selectList(
                Wrappers.lambdaQuery(DiagnosisTaskDO.class).in(
                                DiagnosisTaskDO::getState, TaskStateEnum.WAITING,
                                TaskStateEnum.SQL_RUNNING, TaskStateEnum.RECEIVING)
                        .eq(DiagnosisTaskDO::getDiagnosisType, "sql"));
        if (!CollectionUtils.isEmpty(runTask)) {
            runTask.forEach(f -> {
                Duration duration = Duration.between(f.getTaskStartTime().toInstant(), new Date().toInstant());
                if (duration.toHours() >= 1) {
                    f.setState(TaskStateEnum.TIMEOUT_ERROR);
                    taskMapper.updateById(f);
                }
            });
        }
        if (runTask.size() >= CommonConstants.MAX_RUN_NODE_NUM) {
            return;
        }
        List<String> nodeList = new ArrayList<>();
        for (DiagnosisTaskDO diagnosisTask : runTask) {
            nodeList.add(diagnosisTask.getNodeId());
        }
        LambdaQueryWrapper<DiagnosisTaskDO> sql = Wrappers.lambdaQuery(DiagnosisTaskDO.class).eq(
                        DiagnosisTaskDO::getState, TaskStateEnum.CREATE)
                .eq(DiagnosisTaskDO::getDiagnosisType, "sql");
        if (!nodeList.isEmpty()) {
            sql.notIn(DiagnosisTaskDO::getNodeId, nodeList);
        }
        sql.orderByAsc(DiagnosisTaskDO::getId);
        List<DiagnosisTaskDO> createTask = taskMapper.selectList(sql);
        for (DiagnosisTaskDO diagnosisTask : createTask) {
            if (nodeList.contains(diagnosisTask.getNodeId())) {
                continue;
            }
            if (nodeList.size() > CommonConstants.MAX_RUN_NODE_NUM) {
                break;
            }
            nodeList.add(diagnosisTask.getNodeId());
            diagnosisTask.setState(TaskStateEnum.WAITING);
            taskMapper.updateById(diagnosisTask);
            executorService.execute(() -> taskService.start(diagnosisTask.getId()));
        }
    }
}