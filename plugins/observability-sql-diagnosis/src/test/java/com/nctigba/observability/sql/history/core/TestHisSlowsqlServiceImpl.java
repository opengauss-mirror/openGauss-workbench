/*
 *  Copyright (c) Huawei Technologies Co. 2025-2025.
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
 *  TestHisSlowsqlServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/core/
 *  TestHisSlowsqlServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.core;

import com.nctigba.observability.sql.mapper.DynamicHisSlowSqlMapper;
import com.nctigba.observability.sql.mapper.OpengaussAllSlowsqlMapper;
import com.nctigba.observability.sql.mapper.TimePointMapper;
import com.nctigba.observability.sql.model.dto.SlowSqlDTO;
import com.nctigba.observability.sql.model.entity.TimePointDO;
import com.nctigba.observability.sql.model.query.SlowLogQuery;
import com.nctigba.observability.sql.model.vo.StatementHistoryAggVO;
import com.nctigba.observability.sql.model.vo.StatementHistoryDetailVO;
import com.nctigba.observability.sql.service.MyPage;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.HisSlowsqlServiceImpl;
import com.nctigba.observability.sql.service.impl.TimeConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.jdbc.BadSqlGrammarException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TestHisSlowsqlServiceImpl
 *
 * @author jianghongbo
 * @since 2025-09-15
 */
@ExtendWith(MockitoExtension.class)
public class TestHisSlowsqlServiceImpl {
    @Mock
    private DynamicHisSlowSqlMapper dynamicHisSlowSqlMapper;

    @Mock
    private OpengaussAllSlowsqlMapper opengaussAllSlowsqlMapper;

    @Mock
    private ClusterManager clusterManager;

    @Mock
    private TimePointMapper timePointMapper;

    @Mock
    private TimeConfigServiceImpl timeConfigService;

    @InjectMocks
    private HisSlowsqlServiceImpl hisSlowsqlService;

    private OpsClusterNodeVO testNode;
    private final String testNodeId = "test-node-id";
    private final String testTableName = "tb_slowsqls_test_node_id";

    @BeforeEach
    void setUp() {
        testNode = new OpsClusterNodeVO();
        testNode.setNodeId(testNodeId);
    }

    @Test
    void testCleanExpiredSlowInfo() {
        when(timeConfigService.getPeroid()).thenReturn(30);
        when(dynamicHisSlowSqlMapper.cleanExpiredData(testTableName, 30)).thenReturn(5);

        hisSlowsqlService.cleanExpiredSlowInfo(testNodeId);

        verify(timeConfigService).getPeroid();
        verify(dynamicHisSlowSqlMapper).cleanExpiredData(testTableName, 30);
    }

    @Test
    void testCollectNodeSlowsqls() {
        TimePointDO timePointDO = new TimePointDO();
        timePointDO.setStartTimePoint(new Date(1000L));
        timePointDO.setFinishTimePoint(new Date(2000L));

        when(timePointMapper.selectTimePoint(testTableName)).thenReturn(timePointDO);
        doNothing().when(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        doNothing().when(clusterManager).pool();
        when(opengaussAllSlowsqlMapper.selectSlowSqls(any(Date.class), any(Date.class)))
                .thenReturn(new ArrayList<>());

        hisSlowsqlService.collectNodeSlowsqls(testNodeId);

        verify(timePointMapper).selectTimePoint(testTableName);
        verify(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        verify(opengaussAllSlowsqlMapper).selectSlowSqls(any(Date.class), any(Date.class));
        verify(clusterManager).pool();
        verify(dynamicHisSlowSqlMapper).createTable(testTableName);
    }

    @Test
    void testCollectNodeSlowsqlsWithOldVersionPrimary() {
        TimePointDO timePointDO = new TimePointDO();
        timePointDO.setStartTimePoint(new Date(1000L));
        timePointDO.setFinishTimePoint(new Date(2000L));

        when(timePointMapper.selectTimePoint(testTableName)).thenReturn(timePointDO);
        doNothing().when(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        doNothing().when(clusterManager).pool();
        when(opengaussAllSlowsqlMapper.selectSlowSqls(any(Date.class), any(Date.class)))
                .thenThrow(new BadSqlGrammarException("test", "test", null));
        when(opengaussAllSlowsqlMapper.selectNodeStatus()).thenReturn("primary");
        when(opengaussAllSlowsqlMapper.selectPrimarySlowSqls(anyString()))
                .thenReturn(new ArrayList<>());

        hisSlowsqlService.collectNodeSlowsqls(testNodeId);

        verify(timePointMapper).selectTimePoint(testTableName);
        verify(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        verify(opengaussAllSlowsqlMapper).selectSlowSqls(any(Date.class), any(Date.class));
        verify(opengaussAllSlowsqlMapper).selectNodeStatus();
        verify(opengaussAllSlowsqlMapper).selectPrimarySlowSqls(anyString());
        verify(clusterManager).pool();
    }

    @Test
    void testCollectNodeSlowsqlsWithOldVersionStandby() {
        TimePointDO timePointDO = new TimePointDO();
        timePointDO.setStartTimePoint(new Date(1000L));
        timePointDO.setFinishTimePoint(new Date(2000L));

        when(timePointMapper.selectTimePoint(testTableName)).thenReturn(timePointDO);
        doNothing().when(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        doNothing().when(clusterManager).pool();
        when(opengaussAllSlowsqlMapper.selectSlowSqls(any(Date.class), any(Date.class)))
                .thenThrow(new BadSqlGrammarException("test", "test", null));
        when(opengaussAllSlowsqlMapper.selectNodeStatus()).thenReturn("standby");
        when(opengaussAllSlowsqlMapper.selectStandbySlowSqls(any()))
                .thenReturn(new ArrayList<>());
        hisSlowsqlService.collectNodeSlowsqls(testNodeId);
        verify(timePointMapper).selectTimePoint(testTableName);
        verify(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        verify(opengaussAllSlowsqlMapper).selectSlowSqls(any(Date.class), any(Date.class));
        verify(opengaussAllSlowsqlMapper).selectNodeStatus();
        verify(opengaussAllSlowsqlMapper).selectStandbySlowSqls(any());
        verify(clusterManager).pool();
    }

    @Test
    void testListSlowSQLs() {
        SlowLogQuery query = new SlowLogQuery();
        query.setNodeId(testNodeId);
        query.setPageNum(1);
        query.setPageSize(10);
        query.setQueryCount(true);
        List<StatementHistoryDetailVO> mockList = new ArrayList<>();
        StatementHistoryDetailVO detail = new StatementHistoryDetailVO();
        mockList.add(detail);
        when(dynamicHisSlowSqlMapper.selectCount(anyString(), any())).thenReturn(1L);
        when(dynamicHisSlowSqlMapper.selectAllSlowSql(anyString(), any(SlowSqlDTO.class)))
                .thenReturn(mockList);
        MyPage<StatementHistoryDetailVO> result = hisSlowsqlService.listSlowSQLs(query);
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getCurrent());
        verify(dynamicHisSlowSqlMapper).selectCount(anyString(), any());
        verify(dynamicHisSlowSqlMapper).selectAllSlowSql(anyString(), any(SlowSqlDTO.class));
    }

    @Test
    void testSelectSlowSqlAggData() {
        SlowLogQuery query = new SlowLogQuery();
        query.setNodeId(testNodeId);
        query.setPageNum(1);
        query.setPageSize(10);
        query.setQueryCount(true);
        List<StatementHistoryAggVO> mockList = new ArrayList<>();
        StatementHistoryAggVO agg = new StatementHistoryAggVO();
        agg.setFirstExecuteTime("123456789");
        agg.setFinalExecuteTime("987654321");
        mockList.add(agg);
        when(dynamicHisSlowSqlMapper.selectAggSlowSql(anyString(), any(SlowSqlDTO.class)))
                .thenReturn(mockList);
        MyPage<StatementHistoryAggVO> result = hisSlowsqlService.selectSlowSqlAggData(query);
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getCurrent());
        verify(dynamicHisSlowSqlMapper).selectAggSlowSql(anyString(), any(SlowSqlDTO.class));
    }

    @Test
    void testGetSlowSqlChart() {
        Long start = System.currentTimeMillis() / 1000 - 3600;
        Long end = System.currentTimeMillis() / 1000;
        Integer step = 300;
        String dbName = "testdb";

        when(dynamicHisSlowSqlMapper.selectActiveSlowsqls(anyString(), anyLong(), eq(dbName)))
                .thenReturn(5);
        doNothing().when(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        doNothing().when(clusterManager).pool();
        when(opengaussAllSlowsqlMapper.getSlowsqlThreshold()).thenReturn("1000");
        Map<String, Object> result = hisSlowsqlService.getSlowSqlChart(testNodeId, start, end, step, dbName);
        assertNotNull(result);
        assertTrue(result.containsKey("time"));
        assertTrue(result.containsKey("slowSqlThreshold"));
        assertTrue(result.containsKey("INSTANCE_DB_SLOWSQL"));
        assertEquals("1000", result.get("slowSqlThreshold"));
        verify(dynamicHisSlowSqlMapper, times((int) ((end - start) / step + 1)))
                .selectActiveSlowsqls(anyString(), anyLong(), eq(dbName));
        verify(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        verify(clusterManager).pool();
    }

    @Test
    void testCollectNodeSlowsqlsWithUnknownNodeRole() {
        TimePointDO timePointDO = new TimePointDO();
        timePointDO.setStartTimePoint(new Date(1000L));
        timePointDO.setFinishTimePoint(new Date(2000L));
        when(timePointMapper.selectTimePoint(testTableName)).thenReturn(timePointDO);
        doNothing().when(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        doNothing().when(clusterManager).pool();
        when(opengaussAllSlowsqlMapper.selectSlowSqls(any(Date.class), any(Date.class)))
                .thenThrow(new BadSqlGrammarException("test", "test", null));
        when(opengaussAllSlowsqlMapper.selectNodeStatus()).thenReturn("unknown-role");
        assertThrows(CustomException.class, () -> hisSlowsqlService.collectNodeSlowsqls(testNodeId));
        verify(timePointMapper).selectTimePoint(testTableName);
        verify(clusterManager).setCurrentDatasource(testNodeId, "postgres");
        verify(opengaussAllSlowsqlMapper).selectSlowSqls(any(Date.class), any(Date.class));
        verify(opengaussAllSlowsqlMapper).selectNodeStatus();
        verify(clusterManager).pool();
    }
}
