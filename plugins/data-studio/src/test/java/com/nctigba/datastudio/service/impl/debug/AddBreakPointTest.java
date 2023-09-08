/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.util.LocaleString;
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.BREAK_POINT;
import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK;
import static com.nctigba.datastudio.constants.CommonConstants.DIFFER;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * AddBreakPointTest
 *
 * @since 2023-07-11
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AddBreakPointTest {
    private final Map<String, Object> map = new HashMap<>();
    @InjectMocks
    private AddBreakPointImpl addBreakPoint;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private ResultSetMetaData mockMetaData;
    @Mock
    private WebSocketServer webSocketServer;
    @Mock
    private MessageSource messageSource;
    @Spy
    private LocaleString localeString;
    private String str = "{\n"
            + "  \"operation\": \"addBreakPoint\",\n"
            + "  \"line\":\"15\",\n"
            + "  \"rootWindowName\": \"postgres\",\n"
            + "  \"oldWindowName\": \"\",\n"
            + "  \"windowName\": \"postgres\"\n"
            + "}";

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeString.setMessageSource(messageSource);

        List<Integer> list = new ArrayList<>();
        list.add(10);
        list.add(11);
        Map<Integer, String> breakPointMap = new HashMap<>();
        breakPointMap.put(11, "0");

        map.put(STATEMENT, mockStatement);
        map.put(DIFFER, 4);
        map.put(OID, "201839");
        map.put(CAN_BREAK, list);
        map.put(BREAK_POINT, breakPointMap);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        map.put(BREAK_POINT, null);
        map.put(STATEMENT, null);
        addBreakPoint.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        str = "{\n"
                + "  \"operation\": \"addBreakPoint\",\n"
                + "  \"line\":\"10\",\n"
                + "  \"rootWindowName\": \"postgres\",\n"
                + "  \"oldWindowName\": \"\",\n"
                + "  \"windowName\": \"postgres\"\n"
                + "}";

        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(3);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("name1").thenReturn("name1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("name2").thenReturn("name2");
        when(mockMetaData.getColumnName(eq(3))).thenReturn("name3").thenReturn("name3");
        when(mockResultSet.getString("name1")).thenReturn("t").thenReturn("t");
        when(mockResultSet.getString("name2")).thenReturn("f").thenReturn("f");
        when(mockResultSet.getString("name3")).thenReturn("201839").thenReturn("201830");

        addBreakPoint.operate(webSocketServer, str);
    }

    @Test
    public void testOperate3() throws SQLException, IOException {
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(2);
        when(mockMetaData.getColumnName(eq(1))).thenReturn("name1");
        when(mockMetaData.getColumnName(eq(2))).thenReturn("name2");

        addBreakPoint.operate(webSocketServer, str);
    }
}
