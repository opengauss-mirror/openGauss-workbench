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
 *  TestTaskManager.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/core/TestTaskManager.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.core;

import cn.hutool.core.thread.ThreadUtil;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.service.TaskService;
import com.nctigba.observability.sql.service.impl.core.TaskManager;
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
    private DiagnosisTaskMapper taskMapper;
    @Mock
    private final ExecutorService executorService = ThreadUtil.newExecutor(CommonConstants.MAX_RUN_NODE_NUM);
    @InjectMocks
    private TaskManager taskManager;

    @Test
    public void testRun() {
        DiagnosisTaskDO task1 = new DiagnosisTaskDO();
        task1.setId(1);
        task1.setNodeId("1");
        task1.setTaskStartTime(new Date());
        task1.setTaskEndTime(new Date(new Date().getTime() + 3600000L));
        DiagnosisTaskDO task2 = new DiagnosisTaskDO();
        task2.setId(2);
        task2.setNodeId("2");
        task2.setTaskStartTime(new Date());
        task2.setTaskEndTime(new Date(new Date().getTime() + 3600000L));
        List<DiagnosisTaskDO> runTask1 = new ArrayList<>();
        runTask1.add(task1);
        List<DiagnosisTaskDO> runTask2 = new ArrayList<>();
        runTask2.add(task2);
        when(taskMapper.selectList(any())).thenReturn(runTask1).thenReturn(runTask2);
        taskManager.run();
    }
}
