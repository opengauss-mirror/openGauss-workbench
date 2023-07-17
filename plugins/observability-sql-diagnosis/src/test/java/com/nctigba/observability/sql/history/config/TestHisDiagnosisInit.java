/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.config;

import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.nctigba.observability.sql.config.history.HisDiagnosisInit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

/**
 * TestHisDiagnosisInit
 *
 * @author luomeng
 * @since 2023/7/12
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHisDiagnosisInit {
    @InjectMocks
    private HisDiagnosisInit init;

    @Test
    public void testInit() throws IOException {
        init.init();
    }

    @Test
    public void testInitSqlite() {
        DynamicDataSourceProvider dataSourceProvider = init.sqliteDataSourceInit();
        dataSourceProvider.loadDataSources();
    }
}
