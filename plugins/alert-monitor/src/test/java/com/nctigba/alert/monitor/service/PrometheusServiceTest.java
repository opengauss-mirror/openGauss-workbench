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
 *  PrometheusServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/test/java/com/nctigba/alert/monitor/service/PrometheusServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service;

import cn.hutool.http.HttpUtil;
import com.nctigba.alert.monitor.config.property.AlertProperty;
import com.nctigba.alert.monitor.config.property.AlertmanagerProperty;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.model.entity.AlertClusterNodeConfDO;
import com.nctigba.alert.monitor.model.entity.AlertConfigDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleDO;
import com.nctigba.alert.monitor.model.entity.AlertTemplateRuleItemDO;
import com.nctigba.alert.monitor.model.entity.NctigbaEnvDO;
import com.nctigba.alert.monitor.mapper.AlertClusterNodeConfMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.service.impl.PrometheusServiceImpl;
import com.nctigba.alert.monitor.util.SshSessionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.exception.ServiceException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * test PrometheusService
 *
 * @since 2023/7/5 17:36
 */
@RunWith(SpringRunner.class)
public class PrometheusServiceTest {
    @InjectMocks
    private PrometheusServiceImpl prometheusService;

    @Mock
    private AlertTemplateRuleMapper alertTemplateRuleMapper;

    @Mock
    private AlertTemplateRuleItemMapper alertTemplateRuleItemMapper;

    @Mock
    private NctigbaEnvMapper envMapper;

    @Mock
    private AlertProperty alertProperty;

    @Mock
    private AlertConfigService alertConfigService;

    @Mock
    private EncryptionUtils encryptionUtils;

    @Mock
    private HostFacade hostFacade;

    @Mock
    private HostUserFacade hostUserFacade;

    @Mock
    private AlertClusterNodeConfMapper clusterNodeConfMapper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = ServiceException.class)
    public void testPrometheusEnvDtoThrowEx1() {
        List<AlertConfigDO> alertConfigDOS = new ArrayList<>();
        AlertConfigDO alertConfigDO = new AlertConfigDO().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigDOS.add(alertConfigDO);
        when(alertConfigService.list()).thenReturn(alertConfigDOS);
        NctigbaEnvDO promEnv = null;
        when(envMapper.selectOne(any())).thenReturn(promEnv);

        prometheusService.initPrometheusConfig();

        verify(alertConfigService, times(1)).list();
        verify(envMapper, times(1)).selectOne(any());
    }

    @Test(expected = ServiceException.class)
    public void testPrometheusEnvDtoThrowEx2() {
        List<AlertConfigDO> alertConfigDOS = new ArrayList<>();
        AlertConfigDO alertConfigDO = new AlertConfigDO().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigDOS.add(alertConfigDO);
        when(alertConfigService.list()).thenReturn(alertConfigDOS);
        NctigbaEnvDO promEnv = new NctigbaEnvDO();
        when(envMapper.selectOne(any())).thenReturn(promEnv);

        prometheusService.initPrometheusConfig();

        verify(alertConfigService, times(1)).list();
        verify(envMapper, times(1)).selectOne(any());
    }

    @Test(expected = ServiceException.class)
    public void testPrometheusEnvDtoThrowEx3() {
        List<AlertConfigDO> alertConfigDOS = new ArrayList<>();
        AlertConfigDO alertConfigDO = new AlertConfigDO().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigDOS.add(alertConfigDO);
        when(alertConfigService.list()).thenReturn(alertConfigDOS);
        NctigbaEnvDO promEnv = new NctigbaEnvDO();
        promEnv.setPath("/");
        when(envMapper.selectOne(any())).thenReturn(promEnv);

        prometheusService.initPrometheusConfig();

        verify(alertConfigService, times(1)).list();
        verify(envMapper, times(1)).selectOne(any());
    }

    @Test(expected = ServiceException.class)
    public void testPrometheusEnvDtoThrowEx4() {
        List<AlertConfigDO> alertConfigDOS = new ArrayList<>();
        AlertConfigDO alertConfigDO = new AlertConfigDO().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigDOS.add(alertConfigDO);
        when(alertConfigService.list()).thenReturn(alertConfigDOS);
        NctigbaEnvDO promEnv = new NctigbaEnvDO();
        promEnv.setPath("/").setHostid("1");
        when(envMapper.selectOne(any())).thenReturn(promEnv);
        OpsHostEntity promeHost = new OpsHostEntity();
        promeHost.setHostId("1");
        promeHost.setPublicIp("127.0.0.1");
        promeHost.setPort(9090);
        when(hostFacade.getById(anyString())).thenReturn(promeHost);
        List<OpsHostUserEntity> hostUserList = new ArrayList<>();
        when(hostUserFacade.listHostUserByHostId(anyString())).thenReturn(hostUserList);

        prometheusService.initPrometheusConfig();

        verify(alertConfigService, times(1)).list();
        verify(envMapper, times(1)).selectOne(any());
        verify(hostFacade, times(1)).getById(anyString());
        verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
    }

    @Test(expected = ServiceException.class)
    public void testPrometheusEnvDtoThrowEx5() {
        List<AlertConfigDO> alertConfigDOS = new ArrayList<>();
        AlertConfigDO alertConfigDO = new AlertConfigDO().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigDOS.add(alertConfigDO);
        when(alertConfigService.list()).thenReturn(alertConfigDOS);
        NctigbaEnvDO promEnv = new NctigbaEnvDO();
        promEnv.setPath("/").setHostid("1").setUsername("user1");
        when(envMapper.selectOne(any())).thenReturn(promEnv);
        OpsHostEntity promeHost = new OpsHostEntity();
        promeHost.setHostId("1");
        promeHost.setPublicIp("127.0.0.1");
        promeHost.setPort(9090);
        when(hostFacade.getById(anyString())).thenReturn(promeHost);
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setUsername("user2");
        List<OpsHostUserEntity> hostUserList = new ArrayList<>();
        hostUserList.add(hostUserEntity);
        when(hostUserFacade.listHostUserByHostId(anyString())).thenReturn(hostUserList);

        prometheusService.initPrometheusConfig();

        verify(alertConfigService, times(1)).list();
        verify(envMapper, times(1)).selectOne(any());
        verify(hostFacade, times(1)).getById(anyString());
        verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
    }

    @Test
    public void testInitPrometheusConfigWithoutAlertConfigs() {
        List<AlertConfigDO> alertConfigDOS = new ArrayList<>();
        when(alertConfigService.list()).thenReturn(alertConfigDOS);
        NctigbaEnvDO promEnv = new NctigbaEnvDO();
        promEnv.setPath("/").setHostid("1").setUsername("user1").setPort(9090);
        when(envMapper.selectOne(any())).thenReturn(promEnv);
        OpsHostEntity promeHost = new OpsHostEntity();
        promeHost.setHostId("1");
        promeHost.setPublicIp("127.0.0.1");
        promeHost.setPort(22);
        when(hostFacade.getById(anyString())).thenReturn(promeHost);
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setUsername("user1");
        hostUserEntity.setPassword("passwd");
        List<OpsHostUserEntity> hostUserList = new ArrayList<>();
        hostUserList.add(hostUserEntity);
        when(hostUserFacade.listHostUserByHostId(anyString())).thenReturn(hostUserList);
        when(encryptionUtils.getKey()).thenReturn("");
        when(encryptionUtils.decrypt(anyString())).thenReturn("passwd");

        prometheusService.initPrometheusConfig();

        verify(alertConfigService, times(1)).list();
        verify(envMapper, times(1)).selectOne(any());
        verify(hostFacade, times(1)).getById(anyString());
        verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
        verify(encryptionUtils, times(1)).getKey();
        verify(encryptionUtils, times(1)).decrypt(anyString());
    }

    @Test
    public void testInitPrometheusConfig_getPromConfigNull() {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn("");
            prometheusService.initPrometheusConfig();

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
        }
    }

    @Test
    public void testInitPrometheusConfig_getPromConfigNull2() {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            String result = "{\"status\":\"fail\"}";
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            prometheusService.initPrometheusConfig();

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
        }
    }

    @Test
    public void testInitPrometheusConfig_updatePromConfig1() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            String result = updatePromConfig1Result();
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            doNothing().when(sshSessionUtils).close();

            prometheusService.initPrometheusConfig();

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(alertProperty, times(1)).getRuleFilePrefix();
            verify(alertProperty, times(1)).getRuleFileSuffix();
            verify(alertProperty, times(1)).getAlertmanager();
            verify(sshSessionUtils, times(1)).close();
        }
    }

    private String updatePromConfig1Result() {
        String backslash = String.valueOf((char) 92);
        return "{\"status\":\"success\",\"data\":{\"yaml\":\"global:" + backslash + "n  scrape_interval: 5s"
            + backslash + "n  "
            + "scrape_timeout: 5s" + backslash + "n  evaluation_interval: 1m" + backslash + "nalerting:"
            + backslash + "n  alertmanagers:" + backslash + "n  - follow_redirects: true" + backslash
            + "n    enable_http2: true" + backslash + "n    scheme: http" + backslash + "n   "
            + " path_prefix: /plugins/alert-monitor" + backslash + "n    timeout: 10s" + backslash
            + "n    api_version: v1" + backslash + "n    static_configs:" + backslash + "n   "
            + " - targets:" + backslash + "n      - 127.0.0.1:8080" + backslash + "nrule_files:" + backslash
            + "n- /data/rules/*.yml" + backslash + "n"
            + "scrape_configs:" + backslash + "n- job_name: prometheus" + backslash
            + "n  honor_timestamps: true" + backslash + "n  scrape_interval: 5s" + backslash + "n  "
            + "scrape_timeout: 5s" + backslash + "n  metrics_path: /metrics" + backslash + "n  scheme: http"
            + backslash + "n  follow_redirects: true" + backslash + "n  "
            + "enable_http2: true" + backslash + "n  static_configs:" + backslash + "n  - targets:" + backslash
            + "n    - 192.168.56.52:9100" + backslash + "n    "
            + "labels:" + backslash + "n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b" + backslash
            + "n      type: node" + backslash + "n\"}}";
    }

    @Test
    public void testInitPrometheusConfigUpdatePromConfig2() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            String result = updatePromConfig2Result();
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            when(sshSessionUtils.execute(anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            when(HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            prometheusService.initPrometheusConfig();

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(sshSessionUtils, times(1)).execute(anyString());
            verify(sshSessionUtils, times(1)).upload(anyString(), anyString());
            verify(sshSessionUtils, times(1)).close();
        }
    }

    private String updatePromConfig2Result() {
        String backslash = String.valueOf((char) 92);
        return "{\"status\":\"success\",\"data\":{\"yaml\":\"global:" + backslash + "n  scrape_interval: 5s"
            + backslash + "n  "
            + "scrape_timeout: 5s" + backslash + "n  evaluation_interval: 1m" + backslash + "nalerting:"
            + backslash + "n "
            + " alertmanagers:" + backslash + "n  - follow_redirects: true" + backslash
            + "n    enable_http2: true" + backslash + "n    scheme: http" + backslash + "n   "
            + " path_prefix: /plugins/alert-monitor" + backslash + "n    timeout: 10s" + backslash
            + "n    api_version: v1" + backslash + "n"
            + "    static_configs:" + backslash + "n"
            + "scrape_configs:" + backslash + "n- job_name: prometheus" + backslash
            + "n  honor_timestamps: true" + backslash + "n  scrape_interval: 5s" + backslash + "n  "
            + "scrape_timeout: 5s" + backslash + "n  metrics_path: /metrics" + backslash + "n  scheme: http"
            + backslash + "n  follow_redirects: true" + backslash + "n  "
            + "enable_http2: true" + backslash + "n  static_configs:" + backslash + "n  - targets:" + backslash
            + "n    - 192.168.56.52:9100" + backslash + "n    "
            + "labels:" + backslash + "n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b" + backslash
            + "n      type: node" + backslash + "n\"}}";
    }

    @Test
    public void testInitPrometheusConfigUpdatePromConfig3() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            String result = updatePromConfig3Result();
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            when(sshSessionUtils.execute(anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            when(HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();
            prometheusService.initPrometheusConfig();

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(sshSessionUtils, times(1)).execute(anyString());
            verify(sshSessionUtils, times(1)).upload(anyString(), anyString());
            verify(sshSessionUtils, times(1)).close();
        }
    }

    private String updatePromConfig3Result() {
        String backslash = String.valueOf((char) 92);
        return "{\"status\":\"success\",\"data\":{\"yaml\":\"global:" + backslash + "n  scrape_interval: 5s"
            + backslash + "n  "
            + "scrape_timeout: 5s" + backslash + "n  evaluation_interval: 1m" + backslash + "nalerting:"
            + backslash + "n "
            + " alertmanagers:" + backslash + "n  - follow_redirects: true" + backslash
            + "n    enable_http2: true" + backslash + "n    scheme: http" + backslash + "n   "
            + " path_prefix: /alertCenter" + backslash + "n    timeout: 10s" + backslash
            + "n    api_version: v1" + backslash + "n    static_configs:" + backslash + "n   "
            + " - targets:" + backslash + "n      - 192.168.56.1:8080" + backslash + "nrule_files:" + backslash
            + "n- /data/prometheus-2.42.0/rules1/*.yml" + backslash + "n"
            + "scrape_configs:" + backslash + "n- job_name: prometheus" + backslash
            + "n  honor_timestamps: true" + backslash + "n  scrape_interval: 5s" + backslash + "n  "
            + "scrape_timeout: 5s" + backslash + "n  metrics_path: /metrics" + backslash + "n  scheme: http"
            + backslash + "n  follow_redirects: true" + backslash + "n  "
            + "enable_http2: true" + backslash + "n  static_configs:" + backslash + "n  - targets:" + backslash
            + "n    - 192.168.56.52:9100" + backslash + "n    "
            + "labels:" + backslash + "n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b" + backslash
            + "n      type: node" + backslash + "n\"}}";
    }

    @Test
    public void testInitPrometheusConfigUpdatePromConfig4() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            String backslash = String.valueOf((char) 92);
            String result =
                "{\"status\":\"success\",\"data\":{\"yaml\":\"global:" + backslash + "n  scrape_interval: 5s"
                    + backslash + "n  "
                    + "scrape_timeout: 5s" + backslash + "n  evaluation_interval: 1m" + backslash + "nalerting:"
                    + backslash + "n"
                    + "scrape_configs:" + backslash + "n- job_name: prometheus" + backslash
                    + "n  honor_timestamps: true" + backslash + "n  scrape_interval: 5s" + backslash + "n  "
                    + "scrape_timeout: 5s" + backslash + "n  metrics_path: /metrics" + backslash + "n  scheme: http"
                    + backslash + "n  follow_redirects: true" + backslash + "n  "
                    + "enable_http2: true" + backslash + "n  static_configs:" + backslash + "n  - targets:" + backslash
                    + "n    - 192.168.56.52:9100" + backslash + "n    "
                    + "labels:" + backslash + "n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b" + backslash
                    + "n      type: node" + backslash + "n\"}}";
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            when(sshSessionUtils.execute(anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            when(HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            prometheusService.initPrometheusConfig();

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(sshSessionUtils, times(1)).execute(anyString());
            verify(sshSessionUtils, times(1)).upload(anyString(), anyString());
            verify(sshSessionUtils, times(1)).close();
        }
    }

    @Test
    public void testQueryRange1() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            mockedStatic.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn("");
            Number[][] result = prometheusService.queryRange("127.0.0.1", "8080", "query",
                LocalDateTime.now().minusHours(1), LocalDateTime.now());
            assertEquals(0, result.length);
        }
    }

    @Test
    public void testQueryRange2() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            String res = "{\"status\":\"fail\"}";
            mockedStatic.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn(res);
            Number[][] result = prometheusService.queryRange("127.0.0.1", "8080", "query",
                LocalDateTime.now().minusHours(1), LocalDateTime.now());
            assertEquals(0, result.length);
        }
    }

    @Test
    public void testQueryRange3() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            String res = "{\"status\":\"success\"}";
            mockedStatic.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn(res);
            Number[][] result = prometheusService.queryRange("127.0.0.1", "8080", "query",
                LocalDateTime.now().minusHours(1), LocalDateTime.now());
            assertEquals(0, result.length);
        }
    }

    @Test
    public void testQueryRange4() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            String res = "{\"status\":\"success\",\"data\":{\"result\":[]}}";
            mockedStatic.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn(res);
            Number[][] result = prometheusService.queryRange("127.0.0.1", "8080", "query",
                LocalDateTime.now().minusHours(1), LocalDateTime.now());
            assertEquals(0, result.length);
        }
    }

    @Test
    public void testQueryRange5() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            String res = "{\"status\":\"success\",\"data\":{\"result\":[{}]}}";
            mockedStatic.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn(res);
            Number[][] result = prometheusService.queryRange("127.0.0.1", "8080", "query",
                LocalDateTime.now().minusHours(1), LocalDateTime.now());
            assertEquals(0, result.length);
        }
    }

    @Test
    public void testQueryRange6() {
        try (MockedStatic<HttpUtil> mockedStatic = mockStatic(HttpUtil.class)) {
            String res = "{\"status\":\"success\",\"data\":{\"result\":[{\"values\":[[1683472020,"
                + "\"1.5090909090907871\"]]}]}}";
            mockedStatic.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn(res);
            Number[][] result = prometheusService.queryRange("127.0.0.1", "8080", "query",
                LocalDateTime.now().minusHours(1), LocalDateTime.now());
            assertEquals(1, result.length);
        }
    }

    @Test(expected = ServiceException.class)
    public void testUpdateAlertConfigThrowException() {
        AlertConfigDO alertConfigDO = new AlertConfigDO();
        prometheusService.updatePrometheusConfig(alertConfigDO);
    }

    @Test(expected = ServiceException.class)
    public void testUpdateAlertConfigThrowException2() {
        AlertConfigDO alertConfigDO = new AlertConfigDO().setAlertIp("127.0.0.1");
        prometheusService.updatePrometheusConfig(alertConfigDO);
    }

    @Test
    public void testUpdateAlertConfig() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            String result = testUpdateAlertConfigResult();
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            when(sshSessionUtils.execute(anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            when(HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            AlertConfigDO alertConfigDO =
                new AlertConfigDO().setId(2L).setAlertPort("127.0.0.1").setAlertPort("8080");
            List<AlertConfigDO> delAlertConfigDOList = new ArrayList<>();
            AlertConfigDO delAlertConfigDO =
                new AlertConfigDO().setId(3L).setAlertIp("192.168.56.1").setAlertPort("8080");
            delAlertConfigDOList.add(delAlertConfigDO);
            prometheusService.updatePrometheusConfig(alertConfigDO);

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(sshSessionUtils, times(1)).execute(anyString());
            verify(sshSessionUtils, times(1)).upload(anyString(), anyString());
            verify(sshSessionUtils, times(1)).close();
        }
    }

    private String testUpdateAlertConfigResult() {
        String backslash = String.valueOf((char) 92);
        return "{\"status\":\"success\",\"data\":{\"yaml\":\"global:" + backslash + "n  scrape_interval: 5s"
            + backslash + "n  "
            + "scrape_timeout: 5s" + backslash + "n  evaluation_interval: 1m" + backslash + "nalerting:"
            + backslash + "n "
            + " alertmanagers:" + backslash + "n  - follow_redirects: true" + backslash
            + "n    enable_http2: true" + backslash + "n    scheme: http" + backslash + "n   "
            + " path_prefix: /alertCenter" + backslash + "n    timeout: 10s" + backslash
            + "n    api_version: v1" + backslash + "n    static_configs:" + backslash + "n   "
            + " - targets:" + backslash + "n      - 192.168.56.1:8080" + backslash + "nrule_files:" + backslash
            + "n- /data/prometheus-2.42.0/rules1/*.yml" + backslash + "n"
            + "scrape_configs:" + backslash + "n- job_name: prometheus" + backslash
            + "n  honor_timestamps: true" + backslash + "n  scrape_interval: 5s" + backslash + "n  "
            + "scrape_timeout: 5s" + backslash + "n  metrics_path: /metrics" + backslash + "n  scheme: http"
            + backslash + "n  follow_redirects: true" + backslash + "n  "
            + "enable_http2: true" + backslash + "n  static_configs:" + backslash + "n  - targets:" + backslash
            + "n    - 192.168.56.52:9100" + backslash + "n    "
            + "labels:" + backslash + "n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b" + backslash
            + "n      type: node" + backslash + "n\"}}";
    }

    @Test
    public void testUpdateRuleConfigNull() {
        Map<Long, String> ruleConfigMap = new HashMap<>();
        prometheusService.updateRuleConfig(ruleConfigMap);
    }

    @Test
    public void testUpdateRuleConfigWithNullTemplateRules() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            List<AlertTemplateRuleDO> alertTemplateRuleDOS = new ArrayList<>();
            when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRuleDOS);
            doNothing().when(sshSessionUtils).close();
            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "");
            prometheusService.updateRuleConfig(ruleConfigMap);
            verify(alertTemplateRuleMapper, times(1)).selectList(any());
        }
    }

    @Test
    public void testUpdateRuleConfigWithNullClusterNodeIds1() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            List<AlertTemplateRuleDO> alertTemplateRuleDOS = new ArrayList<>();
            alertTemplateRuleDOS.add(new AlertTemplateRuleDO());
            when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRuleDOS);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rule/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "");
            prometheusService.updateRuleConfig(ruleConfigMap);
            verify(alertProperty, times(4)).getRuleFilePrefix();
        }
    }

    @Test
    public void testUpdateRuleConfigWithNullClusterNodeIds2() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            List<AlertTemplateRuleDO> alertTemplateRuleDOS = new ArrayList<>();
            alertTemplateRuleDOS.add(new AlertTemplateRuleDO());
            when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRuleDOS);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rule/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("rule");
            when(sshSessionUtils.execute("ls /data/rule/")).thenReturn("");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            doNothing().when(sshSessionUtils).close();

            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "");
            prometheusService.updateRuleConfig(ruleConfigMap);
            verify(alertProperty, times(1)).getRuleFileSuffix();
        }
    }

    @Test
    public void testUpdateRuleConfigWithNullClusterNodeIds3() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            List<AlertTemplateRuleDO> alertTemplateRuleDOS = new ArrayList<>();
            alertTemplateRuleDOS.add(new AlertTemplateRuleDO());
            when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRuleDOS);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rule/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("rule");
            when(sshSessionUtils.execute("ls /data/rule/")).thenReturn("rule_template_1.yml");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSessionUtils.execute("rm /data/rule/rule_template_1.yml")).thenReturn("");
            mockedStatic2.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "");
            prometheusService.updateRuleConfig(ruleConfigMap);
        }
    }

    @Test
    public void testUpdateRuleConfigWithMkdirOrTouch() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);

            mockAlertTemplateRulesData();
            when(alertProperty.getRuleFilePrefix()).thenReturn("rule/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("");
            when(sshSessionUtils.execute("mkdir /data/rule/")).thenReturn("");
            when(sshSessionUtils.execute("ls /data/rule/")).thenReturn("");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSessionUtils.execute("touch /data/rule/rule_template_1.yml")).thenReturn("");
            when(sshSessionUtils.execute("cat /data/rule/rule_template_1.yml")).thenReturn("");
            mockAlertTemplateRuleItemsData();
            when(sshSessionUtils.execute("rm /data/rule/rule_template_1.yml")).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            mockedStatic2.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "node1");
            prometheusService.updateRuleConfig(ruleConfigMap);
        }
    }

    @Test
    public void testUpdateRuleConfig() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);

            mockAlertTemplateRulesData();
            when(alertProperty.getRuleFilePrefix()).thenReturn("rule/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("rule");
            when(sshSessionUtils.execute("ls /data/rule/")).thenReturn("rule_template_1.yml");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSessionUtils.execute("cat /data/rule/rule_template_1.yml")).thenReturn(mockRuleConfigData2());
            mockAlertTemplateRuleItemsData();
            when(sshSessionUtils.execute("rm /data/rule/rule_template_1.yml")).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            mockedStatic2.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "node1");
            prometheusService.updateRuleConfig(ruleConfigMap);
        }
    }

    private void mockAlertTemplateRulesData() {
        List<AlertTemplateRuleDO> alertTemplateRuleDOS = new ArrayList<>();
        AlertTemplateRuleDO alertTemplateRuleDO1 = new AlertTemplateRuleDO();
        alertTemplateRuleDO1.setId(1L).setTemplateId(1L).setRuleId(1L).setRuleName("CPU使用率过高").setLevel("warn")
            .setRuleType("index").setRuleExpComb("A").setRuleContent("${nodeName}的CPU使用率超过90%")
            .setNotifyDuration(2).setNotifyDurationUnit("m").setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(0)
            .setAlertNotify("firing").setNotifyWayIds("1").setAlertDesc("test");
        alertTemplateRuleDOS.add(alertTemplateRuleDO1);
        AlertTemplateRuleDO alertTemplateRuleDO2 = new AlertTemplateRuleDO();
        alertTemplateRuleDO2.setId(2L).setTemplateId(1L).setRuleId(2L).setRuleName("CPU使用率过高").setLevel("warn")
            .setRuleType("index").setRuleExpComb("A").setRuleContent("${nodeName}的CPU使用率超过90%")
            .setNotifyDuration(2).setNotifyDurationUnit("m").setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(0)
            .setAlertNotify("firing").setNotifyWayIds("1");
        alertTemplateRuleDOS.add(alertTemplateRuleDO2);
        AlertTemplateRuleDO alertTemplateRuleDO3 = new AlertTemplateRuleDO();
        alertTemplateRuleDO3.setId(1L).setTemplateId(1L).setRuleId(1L).setRuleName("CPU使用率过高").setLevel("info")
            .setRuleType("index").setRuleExpComb("A").setRuleContent("${nodeName}的CPU使用率超过90%")
            .setNotifyDuration(2).setNotifyDurationUnit("m").setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(0)
            .setAlertNotify("firing").setNotifyWayIds("1");
        alertTemplateRuleDOS.add(alertTemplateRuleDO3);
        when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRuleDOS);
    }

    private void mockAlertTemplateRuleItemsData() {
        List<AlertTemplateRuleItemDO> alertTemplateRuleItems1DO = new ArrayList<>();
        AlertTemplateRuleItemDO alertTemplateRuleItemDO1 = new AlertTemplateRuleItemDO();
        alertTemplateRuleItemDO1.setId(1L).setTemplateRuleId(1L).setRuleItemId(1L).setRuleMark("A")
            .setRuleExpName("cpuUsage").setOperate(">=").setLimitValue("90").setUnit("%").setRuleExp("100 - avg"
                + "(rate(agent_cpu_seconds_total{mode=\"idle\",instance=~\"${instances}\"}[5m])) by(instance) "
                + " * 100").setAction("normal");
        alertTemplateRuleItems1DO.add(alertTemplateRuleItemDO1);

        List<AlertTemplateRuleItemDO> alertTemplateRuleItems2DO = new ArrayList<>();
        AlertTemplateRuleItemDO alertTemplateRuleItemDO2 = new AlertTemplateRuleItemDO();
        alertTemplateRuleItemDO2.setId(2L).setTemplateRuleId(2L).setRuleItemId(2L).setRuleMark("A")
            .setRuleExpName("cpuUsage").setOperate(">=").setLimitValue("90").setUnit("%").setRuleExp("100 - avg"
                + "(rate(agent_cpu_seconds_total{mode=\"idle\",instance=~\"${instances}\"}[5m])) by(instance) "
                + " * 100").setAction("normal");
        alertTemplateRuleItems2DO.add(alertTemplateRuleItemDO2);

        List<AlertTemplateRuleItemDO> alertTemplateRuleItems3DO = new ArrayList<>();
        AlertTemplateRuleItemDO alertTemplateRuleItemDO3 = new AlertTemplateRuleItemDO();
        alertTemplateRuleItemDO3.setId(3L).setTemplateRuleId(1L).setRuleItemId(2L).setRuleMark("A")
            .setRuleExpName("cpuUsage").setOperate(">=").setLimitValue("90").setUnit("%").setRuleExp("100 - avg"
                + "(rate(agent_cpu_seconds_total{mode=\"idle\",instance=~\"${instances}\"}[5m])) by(instance) "
                + " * 100").setAction("normal");
        alertTemplateRuleItems3DO.add(alertTemplateRuleItemDO3);

        when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertTemplateRuleItems1DO)
            .thenReturn(alertTemplateRuleItems2DO).thenReturn(alertTemplateRuleItems3DO);
    }

    private void testPrometheusEnvDtoNormal() {
        List<AlertConfigDO> alertConfigDOS = new ArrayList<>();
        AlertConfigDO alertConfigDO = new AlertConfigDO().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigDOS.add(alertConfigDO);
        when(alertConfigService.list()).thenReturn(alertConfigDOS);
        NctigbaEnvDO promEnv = new NctigbaEnvDO();
        promEnv.setPath("/data").setHostid("1").setUsername("user1").setPort(9090);
        when(envMapper.selectOne(any())).thenReturn(promEnv);
        OpsHostEntity promeHost = new OpsHostEntity();
        promeHost.setHostId("1");
        promeHost.setPublicIp("127.0.0.1");
        promeHost.setPort(22);
        when(hostFacade.getById(anyString())).thenReturn(promeHost);
        OpsHostUserEntity hostUserEntity = new OpsHostUserEntity();
        hostUserEntity.setUsername("user1");
        hostUserEntity.setPassword("passwd");
        List<OpsHostUserEntity> hostUserList = new ArrayList<>();
        hostUserList.add(hostUserEntity);
        when(hostUserFacade.listHostUserByHostId(anyString())).thenReturn(hostUserList);
        when(encryptionUtils.getKey()).thenReturn("");
        when(encryptionUtils.decrypt(anyString())).thenReturn("passwd");
    }

    @Test
    public void testUpdateNullRuleConfigByTemplateId() {
        List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
        when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
        prometheusService.updateRuleConfigByTemplateId(anyLong());
        verify(clusterNodeConfMapper, times(1)).selectList(any());
    }

    @Test
    public void testUpdateRuleConfigByTemplateId() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class)) {
            List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
            nodeConfList.add(new AlertClusterNodeConfDO().setTemplateId(1L).setClusterNodeId("node1"));
            when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            List<AlertTemplateRuleDO> alertTemplateRuleDOS = new ArrayList<>();
            when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRuleDOS);
            doNothing().when(sshSessionUtils).close();

            prometheusService.updateRuleConfigByTemplateId(anyLong());
        }
    }

    @Test
    public void testUpdateNullRuleByTemplateRule() {
        List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
        when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
        AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO().setTemplateId(1L);
        prometheusService.updateRuleByTemplateRule(alertTemplateRuleDO);
        verify(clusterNodeConfMapper, times(1)).selectList(any());
    }

    @Test
    public void testUpdateRuleByTemplateRuleWithEmptyGroups() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
            nodeConfList.add(new AlertClusterNodeConfDO().setTemplateId(1L).setClusterNodeId("node1"));
            when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("");
            when(sshSessionUtils.execute("touch /data/rules/")).thenReturn("");
            when(sshSessionUtils.execute("ls /data/rules/")).thenReturn("");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSessionUtils.execute("touch /data/rules/rule_template_1.yml")).thenReturn("");
            when(sshSessionUtils.execute("cat /data/rules/rule_template_1.yml")).thenReturn("");
            when(sshSessionUtils.execute("rm /data/rules/rule_template_1.yml")).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            mockedStatic2.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO();
            alertTemplateRuleDO.setId(1L).setTemplateId(1L).setRuleId(1L).setRuleName("CPU使用率过高").setLevel("warn")
                .setRuleType("index").setRuleExpComb("A").setRuleContent("${nodeName}的CPU使用率超过90%")
                .setNotifyDuration(2).setNotifyDurationUnit("m").setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(0)
                .setAlertNotify("firing").setNotifyWayIds("1").setAlertDesc("test");
            List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = new ArrayList<>();
            AlertTemplateRuleItemDO alertTemplateRuleItemDO = new AlertTemplateRuleItemDO();
            alertTemplateRuleItemDO.setId(1L).setTemplateRuleId(1L).setRuleItemId(1L).setRuleMark("A")
                .setRuleExpName("cpuUsage").setOperate(">=").setLimitValue("90").setUnit("%").setRuleExp("100 - avg"
                    + "(rate(agent_cpu_seconds_total{mode=\"idle\",instance=~\"${instances}\"}[5m])) by(instance) "
                    + " * 100").setAction("normal");
            alertTemplateRuleItemDOS.add(alertTemplateRuleItemDO);
            alertTemplateRuleDO.setAlertRuleItemList(alertTemplateRuleItemDOS);
            prometheusService.updateRuleByTemplateRule(alertTemplateRuleDO);
        }
    }

    @Test
    public void testUpdateRuleByTemplateRuleWithNewGroups() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
            nodeConfList.add(new AlertClusterNodeConfDO().setTemplateId(1L).setClusterNodeId("node1"));
            when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("rules");
            when(sshSessionUtils.execute("ls /data/rules/")).thenReturn("rule_template_1.yml");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSessionUtils.execute("cat /data/rules/rule_template_1.yml")).thenReturn(mockRuleConfigData1());
            when(sshSessionUtils.execute("rm /data/rules/rule_template_1.yml")).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            mockedStatic2.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO();
            alertTemplateRuleDO.setId(1L).setTemplateId(1L).setRuleId(1L).setRuleName("CPU使用率过高").setLevel("warn")
                .setRuleType("index").setRuleExpComb("A").setRuleContent("${nodeName}的CPU使用率超过90%")
                .setNotifyDuration(2).setNotifyDurationUnit("m").setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(0)
                .setAlertNotify("firing").setNotifyWayIds("1").setAlertDesc("test");
            List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = new ArrayList<>();
            AlertTemplateRuleItemDO alertTemplateRuleItemDO = new AlertTemplateRuleItemDO();
            alertTemplateRuleItemDO.setId(1L).setTemplateRuleId(1L).setRuleItemId(1L).setRuleMark("A")
                .setRuleExpName("cpuUsage").setOperate(">=").setLimitValue("90").setUnit("%").setRuleExp("100 - avg"
                    + "(rate(agent_cpu_seconds_total{mode=\"idle\",instance=~\"${instances}\"}[5m])) by(instance) "
                    + " * 100").setAction("normal");
            alertTemplateRuleItemDOS.add(alertTemplateRuleItemDO);
            alertTemplateRuleDO.setAlertRuleItemList(alertTemplateRuleItemDOS);
            prometheusService.updateRuleByTemplateRule(alertTemplateRuleDO);
        }
    }

    private String mockRuleConfigData1() {
        return "groups:" + CommonConstants.LINE_SEPARATOR
            + "- name: rule_1675682442954919938" + CommonConstants.LINE_SEPARATOR
            + "  rules:" + CommonConstants.LINE_SEPARATOR
            + "  - alert: rule_1675682442954919938" + CommonConstants.LINE_SEPARATOR
            + "    expr: 100 - avg(rate(node_cpu_seconds_total{mode=\"idle\","
            + "instance=~\"f8363fcd-d2db-4d00-8ebc-a8fcb6702dac\"}[5m]))" + CommonConstants.LINE_SEPARATOR
            + "      by(instance)  * 100 >=99" + CommonConstants.LINE_SEPARATOR
            + "    labels:" + CommonConstants.LINE_SEPARATOR
            + "      level: warn" + CommonConstants.LINE_SEPARATOR
            + "      templateId: '1675682487615868929'" + CommonConstants.LINE_SEPARATOR
            + "      templateRuleId: '1675682442954919938'" + CommonConstants.LINE_SEPARATOR
            + "    annotations:" + CommonConstants.LINE_SEPARATOR
            + "      summary: CPU使用率" + CommonConstants.LINE_SEPARATOR
            + "      description: ''" + CommonConstants.LINE_SEPARATOR
            + "    for: 2m";
    }

    @Test
    public void testUpdateRuleByTemplateRuleWithUpdateGroups() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
            nodeConfList.add(new AlertClusterNodeConfDO().setTemplateId(1L).setClusterNodeId("node1"));
            when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("rules");
            when(sshSessionUtils.execute("ls /data/rules/")).thenReturn("rule_template_1.yml");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSessionUtils.execute("cat /data/rules/rule_template_1.yml")).thenReturn(mockRuleConfigData2());
            when(sshSessionUtils.execute("rm /data/rules/rule_template_1.yml")).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            mockedStatic2.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO();
            alertTemplateRuleDO.setId(1L).setTemplateId(1L).setRuleId(1L).setRuleName("CPU使用率过高").setLevel("warn")
                .setRuleType("index").setRuleExpComb("A").setRuleContent("${nodeName}的CPU使用率超过90%")
                .setNotifyDuration(2).setNotifyDurationUnit("m").setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(0)
                .setAlertNotify("firing").setNotifyWayIds("1").setAlertDesc("test");
            List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = new ArrayList<>();
            AlertTemplateRuleItemDO alertTemplateRuleItemDO = new AlertTemplateRuleItemDO();
            alertTemplateRuleItemDO.setId(1L).setTemplateRuleId(1L).setRuleItemId(1L).setRuleMark("A")
                .setRuleExpName("cpuUsage").setOperate(">=").setLimitValue("90").setUnit("%").setRuleExp("100 - avg"
                    + "(rate(agent_cpu_seconds_total{mode=\"idle\",instance=~\"${instances}\"}[5m])) by(instance) "
                    + " * 100").setAction("normal");
            alertTemplateRuleItemDOS.add(alertTemplateRuleItemDO);
            alertTemplateRuleDO.setAlertRuleItemList(alertTemplateRuleItemDOS);
            prometheusService.updateRuleByTemplateRule(alertTemplateRuleDO);
        }
    }

    private String mockRuleConfigData2() {
        return "groups:" + CommonConstants.LINE_SEPARATOR
            + "- name: rule_1" + CommonConstants.LINE_SEPARATOR
            + "  rules:" + CommonConstants.LINE_SEPARATOR
            + "  - alert: rule_1" + CommonConstants.LINE_SEPARATOR
            + "    expr: 100 - avg(rate(node_cpu_seconds_total{mode=\"idle\","
            + "instance=~\"f8363fcd-d2db-4d00-8ebc-a8fcb6702dac\"}[5m]))" + CommonConstants.LINE_SEPARATOR
            + "      by(instance)  * 100 >=99" + CommonConstants.LINE_SEPARATOR
            + "    labels:" + CommonConstants.LINE_SEPARATOR
            + "      level: warn" + CommonConstants.LINE_SEPARATOR
            + "      templateId: '1675682487615868929'" + CommonConstants.LINE_SEPARATOR
            + "      templateRuleId: '1675682442954919938'" + CommonConstants.LINE_SEPARATOR
            + "    annotations:" + CommonConstants.LINE_SEPARATOR
            + "      summary: CPU使用率" + CommonConstants.LINE_SEPARATOR
            + "      description: ''" + CommonConstants.LINE_SEPARATOR
            + "    for: 2m";
    }

    @Test
    public void testRemoveNullRuleByTemplateRule() {
        List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
        when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
        AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO().setTemplateId(1L);
        prometheusService.removeRuleByTemplateRule(alertTemplateRuleDO);
    }

    @Test
    public void testRemoveRuleByTemplateRuleWithEmptyConfig() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class)) {
            List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
            nodeConfList.add(new AlertClusterNodeConfDO().setClusterNodeId("node1").setTemplateId(1L));
            when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("rules");
            when(sshSessionUtils.execute("ls /data/rules/")).thenReturn("rule_template_1.yml");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSessionUtils.execute("cat /data/rules/rule_template_1.yml")).thenReturn("");

            AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO().setTemplateId(1L);
            prometheusService.removeRuleByTemplateRule(alertTemplateRuleDO);
        }
    }

    @Test
    public void testRemoveRuleByTemplateRuleHasConfig() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
            nodeConfList.add(new AlertClusterNodeConfDO().setClusterNodeId("node1").setTemplateId(1L));
            when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("rules");
            when(sshSessionUtils.execute("ls /data/rules/")).thenReturn("rule_template_1.yml");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSessionUtils.execute("cat /data/rules/rule_template_1.yml")).thenReturn(mockRuleConfigData1());

            AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO();
            alertTemplateRuleDO.setId(1L).setTemplateId(1L).setRuleId(1L).setRuleName("CPU使用率过高").setLevel("warn")
                .setRuleType("index").setRuleExpComb("A").setRuleContent("${nodeName}的CPU使用率超过90%")
                .setNotifyDuration(2).setNotifyDurationUnit("m").setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(0)
                .setAlertNotify("firing").setNotifyWayIds("1").setAlertDesc("test");
            List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = new ArrayList<>();
            AlertTemplateRuleItemDO alertTemplateRuleItemDO = new AlertTemplateRuleItemDO();
            alertTemplateRuleItemDO.setId(1L).setTemplateRuleId(1L).setRuleItemId(1L).setRuleMark("A")
                .setRuleExpName("cpuUsage").setOperate(">=").setLimitValue("90").setUnit("%").setRuleExp("100 - avg"
                    + "(rate(agent_cpu_seconds_total{mode=\"idle\",instance=~\"${instances}\"}[5m])) by(instance) "
                    + " * 100").setAction("normal");
            alertTemplateRuleItemDOS.add(alertTemplateRuleItemDO);
            alertTemplateRuleDO.setAlertRuleItemList(alertTemplateRuleItemDOS);
            prometheusService.removeRuleByTemplateRule(alertTemplateRuleDO);
        }
    }

    @Test
    public void testRemoveRuleByTemplateRule() throws IOException {
        try (MockedStatic<SshSessionUtils> mockedStatic = mockStatic(SshSessionUtils.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            List<AlertClusterNodeConfDO> nodeConfList = new ArrayList<>();
            nodeConfList.add(new AlertClusterNodeConfDO().setClusterNodeId("node1").setTemplateId(1L));
            when(clusterNodeConfMapper.selectList(any())).thenReturn(nodeConfList);
            testPrometheusEnvDtoNormal();
            SshSessionUtils sshSessionUtils = mock(SshSessionUtils.class);
            mockedStatic.when(() -> SshSessionUtils.connect(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(sshSessionUtils);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(sshSessionUtils.execute("ls /data")).thenReturn("rules");
            when(sshSessionUtils.execute("ls /data/rules/")).thenReturn("rule_template_1.yml");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSessionUtils.execute("cat /data/rules/rule_template_1.yml")).thenReturn(mockRuleConfigData2());
            when(sshSessionUtils.execute("rm /data/rules/rule_template_1.yml")).thenReturn("");
            doNothing().when(sshSessionUtils).upload(anyString(), anyString());
            mockedStatic2.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSessionUtils).close();

            AlertTemplateRuleDO alertTemplateRuleDO = new AlertTemplateRuleDO();
            alertTemplateRuleDO.setId(1L).setTemplateId(1L).setRuleId(1L).setRuleName("CPU使用率过高").setLevel("warn")
                .setRuleType("index").setRuleExpComb("A").setRuleContent("${nodeName}的CPU使用率超过90%")
                .setNotifyDuration(2).setNotifyDurationUnit("m").setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(0)
                .setAlertNotify("firing").setNotifyWayIds("1").setAlertDesc("test");
            List<AlertTemplateRuleItemDO> alertTemplateRuleItemDOS = new ArrayList<>();
            AlertTemplateRuleItemDO alertTemplateRuleItemDO = new AlertTemplateRuleItemDO();
            alertTemplateRuleItemDO.setId(1L).setTemplateRuleId(1L).setRuleItemId(1L).setRuleMark("A")
                .setRuleExpName("cpuUsage").setOperate(">=").setLimitValue("90").setUnit("%").setRuleExp("100 - avg"
                    + "(rate(agent_cpu_seconds_total{mode=\"idle\",instance=~\"${instances}\"}[5m])) by(instance) "
                    + " * 100").setAction("normal");
            alertTemplateRuleItemDOS.add(alertTemplateRuleItemDO);
            alertTemplateRuleDO.setAlertRuleItemList(alertTemplateRuleItemDOS);
            prometheusService.removeRuleByTemplateRule(alertTemplateRuleDO);
        }
    }
}
