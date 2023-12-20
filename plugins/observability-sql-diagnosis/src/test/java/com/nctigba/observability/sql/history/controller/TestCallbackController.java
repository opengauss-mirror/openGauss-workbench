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
 *  TestCallbackController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/controller/TestCallbackController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.controller;

import com.nctigba.observability.sql.constant.AgentParamConstants;
import com.nctigba.observability.sql.controller.CallbackController;
import com.nctigba.observability.sql.service.TaskService;
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
        String type = AgentParamConstants.FILE_TOP;
        MultipartFile file = mock(MultipartFile.class);
        doNothing().when(taskService).bccResult(id, type, file);
        controller.diagnosisResult(id, type, file);
    }
}
