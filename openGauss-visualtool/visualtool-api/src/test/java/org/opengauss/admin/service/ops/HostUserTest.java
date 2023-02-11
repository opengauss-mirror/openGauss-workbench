package org.opengauss.admin.service.ops;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
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
import org.opengauss.admin.common.core.domain.model.ops.HostUserBody;
import org.opengauss.admin.common.core.domain.model.ops.JschResult;
import org.opengauss.admin.common.utils.ops.JschUtil;
import org.opengauss.admin.system.service.ops.IHostService;
import org.opengauss.admin.system.service.ops.IHostUserService;
import org.opengauss.admin.system.service.ops.IOpsClusterNodeService;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.opengauss.admin.system.service.ops.impl.HostUserServiceImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

/**
 * @author lhf
 * @date 2022/11/26 16:34
 **/
@RunWith(SpringRunner.class)
public class HostUserTest {
    @InjectMocks
    private IHostUserService hostUserService = new HostUserServiceImpl() {
        @Override
        public List<OpsHostUserEntity> list() {
            return getMockHostUserList();
        }

        @Override
        public List<OpsHostUserEntity> list(Wrapper<OpsHostUserEntity> queryWrapper) {
            return getMockHostUserList();
        }

        @Override
        public boolean remove(Wrapper<OpsHostUserEntity> queryWrapper) {
            return true;
        }

        @Override
        public OpsHostUserEntity getById(Serializable id) {
            return getMockHostUserEntity();
        }

        @Override
        public long count(Wrapper<OpsHostUserEntity> queryWrapper) {
            return 0;
        }

        @Override
        public boolean save(OpsHostUserEntity entity) {
            return true;
        }

        @Override
        public OpsHostUserEntity getOne(Wrapper<OpsHostUserEntity> queryWrapper, boolean throwEx) {
            return getMockHostUserEntity();
        }

        @Override
        public boolean updateById(OpsHostUserEntity entity) {
            return true;
        }

        @Override
        public boolean removeById(Serializable id) {
            return true;
        }
        @Override
        public boolean edit(String hostUserId, HostUserBody hostUserBody) {
            return true;
        }
    };
    @Mock
    private JschUtil jschUtil;
    @Mock
    private EncryptionUtils encryptionUtils;
    @Mock
    private IHostService hostService;
    @Mock
    private IOpsClusterNodeService opsClusterNodeService;

    @BeforeClass
    public static void before() {
        MockitoAnnotations.initMocks(HostTest.class);
        System.out.println("start host user test........");
    }

    @AfterClass
    public static void after() {
        System.out.println("end host user test........");
    }

    @Test
    public void testListHostUserByHostId() {
        List<OpsHostUserEntity> mockHostUserList = getMockHostUserList();
        String hostId = "1";
        List<OpsHostUserEntity> hostUserEntities = hostUserService.listHostUserByHostId(hostId);
        Assertions.assertEquals(hostUserEntities, mockHostUserList);
    }

    @Test
    public void testRemoveByHostId() {
        String hostId = "1";
        boolean b = hostUserService.removeByHostId(hostId);
        Assertions.assertTrue(b);
    }

    @Test
    public void testListHostUserByHostIdList() {
        List<String> hostIds = Arrays.asList("1");
        List<OpsHostUserEntity> hostUserEntities = hostUserService.listHostUserByHostIdList(hostIds);
        Assertions.assertEquals(hostUserEntities, getMockHostUserList());
    }

    @Test
    public void testAdd() throws JSchException, IOException, InterruptedException {
        JschResult jschResult = new JschResult();
        jschResult.setExitCode(0);
        Mockito.doReturn("000000").when(encryptionUtils).decrypt(any());
        Mockito.doReturn(Optional.ofNullable(getJschSession())).when(jschUtil).getSession(any(), any(), any(), any());
        Mockito.doReturn(jschResult).when(jschUtil).executeCommand(any(), any());
        Mockito.doReturn(jschResult).when(jschUtil).executeCommand(any(), any(), any());
        Mockito.doReturn(getMockHostEntity()).when(hostService).getById(any());
        HostUserBody hostUserBody = new HostUserBody();
        hostUserBody.setHostId("1");
        boolean add = hostUserService.add(hostUserBody);
        Assertions.assertTrue(add);
    }

    @Test
    public void testEdit() {
        String hostUserId = "1";
        HostUserBody hostUserBody = new HostUserBody();
        hostUserBody.setHostId("1");
        boolean edit = hostUserService.edit(hostUserId, hostUserBody);
        Assertions.assertTrue(edit);
    }

    @Test
    public void testDel() {
        String hostUserId = "1";
        Mockito.doReturn(0L).when(opsClusterNodeService).countByHostUserId(any());
        boolean del = hostUserService.del(hostUserId);
        Assertions.assertTrue(del);
    }

    @Test
    public void testGetOmmUserByHostId() {
        String hostId = "1";
        OpsHostUserEntity ommUserByHostId = hostUserService.getOmmUserByHostId(hostId);
        Assertions.assertNotNull(ommUserByHostId);
        Assertions.assertEquals(ommUserByHostId.getUsername(), "omm");
    }

    @Test
    public void testGetRootUserByHostId() {
        String hostId = "1";
        OpsHostUserEntity rootUserByHostId = hostUserService.getRootUserByHostId(hostId);
        Assertions.assertNotNull(rootUserByHostId);
    }

    private OpsHostEntity getMockHostEntity() {
        OpsHostEntity hostEntity = new OpsHostEntity();
        return hostEntity;
    }

    private List<OpsHostUserEntity> getMockHostUserList() {
        List<OpsHostUserEntity> hostUserEntities = new ArrayList<>();

        OpsHostUserEntity opsHostUserEntity = new OpsHostUserEntity();
        opsHostUserEntity.setHostUserId("1");
        opsHostUserEntity.setHostId("1");
        opsHostUserEntity.setUsername("root");
        opsHostUserEntity.setPassword("123456");
        hostUserEntities.add(opsHostUserEntity);
        return hostUserEntities;
    }

    private OpsHostUserEntity getMockHostUserEntity() {
        OpsHostUserEntity opsHostUserEntity = new OpsHostUserEntity();
        opsHostUserEntity.setHostUserId("1");
        opsHostUserEntity.setHostId("1");
        opsHostUserEntity.setUsername("omm");
        opsHostUserEntity.setPassword("123456");
        return opsHostUserEntity;
    }

    private Session getJschSession() throws JSchException {
        return new JSch().getSession("192.168.0.31");
    }
}

