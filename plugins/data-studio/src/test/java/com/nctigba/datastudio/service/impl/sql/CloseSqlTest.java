/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.CONNECTION;
import static com.nctigba.datastudio.constants.CommonConstants.STATEMENT;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * StopDebugTest
 *
 * @since 2023-07-04
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class CloseSqlTest {
    private static final String STR = "{\n"
            + "  \"operation\": \"stopDebug\",\n"
            + "  \"uuid\": \"111\",\n"
            + "  \"rootWindowName\": \"1.1.1.1_1679042795481\",\n"
            + "  \"oldWindowName\": \"\",\n"
            + "  \"windowName\": \"1.1.1.1_1679042795481\"\n"
            + "}";

    @InjectMocks
    private CloseSqlImpl closeSql;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private OperateStatusDO operateStatusDO;
    @Mock
    private WebSocketServer webSocketServer;
    @Mock
    private MessageSource messageSource;
    @Spy
    private LocaleString localeString;

    @Before
    public void setUp() {
        conMap = new HashMap<>();
        conMap.put("111", new ConnectionDTO());
        localeString.setMessageSource(messageSource);
    }

    @Test
    public void testOperate() throws SQLException, IOException {
        closeSql.operate(webSocketServer, STR);
    }

    @Test
    public void testOperate2() throws SQLException, IOException {
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(CONNECTION, mockConnection);
        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(webSocketServer.getStatement(anyString())).thenReturn(mockStatement);

        closeSql.operate(webSocketServer, STR);
    }
}
