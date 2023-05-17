package com.nctigba.observability.instance;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.TopSQLService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
public class TopSQLServiceTest {

    @Mock
    private ClusterManager clusterManager;

    @Mock
    private TopSQLService topSQLService;

    @Test
    public void testGetCluster () {
        Mockito.doReturn(mockClusterList()).when(clusterManager).getAllOpsCluster();
    }

    @Test
    public void testGetTopSQLAction () {
        Mockito.doReturn(mockTopSQLList()).when(topSQLService).getTopSQLList(any());
        Mockito.doReturn(mockJsonObject()).when(topSQLService).getStatisticalInfo(any());
        Mockito.doReturn(mockJsonObject()).when(topSQLService).getExecutionPlan(any(), any());
        Mockito.doReturn(mockTopSQLList()).when(topSQLService).getPartitionList(any());
        Mockito.doReturn(mockStringList()).when(topSQLService).getIndexAdvice(any());
        Mockito.doReturn(mockJsonObject()).when(topSQLService).getObjectInfo(any());
    }

    private ClusterManager.OpsClusterNodeVOSub mockClusterNode() {
        ClusterManager.OpsClusterNodeVOSub sub = new ClusterManager.OpsClusterNodeVOSub();
        sub.setPublicIp("192.168.0.1");
        sub.setPrivateIp("192.168.0.1");
        sub.setDbPort(15400);
        sub.setDbName("postgres");
        sub.setDbUser("root");
        sub.setDbUserPassword("123");
        sub.setAzName("local version");
        sub.setAzAddress("192.168.0.1");
        sub.setClusterRole("local version");
        sub.setNodeId("ffd3dbd2d5a5b920b65441485d949e27");
        return sub;
    }

    private List<OpsClusterVO> mockClusterList() {
        List<OpsClusterVO> opsClusterVOList = new ArrayList<>();
        OpsClusterNodeVO opsClusterNode = new OpsClusterNodeVO() {
            {
                setPublicIp("192.168.0.1");
                setPrivateIp("192.168.0.1");
                setDbPort(15400);
                setDbName("postgres");
                setDbUser("root");
                setDbUserPassword("123");
                setAzName("local version");
                setAzAddress("192.168.0.1");
                setClusterRole("local version");
                setNodeId("ffd3dbd2d5a5b920b65441485d949e27");
            }
        };
        OpsClusterVO opsClusterVO = new OpsClusterVO();
        opsClusterVO.setClusterId("TestID");
        opsClusterVO.setClusterName("TestMASTER");
        opsClusterVO.setClusterNodes(Arrays.asList(opsClusterNode));
        opsClusterVOList.add(opsClusterVO);
        return opsClusterVOList;
    }

    private List<String> mockStringList () {
        List<String> stringList = new ArrayList<>();
        stringList.add("no data");
        return stringList;
    }

    private JSONObject mockJsonObject () {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", new ArrayList<>());
        return jsonObject;
    }

    private List<JSONObject> mockTopSQLList () {
        List<JSONObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(new JSONObject());
        jsonObjectList.add(new JSONObject());
        return jsonObjectList;
    }
}
