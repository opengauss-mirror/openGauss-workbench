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
 *  StartSqlTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/service/impl/sql/StartSqlTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import cn.hutool.core.thread.ThreadUtil;
import com.nctigba.datastudio.base.WebSocketServer;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.entity.OperateStatusDO;
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
public class StartSqlTest {
    private static final String STR = "{\n"
            + "  \"operation\": \"stopDebug\",\n"
            + "  \"sql\": \"select 1\",\n"
            + "  \"rootWindowName\": \"1.1.1.1_1679042795481\",\n"
            + "  \"oldWindowName\": \"\",\n"
            + "  \"windowName\": \"1.1.1.1_1679042795481\"\n"
            + "}";

    @InjectMocks
    private StartSqlImpl startSql;
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    @Mock
    private OperateStatusDO operateStatusDO;
    @Mock
    private WebSocketServer webSocketServer;
    @Mock
    private MessageSource messageSource;
    @Spy
    private LocaleStringUtils localeStringUtils;

    @Before
    public void setUp() {
        conMap = new HashMap<>();
        conMap.put("8359cbf1-9833-4998-a29c-245f24009ab1", new ConnectionDTO());

        localeStringUtils.setMessageSource(messageSource);
    }

    @Test
    public void testOperate() throws SQLException, IOException, InterruptedException {
        Map<String, Object> map = new HashMap<>();
        map.put(STATEMENT, mockStatement);
        map.put(CONNECTION, mockConnection);
        when(webSocketServer.getConnection(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);

        startSql.operate(webSocketServer, STR);
        ThreadUtil.sleep(1000);
    }
}
