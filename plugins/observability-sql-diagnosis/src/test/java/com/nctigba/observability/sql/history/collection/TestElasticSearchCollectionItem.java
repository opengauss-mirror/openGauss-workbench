/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.nctigba.observability.sql.constants.history.ElasticSearchCommon;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.collection.elastic.DeadlockItem;
import com.nctigba.observability.sql.service.history.collection.elastic.ElasticSearchCollectionItem;
import com.nctigba.observability.sql.util.EsLogSearchUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TestElasticSearchCollectionItem
 *
 * @author luomeng
 * @since 2023/7/12
 */
@RunWith(MockitoJUnitRunner.class)
public class TestElasticSearchCollectionItem {
    @Mock
    private EsLogSearchUtils dbUtil;
    @Mock
    private DeadlockItem item;
    @InjectMocks
    private ElasticSearchCollectionItem collectionItem = new ElasticSearchCollectionItem() {
        @Override
        public Object collectData(HisDiagnosisTask task) {
            return super.collectData(task);
        }

        @Override
        public Object queryData(HisDiagnosisTask task) {
            return super.queryData(task);
        }

        @Override
        public String getQueryParam() {
            return item.getQueryParam();
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
        when(item.getQueryParam()).thenReturn(ElasticSearchCommon.DEADLOCK);
        Object data = collectionItem.collectData(hisDiagnosisTask);
        assertNull(data);
    }

    @Test
    public void testCollectData_noEndDate() {
        when(item.getQueryParam()).thenReturn(ElasticSearchCommon.DEADLOCK);
        hisDiagnosisTask.setHisDataEndTime(null);
        Object data = collectionItem.collectData(hisDiagnosisTask);
        assertNull(data);
    }


    @Test
    public void testQueryData() {
        when(item.getQueryParam()).thenReturn(ElasticSearchCommon.LOCK_TIMEOUT);
        hisDiagnosisTask.setHisDataEndTime(null);
        Set<String> indexs = new HashSet<>();
        indexs.add("ob-opengauss-error-log-37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345");
        when(dbUtil.indexList(any())).thenReturn(Optional.of(indexs));
        SearchResponse<HashMap> searchResponse = mock(SearchResponse.class);
        HitsMetadata<HashMap> hitsMetadata = mock(HitsMetadata.class);
        Hit<HashMap> hits = mock(Hit.class);
        List<Hit<HashMap>> hitList = new ArrayList<>();
        hitList.add(hits);
        when(searchResponse.hits()).thenReturn(hitsMetadata);
        when(hitsMetadata.hits()).thenReturn(hitList);
        when(hits.source()).thenReturn(mock(HashMap.class));
        when(dbUtil.queryLogInfo(any())).thenReturn(Optional.ofNullable(searchResponse));
        Object data = collectionItem.queryData(hisDiagnosisTask);
        assertNotNull(data);
    }
}
