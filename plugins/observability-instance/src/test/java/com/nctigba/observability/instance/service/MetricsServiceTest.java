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
 *  MetricsServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/test/java/com/nctigba/observability/instance/service/MetricsServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.http.HttpUtil;
import com.nctigba.observability.instance.enums.MetricsLine;
import com.nctigba.observability.instance.enums.MetricsValue;
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.ClusterManager.OpsClusterNodeVOSub;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * MetricsServiceTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
@Slf4j
class MetricsServiceTest {
    private static final Enum<?>[] METRICS = {MetricsLine.CPU, MetricsValue.SWAP_FREE};

    @InjectMocks
    private MetricsService metricsService;
    @Mock
    private NctigbaEnvMapper envMapper;
    @Mock
    private HostFacade hostFacade;
    @Mock
    private ClusterManager clusterManager;

    @BeforeEach
    void setup() {
        var host = new OpsHostEntity();
        host.setPublicIp("");
        when(hostFacade.getById(anyString())).thenReturn(host);

        OpsClusterNodeVOSub node = new OpsClusterNodeVOSub();
        node.setNodeId("id");
        node.setInstallUserName("name");
        node.setDbUser("name");
        node.setDbUserPassword("password");
        node.setDbPort(123);
        node.setHostId("id");
        when(clusterManager.getOpsNodeById(anyString())).thenReturn(node);
        when(envMapper.selectOne(any())).thenReturn(new NctigbaEnvDO().setPort(1).setHostid("host"));
    }

    @Test
    void test() throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class); MockedStatic<ThreadUtil> thread = mockStatic(
                ThreadUtil.class)) {
            http.when(() -> HttpUtil.get(any(), anyMap())).thenAnswer(invocation -> {
                var url = invocation.getArgument(0);
                if ("http://:1/api/v1/query".equals(url)) {
                    return "{'status':'success','data':{'resultType':'vector','result':[{'metric':{'__name__':'agent_f"
                            + "ree_Swap_free_bytes','type':'exporter'},'value':[1689294857.390,'5823524864']}]}}";
                }
                return "{'status':'success','data':{'resultType':'matrix','result':[{'metric':{},"
                        + "'values':[[1689294785.463,'13.90762676073958']]}]}}";
            });
            metricsService.list("1", 1, 1, 1);
            metricsService.value("1", 1L);
            when(ThreadUtil.newCountDownLatch(anyInt())).thenReturn(new CountDownLatch(0));
            metricsService.listBatch(METRICS, "id", 1L, 1L, 1);

            var extra = metricsService.getClass()
                    .getDeclaredMethod("extracted", OpsClusterNodeVO.class, Long.class, Long.class, Integer.class,
                            Object.class);
            extra.setAccessible(true);
            OpsClusterNodeVO vo = new OpsClusterNodeVO();
            vo.setNodeId("id");
            vo.setHostId("id");
            for (Enum<?> enum1 : METRICS) {
                extra.invoke(metricsService, vo, 1L, 1L, 1, enum1);
            }
        }
    }
}