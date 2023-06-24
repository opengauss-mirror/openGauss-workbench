/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.agent;

import com.nctigba.observability.sql.constants.history.AgentParamCommon;
import org.springframework.stereotype.Service;

/**
 * OtherProcessCurrentCpuItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class OtherProcessCurrentCpuItem extends AgentCollectionItem {
    @Override
    String getHttpParam() {
        return AgentParamCommon.TOP;
    }
}
