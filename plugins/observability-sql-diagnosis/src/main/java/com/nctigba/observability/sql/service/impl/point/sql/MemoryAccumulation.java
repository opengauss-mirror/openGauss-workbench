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
 *  MemoryAccumulation.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/MemoryAccumulation.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.table.ContextUseShareMemItem;
import com.nctigba.observability.sql.util.DbUtils;
import com.nctigba.observability.sql.util.PointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MemoryAccumulation
 *
 * @author luomeng
 * @since 2025/2/25
 */
@Service
public class MemoryAccumulation implements DiagnosisPointService<AutoShowDataVO> {
    private static final String CHART_NAME = "MemoryAccumulationTable";
    private static final String COLUMN_NAME = "contextname";
    private static final String DELIMITER = " union all ";

    @Autowired
    private ContextUseShareMemItem item;
    @Autowired
    private PointUtils pointUtils;
    @Autowired
    private DbUtils dbUtils;

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
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<?> mapList = pointUtils.getStoreData(list);
        if (CollectionUtils.isEmpty(mapList)) {
            return analysisDTO;
        }
        List<String> contextList = mapList.stream()
                .filter(data -> data instanceof HashMap)
                .map(data -> (HashMap<?, ?>) data)
                .map(map -> map.get(COLUMN_NAME))
                .filter(contextName -> contextName instanceof String)
                .map(contextName -> (String) contextName)
                .collect(Collectors.toList());
        String sql = contextList.stream()
                .map(context -> String.format(SqlConstants.GET_DB_CONTEXT_MEM_DETAIL_SQL, context, context))
                .collect(Collectors.joining(DELIMITER));
        Object resultObj = dbUtils.rangQuery(sql, null, null, task.getNodeId());
        List<?> resultList = resultObj instanceof ArrayList ? (List<?>) resultObj : Collections.emptyList();
        List<ShowData> tableList = pointUtils.getTableData(resultList, CHART_NAME);
        AutoShowDataVO dataVO = new AutoShowDataVO();
        dataVO.setData(tableList);
        analysisDTO.setPointData(dataVO);
        return analysisDTO;
    }

    @Override
    public AutoShowDataVO getShowData(int taskId) {
        return null;
    }
}
