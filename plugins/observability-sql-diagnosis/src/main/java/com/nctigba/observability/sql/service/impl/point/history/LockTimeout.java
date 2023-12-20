/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  LockTimeout.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/LockTimeout.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.vo.point.LockAnalysisVO;
import com.nctigba.observability.sql.model.vo.point.LogDetailInfoVO;
import com.nctigba.observability.sql.model.vo.point.LogInfoVO;
import com.nctigba.observability.sql.model.dto.point.LockDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.elastic.LockTimeoutItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
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
public class LockTimeout implements DiagnosisPointService<LockDTO> {
    @Autowired
    LockTimeoutItem item;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionEnum.IS_LOCK));
        return option;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }


    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        Object collectionData = dataStoreService.getData(item).getCollectionData();
        LogInfoVO logInfoVO = null;
        if (collectionData instanceof LogInfoVO) {
            logInfoVO = (LogInfoVO) collectionData;
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (logInfoVO != null && logInfoVO.getLogs().size() > 0) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            List<LogDetailInfoVO> logs = logInfoVO.getLogs();
            List<LockAnalysisVO> lockDtoList = new ArrayList<>();
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
                LockAnalysisVO lockAnalysisVO = new LockAnalysisVO();
                lockAnalysisVO.setLogTime(f.getLogTime().toString());
                lockAnalysisVO.setLogData(f.getLogData().toString());
                lockAnalysisVO.setProcessId(processId);
                lockAnalysisVO.setTransactionId(transactionId);
                lockDtoList.add(lockAnalysisVO);
            });
            LockDTO lockDTO = new LockDTO();
            lockDTO.setLockAnalysisVOList(lockDtoList);
            lockDTO.setChartName(LocaleStringUtils.format("history.LockTimeout.charName"));
            analysisDTO.setPointData(lockDTO);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    @Override
    public LockDTO getShowData(int taskId) {
        return null;
    }
}