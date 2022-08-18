package org.opengauss.admin.plugin.service.modeling;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.plugin.ModelingBaseTest;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.opengauss.admin.plugin.mapper.modeling.ModelingDataFlowMapper;
import org.opengauss.admin.plugin.service.modeling.impl.ModelingDataFlowServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ModelingDataFlowServiceTest extends ModelingBaseTest {

    @InjectMocks
    private IModelingDataFlowService modelingDataFlowService = new ModelingDataFlowServiceImpl();

    @Mock
    private ModelingDataFlowMapper modelingDataFlowMapper;

    private void prepareData(){
        //test data init
        ModelingDataFlowEntity mockEntity = new ModelingDataFlowEntity();
        mockEntity.setName("test");
        mockEntity.setSchema("public");
        ModelingDataFlowEntity mockEntity2 = new ModelingDataFlowEntity();
        mockEntity2.setName("test2");
        mockEntity2.setSchema("public");

        IPage<ModelingDataFlowEntity> page = new Page<>();
        page.setRecords(List.of(mockEntity,mockEntity2));
        page.setPages(1);
        page.setCurrent(1);
        page.setTotal(2);

        //mock into function
        when(modelingDataFlowMapper.selectPage(ArgumentMatchers.any(Page.class),ArgumentMatchers.any())).thenReturn((Page) page);
    }

    @Test
    public void selectList() {
        prepareData();
        IPage<ModelingDataFlowEntity> result = modelingDataFlowService.selectList(new Page<>(), "test");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.getRecords().size(),2 );
        Assertions.assertEquals(result.getRecords().get(0).getName(),"test" );
        Assertions.assertEquals(result.getRecords().get(1).getName(),"test2" );
    }
}
