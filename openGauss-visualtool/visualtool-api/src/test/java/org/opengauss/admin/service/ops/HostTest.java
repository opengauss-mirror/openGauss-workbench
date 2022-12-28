package org.opengauss.admin.service.ops;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostBody;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.core.domain.model.ops.host.OpsHostVO;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.system.mapper.ops.OpsHostMapper;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsClusterService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.admin.system.service.ops.impl.HostServiceImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author lhf
 * @date 2022/11/25 16:28
 **/
@RunWith(SpringRunner.class)
public class HostTest {
    @Mock
    private IHostUserService hostUserService;
    @Mock
    private OpsHostMapper hostMapper;
    @Mock
    private JschUtil jschUtil;
    @Mock
    private IOpsClusterService clusterService;
    @Mock
    private EncryptionUtils encryptionUtils;
    @InjectMocks
    private IHostService hostService = new HostServiceImpl() {
        @Override
        public OpsHostEntity getOne(Wrapper<OpsHostEntity> queryWrapper, boolean throwEx) {
            return null;
        }

        @Override
        public OpsHostEntity getById(Serializable id) {
            OpsHostEntity hostEntity = new OpsHostEntity();
            hostEntity.setHostId("1");
            hostEntity.setHostname("node1");
            hostEntity.setPublicIp("192.168.0.31");
            hostEntity.setPrivateIp("192.168.0.31");
            hostEntity.setAzId("1");
            hostEntity.setRemark("test");
            hostEntity.setPort(22);
            return hostEntity;
        }

        @Override
        public boolean removeById(Serializable id) {
            return true;
        }

        @Override
        public boolean save(OpsHostEntity entity) {
            return true;
        }

        @Override
        public boolean updateById(OpsHostEntity entity) {
            return true;
        }
    };

    @BeforeClass
    public static void before() {
        MockitoAnnotations.initMocks(HostTest.class);
        System.out.println("start host test........");
    }

    @AfterClass
    public static void after() {
        System.out.println("end host test........");
    }

    @Test
    public void testAdd() throws JSchException, IOException, InterruptedException {
        JschResult jschResult = new JschResult();
        jschResult.setExitCode(0);
        Mockito.doReturn("000000").when(encryptionUtils).decrypt(any());
        Mockito.doReturn(Optional.ofNullable(getJschSession())).when(jschUtil).getSession(any(), any(), any(), any());
        Mockito.doReturn(jschResult).when(jschUtil).executeCommand(any(), any());
        Mockito.doReturn(true).when(hostUserService).save(any());
        HostBody hostBody = new HostBody();
        hostBody.setPublicIp("192.168.0.31");
        hostBody.setPrivateIp("192.168.0.31");
        hostBody.setPassword("123456");
        hostBody.setAzId("1");
        hostBody.setRemark("test");
        hostBody.setPort(22);
        boolean add = hostService.add(hostBody);
        Assertions.assertTrue(add);
    }

    @Test
    public void testPing() throws JSchException {
        Mockito.doReturn(Optional.ofNullable(getJschSession())).when(jschUtil).getSession(any(), any(), any(), any());
        Mockito.doReturn("000000").when(encryptionUtils).decrypt(any());
        HostBody hostBody = new HostBody();
        hostBody.setPublicIp("192.168.0.31");
        hostBody.setPrivateIp("192.168.0.31");
        hostBody.setPassword("123456");
        hostBody.setAzId("1");
        hostBody.setRemark("test");
        hostBody.setPort(22);
        boolean ping = hostService.ping(hostBody);
        Assertions.assertTrue(ping);
    }

    @Test
    public void testDel() {
        Mockito.doReturn(0L).when(clusterService).countByHostId(any());
        Mockito.doReturn(true).when(hostUserService).removeByHostId(any());
        String hostId = "1";
        boolean del = hostService.del(hostId);
        Assertions.assertTrue(del);
    }

    @Test
    public void testPing1() throws JSchException {
        Mockito.doReturn(hostUserEntityList()).when(hostUserService).listHostUserByHostId(any());
        Mockito.doReturn(Optional.ofNullable(getJschSession())).when(jschUtil).getSession(any(), any(), any(), any());
        String hostId = "1";
        boolean ping = hostService.ping(hostId, "123456");
        Assertions.assertTrue(ping);
    }

    @Test
    public void testEdit() throws JSchException, IOException, InterruptedException {
        Mockito.doReturn(Optional.ofNullable(getJschSession())).when(jschUtil).getSession(any(), any(), any(), any());
        JschResult jschResult = new JschResult();
        jschResult.setExitCode(0);
        Mockito.doReturn(jschResult).when(jschUtil).executeCommand(any(), any());
        String hostId = "1";
        HostBody hostBody = new HostBody();
        hostBody.setPublicIp("192.168.0.31");
        hostBody.setPrivateIp("192.168.0.31");
        hostBody.setPassword("123456");
        hostBody.setAzId("1");
        hostBody.setRemark("test");
        hostBody.setPort(22);
        boolean edit = hostService.edit(hostId, hostBody);
        Assertions.assertTrue(edit);
    }

    @Test
    public void testPageHost() {
        List<OpsHostVO> opsHostVOList = new ArrayList<>();
        IPage<OpsHostVO> page = new Page<>();
        page.setRecords(opsHostVOList);
        page.setTotal(1L);

        Mockito.doReturn(page).when(hostMapper).pageHost(any(), any());
        IPage<OpsHostVO> opsHostVOIPage = hostService.pageHost(new Page(1, 10), "192.168.0.31");
        Assertions.assertEquals(opsHostVOIPage.getTotal(), 1);
        Assertions.assertEquals(opsHostVOIPage.getRecords(), opsHostVOList);
    }

    private List<OpsHostUserEntity> hostUserEntityList() {
        List<OpsHostUserEntity> hostUserEntityList = new ArrayList<>();

        OpsHostUserEntity root = new OpsHostUserEntity();
        root.setHostId("1");
        root.setHostUserId("1");
        root.setUsername("root");
        root.setPassword("Lxgiwyl#0847");
        hostUserEntityList.add(root);

        OpsHostUserEntity omm = new OpsHostUserEntity();
        omm.setHostId("1");
        omm.setHostUserId("2");
        omm.setUsername("omm");
        omm.setPassword("omm");
        hostUserEntityList.add(omm);

        return hostUserEntityList;
    }

    private Session getJschSession() throws JSchException {
        return new JSch().getSession("192.168.0.31");
    }
}
