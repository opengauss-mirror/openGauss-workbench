/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.constants.history.MetricCommon;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.dto.MetricToTableDTO;
import com.nctigba.observability.sql.model.history.point.PrometheusDataDTO;
import com.nctigba.observability.sql.util.PrometheusUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * TestPrometheusUtil
 *
 * @author luomeng
 * @since 2023/7/13
 */
@RunWith(MockitoJUnitRunner.class)
public class TestPrometheusUtil {
    @Mock
    private NctigbaEnvMapper envMapper;

    @Mock
    private HostFacade hostFacade;
    @InjectMocks
    private PrometheusUtil util;

    private List<PrometheusData> list;

    @Before
    public void before() {
        list = new ArrayList<>();
        PrometheusData prometheusData = new PrometheusData();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wait_status", "");
        jsonObject.put("warning_msg", "");
        prometheusData.setMetric(jsonObject);
        JSONArray objects = new JSONArray();
        for (int i = 0; i <= 10; i++) {
            JSONArray array = new JSONArray();
            array.add("100000");
            array.add("100001");
            objects.add(array);
        }
        prometheusData.setValues(objects);
        list.add(prometheusData);
    }

    @Test
    public void testRangQuery() {
        NctigbaEnv env = new NctigbaEnv();
        env.setPort(11);
        env.setHostid("8080");
        when(envMapper.selectOne(any())).thenReturn(env);
        OpsHostEntity entity = new OpsHostEntity();
        entity.setPublicIp("127.0.0.1");
        when(hostFacade.getById(env.getHostid())).thenReturn(entity);
        try (MockedStatic<UriComponentsBuilder> mockStatic = mockStatic(UriComponentsBuilder.class)) {
            UriComponentsBuilder builder = spy(UriComponentsBuilder.class);
            mockStatic.when(() -> UriComponentsBuilder.fromHttpUrl(anyString()))
                    .thenReturn(builder);
            UriComponents uriComponents = mock(UriComponents.class);
            when(builder.build()).thenReturn(uriComponents);
            when(uriComponents.encode()).thenReturn(uriComponents);
            when(uriComponents.toUriString()).thenReturn("http://127.0.0.1:8080");
            String paramId = "";
            String item = MetricCommon.AVG_CPU_USAGE_RATE;
            String start = "";
            String end = "";
            String step = "";
            Object result = util.rangeQuery(paramId, item, start, end, step);
            assertNotNull(result);
        }
    }

    @Test
    public void testTransToTimeList() {
        Map<String, List<Object>> result = util.transToTimeList(list);
        assertNotNull(result);
    }

    @Test
    public void testMetricToTable() {
        List<MetricToTableDTO> result = util.metricToTable(list);
        assertNotNull(result);
    }

    @Test
    public void testMetricToLine() {
        PrometheusDataDTO result = util.metricToLine(list);
        assertNotNull(result);
    }
}
