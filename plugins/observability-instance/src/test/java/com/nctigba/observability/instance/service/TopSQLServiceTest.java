/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  TopSQLServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/test/java/com/nctigba/observability/instance/service/TopSQLServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterVO;

import com.alibaba.fastjson.JSONObject;

/**
 * TopSQLServiceTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class TopSQLServiceTest {
    @Mock
    private ClusterManager clusterManager;

    @Mock
    private TopSQLService topSQLService;

    @Test
    public void testGetCluster() {
        Mockito.doReturn(mockClusterList()).when(clusterManager).getAllOpsCluster();
        assertEquals(mockClusterList(), clusterManager.getAllOpsCluster());
    }

    // @Test
    void testGetTopSQLAction() {
        Mockito.doReturn(mockTopSQLList()).when(topSQLService).topSQLList(any());
        Mockito.doReturn(mockJsonObject()).when(topSQLService).detail(anyString(), anyString());
        Mockito.doReturn(mockJsonObject()).when(topSQLService).executionPlan(anyString(), anyString());
        Mockito.doReturn(mockStringList()).when(topSQLService).indexAdvice(anyString(), anyString());
        Mockito.doReturn(mockJsonObject()).when(topSQLService).objectInfo(anyString(), anyString());
    }

    private List<OpsClusterVO> mockClusterList() {
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
        List<OpsClusterVO> opsClusterVOList = new ArrayList<>();
        opsClusterVOList.add(opsClusterVO);
        return opsClusterVOList;
    }

    private List<String> mockStringList() {
        List<String> stringList = new ArrayList<>();
        stringList.add("no data");
        return stringList;
    }

    private JSONObject mockJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", new ArrayList<>());
        return jsonObject;
    }

    private List<JSONObject> mockTopSQLList() {
        List<JSONObject> jsonObjectList = new ArrayList<>();
        jsonObjectList.add(new JSONObject());
        jsonObjectList.add(new JSONObject());
        return jsonObjectList;
    }
}