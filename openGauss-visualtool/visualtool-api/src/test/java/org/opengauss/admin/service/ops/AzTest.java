/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * AzTest.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/test/java/org/opengauss/admin/service/ops/AzTest.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.service.ops;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AzTest {

    @InjectMocks
    private IOpsAzService azService = new OpsAzServiceImpl() {
        @Override
        public boolean save(OpsAzEntity entity) {
            return true;
        }

        @Override
        public long count(Wrapper<OpsAzEntity> queryWrapper) {
            return 0;
        }
    };

    @BeforeClass
    public static void before() {
        MockitoAnnotations.initMocks(HostTest.class);
        log.info("start az test........");
    }

    @AfterClass
    public static void after() {
        log.info("end az test........");
    }

    @Test
    public void testAdd() {
        OpsAzEntity opsAzEntity = new OpsAzEntity();
        opsAzEntity.setName("abc");
        boolean add = azService.add(opsAzEntity);
        Assertions.assertTrue(add);
    }
}
