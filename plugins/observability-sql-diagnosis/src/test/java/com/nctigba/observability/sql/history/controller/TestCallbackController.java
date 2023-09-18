/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.controller;

import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import com.nctigba.observability.sql.controller.CallbackController;
import com.nctigba.observability.sql.service.history.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * TestCallbackController
 *
 * @author luomeng
 * @since 2023/8/24
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCallbackController {
    @Mock
    private TaskService taskService;
    @InjectMocks
    private CallbackController controller;

    @Test
    public void testDiagnosisResult() {
        String id = "1";
        String type = AgentParamCommon.FILETOP;
        MultipartFile file = mock(MultipartFile.class);
        doNothing().when(taskService).bccResult(id, type, file);
        controller.diagnosisResult(id, type, file);
    }
}
