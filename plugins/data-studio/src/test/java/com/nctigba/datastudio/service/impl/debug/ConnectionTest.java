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
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.context.MessageSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * ConnectionTest
 *
 * @since 2023-07-04
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ConnectionTest {
    @InjectMocks
    private ConnectionImpl connection;

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
    public void testOperate() throws SQLException, IOException {
        String str = "{" + LF
                + "  \"operation\": \"connection\"," + LF
                + "  \"language\": \"zh-CN\"," + LF
                + "  \"webUser\": \"A\"," + LF
                + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f24009ab1\"," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"" + LF
                + "}";
        connection.operate(webSocketServer, str);
        str = "{" + LF
                + "  \"operation\": \"connection\"," + LF
                + "  \"language\": \"zh-CN\"," + LF
                + "  \"webUser\": \"A\"," + LF
                + "  \"uuid\": \"8359cbf1-9833-4998-a29c-245f2009ab1\"," + LF
                + "  \"rootWindowName\": \"postgres\"," + LF
                + "  \"oldWindowName\": \"\"," + LF
                + "  \"windowName\": \"postgres\"" + LF
                + "}";
        try {
            connection.operate(webSocketServer, str);
        } catch (CustomException e) {
            log.info(e.getMessage());
        }
    }
}
