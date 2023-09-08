/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.ebpf;

import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import org.springframework.stereotype.Service;

/**
 * OsParamItem
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class OsParamItem extends EbpfCollectionItem {
    @Override
    public String getHttpParam() {
        return AgentParamCommon.OSPARAM;
    }

    public String getCollectionType() {
        return CollectionTypeCommon.AFTER;
    }
}