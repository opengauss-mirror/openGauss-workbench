/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.util;

import cn.hutool.http.HttpUtil;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.util.EbpfUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.system.plugin.facade.HostFacade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * TestEbpfUtil
 *
 * @author luomeng
 * @since 2023/9/18
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEbpfUtil {
    @Mock
    private NctigbaEnvMapper envMapper;
    @Mock
    private HostFacade hostFacade;
    @InjectMocks
    private EbpfUtil util;

    @Test
    public void testCallMonitor() {
        HisDiagnosisTask task = mock(HisDiagnosisTask.class);
        String param = AgentParamCommon.FILETOP;
        try {
            util.callMonitor(task, param);
        } catch (HisDiagnosisException e) {
            assertEquals("Agent not found", e.getMessage());
        }
        NctigbaEnv env = new NctigbaEnv();
        env.setPort(11);
        env.setHostid("8080");
        when(envMapper.selectOne(any())).thenReturn(env);
        OpsHostEntity entity = new OpsHostEntity();
        entity.setPublicIp("127.0.0.1");
        when(hostFacade.getById(env.getHostid())).thenReturn(entity);
        try (MockedStatic<HttpUtil> mockStatic = mockStatic(HttpUtil.class)) {
            mockStatic.when(() -> HttpUtil.post(anyString(), anyMap())).thenReturn("test");
            String result = util.callMonitor(task, param);
            assertEquals("success", result);
        }
    }
}
