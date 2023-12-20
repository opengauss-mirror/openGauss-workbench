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
 *  TestPrometheusUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/util/TestPrometheusUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.sql.constant.MetricConstants;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.dto.point.PrometheusDataDTO;
import com.nctigba.observability.sql.model.entity.NctigbaEnvDO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.vo.point.MetricToTableVO;
import com.nctigba.observability.sql.util.PrometheusUtils;
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
public class TestPrometheusUtils {
    @Mock
    private NctigbaEnvMapper envMapper;

    @Mock
    private HostFacade hostFacade;
    @InjectMocks
    private PrometheusUtils util;

    private List<PrometheusVO> list;

    @Before
    public void before() {
        list = new ArrayList<>();
        PrometheusVO prometheusVO = new PrometheusVO();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wait_status", "");
        jsonObject.put("warning_msg", "");
        prometheusVO.setMetric(jsonObject);
        JSONArray objects = new JSONArray();
        for (int i = 0; i <= 10; i++) {
            JSONArray array = new JSONArray();
            array.add("100000");
            array.add("100001");
            objects.add(array);
        }
        prometheusVO.setValues(objects);
        list.add(prometheusVO);
    }

    @Test
    public void testRangQuery() {
        NctigbaEnvDO env = new NctigbaEnvDO();
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
            String item = MetricConstants.AVG_CPU_USAGE_RATE;
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
        List<MetricToTableVO> result = util.metricToTable(list);
        assertNotNull(result);
    }

    @Test
    public void testMetricToLine() {
        PrometheusDataDTO result = util.metricToLine(list);
        assertNotNull(result);
    }
}
