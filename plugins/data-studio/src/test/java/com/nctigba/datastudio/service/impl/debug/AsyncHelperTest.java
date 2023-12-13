/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.query.PublicParamQuery;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
import com.nctigba.datastudio.model.entity.SqlHistoryDO;
import com.nctigba.datastudio.service.impl.sql.SqlHistoryManagerServiceImpl;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * ExecuteTest
 *
 * @since 2023-07-04
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AsyncHelperTest {
    @InjectMocks
    private AsyncHelper asyncHelper;

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ResultSetMetaData mockMetaData;

    @Mock
    private WebSocketServer webSocketServer;

    @Spy
    private PublicParamQuery paramReq;

    @Mock
    private OperateStatusDO operateStatusDO;

    @Mock
    private MessageSource messageSource;

    @Spy
    private LocaleStringUtils localeStringUtils;

    @Mock
    private SqlHistoryManagerServiceImpl managerService;

    @Before
    public void setUp() {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeStringUtils.setMessageSource(messageSource);
        Map<String, Object> map = new HashMap<>();
        map.put(OID, "201839");
        map.put("201839", "window_name");
        when(webSocketServer.getOperateStatus(anyString())).thenReturn(operateStatusDO);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

        paramReq.setWindowName("postgres");
        paramReq.setSql("CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)\\n "
                + "RETURNS SETOF integer\\n LANGUAGE plpgsql\\n NOT FENCED NOT SHIPPABLE\\nAS $$\\n"
                + "DECLARE\\n\\n\\nBEGIN\\n  result = i + 1;\\n  result = result + 2;\\n  "
                + "if result < 10\\n  then\\n    result = test(result);\\n  else\\n    "
                + "result = result + 3;\\n  end if;\\n  result = result + 4;\\n\\nRETURN NEXT;\\nEND;$$;\\n/");
    }

    @Test
    public void testOperateException() throws SQLException {
        paramReq.setOid("0");
        paramReq.setInPackage(true);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate(anyString())).thenThrow(new SQLException(""));

        try {
            asyncHelper.task(webSocketServer, paramReq);
        } catch (SQLException | IOException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testOperateException2() throws SQLException {
        paramReq.setOid("201839");
        paramReq.setInPackage(false);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException(""));

        try {
            asyncHelper.task(webSocketServer, paramReq);
        } catch (SQLException | IOException e) {
            log.info(e.getMessage());
        }
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        paramReq.setOid("0");
        paramReq.setCoverage(true);
        paramReq.setInPackage(false);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);

        asyncHelper.task(webSocketServer, paramReq);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        paramReq.setOid("201839");
        paramReq.setCoverage(false);
        paramReq.setInPackage(true);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("key2");
        when(mockResultSet.getObject("key1")).thenReturn("111");
        when(mockResultSet.getObject("key2")).thenReturn("222");

        asyncHelper.task(webSocketServer, paramReq);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        paramReq.setOid("201839");
        paramReq.setCoverage(false);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("key2");
        when(mockResultSet.getObject("key1")).thenReturn("111").thenReturn("222");
        when(mockResultSet.getObject("key2")).thenReturn("333").thenReturn("444");

        asyncHelper.task(webSocketServer, paramReq);
    }

    @Test
    public void testOperate4() throws SQLException, IOException {
        paramReq.setOid("201839");
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(CONNECTION, mockConnection);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);

        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(1);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("key1");
        when(mockResultSet.getObject("key1")).thenReturn("111");

        paramReq.setCoverage(false);
        asyncHelper.task(webSocketServer, paramReq);
    }

    @Test
    public void testOperate5() throws SQLException, IOException {
        paramReq.setOid("201839");
        paramReq.setCoverage(true);
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(CONNECTION, mockConnection);
        map.put(OID, "201800");
        map.put("201839", "postgres");
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);

        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);

        asyncHelper.task(webSocketServer, paramReq);
    }

    @Test
    public void testInsertHistory() {
        SqlHistoryDO sqlHistoryDO = new SqlHistoryDO();
        sqlHistoryDO.setWebUser("admin");
        sqlHistoryDO.setSql("select 1");
        sqlHistoryDO.setSuccess(true);
        sqlHistoryDO.setLock(false);
        sqlHistoryDO.setStartTime("2023-08-29 12:23:34");
        sqlHistoryDO.setExecuteTime("2023-08-29 12:23:34");

        List<SqlHistoryDO> list = new ArrayList<>();
        list.add(sqlHistoryDO);

        asyncHelper.insertSqlHistory(list);
    }
}
