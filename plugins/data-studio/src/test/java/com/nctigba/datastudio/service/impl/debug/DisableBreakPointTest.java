/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
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
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * DisableBreakPointTest
 *
 * @since 2023-07-11
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DisableBreakPointTest {
    private final Map<String, Object> map = new HashMap<>();
    @InjectMocks
    private DisableBreakPointImpl disableBreakPoint;
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
    private LocaleStringUtils localeStringUtils;
    private String str = "{" + LF
            + "  \"operation\": \"disableBreakPoint\"," + LF
            + "  \"breakPoints\": [15]," + LF
            + "  \"rootWindowName\": \"postgres\"," + LF
            + "  \"oldWindowName\": \"\"," + LF
            + "  \"windowName\": \"postgres\"" + LF
            + "}";

    @Before
    public void setUp() throws SQLException {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeStringUtils.setMessageSource(messageSource);

        List<Integer> list = new ArrayList<>();
        list.add(10);
        list.add(11);
        Map<Integer, String> breakPointMap = new HashMap<>();
        breakPointMap.put(14, "0");
        breakPointMap.put(15, "1");

        map.put(STATEMENT, mockStatement);
        map.put(DIFFER, 4);
        map.put(OID, "201839");
        map.put(CAN_BREAK, list);
        map.put(BREAK_POINT, breakPointMap);
        when(webSocketServer.getParamMap(anyString())).thenReturn(map);

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
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        map.put(STATEMENT, null);
        disableBreakPoint.operate(webSocketServer, str);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        str = "{" + LF
                + "  \"operation\": \"disableBreakPoint\"," + LF
                + "  \"breakPoints\": [14]," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"" + LF
                + "}";

        disableBreakPoint.operate(webSocketServer, str);
    }


    @Test
    public void testOperate4() throws SQLException, IOException {
        str = "{" + LF
                + "  \"operation\": \"disableBreakPoint\"," + LF
                + "  \"breakPoints\": [18]," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"" + LF
                + "}";

        disableBreakPoint.operate(webSocketServer, str);
    }
}
