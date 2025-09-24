/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

import cn.hutool.core.collection.ListUtil;
import jakarta.websocket.Session;

import com.alibaba.fastjson.JSON;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.utils.http.HttpUtils;
import org.opengauss.admin.plugin.domain.entity.ops.OpsOlkEntity;
import org.opengauss.admin.plugin.domain.model.ops.WsSession;
import org.opengauss.admin.plugin.domain.model.ops.cache.WsConnectorManager;
import org.opengauss.admin.plugin.domain.model.ops.olk.InstallOlkBody;
import org.opengauss.admin.plugin.domain.model.ops.olk.InstallOlkContext;
import org.opengauss.admin.plugin.domain.model.ops.olk.OlkConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.ShardingDatasourceConfig;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.DadReqPath;
import org.opengauss.admin.plugin.domain.model.ops.olk.dadReq.DadResult;
import org.opengauss.admin.plugin.service.ops.IOpsOlkService;
import org.opengauss.admin.plugin.service.ops.impl.OpsOlkServiceImpl;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * OpenLooKengTest
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class OpenLooKengTest {
    private static final String BUSINESS_ID = "testBusinessId";

    @InjectMocks
    @Spy
    private IOpsOlkService olkService = new OpsOlkServiceImpl();
    @Mock
    private WsConnectorManager wsConnectorManager;
    @Mock
    private ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    @Mock
    private HostFacade hostFacade;

    @Before
    public void setup() throws IOException, InterruptedException {
        when(wsConnectorManager.getSession(any())).thenReturn(
            Optional.of(new WsSession(getMockSession(), "testSessionId")));
        when(threadPoolTaskExecutor.submit(any(Runnable.class))).thenReturn(new CompletableFuture<>());
        doReturn(true).when(olkService).removeById(anyString());
        doReturn(getOlkEntity()).when(olkService).getById(anyString());
        when(hostFacade.getById(any())).thenReturn(getHost());
    }

    @Test
    public void testInstall() {
        InstallOlkBody installOlkBody = new InstallOlkBody();
        installOlkBody.setBusinessId(BUSINESS_ID);
        InstallOlkContext olkContext = new InstallOlkContext();
        OlkConfig olkConfig = new OlkConfig();
        olkConfig.setRuleYaml("testRuleYaml");
        olkContext.setOlkConfig(olkConfig);
        installOlkBody.setInstallContext(olkContext);
        olkService.install(installOlkBody);
    }

    @Test
    public void testDelete() {
        Assert.assertTrue(olkService.removeById("1"));
    }

    @Test
    public void testDestroy() {
        MockedStatic<HttpUtils> httpUtils = Mockito.mockStatic(HttpUtils.class);
        httpUtils.when(() -> HttpUtils.sendPost(any(), any())).thenReturn(getDadResult());
        olkService.destroy("1", "bid");
        httpUtils.close();
    }

    @Test
    public void testGenerateRuleYaml() {
        String yaml = olkService.generateRuleYaml(getYamlConfig());
        Assert.assertNotNull(yaml);
    }

    @Test
    public void testStart() {
        MockedStatic<HttpUtils> httpUtils = Mockito.mockStatic(HttpUtils.class);
        httpUtils.when(() -> HttpUtils.sendPost(any(), any())).thenReturn(getDadResult());
        olkService.start("1", "testBusinessId");
        httpUtils.close();
    }

    @Test
    public void testStop() {
        MockedStatic<HttpUtils> httpUtils = Mockito.mockStatic(HttpUtils.class);
        httpUtils.when(() -> HttpUtils.sendPost(any(), any())).thenReturn(getDadResult());
        olkService.stop("1", "testBusinessId");
        httpUtils.close();
    }

    private OpsOlkEntity getOlkEntity() {
        OpsOlkEntity olkEntity = new OpsOlkEntity();
        olkEntity.setId("1");
        return olkEntity;
    }

    private OpsHostEntity getHost() {
        OpsHostEntity hostEntity = new OpsHostEntity();
        hostEntity.setPort(22);
        hostEntity.setPublicIp("127.0.0.1");
        return hostEntity;
    }

    private HttpUtils.PostResponse getDadResult() {
        HttpUtils.PostResponse response = new HttpUtils.PostResponse();
        DadResult dadResult = DadResult.success("test return msg");
        response.setBody(JSON.toJSONString(dadResult));
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(DadReqPath.REQ_ID_KEY, ListUtil.toList("testReqId"));
        response.setHeaders(headers);
        return response;
    }

    private OlkConfig getYamlConfig() {
        List<ShardingDatasourceConfig> dsConfigList = new ArrayList<>();
        ShardingDatasourceConfig dsConfig1 = new ShardingDatasourceConfig();
        dsConfig1.setPort("5432");
        dsConfig1.setUsername("gaussdb");
        dsConfig1.setDbName("template1");
        dsConfig1.setPassword("1qaz2wsx#EDC");
        dsConfig1.setHost("175.24.226.99");
        dsConfigList.add(dsConfig1);
        ShardingDatasourceConfig dsConfig2 = new ShardingDatasourceConfig();
        dsConfig2.setPort("5432");
        dsConfig2.setUsername("gaussdb");
        dsConfig2.setDbName("template1");
        dsConfig2.setPassword("1qaz2wsx#EDC");
        dsConfig2.setHost("175.24.226.99");
        dsConfigList.add(dsConfig2);
        OlkConfig olkConfig = new OlkConfig();
        olkConfig.setDsConfig(dsConfigList);
        olkConfig.setColumns("key1,key2");
        olkConfig.setTableName("table1,table2");
        return olkConfig;
    }

    private Session getMockSession() {
        return Mockito.mock(Session.class);
    }
}
