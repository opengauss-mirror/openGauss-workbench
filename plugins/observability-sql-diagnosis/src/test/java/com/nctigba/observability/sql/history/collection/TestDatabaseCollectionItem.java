/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.collection.table.DatabaseCollectionItem;
import com.nctigba.observability.sql.service.history.collection.table.PoorSqlItem;
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
    @InjectMocks
    private DatabaseCollectionItem collectionItem = new DatabaseCollectionItem() {
        @Override
        public Object collectData(HisDiagnosisTask task) {
            return super.collectData(task);
        }

        @Override
        public Object queryData(HisDiagnosisTask task) {
            return super.queryData(task);
        }

        @Override
        public String getDatabaseSql() {
            return sqlItem.getDatabaseSql();
        }
    };

    private HisDiagnosisTask hisDiagnosisTask;

    @Before
    public void before() {
        OptionQuery optionQuery = new OptionQuery();
        optionQuery.setOption(String.valueOf(OptionCommon.IS_CPU));
        optionQuery.setIsCheck(true);
        HisDiagnosisThreshold diagnosisThreshold = new HisDiagnosisThreshold();
        diagnosisThreshold.setThreshold(ThresholdCommon.DURING);
        diagnosisThreshold.setThresholdValue("20");
        List<HisDiagnosisThreshold> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        hisDiagnosisTask = new HisDiagnosisTask();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        Date sTime = new Date();
        Date eTime = new Date();
        hisDiagnosisTask.setNodeId(nodeId);
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(eTime);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        hisDiagnosisTask.setConfigs(config);
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
    }

    @Test
    public void testCollectData() {
        when(sqlItem.getDatabaseSql()).thenReturn(SqlCommon.POOR_SQL);
        hisDiagnosisTask.setHisDataEndTime(null);
        Object data = collectionItem.collectData(hisDiagnosisTask);
        assertNull(data);
    }

    @Test
    public void testCollectData_during() {
        when(sqlItem.getDatabaseSql()).thenReturn(SqlCommon.SLOW_SQL);
        Object data = collectionItem.collectData(hisDiagnosisTask);
        assertNull(data);
    }

    @Test
    public void testQueryData() {
        when(sqlItem.getDatabaseSql()).thenReturn(SqlCommon.POOR_SQL);
        hisDiagnosisTask.setHisDataEndTime(null);
        Object data = collectionItem.queryData(hisDiagnosisTask);
        assertNull(data);
    }

    @Test
    public void testQueryData_during() {
        when(sqlItem.getDatabaseSql()).thenReturn(SqlCommon.SLOW_SQL);
        Object data = collectionItem.queryData(hisDiagnosisTask);
        assertNull(data);
    }
}
