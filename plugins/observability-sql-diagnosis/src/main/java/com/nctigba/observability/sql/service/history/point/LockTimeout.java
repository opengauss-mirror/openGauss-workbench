/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point;

import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.dto.LockAnalysisDTO;
import com.nctigba.observability.sql.model.history.dto.LogDetailInfoDTO;
import com.nctigba.observability.sql.model.history.dto.LogInfoDTO;
import com.nctigba.observability.sql.model.history.point.LockDTO;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.elastic.LockTimeoutItem;
import com.nctigba.observability.sql.util.LocaleString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LockTimeout
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class LockTimeout implements HisDiagnosisPointService<LockDTO> {
    @Autowired
    LockTimeoutItem item;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionCommon.IS_LOCK));
        return option;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }


    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        Object collectionData = dataStoreService.getData(item).getCollectionData();
        LogInfoDTO logInfoDTO = null;
        if (collectionData instanceof LogInfoDTO) {
            logInfoDTO = (LogInfoDTO) collectionData;
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (logInfoDTO != null && logInfoDTO.getLogs().size() > 0) {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
            List<LogDetailInfoDTO> logs = logInfoDTO.getLogs();
            List<LockAnalysisDTO> lockDtoList = new ArrayList<>();
            Pattern pattern = Pattern.compile(
                    "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+(\\w+)\\s+(\\w+)\\s+([\\d\\.]+)\\s"
                            + "+(\\d+)\\s+\\d+\\[\\d+:\\d+#\\d+\\]\\s+(\\d+)\\s+\\[(\\w+)\\]\\s+(.*)");
            logs.forEach(f -> {
                String logMessage = f.getLogData().toString();
                Matcher matcher = pattern.matcher(logMessage);
                String processId = null;
                String transactionId = null;
                if (matcher.matches()) {
                    processId = matcher.group(5);
                    transactionId = matcher.group(6);
                }
                LockAnalysisDTO lockAnalysisDTO = new LockAnalysisDTO();
                lockAnalysisDTO.setLogTime(f.getLogTime().toString());
                lockAnalysisDTO.setLogData(f.getLogData().toString());
                lockAnalysisDTO.setProcessId(processId);
                lockAnalysisDTO.setTransactionId(transactionId);
                lockDtoList.add(lockAnalysisDTO);
            });
            LockDTO lockDTO = new LockDTO();
            lockDTO.setLockAnalysisDTOList(lockDtoList);
            lockDTO.setChartName(LocaleString.format("history.LockTimeout.charName"));
            analysisDTO.setPointData(lockDTO);
        } else {
            analysisDTO.setIsHint(HisDiagnosisResult.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    @Override
    public LockDTO getShowData(int taskId) {
        return null;
    }
}