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
 *  TestDataStoreServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/test/java/com/nctigba/observability/sql/history/core/TestDataStoreServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.history.core;

import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.metric.DbAvgCpuItem;
import com.nctigba.observability.sql.service.impl.core.DataStoreServiceImpl;
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
    private final List<DataStoreVO> dataList = new ArrayList<>();
    @InjectMocks
    private DataStoreServiceImpl dataStoreService;

    @Test
    public void testStoreData() {
        DataStoreVO config = new DataStoreVO();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        dataList.add(config);
        dataStoreService.storeData(dataList);
    }

    @Test
    public void testGetCollectionItem() {
        DataStoreVO config = new DataStoreVO();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        dataList.add(config);
        dataStoreService.storeData(dataList);
        List<CollectionItem<?>> list = dataStoreService.getCollectionItem();
        assertNotNull(list);
    }

    @Test
    public void testGetData_NotData() {
        DataStoreVO config = new DataStoreVO();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        dataList.add(config);
        dataStoreService.storeData(dataList);
        DataStoreVO data = dataStoreService.getData(null);
        assertNotNull(data);
    }

    @Test
    public void testGetData_hasData() {
        DataStoreVO config = new DataStoreVO();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(1);
        dataList.add(config);
        dataStoreService.storeData(dataList);
        DataStoreVO data = dataStoreService.getData(dbAvgCpuItem);
        assertNotNull(data);
    }

    @Test
    public void testGetData_hasData2() {
        DataStoreVO config = new DataStoreVO();
        config.setCollectionItem(dbAvgCpuItem);
        config.setCount(2);
        dataList.add(config);
        dataStoreService.storeData(dataList);
        DataStoreVO data = dataStoreService.getData(dbAvgCpuItem);
        assertNotNull(data);
    }

    @Test
    public void testClearData() {
        dataStoreService.clearData();
    }
}
