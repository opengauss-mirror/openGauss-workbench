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
 *  DataStoreServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/core/DataStoreServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.core;

import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * DataStoreServiceImpl
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class DataStoreServiceImpl implements DataStoreService {
    private final List<DataStoreVO> dataList = new CopyOnWriteArrayList<>();

    @Override
    public void storeData(List<DataStoreVO> list) {
        dataList.addAll(list);
    }

    @Override
    public List<CollectionItem<?>> getCollectionItem() {
        List<CollectionItem<?>> itemList = new ArrayList<>();
        for (DataStoreVO config : dataList) {
            itemList.add(config.getCollectionItem());
        }
        return itemList;
    }

    @Override
    public DataStoreVO getData(CollectionItem<?> item) {
        DataStoreVO data = new DataStoreVO();
        for (DataStoreVO config : dataList) {
            if (config.getCollectionItem() == item) {
                data = config;
                config.setCount(config.getCount() - 1);
            }
        }
        dataList.removeIf(dataStoreConfig -> dataStoreConfig.getCount() == 0);
        return data;
    }

    @Override
    public void clearData() {
        dataList.clear();
    }
}
