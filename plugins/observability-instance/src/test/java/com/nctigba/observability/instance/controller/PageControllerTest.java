/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.constants.MetricsValue;
import com.nctigba.observability.instance.mapper.DbConfigMapper;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.util.Language;

/**
 * PageControllerTest.java
 *
 * @since 2023年7月17日
 */
@ExtendWith(MockitoExtension.class)
class PageControllerTest {
    @InjectMocks
    private PageController controller;
    @Mock
    private MetricsService metricsService;
    @Mock
    private DbConfigMapper dbConfigMapper;
    @Mock
    private ClusterManager clusterManager;
    @Mock
    private MessageSource messageSource;
    @Mock
    private Language language;

    @BeforeEach
    void setUp() throws Exception {
        HashMap<String, Object> result = new HashMap<>();
        result.put("IO_TPS", Map.of("device", "test"));
        result.put("NETWORK_TX", Map.of("device", "test"));
        result.put(MetricsValue.MEM_TOTAL.name(), "1");
        result.put(MetricsLine.MEMORY_DB_USED.name(), List.of(1, 2, 3));
        when(metricsService.listBatch(any(), any(), any(), any(), any())).thenReturn(result);
        when(messageSource.getMessage(any(), any(), any(), any())).thenReturn("any");
        var config = new HashMap<String, Object>();
        config.put("memorytype", "test");
        config.put("name", "test");
        when(dbConfigMapper.memoryConfig()).thenReturn(List.of(config));
        when(dbConfigMapper.memoryNodeDetail()).thenReturn(List.of(config));
    }

    @Test
    void test() {
        controller.memory("id", null, null, null);
        controller.io("id", null, null, null);
        controller.network("id", null, null, null);
        controller.instance("id", null, null, null);
        controller.waitEvent("id", null, null, null);
    }
}