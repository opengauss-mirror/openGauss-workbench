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
 *  ContextUseShareMem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/ContextUseShareMem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.table.ContextUseShareMemItem;
import com.nctigba.observability.sql.util.PointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ContextUseShareMem
 *
 * @author luomeng
 * @since 2025/2/17
 */
@Service
public class ContextUseShareMem implements DiagnosisPointService<AutoShowDataVO> {
    private static final String CHART_NAME = "ContextUseShareMemTable";

    @Autowired
    private ContextUseShareMemItem item;
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
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<?> mapList = pointUtils.getStoreData(list);
        if (mapList == null || mapList.isEmpty()) {
            return analysisDTO;
        }
        List<ShowData> tableList = pointUtils.getTableData(list, CHART_NAME);
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
