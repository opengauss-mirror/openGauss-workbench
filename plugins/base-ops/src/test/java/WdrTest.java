/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterEntity;
import org.opengauss.admin.plugin.domain.entity.ops.OpsClusterNodeEntity;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.enums.ops.ClusterRoleEnum;
import org.opengauss.admin.plugin.enums.ops.OpenGaussVersionEnum;
import org.opengauss.admin.plugin.service.ops.IOpsClusterService;
import org.opengauss.admin.plugin.service.ops.IOpsWdrService;
import org.opengauss.admin.plugin.service.ops.impl.OpsWdrServiceImpl;
import org.opengauss.admin.plugin.vo.ops.DwrSnapshotVO;
import org.opengauss.admin.system.plugin.facade.HostFacade;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * WdrTest
 *
 * @author: wangchao
 * @Date: 2025/9/11 21:21
 * @since 7.0.0-RC2
 **/
@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class WdrTest {
    private static final String HOST_ID = "testHostId";
    private static final String OLD_CLUSTER_ID = "testClusterId";

    @InjectMocks
    @Spy
    private IOpsWdrService wdrService = new OpsWdrServiceImpl();
    @Mock
    private IOpsClusterService opsClusterService;
    @Mock
    private HostFacade hostFacade;

    @Before
    public void setup() throws IOException, InterruptedException {
        when(opsClusterService.getById(any())).thenReturn(getClusterEntity());
        when(hostFacade.getById(any())).thenReturn(getHost());
    }

    @Test
    public void testListSnapshot() {
        Page<DwrSnapshotVO> result = wdrService.listSnapshot(getPage(), OLD_CLUSTER_ID, HOST_ID);
        Assert.assertNotNull(result);
    }

    private Page getPage() {
        Page page = new Page<>();
        page.setCurrent(1);
        page.setSize(10);
        return page;
    }

    private OpsClusterEntity getClusterEntity() {
        OpsClusterEntity clusterEntity = new OpsClusterEntity();
        clusterEntity.setClusterId(OLD_CLUSTER_ID);
        clusterEntity.setInstallPath("/ops/opengauss");
        clusterEntity.setPort(5432);
        clusterEntity.setDatabaseUsername("gaussdb");
        clusterEntity.setDatabasePassword("1qaz2wsx#EDC");
        clusterEntity.setVersion(OpenGaussVersionEnum.ENTERPRISE);
        return clusterEntity;
    }

    private OpsHostEntity getHost() {
        OpsHostEntity hostEntity = new OpsHostEntity();
        hostEntity.setHostId(HOST_ID);
        hostEntity.setPublicIp("124.222.78.113");
        hostEntity.setPort(22);
        return hostEntity;
    }

    private List<OpsClusterNodeEntity> getClusterNodeEntity() {
        List<OpsClusterNodeEntity> nodeEntityList = new ArrayList<>();
        OpsClusterNodeEntity clusterNodeEntity = new OpsClusterNodeEntity();
        clusterNodeEntity.setClusterId(OLD_CLUSTER_ID);
        clusterNodeEntity.setHostId(HOST_ID);
        clusterNodeEntity.setClusterRole(ClusterRoleEnum.MASTER);
        nodeEntityList.add(clusterNodeEntity);
        return nodeEntityList;
    }

    private OpsHostUserEntity getHostUser() {
        OpsHostUserEntity userEntity1 = new OpsHostUserEntity();
        userEntity1.setUsername("lhf");
        userEntity1.setHostId(HOST_ID);
        userEntity1.setPassword("testPassword");
        return userEntity1;
    }

    private JschResult getCmdResult() {
        JschResult result = new JschResult();
        result.setExitCode(0);
        result.setResult("testCmdResult");
        return result;
    }
}
