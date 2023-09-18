/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.core;

import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
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
    private final List<DataStoreConfig> dataList = new CopyOnWriteArrayList<>();

    @Override
    public void storeData(List<DataStoreConfig> list) {
        dataList.addAll(list);
    }

    @Override
    public List<CollectionItem<?>> getCollectionItem() {
        List<CollectionItem<?>> itemList = new ArrayList<>();
        for (DataStoreConfig config : dataList) {
            itemList.add(config.getCollectionItem());
        }
        return itemList;
    }

    @Override
    public DataStoreConfig getData(CollectionItem<?> item) {
        DataStoreConfig data = new DataStoreConfig();
        for (DataStoreConfig config : dataList) {
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
