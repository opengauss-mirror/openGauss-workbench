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
 *  TestDatabaseCollectionItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/collection/TestDatabaseCollectionItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.service.impl.collection.table.DatabaseCollectionItem;
import com.nctigba.observability.sql.service.impl.collection.table.PoorSqlItem;
import com.nctigba.observability.sql.util.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * TestDatabaseCollectionItem
 *
 * @author luomeng
 * @since 2023/7/12
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDatabaseCollectionItem {
    @Mock
    private PoorSqlItem sqlItem;
    @Mock
    private DbUtils dbUtils;
    @InjectMocks
    private DatabaseCollectionItem collectionItem = new DatabaseCollectionItem() {
        @Override
        public Object collectData(DiagnosisTaskDO task) {
            return super.collectData(task);
        }

        @Override
        public Object queryData(DiagnosisTaskDO task) {
            return super.queryData(task);
        }

        @Override
        public String getDatabaseSql() {
            return sqlItem.getDatabaseSql();
        }
    };

    private DiagnosisTaskDO diagnosisTaskDO;

    @Before
    public void before() {
        OptionVO optionVO = new OptionVO();
        optionVO.setOption(String.valueOf(OptionEnum.IS_CPU));
        optionVO.setIsCheck(true);
        DiagnosisThresholdDO diagnosisThreshold = new DiagnosisThresholdDO();
        diagnosisThreshold.setThreshold(ThresholdConstants.DURING);
        diagnosisThreshold.setThresholdValue("20");
        diagnosisTaskDO = new DiagnosisTaskDO();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        diagnosisTaskDO.setNodeId(nodeId);
        diagnosisTaskDO.setHisDataStartTime(sTime);
        diagnosisTaskDO.setHisDataEndTime(eTime);
        List<OptionVO> config = new ArrayList<>() {{
            add(optionVO);
        }};
        diagnosisTaskDO.setConfigs(config);
        List<DiagnosisThresholdDO> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        diagnosisTaskDO.setThresholds(threshold);
        diagnosisTaskDO.setSpan("50s");
    }

    @Test
    public void testCollectData() {
        when(sqlItem.getDatabaseSql()).thenReturn(SqlConstants.POOR_SQL);
        diagnosisTaskDO.setHisDataEndTime(null);
        Object data = collectionItem.collectData(diagnosisTaskDO);
        assertNull(data);
    }

    @Test
    public void testCollectData_during() {
        when(sqlItem.getDatabaseSql()).thenReturn(SqlConstants.SLOW_SQL);
        Object data = collectionItem.collectData(diagnosisTaskDO);
        assertNull(data);
    }

    @Test
    public void testQueryData() {
        when(sqlItem.getDatabaseSql()).thenReturn(SqlConstants.POOR_SQL);
        diagnosisTaskDO.setHisDataEndTime(null);
        Object data = collectionItem.queryData(diagnosisTaskDO);
        assertNull(data);
    }

    @Test
    public void testQueryData_during() {
        when(sqlItem.getDatabaseSql()).thenReturn(SqlConstants.SLOW_SQL);
        Object data = collectionItem.queryData(diagnosisTaskDO);
        assertNull(data);
    }
}
