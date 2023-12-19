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
 *  EnvironmentServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/EnvironmentServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import com.nctigba.alert.monitor.model.entity.NctigbaEnvDO;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
        List<NctigbaEnvDO> envList = new ArrayList<>();
        when(envMapper.selectList(any())).thenReturn(envList);
        environmentService.checkPrometheus();
        verify(envMapper, times(1)).selectList(any());
    }

    @Test
    public void testCheckPrometheus() {
        List<NctigbaEnvDO> envList = new ArrayList<>();
        NctigbaEnvDO nctigbaEnvDO = new NctigbaEnvDO();
        nctigbaEnvDO.setType(NctigbaEnvDO.Type.PROMETHEUS);
        envList.add(nctigbaEnvDO);
        when(envMapper.selectList(any())).thenReturn(envList);
        environmentService.checkPrometheus();
        verify(envMapper, times(1)).selectList(any());
    }
}
