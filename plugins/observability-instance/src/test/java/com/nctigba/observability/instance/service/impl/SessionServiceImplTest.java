/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.constants.DatabaseType;
import com.nctigba.observability.instance.dto.session.DetailStatisticDto;
import com.nctigba.observability.instance.factory.SessionHandlerFactory;
import com.nctigba.observability.instance.handler.session.SessionHandler;
import com.nctigba.observability.instance.model.InstanceNodeInfo;
import com.nctigba.observability.instance.service.ClusterManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test
 *
 * @author liupengfei
 * @since 2023/6/30
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionServiceImplTest {
    @InjectMocks
    private SessionServiceImpl sessionService;
    @Mock
    private SessionHandlerFactory sessionHandlerFactory;
    @Mock
    private ClusterManager opsFacade;
    @Mock
    private SessionHandler sessionHandler;
    private Connection connection;
    private InstanceNodeInfo instanceNodeInfo;
    private ClusterManager.OpsClusterNodeVOSub opsClusterNode;

    @Before
    public void setUp() {
        instanceNodeInfo = new InstanceNodeInfo();
        instanceNodeInfo.setIp("127.0.0.1");
        instanceNodeInfo.setPort(5432);
        instanceNodeInfo.setDbName("testDb");
        instanceNodeInfo.setDbUser("testUser");
        instanceNodeInfo.setDbUserPassword("testPassword");
        instanceNodeInfo.setDbType(DatabaseType.DEFAULT.getDbType());
        opsClusterNode = new ClusterManager.OpsClusterNodeVOSub();
        opsClusterNode.setPublicIp("127.0.0.1");
        opsClusterNode.setDbPort(5432);
        opsClusterNode.setDbName("testDb");
        opsClusterNode.setDbUser("testUser");
        opsClusterNode.setDbUserPassword("testPassword");
        when(opsFacade.getOpsNodeById(getId())).thenReturn(opsClusterNode);
        when(sessionHandlerFactory.getInstance(DatabaseType.DEFAULT.getDbType())).thenReturn(sessionHandler);
        when(sessionHandler.getConnection(instanceNodeInfo)).thenReturn(connection);
    }

    private static String getId() {
        return "12345";
    }

    @Test
    public void testDetailGeneral() {
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("key", "value");
        when(sessionHandler.detailGeneral(connection, getId())).thenReturn(expectedResult);
        JSONObject result = sessionService.detailGeneral(getId(), getId());
        assertEquals(expectedResult, result);
        verify(sessionHandlerFactory, times(1)).getInstance(DatabaseType.DEFAULT.getDbType());
        verify(sessionHandler, times(1)).getConnection(instanceNodeInfo);
        verify(sessionHandler, times(1)).detailGeneral(connection, getId());
        verify(sessionHandler, times(1)).close(connection);
    }

    @Test
    public void testDetailStatistic() {
        // Arrange
        List<DetailStatisticDto> expectedResult = List.of(new DetailStatisticDto());
        when(sessionHandler.detailStatistic(connection, getId())).thenReturn(expectedResult);
        // Act
        List<DetailStatisticDto> result = sessionService.detailStatistic(getId(), getId());
        // Assert
        assertNotNull(result);
        assertEquals(expectedResult, result);
        verify(sessionHandler, times(1)).detailStatistic(connection, getId());
        verify(sessionHandler, times(1)).close(connection);
    }

    @Test
    public void testDetailWaiting() {
        JSONObject object = new JSONObject();
        object.put("key", "value");
        List<JSONObject> expectedResult = List.of(object);
        when(sessionHandler.detailWaiting(connection, getId())).thenReturn(expectedResult);
        List<JSONObject> result = sessionService.detailWaiting(getId(), getId());
        assertEquals(expectedResult, result);
        verify(sessionHandlerFactory, times(1)).getInstance(DatabaseType.DEFAULT.getDbType());
        verify(sessionHandler, times(1)).getConnection(instanceNodeInfo);
        verify(sessionHandler, times(1)).detailWaiting(connection, getId());
        verify(sessionHandler, times(1)).close(connection);
    }

    @Test
    public void testDetailBlockTree() {
        JSONObject object = new JSONObject();
        object.put("key", "value");
        List<JSONObject> expectedResult = List.of(object);
        when(sessionHandler.detailBlockTree(connection, getId())).thenReturn(expectedResult);
        List<JSONObject> result = sessionService.detailBlockTree(getId(), getId());
        assertEquals(expectedResult, result);
        verify(sessionHandlerFactory, times(1)).getInstance(DatabaseType.DEFAULT.getDbType());
        verify(sessionHandler, times(1)).getConnection(instanceNodeInfo);
        verify(sessionHandler, times(1)).detailBlockTree(connection, getId());
        verify(sessionHandler, times(1)).close(connection);
    }

    @Test
    public void testSimpleStatistic() {
        JSONObject expectedResult = new JSONObject();
        expectedResult.put("key", "value");
        when(sessionHandler.simpleStatistic(connection)).thenReturn(expectedResult);
        JSONObject result = sessionService.simpleStatistic(getId());
        assertEquals(expectedResult, result);

        verify(sessionHandlerFactory, times(1)).getInstance(DatabaseType.DEFAULT.getDbType());
        verify(sessionHandler, times(1)).getConnection(instanceNodeInfo);
        verify(sessionHandler, times(1)).simpleStatistic(connection);
        verify(sessionHandler, times(1)).close(connection);
    }

    @Test
    public void testLongTxc() {
        JSONObject object = new JSONObject();
        object.put("key", "value");
        List<JSONObject> expectedResult = List.of(object);
        when(sessionHandler.longTxc(connection)).thenReturn(expectedResult);
        List<JSONObject> result = sessionService.longTxc(getId());
        assertEquals(expectedResult, result);
        verify(sessionHandlerFactory, times(1)).getInstance(DatabaseType.DEFAULT.getDbType());
        verify(sessionHandler, times(1)).getConnection(instanceNodeInfo);
        verify(sessionHandler, times(1)).longTxc(connection);
        verify(sessionHandler, times(1)).close(connection);
    }

    @Test
    public void testBlockAndLongTxc() {
        JSONObject object = new JSONObject();
        object.put("key", "value");
        List<JSONObject> longTxc = List.of(object);
        List<JSONObject> blockTree = List.of(object);
        HashMap<String, List<JSONObject>> expectedResult = new HashMap<>();
        expectedResult.put("blockTree", blockTree);
        expectedResult.put("longTxc", longTxc);

        when(sessionHandler.detailBlockTree(connection, null)).thenReturn(blockTree);
        when(sessionHandler.longTxc(connection)).thenReturn(longTxc);

        HashMap<String, List<JSONObject>> result = sessionService.blockAndLongTxc(getId());
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDetail() {
        JSONObject object = new JSONObject();
        object.put("key", "value");
        List<JSONObject> blockTree = List.of(object);
        List<JSONObject> waiting = List.of(object);
        List<DetailStatisticDto> statistic = List.of(new DetailStatisticDto());
        HashMap<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("blockTree", blockTree);
        expectedResult.put("general", object);
        expectedResult.put("statistic", statistic);
        expectedResult.put("waiting", waiting);

        when(sessionHandler.detailWaiting(connection, getId())).thenReturn(waiting);
        when(sessionHandler.detailStatistic(connection, getId())).thenReturn(statistic);
        when(sessionHandler.detailBlockTree(connection, getId())).thenReturn(blockTree);
        when(sessionHandler.detailGeneral(connection, getId())).thenReturn(object);

        Map<String, Object> result = sessionService.detail(getId(), getId());
        assertEquals(expectedResult, result);
    }
}