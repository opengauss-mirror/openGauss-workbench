/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import cn.hutool.core.io.FileUtil;

/**
 * ParamInfoInitConfigTest
 *
 * @since 2023年7月17日
 */
class ParamInfoInitConfigTest {
    @Test
    void testGetCon() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        deleteFile();
        try (MockedStatic<FileUtil> mockedStatic = mockStatic(FileUtil.class)) {
            File fileMock = mock(File.class);
            when(fileMock.exists()).thenReturn(false);
            when(fileMock.getParentFile()).thenReturn(fileMock);
            mockedStatic.when(() -> FileUtil.file(anyString())).thenReturn(fileMock);
            ParamInfoInitConfig.getCon(ParamInfoInitConfig.PARAMINFO);

            deleteFile();
            clearCache();
            ParamInfoInitConfig.getCon(ParamInfoInitConfig.PARAMINFO);

            when(fileMock.exists()).thenReturn(true);
            deleteFile();
            clearCache();
            ParamInfoInitConfig.getCon(ParamInfoInitConfig.PARAMINFO);
        }
    }

    private static void deleteFile() {
        File info = new File("data" + File.separatorChar + "paramInfo.db");
        if (info.exists()) {
            info.delete();
        }
        File valueInfo = new File("data" + File.separatorChar + "paramValueInfo.db");
        if (valueInfo.exists()) {
            valueInfo.delete();
        }
        File nul = new File("null");
        if (nul.exists()) {
            nul.delete();
        }
    }

    private static void clearCache()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Class<ParamInfoInitConfig> clazz = ParamInfoInitConfig.class;
        Field field = clazz.getDeclaredField("map");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var map = (Map<String, Connection>) field.get(null);
        map.clear();
    }
}