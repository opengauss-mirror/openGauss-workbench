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
 * ModelingDataFlowControllerTest.java
 *
 * IDENTIFICATION
 * base-ops/src/test/java/org/opengauss/admin/plugin/controller/modeling/ModelingDataFlowControllerTest.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.controller.modeling;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.plugin.ModelingBaseTest;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.opengauss.admin.plugin.service.modeling.IModelingDataFlowService;
import org.junit.*;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;


import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ModelingDataFlowControllerTest extends ModelingBaseTest {

    @InjectMocks
    private ModelingDataFlowController modelingDataFlowController = new ModelingDataFlowController();

    @Mock
    private IModelingDataFlowService modelingDataFlowService;

    private void prepareData(){
        //test data init
        ModelingDataFlowEntity mockEntity = new ModelingDataFlowEntity();
        mockEntity.setName("test");
        mockEntity.setSchema("public");

        //mock into function
        when(modelingDataFlowService.getById("1381")).thenReturn(mockEntity);
    }

    @Test
    public void getById() {
        prepareData();
        AjaxResult result = modelingDataFlowController.get("1381");
        Assertions.assertEquals(result.get("code"),200);
        Assertions.assertNotNull(result.get("data"));
    }

    @Test
    public void getById_fail() {
        prepareData();
        AjaxResult result = modelingDataFlowController.get("1005");
        Assertions.assertEquals(result.get("code"),200);
        Assertions.assertNull(result.get("data"));
    }
}
