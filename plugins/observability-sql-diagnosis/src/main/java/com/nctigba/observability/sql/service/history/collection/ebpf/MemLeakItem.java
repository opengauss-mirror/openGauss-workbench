/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.ebpf;

import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import org.springframework.stereotype.Service;

/**
 * MemLeakItem
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class MemLeakItem extends EbpfCollectionItem {
    @Override
    public String getHttpParam() {
        return AgentParamCommon.MEMLEAK;
    }

    public String getCollectionType() {
        return CollectionTypeCommon.AFTER;
    }
}
