package org.opengauss.admin.plugin.service.modeling;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.opengauss.admin.plugin.ModelingBaseTest;
import org.opengauss.admin.plugin.domain.model.modeling.ModelingDataFlowSqlObject;
import org.opengauss.admin.plugin.service.modeling.impl.ModelingDataBaseServiceImpl;
import org.opengauss.admin.system.plugin.facade.OpsFacade;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ModelingDataBaseServiceTest extends ModelingBaseTest {

    @InjectMocks
    private IModelingDataBaseService modelingDataBaseService = new ModelingDataBaseServiceImpl();

    @Mock
    private OpsFacade opsFacade;

    private ModelingDataFlowSqlObject sqlObject;

    private void prepareData(){
        //test data init
        OpsClusterNodeVO clusterNodeVO = new OpsClusterNodeVO();
        clusterNodeVO.setNodeId("99999999");
        clusterNodeVO.setDbPort(15400);
        clusterNodeVO.setDbName("postgres");
        clusterNodeVO.setDbUser("leige");
        clusterNodeVO.setDbUserPassword("leige@111");

        OpsClusterVO clusterVO = new OpsClusterVO();
        clusterVO.setClusterId("test");
        clusterVO.setClusterNodes(List.of(clusterNodeVO));

        sqlObject = new ModelingDataFlowSqlObject();
        sqlObject.setPreparedSql("select * from modeling_test_data_sales_list limit 100;");
        sqlObject.setPreparedParams(List.of());
        sqlObject.setExecuteClusterId("99999999");
        sqlObject.setExecuteSchema("public");
        sqlObject.setExecuteClusterNode(clusterNodeVO);

        //mock into function
        when(opsFacade.listCluster()).thenReturn(List.of(clusterVO));
    }

    @Test
    public void getById() throws SQLException, ClassNotFoundException {
        prepareData();
        List<Map<String, Object>> result = modelingDataBaseService.queryWithSqlObject(sqlObject);
        Assertions.assertNotNull(result);
    }


}
