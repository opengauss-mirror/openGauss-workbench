package org.opengauss.admin.service.ops;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.opengauss.admin.common.core.domain.entity.ops.OpsAzEntity;
import org.opengauss.admin.system.service.ops.IOpsAzService;
import org.opengauss.admin.system.service.ops.impl.OpsAzServiceImpl;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author lhf
 * @date 2022/11/26 16:25
 **/
@RunWith(SpringRunner.class)
public class AzTest {

    @InjectMocks
    private IOpsAzService azService = new OpsAzServiceImpl() {
        @Override
        public boolean save(OpsAzEntity entity) {
            return true;
        }
    };

    @BeforeClass
    public static void before() {
        MockitoAnnotations.initMocks(HostTest.class);
        System.out.println("start az test........");
    }

    @AfterClass
    public static void after() {
        System.out.println("end az test........");
    }

    @Test
    public void testAdd() {
        OpsAzEntity opsAzEntity = new OpsAzEntity();
        boolean add = azService.add(opsAzEntity);
        Assertions.assertTrue(add);
    }
}
