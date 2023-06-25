/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history;

import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.query.HisThresholdQuery;

import java.util.HashMap;
import java.util.List;

/**
 * HisThresholdService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface HisThresholdService {
    List<HisDiagnosisThreshold> select();
    void insertOrUpdate(HisThresholdQuery hisThresholdQuery);
    void delete(int id);
    HashMap<String, String> getThresholdValue(List<String> list);
}
