/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import com.nctigba.alert.monitor.entity.NctigbaEnv;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.utils.MessageSourceUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EnvironmentService function test
 *
 * @since 2023/7/3 18:52
 */
@RunWith(SpringRunner.class)
public class EnvironmentServiceTest {
    @InjectMocks
    private EnvironmentService environmentService;
    @Mock
    private NctigbaEnvMapper envMapper;
    @Mock
    private OpsFacade opsFacade;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCluster() {
        List<OpsClusterVO> list = new ArrayList<>();
        when(opsFacade.listCluster()).thenReturn(list);
        List clusterList = environmentService.cluster();
        verify(opsFacade, times(1)).listCluster();
        assertEquals(clusterList, list);
    }

    @Test(expected = ServiceException.class)
    public void testCheckPrometheusThrowException() {
        List<NctigbaEnv> envList = new ArrayList<>();
        when(envMapper.selectList(any())).thenReturn(envList);
        environmentService.checkPrometheus();
        verify(envMapper, times(1)).selectList(any());
    }

    @Test
    public void testCheckPrometheus() {
        List<NctigbaEnv> envList = new ArrayList<>();
        NctigbaEnv nctigbaEnv = new NctigbaEnv();
        nctigbaEnv.setType(NctigbaEnv.Type.PROMETHEUS);
        envList.add(nctigbaEnv);
        when(envMapper.selectList(any())).thenReturn(envList);
        environmentService.checkPrometheus();
        verify(envMapper, times(1)).selectList(any());
    }

    @Test
    public void testGetAlertContentParam1() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            Map<String, Map<String, String>> map = environmentService.getAlertContentParam("alert");
            Assertions.assertEquals(7, map.keySet().size());
        }
    }

    @Test
    public void testGetAlertContentParam2() {
        try (MockedStatic<MessageSourceUtil> mockedStatic = mockStatic(MessageSourceUtil.class)) {
            Map<String, Map<String, String>> map = environmentService.getAlertContentParam("");
            Assertions.assertEquals(0, map.keySet().size());
        }
    }
}
