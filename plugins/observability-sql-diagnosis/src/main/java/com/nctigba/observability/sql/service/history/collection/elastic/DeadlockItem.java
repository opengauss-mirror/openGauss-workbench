/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.elastic;

import com.nctigba.observability.sql.constants.history.ElasticSearchCommon;
import org.springframework.stereotype.Service;

/**
 * DeadlockItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class DeadlockItem extends ElasticSearchCollectionItem {
    @Override
    String getQueryParam() {
        return ElasticSearchCommon.DEADLOCK;
    }
}