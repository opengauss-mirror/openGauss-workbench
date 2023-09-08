/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;

import com.nctigba.observability.instance.entity.ParamInfo;
import com.nctigba.observability.instance.mapper.DbConfigMapper;
import com.nctigba.observability.instance.mapper.ParamInfoMapper;
import com.nctigba.observability.instance.mapper.ParamValueInfoMapper;
import com.nctigba.observability.instance.model.ParamQuery;
import com.nctigba.observability.instance.service.ClusterManager.OpsClusterNodeVOSub;
import com.nctigba.observability.instance.util.MessageSourceUtil;
import com.nctigba.observability.instance.util.SshSession;

/**
 * ParamInfoServiceTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class ParamInfoServiceTest {
    @InjectMocks
    private ParamInfoService paramInfoService;
    @Mock
    private ClusterManager opsFacade;
    @Mock
    private EncryptionUtils encryptionUtils;
    @Mock
    private ParamValueInfoMapper paramValueInfoMapper;
    @Mock
    private SshSession sshSession;
    @Mock
    private DbConfigMapper dbConfigMapper;

    @Test
    void test() throws IOException, SQLException {
        when(encryptionUtils.decrypt(anyString())).thenReturn("");
        OpsClusterNodeVOSub node = new OpsClusterNodeVOSub();
        node.setPublicIp("1");
        node.setHostPort(12);
        when(opsFacade.getOpsNodeById(anyString())).thenReturn(node);
        when(dbConfigMapper.settings()).thenReturn(List.of(Map.of("", "")));
        when(sshSession.execute(anyString())).thenReturn("net.ipv6.mld_max_msf = 64" + System.lineSeparator()
                + "net.ipv6.mld_qrv = 2" + System.lineSeparator()
                + "net.ipv6.neigh.br-728fbf973182.anycast_delay = 100" + System.lineSeparator()
                + "net.ipv6.neigh.br-728fbf973182.app_solicit = 0" + System.lineSeparator()
                + "net.ipv6.neigh.br-728fbf973182.base_reachable_time_ms = 30000" + System.lineSeparator()
                + "net.ipv6.neigh.br-728fbf973182.delay_first_probe_time = 5" + System.lineSeparator());
        try (MockedStatic<SshSession> mockStatic = mockStatic(SshSession.class);
                MockedStatic<ParamInfoMapper> mapper = mockStatic(ParamInfoMapper.class);
                MockedStatic<MessageSourceUtil> util = mockStatic(MessageSourceUtil.class)) {
            util.when(() -> MessageSourceUtil.get(anyString())).thenReturn("123");
            ParamInfo nul = null;
            mapper.when(() -> ParamInfoMapper.getParamInfo(any(), anyString()))
                .thenReturn(new ParamInfo().setId(1), nul);
            mockStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSession);
            paramInfoService.getParamInfo(new ParamQuery().setIsRefresh("1").setPassword("2").setNodeId("3"));
        }
    }
}