package org.opengauss.admin.plugin.service.modeling;

import com.alibaba.fastjson.JSONObject;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.plugin.ModelingBaseTest;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.opengauss.admin.plugin.service.modeling.operator.IModelingDataFlowProcessService;
import org.opengauss.admin.plugin.service.modeling.operator.factory.ModelingDataFlowOperatorFactory;
import org.opengauss.admin.plugin.service.modeling.operator.impl.ModelingDataFlowProcessServiceImpl;
import org.opengauss.admin.plugin.service.modeling.operator.impl.OperatorConditionBuilderServiceImpl;
import org.opengauss.admin.plugin.service.modeling.operator.impl.OperatorQueryBuilderServiceImpl;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ModelingDataFlowProcessServiceTest extends ModelingBaseTest {

    @InjectMocks
    private IModelingDataFlowProcessService modelingDataFlowProcessService = new ModelingDataFlowProcessServiceImpl();

    @Mock
    ModelingDataFlowOperatorFactory modelingDataFlowOperatorFactory;
    @Mock
    IModelingDataFlowOperatorService modelingDataFlowOperatorService;
    @Mock
    private IModelingDataFlowService modelingDataFlowService;
    @Mock
    private IModelingDataBaseService modelingDataBaseService;

    public JSONObject paramsObject;

    private void prepareData(){
        //test data init
        String paramsInJsonString = "{\"cells\":[{\"shape\":\"edge\",\"attrs\":{\"line\":{\"stroke\":\"#222c36\",\"strokeWidth\":1,\"targetMarker\":{\"name\":\"classic\",\"size\":8}}},\"id\":\"ab204a45-add6-4f4a-b286-87cb7d7f5e77\",\"zIndex\":0,\"connector\":{\"name\":\"rounded\"},\"router\":{\"name\":\"manhattan\"},\"source\":{\"cell\":\"f846ca89-9c26-498c-907e-af49de72a141\",\"port\":\"port2\"},\"target\":{\"cell\":\"704e90aa-9e5d-4794-8fa4-a208d6f0fd5d\",\"port\":\"port1\"},\"cells_type\":\"line\"},{\"shape\":\"edge\",\"attrs\":{\"line\":{\"stroke\":\"#222c36\",\"strokeWidth\":1,\"targetMarker\":{\"name\":\"classic\",\"size\":8}}},\"id\":\"579f58e9-4c6a-405d-88cd-d678e3e7e11f\",\"zIndex\":0,\"connector\":{\"name\":\"rounded\"},\"router\":{\"name\":\"manhattan\"},\"source\":{\"cell\":\"f846ca89-9c26-498c-907e-af49de72a141\",\"port\":\"port2\"},\"target\":{\"cell\":\"34a02afb-5ccb-4942-9bdc-c2e5e7d264e6\",\"port\":\"port1\"},\"cells_type\":\"line\"},{\"shape\":\"edge\",\"attrs\":{\"line\":{\"stroke\":\"#222c36\",\"strokeWidth\":1,\"targetMarker\":{\"name\":\"classic\",\"size\":8}}},\"id\":\"37023d5a-5b6e-43bc-8918-01e8ae84800c\",\"zIndex\":0,\"connector\":{\"name\":\"rounded\"},\"router\":{\"name\":\"manhattan\"},\"source\":{\"cell\":\"704e90aa-9e5d-4794-8fa4-a208d6f0fd5d\",\"port\":\"port2\"},\"target\":{\"cell\":\"8a5c7f0a-2856-4c91-a54b-b617290e927e\",\"port\":\"port1\"},\"cells_type\":\"line\"},{\"position\":{\"x\":600,\"y\":380},\"size\":{\"width\":220,\"height\":60},\"view\":\"vue-shape-view\",\"shape\":\"BaseNode\",\"component\":{},\"id\":\"704e90aa-9e5d-4794-8fa4-a208d6f0fd5d\",\"data\":{\"type\":\"BaseNode\",\"cells_type\":\"query\",\"text\":\"查询算子\",\"configName\":\"QueryOperator\",\"icon\":\"modeling-chaxun\",\"table\":\"modeling_test_data_sales_list\",\"fields\":[],\"showDisabledCheckbox\":false,\"antvSelected\":false},\"ports\":{\"groups\":{\"top\":{\"position\":\"top\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"bottom\":{\"position\":\"bottom\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"left\":{\"position\":\"left\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"right\":{\"position\":\"right\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}}},\"items\":[{\"group\":\"top\",\"id\":\"port1\"},{\"group\":\"bottom\",\"id\":\"port2\"},{\"group\":\"left\",\"id\":\"port3\"},{\"group\":\"right\",\"id\":\"port4\"}]},\"zIndex\":1,\"cells_type\":\"query\"},{\"position\":{\"x\":926,\"y\":380},\"size\":{\"width\":220,\"height\":60},\"view\":\"vue-shape-view\",\"shape\":\"BaseNode\",\"component\":{},\"id\":\"34a02afb-5ccb-4942-9bdc-c2e5e7d264e6\",\"data\":{\"type\":\"BaseNode\",\"cells_type\":\"query\",\"text\":\"查询算子\",\"configName\":\"QueryOperator\",\"icon\":\"modeling-chaxun\",\"table\":\"modeling_test_data_relations\",\"fields\":[],\"showDisabledCheckbox\":false,\"antvSelected\":false},\"ports\":{\"groups\":{\"top\":{\"position\":\"top\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"bottom\":{\"position\":\"bottom\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"left\":{\"position\":\"left\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"right\":{\"position\":\"right\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}}},\"items\":[{\"group\":\"top\",\"id\":\"port1\"},{\"group\":\"bottom\",\"id\":\"port2\"},{\"group\":\"left\",\"id\":\"port3\"},{\"group\":\"right\",\"id\":\"port4\"}]},\"zIndex\":2,\"cells_type\":\"query\"},{\"position\":{\"x\":760,\"y\":250},\"size\":{\"width\":220,\"height\":60},\"view\":\"vue-shape-view\",\"shape\":\"BaseNode\",\"component\":{},\"id\":\"f846ca89-9c26-498c-907e-af49de72a141\",\"data\":{\"type\":\"BaseNode\",\"cells_type\":\"dataSource\",\"text\":\"数据源算子\",\"configName\":\"DataSource\",\"icon\":\"modeling-dataSource\",\"source\":[\"tjj\",\"1592093252579741698\",\"public\"],\"showDisabledCheckbox\":false,\"antvSelected\":false,\"clusterId\":\"tjj\",\"clusterNodeId\":\"1592093252579741698\",\"schema\":\"public\"},\"ports\":{\"groups\":{\"top\":{\"position\":\"top\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"bottom\":{\"position\":\"bottom\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"left\":{\"position\":\"left\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"right\":{\"position\":\"right\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}}},\"items\":[{\"group\":\"top\",\"id\":\"port1\"},{\"group\":\"bottom\",\"id\":\"port2\"},{\"group\":\"left\",\"id\":\"port3\"},{\"group\":\"right\",\"id\":\"port4\"}]},\"zIndex\":3,\"cells_type\":\"dataSource\"},{\"position\":{\"x\":590,\"y\":540},\"size\":{\"width\":220,\"height\":60},\"view\":\"vue-shape-view\",\"shape\":\"BaseNode\",\"component\":{},\"id\":\"8a5c7f0a-2856-4c91-a54b-b617290e927e\",\"data\":{\"type\":\"BaseNode\",\"cells_type\":\"condition\",\"text\":\"条件算子\",\"configName\":\"ConditionOperator\",\"icon\":\"modeling-tiaojian\",\"or\":[[{\"field\":\"modeling_test_data_sales_list.id\",\"condition\":\"equal\",\"value\":\"1\"}]],\"showDisabledCheckbox\":false,\"antvSelected\":false},\"ports\":{\"groups\":{\"top\":{\"position\":\"top\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"bottom\":{\"position\":\"bottom\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"left\":{\"position\":\"left\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}},\"right\":{\"position\":\"right\",\"attrs\":{\"circle\":{\"r\":5,\"magnet\":true,\"stroke\":\"#5F95FF\",\"strokeWidth\":1,\"fill\":\"#fff\",\"style\":{\"visibility\":\"hidden\"}}}}},\"items\":[{\"group\":\"top\",\"id\":\"port1\"},{\"group\":\"bottom\",\"id\":\"port2\"},{\"group\":\"left\",\"id\":\"port3\"},{\"group\":\"right\",\"id\":\"port4\"}]},\"zIndex\":4,\"cells_type\":\"condition\"}],\"dataFlowId\":\"1381\"}";
        paramsObject = JSONObject.parseObject(paramsInJsonString);

        ModelingDataFlowEntity mockEntity = new ModelingDataFlowEntity();
        mockEntity.setName("test");
        mockEntity.setClusterNodeId("1592093252579741698");
        mockEntity.setSchema("public");

        List<String> enableOperatorType = List.of("query","delete","update","insert");

        //mock into function
        when(modelingDataFlowService.getById("1381")).thenReturn(mockEntity);
        when(modelingDataFlowOperatorService.selectMainOperatorConfigName()).thenReturn(enableOperatorType);
        when(modelingDataFlowOperatorFactory.getBuilder("query")).thenReturn(new OperatorQueryBuilderServiceImpl());
        when(modelingDataFlowOperatorFactory.getBuilder("condition")).thenReturn(new OperatorConditionBuilderServiceImpl());
        when(modelingDataBaseService.getClusterNodeById("1592093252579741698")).thenReturn(new OpsClusterNodeVO());

    }

    @Test
    public void getAllSqlObject() {
        prepareData();
        List<ModelingDataFlowSqlObject> result = modelingDataFlowProcessService.getAllSqlObject(paramsObject);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.size(),2);
        Assertions.assertEquals(result.get(1).toSql(),"select * from modeling_test_data_relations  limit 1000;");

    }

    @Test
    public void getQueryCount() {
        prepareData();
        Integer result = modelingDataFlowProcessService.getQueryCount(paramsObject);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result,2);
    }

    @Test
    public void getSqlObjectByTargetOperatorId() {
        prepareData();
        ModelingDataFlowSqlObject result = modelingDataFlowProcessService.getSqlObjectByTargetOperatorId(paramsObject, "8a5c7f0a-2856-4c91-a54b-b617290e927e","1381");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result.toSql(),"select * from modeling_test_data_sales_list where ( modeling_test_data_sales_list.id = '1' )  limit 1000;");
    }

    @Test
    public void getMainQueryType() {
        prepareData();
        String result = modelingDataFlowProcessService.getMainQueryType(paramsObject);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(result,"query");
    }

}
