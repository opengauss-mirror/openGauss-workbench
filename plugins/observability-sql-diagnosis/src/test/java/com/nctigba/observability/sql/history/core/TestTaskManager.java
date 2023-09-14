/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.core;

import cn.hutool.core.thread.ThreadUtil;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.service.history.TaskService;
import com.nctigba.observability.sql.service.history.core.TaskManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TestTaskManager
 *
 * @author luomeng
 * @since 2023/8/29
 */
@RunWith(MockitoJUnitRunner.class)
public class TestTaskManager {
    @Mock
    private TaskService taskService;
    @Mock
    private HisDiagnosisTaskMapper taskMapper;
    @Mock
    private final ExecutorService executorService = ThreadUtil.newExecutor(CommonConstants.MAX_RUN_NODE_NUM);
    @InjectMocks
    private TaskManager taskManager;

    @Test
    public void testRun() {
        HisDiagnosisTask task1 = new HisDiagnosisTask();
        task1.setId(1);
        task1.setNodeId("1");
        task1.setTaskStartTime(new Date());
        task1.setTaskEndTime(new Date(new Date().getTime() + 3600000L));
        HisDiagnosisTask task2 = new HisDiagnosisTask();
        task2.setId(2);
        task2.setNodeId("2");
        task2.setTaskStartTime(new Date());
        task2.setTaskEndTime(new Date(new Date().getTime() + 3600000L));
        List<HisDiagnosisTask> runTask1 = new ArrayList<>();
        runTask1.add(task1);
        List<HisDiagnosisTask> runTask2 = new ArrayList<>();
        runTask2.add(task2);
        when(taskMapper.selectList(any())).thenReturn(runTask1).thenReturn(runTask2);
        taskManager.run();
    }
}
