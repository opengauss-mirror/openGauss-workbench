/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.elastic;

import com.nctigba.observability.sql.constants.history.ElasticSearchCommon;
import org.springframework.stereotype.Service;

/**
 * LockTimeoutItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class LockTimeoutItem extends ElasticSearchCollectionItem {
    @Override
    String getQueryParam() {
        return ElasticSearchCommon.LOCK_TIMEOUT;
    }
}
