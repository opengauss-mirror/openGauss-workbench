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
 *  CoverageRateServiceTest.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/test/java/com/nctigba/datastudio/service/impl/debug/CoverageRateServiceTest.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.debug;

import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.CoverageRateQuery;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.nctigba.datastudio.constants.CommonConstants.CAN_BREAK_LINE;
import static com.nctigba.datastudio.constants.CommonConstants.CID;
import static com.nctigba.datastudio.constants.CommonConstants.COVERAGE_LINES;
import static com.nctigba.datastudio.constants.CommonConstants.END_TIME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PARAMS;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.REMARK_LINES;
import static com.nctigba.datastudio.constants.CommonConstants.SOURCECODE;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * CoverageRateTest
 *
 * @since 2023-07-12
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class CoverageRateServiceTest {
    @InjectMocks
    private CoverageRateServiceImpl coverageRateService;

    @Mock
    private Connection mockConnection;

    @Mock
    private Statement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private ConnectionConfig connectionConfig;

    @Mock
    private MockHttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    @Mock
    private MessageSource messageSource;

    @Spy
    private LocaleStringUtils localeStringUtils;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @Before
    public void setUp() throws SQLException {
        localeStringUtils.setMessageSource(messageSource);

        when(connectionConfig.connectDatabase(anyString())).thenReturn(mockConnection);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString(PRO_NAME)).thenReturn("test");
        when(mockResultSet.getLong(OID)).thenReturn(36466L);
        when(mockResultSet.getLong(CID)).thenReturn(1687663170027L);
        when(mockResultSet.getString(COVERAGE_LINES)).thenReturn("10,11,12,14,18,20");
        when(mockResultSet.getString(REMARK_LINES)).thenReturn("10,11,12,14,16,18,20");
        when(mockResultSet.getLong(END_TIME)).thenReturn(1687663170027L);
        when(mockResultSet.getString(SOURCECODE)).thenReturn(
                "CREATE OR REPLACE FUNCTION scott.step_in(i integer, OUT result integer)" + LF
                        + " RETURNS SETOF integer" + LF
                        + " LANGUAGE plpgsql" + LF
                        + " NOT FENCED NOT SHIPPABLE" + LF
                        + "AS $$" + LF
                        + "DECLARE" + LF
                        + "" + LF
                        + "" + LF
                        + "BEGIN" + LF
                        + "  result = i + 1;" + LF
                        + "  result = result + 2;" + LF
                        + "  if result < 10" + LF
                        + "  then" + LF
                        + "    result = test(result);" + LF
                        + "  else" + LF
                        + "    result = result + 3;" + LF
                        + "  end if;" + LF
                        + "  result = result + 4;" + LF
                        + "" + LF
                        + "RETURN NEXT;" + LF
                        + "END;$$;" + LF
                        + "/");
        when(mockResultSet.getString(PARAMS)).thenReturn("[5]");
        when(mockResultSet.getString(CAN_BREAK_LINE)).thenReturn("10,11,12,14,16,18,20");
    }

    @Test
    public void testQueryCoverageRate() throws SQLException {
        CoverageRateQuery request = new CoverageRateQuery();
        request.setOid(201839L);
        request.setUuid("8359cbf1-9833-4998-a29c-245f24009ab1");

        coverageRateService.queryCoverageRate(request);
    }

    @Test
    public void testDelete() throws SQLException {
        List<Long> list = new ArrayList<>();
        CoverageRateQuery request = new CoverageRateQuery();
        request.setOid(201839L);
        request.setCidList(list);
        request.setUuid("8359cbf1-9833-4998-a29c-245f24009ab1");

        coverageRateService.delete(request);

        list.add(1687663170027L);
        list.add(1687663176632L);
        request.setCidList(list);

        coverageRateService.delete(request);
    }

    @Test
    public void testExport() throws SQLException, IOException, NoSuchFieldException, IllegalAccessException {
        List<Long> list = new ArrayList<>();
        list.add(1687663170027L);

        CoverageRateQuery request = new CoverageRateQuery();
        request.setOid(201839L);
        request.setCidList(list);
        request.setUuid("8359cbf1-9833-4998-a29c-245f24009ab1");

        MockedStatic<LocaleStringUtils> mockStatic = Mockito.mockStatic(LocaleStringUtils.class);
        mockStatic.when(() -> LocaleStringUtils.transLanguage(anyString())).thenReturn("123");
        when(response.getOutputStream()).thenReturn(outputStream);
        when(response.getOutputStream()).thenReturn(outputStream);

        coverageRateService.export(request, response);
        mockStatic.close();
    }

    @Test
    public void testExport2() throws SQLException, IOException, NoSuchFieldException, IllegalAccessException {
        List<Long> list = new ArrayList<>();
        list.add(1687663170020L);

        CoverageRateQuery request = new CoverageRateQuery();
        request.setOid(201839L);
        request.setCidList(list);
        request.setUuid("8359cbf1-9833-4998-a29c-245f24009ab1");

        MockedStatic<LocaleStringUtils> mockStatic = Mockito.mockStatic(LocaleStringUtils.class);
        mockStatic.when(() -> LocaleStringUtils.transLanguage(anyString())).thenReturn("123");
        when(response.getOutputStream()).thenReturn(outputStream);

        coverageRateService.export(request, response);
        mockStatic.close();
    }
}
