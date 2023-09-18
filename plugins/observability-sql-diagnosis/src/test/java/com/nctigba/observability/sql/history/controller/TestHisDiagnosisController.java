/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.controller;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.controller.HisDiagnosisController;
import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.result.HisTreeNode;
import com.nctigba.observability.sql.model.history.result.Resource;
import com.nctigba.observability.sql.service.history.HisDiagnosisService;
import com.nctigba.observability.sql.util.LocaleString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    private HisDiagnosisService service;
    @Mock
    private LocaleString localeToString;
    @Mock
    private DiagnosisResourceMapper resourceMapper;
    @InjectMocks
    private HisDiagnosisController controller;

    @Test
    public void testGetNodeDetail() {
        int taskId = 1;
        String pointName = "offCpu";
        String diagnosisType = DiagnosisTypeCommon.SQL;
        when(service.getNodeDetail(taskId, pointName, diagnosisType)).thenReturn(mock(Object.class));
        Object result = controller.getNodeDetail(taskId, pointName, diagnosisType);
        assertNotNull(result);
    }

    @Test
    public void testGetTopologyMap() {
        int taskId = 1;
        boolean isAll = true;
        String diagnosisType = DiagnosisTypeCommon.SQL;
        HisTreeNode treeNode = new HisTreeNode();
        treeNode.setIsHidden(false);
        treeNode.setPointName("");
        treeNode.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        treeNode.setTitle("");
        treeNode.setState(HisDiagnosisResult.PointState.NOT_MATCH_OPTION);
        treeNode.appendChild(new HisTreeNode());
        when(service.getTopologyMap(taskId, isAll, diagnosisType)).thenReturn(treeNode);
        when(localeToString.trapLanguage(treeNode)).thenReturn(mock(Object.class));
        Object result = controller.getTopologyMap(taskId, isAll, diagnosisType);
        assertNotNull(result);
    }

    @Test
    public void testRes() {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStream os = mock(ServletOutputStream.class);
        try {
            when(resp.getOutputStream()).thenReturn(os);
            HisDiagnosisTask task = mock(HisDiagnosisTask.class);
            Resource resource = new Resource(task, GrabType.profile);
            resource.setGrabType(GrabType.offcputime);
            resource.setId(1);
            resource.setTaskid(1);
            byte[] f = new byte[1024];
            resource.setF(f);
            String id = "1";
            String type = "svg";
            resource.to(os);
            when(resourceMapper.selectById(id)).thenReturn(resource);
            controller.res(id, type, resp);
            type = "png";
            controller.res(id, type, resp);
            type = "txt";
            controller.res(id, type, resp);
        } catch (IOException e) {
            throw new HisDiagnosisException("connect fail");
        }
    }
}
