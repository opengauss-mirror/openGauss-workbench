/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history;

import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;

import java.util.List;

/**
 * DataStoreService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface DataStoreService {
    void storeData(List<DataStoreConfig> list);

    DataStoreConfig getData(CollectionItem<?> item);

    List<CollectionItem<?>> getCollectionItem();
    void clearData();
}
