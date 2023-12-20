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
 *  TestDiagnosisTaskController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/controller/TestDiagnosisTaskController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nctigba.observability.sql.constant.DiagnosisTypeConstants;
import com.nctigba.observability.sql.controller.DiagnosisTaskController;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.DiagnosisTaskDTO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.query.TaskQuery;
import com.nctigba.observability.sql.service.TaskService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
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
 * TestDiagnosisTaskDOController
 *
 * @author luomeng
 * @since 2023/8/24
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDiagnosisTaskController {
    @Mock
    private TaskService taskService;
    @Mock
    private LocaleStringUtils localeToString;
    @InjectMocks
    private DiagnosisTaskController controller;

    @Test
    public void testInitBinder() {
        WebDataBinder webDataBinder = mock(WebDataBinder.class);
        controller.initBinder(webDataBinder);
    }

    @Test
    public void testStart() {
        DiagnosisTaskDTO diagnosisTaskDTO = new DiagnosisTaskDTO();
        diagnosisTaskDTO.setDiagnosisType(DiagnosisTypeConstants.HISTORY);
        int taskId = 1;
        when(taskService.add(diagnosisTaskDTO)).thenReturn(taskId);
        doNothing().when(taskService).start(taskId);
        controller.start(diagnosisTaskDTO);
    }

    @Test
    public void testOption() {
        String diagnosisType = DiagnosisTypeConstants.SQL;
        OptionVO query = mock(OptionVO.class);
        List<OptionVO> list = new ArrayList<>(Collections.singleton(query));
        when(taskService.getOption(diagnosisType)).thenReturn(list);
        Object result = controller.option(diagnosisType);
        assertNotNull(result);
    }

    @Test
    public void testSelectByPage() {
        TaskQuery query = mock(TaskQuery.class);
        IPage<DiagnosisTaskDO> page = mock(IPage.class);
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
        DiagnosisTaskDO task = mock(DiagnosisTaskDO.class);
        when(taskService.selectById(taskId)).thenReturn(task);
        Object result = controller.selectById(taskId);
        assertNotNull(result);
    }
}
