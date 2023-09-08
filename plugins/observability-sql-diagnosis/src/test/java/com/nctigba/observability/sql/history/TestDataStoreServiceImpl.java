/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.history;

import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.service.history.impl.DataStoreServiceImpl;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.metric.DbAvgCpuItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TestDataStoreServiceImpl
 *
 * @author luomeng
 * @since 2023/7/5
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDataStoreServiceImpl {
    @Mock
    private DbAvgCpuItem dbAvgCpuItem;
    private final List<DataStoreConfig> dataList = new ArrayList<>();
    @InjectMocks
    private DataStoreServiceImpl dataStoreService;

    @Test
    public void testStoreData() {
        DataStoreConfig config = new DataStoreConfig();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        dataList.add(config);
        dataStoreService.storeData(dataList);
    }

    @Test
    public void testGetCollectionItem() {
        DataStoreConfig config = new DataStoreConfig();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        dataList.add(config);
        dataStoreService.storeData(dataList);
        List<CollectionItem<?>> list = dataStoreService.getCollectionItem();
        assertNotNull(list);
    }

    @Test
    public void testGetData_NotData() {
        DataStoreConfig config = new DataStoreConfig();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        dataList.add(config);
        dataStoreService.storeData(dataList);
        DataStoreConfig data = dataStoreService.getData(null);
        assertNotNull(data);
    }

    @Test
    public void testGetData_hasData() {
        DataStoreConfig config = new DataStoreConfig();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        dataList.add(config);
        dataStoreService.storeData(dataList);
        DataStoreConfig data = dataStoreService.getData(dbAvgCpuItem);
        assertNotNull(data);
    }

    @Test
    public void testGetData_hasData2() {
        DataStoreConfig config = new DataStoreConfig();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(2);
        dataList.add(config);
        dataStoreService.storeData(dataList);
        DataStoreConfig data = dataStoreService.getData(dbAvgCpuItem);
        assertNotNull(data);
    }

    @Test
    public void testClearData() {
        dataStoreService.clearData();
    }
}
