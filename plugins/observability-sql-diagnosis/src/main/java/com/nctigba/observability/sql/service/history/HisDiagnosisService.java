/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history;

import com.nctigba.observability.sql.model.history.result.HisTreeNode;

/**
 * HisDiagnosisService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface HisDiagnosisService {
    HisTreeNode getTopologyMap(int taskId, boolean isAll);

    Object getNodeDetail(int taskId, String pointName);

    Object getAllPoint(int taskId);
}
