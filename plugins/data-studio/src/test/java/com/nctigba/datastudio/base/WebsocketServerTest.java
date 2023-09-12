/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.base;

import com.nctigba.datastudio.model.entity.OperateStatusDO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import javax.websocket.Session;
import java.sql.Connection;
import java.sql.Statement;

/**
 * WebsocketServerTest
 *
 * @since 2023-07-14
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class WebsocketServerTest {
    @InjectMocks
    private WebSocketServer webSocketServer;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private Session session;

    @Mock
    private OperateStatusDO operateStatusDO;

    @Mock
    private ApplicationContext applicationContext;

    @Test
    public void testOnOpen() {
        webSocketServer.onOpen("webds-plugin", "postgres", session);
    }

    @Test
    public void testProcess() {
        webSocketServer.processMessage("postgres", "");
    }

    @Test
    public void testProcess2() {
        webSocketServer.processMessage("postgres", "message");
    }

    @Test
    public void testProcess3() {
        webSocketServer.processMessage("postgres", "{\n"
                + "  \"connection\": \"connection\""
                + "}");
    }

    @Test
    public void testProcess4() {
        MockedStatic<SpringApplicationContext> staticUtilsMockedStatic = Mockito.mockStatic(
                SpringApplicationContext.class);
        staticUtilsMockedStatic.when(SpringApplicationContext::getApplicationContext).thenReturn(
                applicationContext);

        webSocketServer.processMessage("postgres", "{\n"
                + "  \"operation\": \"connection\",\n"
                + "  \"language\": \"zh-CN\",\n"
                + "  \"webUser\": \"A\",\n"
                + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\",\n"
                + "  \"rootWindowName\": \"119.3.170.242_1679042795481\",\n"
                + "  \"oldWindowName\": \"\",\n"
                + "  \"windowName\": \"119.3.170.242_1679042795481\"\n"
                + "}");
    }

    @Test
    public void testOnClose() {
        webSocketServer.onClose("webds-plugin", "postgres");
    }

    @Test
    public void testGetStatement() {
        webSocketServer.getStatement("webds-plugin");
    }

    @Test
    public void testSetStatement() {
        webSocketServer.setStatement("webds-plugin", statement);
    }

    @Test
    public void testGetConnection() {
        webSocketServer.getConnection("webds-plugin");
    }

    @Test
    public void testSetConnection() {
        webSocketServer.setConnection("webds-plugin", connection);
    }

    @Test
    public void testGetOperateStatus() {
        webSocketServer.getOperateStatus("webds-plugin");
    }

    @Test
    public void testSetOperateStatus() {
        webSocketServer.setOperateStatus("webds-plugin", operateStatusDO);
    }

    @Test
    public void testGetLanguage() {
        webSocketServer.getLanguage();
    }

    @Test
    public void testSetLanguage() {
        webSocketServer.setLanguage("zh_CN");
    }
}
