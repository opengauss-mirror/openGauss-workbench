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
 *  TestHisDiagnosisController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/controller/TestHisDiagnosisController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.controller;

import com.nctigba.observability.sql.constant.DiagnosisTypeConstants;
import com.nctigba.observability.sql.controller.DiagnosisResultController;
import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.model.dto.TreeNodeDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.service.DiagnosisService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestHisDiagnosisController
 *
 * @author luomeng
 * @since 2023/8/24
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHisDiagnosisController {
    @Mock
    private DiagnosisService service;
    @Mock
    private LocaleStringUtils localeToString;
    @Mock
    private DiagnosisResourceMapper resourceMapper;
    @InjectMocks
    private DiagnosisResultController controller;

    @Test
    public void testGetNodeDetail() {
        int taskId = 1;
        String pointName = "offCpu";
        String diagnosisType = DiagnosisTypeConstants.SQL;
        when(service.getNodeDetail(taskId, pointName, diagnosisType)).thenReturn(mock(Object.class));
        Object result = controller.getNodeDetail(taskId, pointName, diagnosisType);
        assertNotNull(result);
    }

    @Test
    public void testGetTopologyMap() {
        int taskId = 1;
        boolean isAll = true;
        String diagnosisType = DiagnosisTypeConstants.SQL;
        TreeNodeDTO treeNodeDTO = new TreeNodeDTO();
        treeNodeDTO.setIsHidden(false);
        treeNodeDTO.setPointName("");
        treeNodeDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        treeNodeDTO.setTitle("");
        treeNodeDTO.setState(DiagnosisResultDO.PointState.NOT_MATCH_OPTION);
        treeNodeDTO.appendChild(new TreeNodeDTO());
        when(service.getTopologyMap(taskId, isAll, diagnosisType)).thenReturn(treeNodeDTO);
        when(localeToString.trapLanguage(treeNodeDTO)).thenReturn(mock(Object.class));
        Object result = controller.getTopologyMap(taskId, isAll, diagnosisType);
        assertNotNull(result);
    }
}
