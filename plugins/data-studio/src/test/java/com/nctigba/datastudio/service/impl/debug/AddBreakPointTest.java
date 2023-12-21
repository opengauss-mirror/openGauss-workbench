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
 *  AddBreakPointTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/service/impl/debug/AddBreakPointTest.java
 *
 *  -------------------------------------------------------------------------
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
    private LocaleStringUtils localeStringUtils;
    private String str = "{" + LF
            + "  \"operation\": \"addBreakPoint\"," + LF
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
        str = "{" + LF
                + "  \"operation\": \"addBreakPoint\"," + LF
                + "  \"breakPoints\": [10]," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"" + LF
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
