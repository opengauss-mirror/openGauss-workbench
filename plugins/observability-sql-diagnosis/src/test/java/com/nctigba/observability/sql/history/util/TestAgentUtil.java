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
                    .thenReturn("[\n"
                            + "    [\n"
                            + "        {\n"
                            + "            \"RES\": \"2160\",\n"
                            + "            \"%MEM\": \"0.0\",\n"
                            + "            \"PR\": \"20\",\n"
                            + "            \"%CPU\": \"5.6\",\n"
                            + "            \"S\": \"R\",\n"
                            + "            \"PID\": \"15563\",\n"
                            + "            \"COMMAND\": \"top\",\n"
                            + "            \"NI\": \"0\",\n"
                            + "            \"USER\": \"omm\",\n"
                            + "            \"SHR\": \"1532\",\n"
                            + "            \"TIME+\": \"0:00.04\",\n"
                            + "            \"VIRT\": \"162104\"\n"
                            + "        },\n"
                            + "        {\n"
                            + "            \"RES\": \"4096\",\n"
                            + "            \"%MEM\": \"0.0\",\n"
                            + "            \"PR\": \"20\",\n"
                            + "            \"%CPU\": \"0.0\",\n"
                            + "            \"S\": \"S\",\n"
                            + "            \"PID\": \"1\",\n"
                            + "            \"COMMAND\": \"systemd\",\n"
                            + "            \"NI\": \"0\",\n"
                            + "            \"USER\": \"root\",\n"
                            + "            \"SHR\": \"2504\",\n"
                            + "            \"TIME+\": \"5:18.99\",\n"
                            + "            \"VIRT\": \"193996\"\n"
                            + "        }\n"
                            + "\t],\n"
                            + "\t[\n"
                            + "        {\n"
                            + "            \"RES\": \"2160\",\n"
                            + "            \"%MEM\": \"0.0\",\n"
                            + "            \"PR\": \"20\",\n"
                            + "            \"%CPU\": \"5.6\",\n"
                            + "            \"S\": \"R\",\n"
                            + "            \"PID\": \"15563\",\n"
                            + "            \"COMMAND\": \"top\",\n"
                            + "            \"NI\": \"0\",\n"
                            + "            \"USER\": \"omm\",\n"
                            + "            \"SHR\": \"1532\",\n"
                            + "            \"TIME+\": \"0:00.04\",\n"
                            + "            \"VIRT\": \"162104\"\n"
                            + "        },\n"
                            + "        {\n"
                            + "            \"RES\": \"4096\",\n"
                            + "            \"%MEM\": \"0.0\",\n"
                            + "            \"PR\": \"20\",\n"
                            + "            \"%CPU\": \"0.0\",\n"
                            + "            \"S\": \"S\",\n"
                            + "            \"PID\": \"1\",\n"
                            + "            \"COMMAND\": \"systemd\",\n"
                            + "            \"NI\": \"0\",\n"
                            + "            \"USER\": \"root\",\n"
                            + "            \"SHR\": \"2504\",\n"
                            + "            \"TIME+\": \"5:18.99\",\n"
                            + "            \"VIRT\": \"193996\"\n"
                            + "        }\n"
                            + "\t]\n"
                            + "]");
            Object data = util.rangQuery(id, param);
            assertNotNull(data);
        }
    }
}
