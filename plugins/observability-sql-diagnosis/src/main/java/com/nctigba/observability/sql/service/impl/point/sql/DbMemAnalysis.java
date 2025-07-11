/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2025.
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
 *  DbMemAnalysis.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/DbMemAnalysis.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.table.DbMemAnalysisItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.nctigba.observability.sql.constant.CommonConstants.DATE_FORMAT;
import static com.nctigba.observability.sql.constant.CommonConstants.HUNDRED;
import static com.nctigba.observability.sql.constant.CommonConstants.NUMERIC_FORMAT;
import static com.nctigba.observability.sql.constant.CommonConstants.SCALE;

/**
 * DbMemAnalysis
 *
 * @author luomeng
 * @since 2025/2/17
 */
@Service
public class DbMemAnalysis implements DiagnosisPointService<AutoShowDataVO> {
    private static final String SUGGEST_HIGH_FORMAT = "sql.DbMemAnalysis.suggest.high";
    private static final String SUGGEST_NORMAL_FORMAT = "sql.DbMemAnalysis.suggest.normal";
    private static final String MEMORY_TYPE = "memorytype";
    private static final String MEMORY_M_BYTES = "memorymbytes";
    private static final String PROCESS_USED_MEMORY = "process_used_memory";
    private static final String MAX_PROCESS_MEMORY = "max_process_memory";
    private static final String USED_MEMORY = "processUsedMemory";
    private static final String MAX_MEMORY = "maxProcessMemory";
    private static final String CHART_NAME = "DatabaseMemoryUsageTable";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    @Autowired
    private DbMemAnalysisItem item;
    @Autowired
    private PointUtils pointUtils;

    @Override
    public List<String> getOption() {
        return new ArrayList<>();
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        return List.of(item);
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<?> mapList = pointUtils.getStoreData(list);
        if (mapList == null || mapList.isEmpty()) {
            return analysisDTO;
        }
        Map<String, Integer> map = getMemoryInfo(mapList);
        if (map.get(MAX_MEMORY) == 0) {
            return analysisDTO;
        }
        String thresholdValue = pointUtils.getThresholdValue(
                task.getThresholds(), ThresholdConstants.DB_MEM_USAGE_RATE);
        BigDecimal usageRate = new BigDecimal(map.get(USED_MEMORY)).divide(
                BigDecimal.valueOf(map.get(MAX_MEMORY)), SCALE, RoundingMode.HALF_UP).multiply(
                BigDecimal.valueOf(HUNDRED));
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DATE_TIME_FORMATTER);
        if (usageRate.intValue() > Integer.parseInt(thresholdValue)) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            analysisDTO.setSuggestion(
                    LocaleStringUtils.format(SUGGEST_HIGH_FORMAT, formattedDate,
                            String.format(Locale.ROOT, NUMERIC_FORMAT, usageRate), thresholdValue));
        } else {
            analysisDTO.setSuggestion(
                    LocaleStringUtils.format(SUGGEST_NORMAL_FORMAT, formattedDate,
                            String.format(Locale.ROOT, NUMERIC_FORMAT, usageRate), thresholdValue));
        }
        List<ShowData> tableList = pointUtils.getTableData(list, CHART_NAME);
        AutoShowDataVO dataVO = new AutoShowDataVO();
        dataVO.setData(tableList);
        analysisDTO.setPointData(dataVO);
        return analysisDTO;
    }

    private Map<String, Integer> getMemoryInfo(List<?> mapList) {
        Map<String, Integer> result = new HashMap<>();
        result.put(MAX_MEMORY, 0);
        result.put(USED_MEMORY, 0);
        mapList.stream()
                .filter(HashMap.class::isInstance)
                .map(HashMap.class::cast)
                .forEach(map -> {
                    Object memoryTypeObj = map.get(MEMORY_TYPE);
                    Object memoryMBytesObj = map.get(MEMORY_M_BYTES);
                    if (memoryTypeObj instanceof String && memoryMBytesObj instanceof Integer) {
                        String memoryType = (String) memoryTypeObj;
                        int memoryMBytes = (Integer) memoryMBytesObj;
                        switch (memoryType) {
                            case PROCESS_USED_MEMORY:
                                result.put(USED_MEMORY, memoryMBytes);
                                break;
                            case MAX_PROCESS_MEMORY:
                                result.put(MAX_MEMORY, memoryMBytes);
                                break;
                            default:
                                break;
                        }
                    }
                });
        return result;
    }

    @Override
    public AutoShowDataVO getShowData(int taskId) {
        return null;
    }
}
