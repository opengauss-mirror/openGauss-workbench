/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.alert.monitor.service;

import cn.hutool.http.HttpUtil;
import com.nctigba.alert.monitor.config.properties.AlertProperty;
import com.nctigba.alert.monitor.config.properties.AlertmanagerProperty;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.entity.AlertConfig;
import com.nctigba.alert.monitor.entity.AlertTemplateRule;
import com.nctigba.alert.monitor.entity.AlertTemplateRuleItem;
import com.nctigba.alert.monitor.entity.NctigbaEnv;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleItemMapper;
import com.nctigba.alert.monitor.mapper.AlertTemplateRuleMapper;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.utils.SshSession;
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
    private PrometheusService prometheusService;

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

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPrometheusEnvDto1() {
        List<AlertConfig> alertConfigs = new ArrayList<>();
        AlertConfig alertConfig = new AlertConfig().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigs.add(alertConfig);
        when(alertConfigService.list()).thenReturn(alertConfigs);
        NctigbaEnv promEnv = null;
        when(envMapper.selectOne(any())).thenReturn(promEnv);

        prometheusService.initPrometheusConfig();

        verify(alertConfigService, times(1)).list();
        verify(envMapper, times(1)).selectOne(any());
    }

    @Test
    public void testPrometheusEnvDto2() {
        List<AlertConfig> alertConfigs = new ArrayList<>();
        AlertConfig alertConfig = new AlertConfig().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigs.add(alertConfig);
        when(alertConfigService.list()).thenReturn(alertConfigs);
        NctigbaEnv promEnv = new NctigbaEnv();
        when(envMapper.selectOne(any())).thenReturn(promEnv);

        prometheusService.initPrometheusConfig();

        verify(alertConfigService, times(1)).list();
        verify(envMapper, times(1)).selectOne(any());
    }

    @Test
    public void testPrometheusEnvDto3() {
        List<AlertConfig> alertConfigs = new ArrayList<>();
        AlertConfig alertConfig = new AlertConfig().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigs.add(alertConfig);
        when(alertConfigService.list()).thenReturn(alertConfigs);
        NctigbaEnv promEnv = new NctigbaEnv();
        promEnv.setPath("/");
        when(envMapper.selectOne(any())).thenReturn(promEnv);

        prometheusService.initPrometheusConfig();

        verify(alertConfigService, times(1)).list();
        verify(envMapper, times(1)).selectOne(any());
    }

    @Test
    public void testPrometheusEnvDto4() {
        List<AlertConfig> alertConfigs = new ArrayList<>();
        AlertConfig alertConfig = new AlertConfig().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigs.add(alertConfig);
        when(alertConfigService.list()).thenReturn(alertConfigs);
        NctigbaEnv promEnv = new NctigbaEnv();
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

    @Test
    public void testPrometheusEnvDto5() {
        List<AlertConfig> alertConfigs = new ArrayList<>();
        AlertConfig alertConfig = new AlertConfig().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigs.add(alertConfig);
        when(alertConfigService.list()).thenReturn(alertConfigs);
        NctigbaEnv promEnv = new NctigbaEnv();
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
        List<AlertConfig> alertConfigs = new ArrayList<>();
        when(alertConfigService.list()).thenReturn(alertConfigs);
        NctigbaEnv promEnv = new NctigbaEnv();
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
    public void testInitPrometheusConfigDisconnectSession() {
        testPrometheusEnvDtoNormal();
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
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
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
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
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
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
            String result = "{\"status\":\"success\",\"data\":{\"yaml\":\"global:\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  evaluation_interval: 1m\\nalerting:\\n "
                + " alertmanagers:\\n  - follow_redirects: true\\n    enable_http2: true\\n    scheme: http\\n   "
                + " path_prefix: /plugins/alert-monitor\\n    timeout: 10s\\n    api_version: v1\\n"
                + "    static_configs:\\n   "
                + " - targets:\\n      - 127.0.0.1:8080\\nrule_files:\\n- /data/rules/*.yml\\n"
                + "scrape_configs:\\n- job_name: prometheus\\n  honor_timestamps: true\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  metrics_path: /metrics\\n  scheme: http\\n  follow_redirects: true\\n  "
                + "enable_http2: true\\n  static_configs:\\n  - targets:\\n    - 192.168.56.52:9100\\n    "
                + "labels:\\n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b\\n      type: node\\n\"}}";
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            doNothing().when(sshSession).close();

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
            verify(sshSession, times(1)).close();
        }
    }

    @Test
    public void testInitPrometheusConfig_updatePromConfig2() throws IOException {
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
            String result = "{\"status\":\"success\",\"data\":{\"yaml\":\"global:\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  evaluation_interval: 1m\\nalerting:\\n "
                + " alertmanagers:\\n  - follow_redirects: true\\n    enable_http2: true\\n    scheme: http\\n   "
                + " path_prefix: /plugins/alert-monitor\\n    timeout: 10s\\n    api_version: v1\\n"
                + "    static_configs:\\n"
                + "scrape_configs:\\n- job_name: prometheus\\n  honor_timestamps: true\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  metrics_path: /metrics\\n  scheme: http\\n  follow_redirects: true\\n  "
                + "enable_http2: true\\n  static_configs:\\n  - targets:\\n    - 192.168.56.52:9100\\n    "
                + "labels:\\n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b\\n      type: node\\n\"}}";
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            when(sshSession.execute(anyString())).thenReturn("");
            doNothing().when(sshSession).upload(anyString(), anyString());
            when(HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSession).close();

            prometheusService.initPrometheusConfig();

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(sshSession, times(1)).execute(anyString());
            verify(sshSession, times(1)).upload(anyString(), anyString());
            verify(sshSession, times(1)).close();
        }
    }

    @Test
    public void testInitPrometheusConfig_updatePromConfig3() throws IOException {
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
            String result = "{\"status\":\"success\",\"data\":{\"yaml\":\"global:\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  evaluation_interval: 1m\\nalerting:\\n "
                + " alertmanagers:\\n  - follow_redirects: true\\n    enable_http2: true\\n    scheme: http\\n   "
                + " path_prefix: /alertCenter\\n    timeout: 10s\\n    api_version: v1\\n    static_configs:\\n   "
                + " - targets:\\n      - 192.168.56.1:8080\\nrule_files:\\n- /data/prometheus-2.42.0/rules1/*.yml\\n"
                + "scrape_configs:\\n- job_name: prometheus\\n  honor_timestamps: true\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  metrics_path: /metrics\\n  scheme: http\\n  follow_redirects: true\\n  "
                + "enable_http2: true\\n  static_configs:\\n  - targets:\\n    - 192.168.56.52:9100\\n    "
                + "labels:\\n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b\\n      type: node\\n\"}}";
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            when(sshSession.execute(anyString())).thenReturn("");
            doNothing().when(sshSession).upload(anyString(), anyString());
            when(HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSession).close();
            prometheusService.initPrometheusConfig();

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(sshSession, times(1)).execute(anyString());
            verify(sshSession, times(1)).upload(anyString(), anyString());
            verify(sshSession, times(1)).close();
        }
    }

    @Test
    public void testInitPrometheusConfig_updatePromConfig4() throws IOException {
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
            String result = "{\"status\":\"success\",\"data\":{\"yaml\":\"global:\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  evaluation_interval: 1m\\nalerting:\\n"
                + "scrape_configs:\\n- job_name: prometheus\\n  honor_timestamps: true\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  metrics_path: /metrics\\n  scheme: http\\n  follow_redirects: true\\n  "
                + "enable_http2: true\\n  static_configs:\\n  - targets:\\n    - 192.168.56.52:9100\\n    "
                + "labels:\\n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b\\n      type: node\\n\"}}";
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            when(sshSession.execute(anyString())).thenReturn("");
            doNothing().when(sshSession).upload(anyString(), anyString());
            when(HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSession).close();

            prometheusService.initPrometheusConfig();

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(sshSession, times(1)).execute(anyString());
            verify(sshSession, times(1)).upload(anyString(), anyString());
            verify(sshSession, times(1)).close();
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
        AlertConfig alertConfig = new AlertConfig();
        List<AlertConfig> delAlertConfigList = new ArrayList<>();
        prometheusService.updateAlertConfig(alertConfig, delAlertConfigList);
    }

    @Test(expected = ServiceException.class)
    public void testUpdateAlertConfigThrowException2() {
        AlertConfig alertConfig = new AlertConfig().setAlertIp("127.0.0.1");
        List<AlertConfig> delAlertConfigList = new ArrayList<>();
        prometheusService.updateAlertConfig(alertConfig, delAlertConfigList);
    }

    @Test(expected = ServiceException.class)
    public void testUpdateAlertConfigThrowException3() {
        testPrometheusEnvDtoNormal();
        AlertConfig alertConfig = new AlertConfig().setAlertIp("127.0.0.1").setAlertPort("8080");
        List<AlertConfig> delAlertConfigList = new ArrayList<>();
        prometheusService.updateAlertConfig(alertConfig, delAlertConfigList);
    }

    @Test
    public void testUpdateAlertConfig() throws IOException {
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
            String result = "{\"status\":\"success\",\"data\":{\"yaml\":\"global:\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  evaluation_interval: 1m\\nalerting:\\n "
                + " alertmanagers:\\n  - follow_redirects: true\\n    enable_http2: true\\n    scheme: http\\n   "
                + " path_prefix: /alertCenter\\n    timeout: 10s\\n    api_version: v1\\n    static_configs:\\n   "
                + " - targets:\\n      - 192.168.56.1:8080\\nrule_files:\\n- /data/prometheus-2.42.0/rules1/*.yml\\n"
                + "scrape_configs:\\n- job_name: prometheus\\n  honor_timestamps: true\\n  scrape_interval: 5s\\n  "
                + "scrape_timeout: 5s\\n  metrics_path: /metrics\\n  scheme: http\\n  follow_redirects: true\\n  "
                + "enable_http2: true\\n  static_configs:\\n  - targets:\\n    - 192.168.56.52:9100\\n    "
                + "labels:\\n      instance: 00336d3d-9b34-42a9-8ce8-b27048e50b8b\\n      type: node\\n\"}}";
            mockedStatic2.when(() -> HttpUtil.get(anyString())).thenReturn(result);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rules/");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            AlertmanagerProperty alertmanagerPro = new AlertmanagerProperty();
            alertmanagerPro.setPathPrefix("/plugins/alert-monitor");
            alertmanagerPro.setApiVersion("v1");
            when(alertProperty.getAlertmanager()).thenReturn(alertmanagerPro);
            when(sshSession.execute(anyString())).thenReturn("");
            doNothing().when(sshSession).upload(anyString(), anyString());
            when(HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSession).close();

            AlertConfig alertConfig = new AlertConfig().setId(2L).setAlertPort("127.0.0.1").setAlertPort("8080");
            List<AlertConfig> delAlertConfigList = new ArrayList<>();
            AlertConfig delAlertConfig = new AlertConfig().setId(3L).setAlertIp("192.168.56.1").setAlertPort("8080");
            delAlertConfigList.add(delAlertConfig);
            prometheusService.updateAlertConfig(alertConfig, delAlertConfigList);

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(sshSession, times(1)).execute(anyString());
            verify(sshSession, times(1)).upload(anyString(), anyString());
            verify(sshSession, times(1)).close();
        }
    }

    @Test
    public void testUpdateRuleConfigNull() {
        Map<Long, String> ruleConfigMap = new HashMap<>();
        prometheusService.updateRuleConfig(ruleConfigMap);
    }

    @Test
    public void testUpdateRuleConfigWithNullClusterNode() throws IOException {
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rule/");
            when(sshSession.execute("ls /data")).thenReturn("");
            doNothing().when(sshSession).close();

            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "");
            prometheusService.updateRuleConfig(ruleConfigMap);

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(alertProperty, times(4)).getRuleFilePrefix();
            verify(sshSession, times(1)).execute("ls /data");
            verify(sshSession, times(1)).close();
        }
    }

    @Test
    public void testUpdateRuleConfig1() throws IOException {
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rule/");
            when(sshSession.execute("ls /data")).thenReturn("");
            when(sshSession.execute("mkdir /data/rule/")).thenReturn("");
            when(sshSession.execute("ls /data/rule/")).thenReturn("");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSession.execute("touch /data/rule/rule_template_1.yml")).thenReturn("");
            when(sshSession.execute("cat /data/rule/rule_template_1.yml")).thenReturn("");

            List<AlertTemplateRule> alertTemplateRules = new ArrayList<>();
            AlertTemplateRule alertTemplateRule = new AlertTemplateRule();
            alertTemplateRule.setId(1L).setTemplateId(1L).setRuleId(1L).setRuleName("CPU使用率过高").setLevel("warn")
                .setRuleType("index").setRuleExpComb("A").setRuleContent("${nodeName}的CPU使用率超过90%")
                .setNotifyDuration(2).setNotifyDurationUnit("m").setIsRepeat(CommonConstants.IS_REPEAT).setIsSilence(0)
                .setAlertNotify("firing").setNotifyWayIds("1");
            alertTemplateRules.add(alertTemplateRule);
            when(alertTemplateRuleMapper.selectList(any())).thenReturn(alertTemplateRules);
            List<AlertTemplateRuleItem> alertTemplateRuleItems = new ArrayList<>();
            AlertTemplateRuleItem alertTemplateRuleItem = new AlertTemplateRuleItem();
            alertTemplateRuleItem.setId(1L).setTemplateRuleId(1L).setRuleItemId(1L).setRuleMark("A")
                .setRuleExpName("cpuUsage").setOperate(">=").setLimitValue("90").setUnit("%").setRuleExp("100 - avg"
                    + "(rate(agent_cpu_seconds_total{mode=\"idle\",instance=~\"${instances}\"}[5m])) by(instance) "
                    + " * 100").setAction("normal");
            alertTemplateRuleItems.add(alertTemplateRuleItem);
            when(alertTemplateRuleItemMapper.selectList(any())).thenReturn(alertTemplateRuleItems);

            when(sshSession.execute("rm /data/rule/rule_template_1.yml")).thenReturn("");
            doNothing().when(sshSession).upload(anyString(), anyString());
            mockedStatic2.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSession).close();

            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "node1");
            prometheusService.updateRuleConfig(ruleConfigMap);

            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(alertProperty, times(5)).getRuleFilePrefix();
            verify(alertProperty, times(2)).getRuleFileSuffix();
            verify(sshSession, times(6)).execute(anyString());
            verify(sshSession, times(1)).upload(anyString(), anyString());
            verify(sshSession, times(1)).close();
        }
    }

    @Test
    public void testUpdateRuleConfigWithNullClusterNode2() throws IOException {
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rule/");
            when(sshSession.execute("ls /data")).thenReturn("rule");
            when(sshSession.execute("ls /data/rule/")).thenReturn("");
            doNothing().when(sshSession).close();

            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "");
            prometheusService.updateRuleConfig(ruleConfigMap);

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(alertProperty, times(4)).getRuleFilePrefix();
            verify(sshSession, times(1)).execute("ls /data");
            verify(sshSession, times(1)).execute("ls /data/rule/");
            verify(sshSession, times(1)).close();
        }
    }

    @Test
    public void testUpdateRuleConfigWithNullClusterNode3() throws IOException {
        try (MockedStatic<SshSession> mockedStatic = mockStatic(SshSession.class);
             MockedStatic<HttpUtil> mockedStatic2 = mockStatic(HttpUtil.class)) {
            testPrometheusEnvDtoNormal();
            SshSession sshSession = mock(SshSession.class);
            mockedStatic.when(() -> SshSession.connect(anyString(), anyInt(), anyString(), anyString())).thenReturn(
                sshSession);
            when(alertProperty.getRuleFilePrefix()).thenReturn("rule/");
            when(sshSession.execute("ls /data")).thenReturn("rule");
            when(sshSession.execute("ls /data/rule/")).thenReturn("rule_template_1.yml");
            when(alertProperty.getRuleFileSuffix()).thenReturn(".yml");
            when(sshSession.execute("rm -f /data/rule/rule_template_1.yml")).thenReturn("");
            mockedStatic2.when(() -> HttpUtil.post(anyString(), anyString())).thenReturn("");
            doNothing().when(sshSession).close();

            Map<Long, String> ruleConfigMap = new HashMap<>();
            ruleConfigMap.put(1L, "");
            prometheusService.updateRuleConfig(ruleConfigMap);

            verify(alertConfigService, times(1)).list();
            verify(envMapper, times(1)).selectOne(any());
            verify(hostFacade, times(1)).getById(anyString());
            verify(hostUserFacade, times(1)).listHostUserByHostId(anyString());
            verify(encryptionUtils, times(1)).getKey();
            verify(encryptionUtils, times(1)).decrypt(anyString());
            verify(alertProperty, times(4)).getRuleFilePrefix();
            verify(sshSession, times(1)).execute("ls /data");
            verify(sshSession, times(1)).execute("ls /data/rule/");
            verify(alertProperty, times(1)).getRuleFileSuffix();
            verify(sshSession, times(1))
                .execute("rm -f /data/rule/rule_template_1.yml");
            verify(sshSession, times(1)).close();
        }
    }

    private void testPrometheusEnvDtoNormal() {
        List<AlertConfig> alertConfigs = new ArrayList<>();
        AlertConfig alertConfig = new AlertConfig().setAlertIp("127.0.0.1").setAlertPort("8080").setId(1L);
        alertConfigs.add(alertConfig);
        when(alertConfigService.list()).thenReturn(alertConfigs);
        NctigbaEnv promEnv = new NctigbaEnv();
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
}
