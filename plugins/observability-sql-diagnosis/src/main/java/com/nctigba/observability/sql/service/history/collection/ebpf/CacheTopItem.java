/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.ebpf;

import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import org.springframework.stereotype.Service;

/**
 * CacheTopItem
 *
 * @author luomeng
 * @since 2023/8/2
 */
@Service
public class CacheTopItem extends EbpfCollectionItem {
    @Override
    public String getHttpParam() {
        return AgentParamCommon.CACHETOP;
    }

    public String getCollectionType() {
        return CollectionTypeCommon.AFTER;
    }
}
