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
 *  DbShareMemory.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/DbShareMemory.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.table.DbMemAnalysisItem;
import com.nctigba.observability.sql.service.impl.collection.table.DbShareMemoryItem;
import com.nctigba.observability.sql.util.PointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DbShareMemory
 *
 * @author luomeng
 * @since 2025/2/17
 */
@Service
public class DbShareMemory implements DiagnosisPointService<AutoShowDataVO> {
    @Autowired
    private DbShareMemoryItem dbShareMemoryItem;
    @Autowired
    private DbMemAnalysisItem item;
    @Autowired
    private PointUtils pointUtils;

    @Override
    public List<String> getOption() {
        return List.of();
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        return List.of(item, dbShareMemoryItem);
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        return pointUtils.pointAnalysis(task, dataStoreService, item, dbShareMemoryItem, "DbShareMemory");
    }

    @Override
    public AutoShowDataVO getShowData(int taskId) {
        return null;
    }
}
