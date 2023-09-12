/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.dao;

import com.nctigba.datastudio.model.dto.WinInfoDTO;
import com.nctigba.datastudio.service.impl.sql.TableDataServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;

import static com.nctigba.datastudio.dao.ResultSetMapDAO.winMap;

/**
 * ResultSetMapDAOTest
 *
 * @since 2023-08-30
 */
@RunWith(MockitoJUnitRunner.class)
public class ResultSetMapDAOTest {
    @InjectMocks
    private ResultSetMapDAO resultSetMapDAO;
    @Mock
    private TableDataServiceImpl tableDataService;

    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    @Before
    public void setUp() throws SQLException {
        winMap = new HashMap<>();
        WinInfoDTO winInfoDTO = new WinInfoDTO();
        winInfoDTO.setSchema("openGauss");
        winInfoDTO.setLastDate(new Date());
        winMap.put("uuid", winInfoDTO);
    }

    @Test
    public void testOvertime() throws SQLException {
        resultSetMapDAO.overtime();
    }

    @Test
    public void testOvertimeCloseWin() throws SQLException {
        resultSetMapDAO.overtimeCloseWin("uuid");
    }
}
