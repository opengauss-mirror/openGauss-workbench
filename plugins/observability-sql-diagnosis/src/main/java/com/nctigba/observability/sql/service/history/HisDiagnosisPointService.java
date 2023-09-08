/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history;

import com.nctigba.observability.sql.constants.history.PointTypeCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;

import java.util.List;

/**
 * HisDiagnosisPointService
 *
 * @author luomeng
 * @since 2023/6/9
 */
public interface HisDiagnosisPointService<T> {
    List<String> getOption();

    List<CollectionItem<?>> getSourceDataKeys();

    AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService);

    T getShowData(int taskId);

    default String getDiagnosisType() {
        return PointTypeCommon.HISTORY;
    }
}
