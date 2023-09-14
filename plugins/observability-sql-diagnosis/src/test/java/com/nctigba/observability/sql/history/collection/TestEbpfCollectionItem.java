/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history.collection;

import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.service.history.collection.ebpf.BioSnoopItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.EbpfCollectionItem;
import com.nctigba.observability.sql.util.EbpfUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TestEbpfCollectionItem
 *
 * @author luomeng
 * @since 2023/8/21
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEbpfCollectionItem {
    @Mock
    private BioSnoopItem item;
    @Mock
    private EbpfUtil util;
    @InjectMocks
    private EbpfCollectionItem collectionItem = new EbpfCollectionItem() {
        @Override
        public Object collectData(HisDiagnosisTask task) {
            return super.collectData(task);
        }

        @Override
        public Object queryData(HisDiagnosisTask task) {
            return super.queryData(task);
        }

        @Override
        public String getHttpParam() {
            return item.getHttpParam();
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
        hisDiagnosisTask = new HisDiagnosisTask();
        String nodeId = "37e8a893-0b7e-49b2-a0b4-e6fdf7dc4345";
        hisDiagnosisTask.setNodeId(nodeId);
        Date sTime = new Date();
        Date eTime = new Date();
        hisDiagnosisTask.setHisDataStartTime(sTime);
        hisDiagnosisTask.setHisDataEndTime(eTime);
        List<OptionQuery> config = new ArrayList<>() {{
            add(optionQuery);
        }};
        hisDiagnosisTask.setConfigs(config);
        List<HisDiagnosisThreshold> threshold = new ArrayList<>() {{
            add(diagnosisThreshold);
        }};
        hisDiagnosisTask.setThresholds(threshold);
        hisDiagnosisTask.setSpan("50s");
    }

    @Test
    public void testCollectData() {
        when(item.getHttpParam()).thenReturn(AgentParamCommon.BIOSNOOP);
        when(util.callMonitor(any(), any())).thenReturn(null);
        Object data = collectionItem.collectData(hisDiagnosisTask);
        assertNull(data);
    }

    @Test
    public void testQueryData() {
        Object data = collectionItem.queryData(hisDiagnosisTask);
        assertNotNull(data);
    }

    @Test
    public void testCollectionType() {
        String data = collectionItem.getCollectionType();
        Assertions.assertEquals(CollectionTypeCommon.MIDDLE, data);
    }
}
