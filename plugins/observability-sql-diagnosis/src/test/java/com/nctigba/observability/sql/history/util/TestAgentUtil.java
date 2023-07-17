/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.util;

import cn.hutool.http.HttpUtil;
import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.util.AgentUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.system.plugin.facade.HostFacade;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * TestAgentUtil
 *
 * @author luomeng
 * @since 2023/7/12
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAgentUtil {
    @Mock
    protected NctigbaEnvMapper envMapper;

    @Mock
    protected HostFacade hostFacade;
    @InjectMocks
    private AgentUtil util;

    @Test
    public void testRangQuery() {
        String id = "1";
        String param = AgentParamCommon.TOP;
        NctigbaEnv env = new NctigbaEnv();
        env.setPort(11);
        env.setHostid("8080");
        when(envMapper.selectOne(any())).thenReturn(env);
        OpsHostEntity entity = new OpsHostEntity();
        entity.setPublicIp("127.0.0.1");
        when(hostFacade.getById(env.getHostid())).thenReturn(entity);
        try (MockedStatic<HttpUtil> mockStatic = mockStatic(HttpUtil.class)) {
            mockStatic.when(() -> HttpUtil.get(anyString(), anyMap()))
                    .thenReturn("["
                            + "    ["
                            + "        {"
                            + "            \"RES\": \"2160\","
                            + "            \"%MEM\": \"0.0\","
                            + "            \"PR\": \"20\","
                            + "            \"%CPU\": \"5.6\","
                            + "            \"S\": \"R\","
                            + "            \"PID\": \"15563\","
                            + "            \"COMMAND\": \"top\","
                            + "            \"NI\": \"0\","
                            + "            \"USER\": \"omm\","
                            + "            \"SHR\": \"1532\","
                            + "            \"TIME+\": \"0:00.04\","
                            + "            \"VIRT\": \"162104\""
                            + "        },"
                            + "        {"
                            + "            \"RES\": \"4096\","
                            + "            \"%MEM\": \"0.0\","
                            + "            \"PR\": \"20\","
                            + "            \"%CPU\": \"0.0\","
                            + "            \"S\": \"S\","
                            + "            \"PID\": \"1\","
                            + "            \"COMMAND\": \"systemd\","
                            + "            \"NI\": \"0\","
                            + "            \"USER\": \"root\","
                            + "            \"SHR\": \"2504\","
                            + "            \"TIME+\": \"5:18.99\","
                            + "            \"VIRT\": \"193996\""
                            + "        }"
                            + "\t],"
                            + "\t["
                            + "        {"
                            + "            \"RES\": \"2160\","
                            + "            \"%MEM\": \"0.0\","
                            + "            \"PR\": \"20\","
                            + "            \"%CPU\": \"5.6\","
                            + "            \"S\": \"R\","
                            + "            \"PID\": \"15563\","
                            + "            \"COMMAND\": \"top\","
                            + "            \"NI\": \"0\","
                            + "            \"USER\": \"omm\","
                            + "            \"SHR\": \"1532\","
                            + "            \"TIME+\": \"0:00.04\","
                            + "            \"VIRT\": \"162104\""
                            + "        },"
                            + "        {"
                            + "            \"RES\": \"4096\","
                            + "            \"%MEM\": \"0.0\","
                            + "            \"PR\": \"20\","
                            + "            \"%CPU\": \"0.0\","
                            + "            \"S\": \"S\","
                            + "            \"PID\": \"1\","
                            + "            \"COMMAND\": \"systemd\","
                            + "            \"NI\": \"0\","
                            + "            \"USER\": \"root\","
                            + "            \"SHR\": \"2504\","
                            + "            \"TIME+\": \"5:18.99\","
                            + "            \"VIRT\": \"193996\""
                            + "        }"
                            + "\t]"
                            + "]");
            Object data = util.rangQuery(id, param);
            assertNotNull(data);
        }
    }
}
