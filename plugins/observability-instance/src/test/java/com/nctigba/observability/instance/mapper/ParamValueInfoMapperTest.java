/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.mapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nctigba.observability.instance.config.ParamInfoInitConfig;
import com.nctigba.observability.instance.entity.ParamValueInfo;

/**
 * ParamValueInfoMapperTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class ParamValueInfoMapperTest {
    @InjectMocks
    private ParamValueInfoMapper paramValueInfoMapper;
    @Mock
    private Connection connection;
    @Mock
    private Statement statement;
    @Mock
    private PreparedStatement pstatement;
    @Mock
    private ResultSet resultSet;

    @Test
    void test() throws SQLException {
        try (MockedStatic<ParamInfoInitConfig> mockedStatic = mockStatic(ParamInfoInitConfig.class)) {
            when(resultSet.next()).thenReturn(true, true, false);
            when(resultSet.getInt(anyInt())).thenReturn(1, 2, 3);
            when(resultSet.getString(anyInt())).thenReturn("OS");
            when(pstatement.executeQuery()).thenReturn(resultSet);
            when(connection.createStatement()).thenReturn(statement);
            when(connection.prepareStatement(anyString())).thenReturn(pstatement);
            mockedStatic.when(() -> ParamInfoInitConfig.getCon(anyString())).thenReturn(connection);

            paramValueInfoMapper.query("");
            paramValueInfoMapper.refresh(null);
            ParamValueInfoMapper.insert(new ParamValueInfo(1, "", ""));
            ParamValueInfoMapper.insertBatch(List.of(new ParamValueInfo(1, "", ""), new ParamValueInfo(1, "", "")));
            ParamValueInfoMapper.delBySids(List.of(1, 2, 3));
            ParamValueInfoMapper.delBySids(List.of());
            ParamValueInfoMapper.selectByInstanceId("");
            reset(connection);
            when(connection.createStatement()).thenThrow(SQLException.class);
            when(connection.prepareStatement(anyString())).thenThrow(SQLException.class);
            assertThrows(RuntimeException.class, () -> ParamValueInfoMapper.selectByInstanceId(""));
            assertThrows(RuntimeException.class, () -> ParamValueInfoMapper.insert(new ParamValueInfo(1, "", "")));
            assertThrows(RuntimeException.class, () -> ParamValueInfoMapper
                    .insertBatch(List.of(new ParamValueInfo(1, "", ""), new ParamValueInfo(1, "", ""))));
            assertThrows(RuntimeException.class, () -> ParamValueInfoMapper.delBySids(List.of(1, 2, 3)));
        }
    }
}