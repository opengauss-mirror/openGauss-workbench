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
 *  ExporterInstallServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/
 *  src/test/java/com/nctigba/observability/instance/service/ExporterInstallServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.nctigba.observability.instance.model.dto.ExporterInstallDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;

import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.ClusterManager.OpsClusterNodeVOSub;
import com.nctigba.observability.instance.util.MessageSourceUtils;
import com.nctigba.observability.instance.util.SshSessionUtils;

import cn.hutool.http.HttpUtil;

/**
 * ExporterInstallServiceTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class ExporterInstallServiceTest {
    @InjectMocks
    private ExporterInstallService exporterInstallService;
    @Mock
    private HostFacade hostFacade;
    @Mock
    private EncryptionUtils encryptionUtils;
    @Mock
    private HostUserFacade hostUserFacade;
    @Mock
    private NctigbaEnvMapper envMapper;
    @Mock
    private WsUtil wsUtil;
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private ResourceLoader loader;
    @Mock
    private SshSessionUtils sshSession;

    @BeforeEach
    void setup() throws IOException {
        OpsClusterNodeVOSub node = new OpsClusterNodeVOSub();
        node.setNodeId("id");
        node.setInstallUserName("name");
        node.setDbUser("name");
        node.setDbUserPassword("password");
        node.setDbPort(123);
        node.setHostId("id");
        when(clusterManager.getOpsNodeById(anyString())).thenReturn(node);

        OpsHostEntity host = new OpsHostEntity();
        host.setHostId("id");
        host.setPublicIp("123");
        host.setPort(123);
        when(hostFacade.getById(any())).thenReturn(host);
        when(loader.getResource(anyString())).thenReturn(new FileSystemResource(File.createTempFile("tmp", "tmp")));

        OpsHostUserEntity user = new OpsHostUserEntity();
        user.setUsername("name");
        user.setPassword("");
        when(hostUserFacade.listHostUserByHostId(anyString())).thenReturn(List.of(user));
        NctigbaEnvDO nul = null;
        when(envMapper.selectOne(any()))
                .thenReturn(new NctigbaEnvDO().setPath("11").setUsername("name").setHostid("host"), nul);
        when(encryptionUtils.decrypt(anyString())).thenReturn("");
        when(sshSession.test(anyString())).thenReturn(false);
    }

    @Test
    void test() throws IOException {
        try (MockedStatic<SshSessionUtils> mockStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<MessageSourceUtils> msg = mockStatic(MessageSourceUtils.class);
             MockedStatic<HttpUtil> util = mockStatic(HttpUtil.class)) {
            when(sshSession.execute(anyString())).thenReturn("scrape_configs:" + System.lineSeparator()
                    + "- scheme: http" + System.lineSeparator() + "" + "  job_name: prometheus" + System.lineSeparator()
                    + "  static_configs:" + System.lineSeparator() + "  - targets:" + System.lineSeparator()
                    + "    - localhost:9090");
            mockStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                    .thenReturn(sshSession);
            msg.when(() -> MessageSourceUtils.get(anyString())).thenReturn("");
            util.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn(null);
            util.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn(null);
            exporterInstallService.install(new WsSession(null, "id"), new ExporterInstallDTO());

            exporterInstallService.uninstall(new WsSession(null, "id"), "id");
        }
    }
}