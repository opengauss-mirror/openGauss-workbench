/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

import com.jcraft.jsch.Session;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.service.ops.IHostService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * HostTest
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class HostTest {
    private static final String MASTER_NODE_HOST_ID = "testHostId";
    private static final String ROOT_PASSWORD = "1qaz2wsx#EDC";

    @Mock
    private HostFacade hostFacade;
    @Mock
    private HostUserFacade hostUserFacade;
    @Mock
    private JschUtil jschUtil;
    @InjectMocks
    @Spy
    private IHostService hostService;

    @Before
    public void setup() throws IOException, InterruptedException {
        when(hostFacade.getById(MASTER_NODE_HOST_ID)).thenReturn(getHost());
        when(hostUserFacade.getRootUserByHostId(MASTER_NODE_HOST_ID)).thenReturn(getHostUser());
        Session session = null;
        doReturn(Optional.of(session)).when(jschUtil).getSession(any(), any(), any(), any());
        doReturn(getCmdResult()).when(jschUtil).executeCommand(any(), any());
    }

    @Test
    public void testPathEmpty() {
        boolean isPathEmptyResult = hostService.pathEmpty(MASTER_NODE_HOST_ID, "/test/path", ROOT_PASSWORD);
        Assert.assertTrue(isPathEmptyResult);
    }

    @Test
    public void testPortUsed() {
        boolean isPortUsedResult = hostService.portUsed(MASTER_NODE_HOST_ID, 5432, ROOT_PASSWORD);
        Assert.assertTrue(isPortUsedResult);
    }

    @Test
    public void testFileExist() {
        boolean isFileExistResult = hostService.fileExist(MASTER_NODE_HOST_ID, "/ops/files/1.txt", ROOT_PASSWORD);
        Assert.assertTrue(isFileExistResult);
    }

    @Test
    public void testMultiPathQuery() {
        List<String> result = hostService.multiPathQuery(MASTER_NODE_HOST_ID, ROOT_PASSWORD);
        Assert.assertNotNull(result);
    }

    private OpsHostEntity getHost() {
        return new OpsHostEntity();
    }

    private OpsHostUserEntity getHostUser() {
        return new OpsHostUserEntity();
    }

    private JschResult getCmdResult() {
        JschResult result = new JschResult();
        result.setExitCode(0);
        result.setResult("0");
        return result;
    }
}
