/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.mapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nctigba.observability.instance.config.ParamInfoInitConfig;
import com.nctigba.observability.instance.entity.ParamInfo.ParamType;

/**
 * ParamInfoMapperTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class ParamInfoMapperTest {
    @InjectMocks
    private ParamInfoMapper paramInfoMapper;
    @Mock
    private Connection connection;
    @Mock
    private Statement statement;
    @Mock
    private ResultSet resultSet;

    @Test
    void test() throws Exception {
        try (MockedStatic<ParamInfoInitConfig> mockedStatic = mockStatic(ParamInfoInitConfig.class)) {
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getInt(anyString())).thenReturn(1, 2, 3);
            when(resultSet.getString(anyString())).thenReturn("OS");
            when(statement.executeQuery(anyString())).thenReturn(resultSet);
            when(connection.createStatement()).thenReturn(statement);
            mockedStatic.when(() -> {
                ParamInfoInitConfig.getCon(anyString());
            }).thenReturn(connection);
            paramInfoMapper.afterPropertiesSet();
            ParamInfoMapper.getAll();
            ParamInfoMapper.getById(1);
            ParamInfoMapper.getIds(ParamType.OS);
            ParamInfoMapper.getParamInfo(ParamType.OS, "OS");
            reset(connection);
            when(connection.createStatement()).thenThrow(SQLException.class);
            assertThrows(RuntimeException.class, () -> paramInfoMapper.afterPropertiesSet());
        }
    }
}