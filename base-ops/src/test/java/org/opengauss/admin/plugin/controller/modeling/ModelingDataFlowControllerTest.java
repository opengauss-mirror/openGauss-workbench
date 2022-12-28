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
