/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * LockAnalysis
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class LockAnalysis implements HisDiagnosisPointService<String> {
    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionCommon.IS_LOCK));
        return option;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        return null;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        analysisDTO.setPointType(HisDiagnosisResult.PointType.CENTER);
        return analysisDTO;
    }

    @Override
    public String getShowData(int taskId) {
        return null;
    }
}
