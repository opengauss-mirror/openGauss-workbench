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
 *  TestElasticSearchCollectionItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/collection/TestElasticSearchCollectionItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.collection;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.nctigba.observability.sql.constant.ElasticSearchConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.service.impl.collection.elastic.DeadlockItem;
import com.nctigba.observability.sql.service.impl.collection.elastic.ElasticSearchCollectionItem;
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
        public Object collectData(DiagnosisTaskDO task) {
            return super.collectData(task);
        }

        @Override
        public Object queryData(DiagnosisTaskDO task) {
            return super.queryData(task);
        }

        @Override
        public String getQueryParam() {
            return item.getQueryParam();
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
        when(item.getQueryParam()).thenReturn(ElasticSearchConstants.DEADLOCK);
        Object data = collectionItem.collectData(diagnosisTaskDO);
        assertNull(data);
    }

    @Test
    public void testCollectData_noEndDate() {
        when(item.getQueryParam()).thenReturn(ElasticSearchConstants.DEADLOCK);
        diagnosisTaskDO.setHisDataEndTime(null);
        Object data = collectionItem.collectData(diagnosisTaskDO);
        assertNull(data);
    }


    @Test
    public void testQueryData() {
        when(item.getQueryParam()).thenReturn(ElasticSearchConstants.LOCK_TIMEOUT);
        diagnosisTaskDO.setHisDataEndTime(null);
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
        Object data = collectionItem.queryData(diagnosisTaskDO);
        assertNotNull(data);
    }
}
