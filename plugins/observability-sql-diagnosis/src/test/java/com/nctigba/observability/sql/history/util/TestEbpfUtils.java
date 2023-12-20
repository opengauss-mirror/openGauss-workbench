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
 *  TestEbpfUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/util/TestEbpfUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.util;

import cn.hutool.http.HttpUtil;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.AgentParamConstants;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.entity.NctigbaEnvDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.util.EbpfUtils;
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
public class TestEbpfUtils {
    @Mock
    private NctigbaEnvMapper envMapper;
    @Mock
    private HostFacade hostFacade;
    @InjectMocks
    private EbpfUtils util;

    @Test
    public void testCallMonitor() {
        DiagnosisTaskDO task = mock(DiagnosisTaskDO.class);
        String param = AgentParamConstants.FILE_TOP;
        try {
            util.callMonitor(task, param);
        } catch (HisDiagnosisException e) {
            assertEquals("Agent not found", e.getMessage());
        }
        NctigbaEnvDO env = new NctigbaEnvDO();
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
