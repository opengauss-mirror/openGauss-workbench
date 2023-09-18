/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.controller.HisDiagnosisTaskController;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.HisDiagnosisTaskDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.model.history.query.TaskQuery;
import com.nctigba.observability.sql.service.history.TaskService;
import com.nctigba.observability.sql.util.LocaleString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.bind.WebDataBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestHisDiagnosisTaskController
 *
 * @author luomeng
 * @since 2023/8/24
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHisDiagnosisTaskController {
    @Mock
    private TaskService taskService;
    @Mock
    private LocaleString localeToString;
    @InjectMocks
    private HisDiagnosisTaskController controller;

    @Test
    public void testInitBinder() {
        WebDataBinder webDataBinder = mock(WebDataBinder.class);
        controller.initBinder(webDataBinder);
    }

    @Test
    public void testStart() {
        HisDiagnosisTaskDTO hisDiagnosisTaskDTO = new HisDiagnosisTaskDTO();
        hisDiagnosisTaskDTO.setDiagnosisType(DiagnosisTypeCommon.HISTORY);
        int taskId = 1;
        when(taskService.add(hisDiagnosisTaskDTO)).thenReturn(taskId);
        doNothing().when(taskService).start(taskId);
        controller.start(hisDiagnosisTaskDTO);
    }

    @Test
    public void testOption() {
        String diagnosisType = DiagnosisTypeCommon.SQL;
        OptionQuery query = mock(OptionQuery.class);
        List<OptionQuery> list = new ArrayList<>(Collections.singleton(query));
        when(taskService.getOption(diagnosisType)).thenReturn(list);
        Object result = controller.option(diagnosisType);
        assertNotNull(result);
    }

    @Test
    public void testSelectByPage() {
        TaskQuery query = mock(TaskQuery.class);
        IPage<HisDiagnosisTask> page = mock(IPage.class);
        when(taskService.selectByPage(query)).thenReturn(page);
        Object result = controller.selectByPage(query);
        assertNotNull(result);
    }

    @Test
    public void testDelete() {
        int taskId = 1;
        doNothing().when(taskService).delete(taskId);
        controller.delete(taskId);
    }

    @Test
    public void testSelectById() {
        int taskId = 1;
        HisDiagnosisTask task = mock(HisDiagnosisTask.class);
        when(taskService.selectById(taskId)).thenReturn(task);
        Object result = controller.selectById(taskId);
        assertNotNull(result);
    }
}
