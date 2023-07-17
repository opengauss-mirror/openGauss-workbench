/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.agent;

import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import org.springframework.stereotype.Service;

/**
 * CurrentCpuUsageItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class CurrentCpuUsageItem extends AgentCollectionItem {
    @Override
    public String getHttpParam() {
        return AgentParamCommon.TOP;
    }
}
