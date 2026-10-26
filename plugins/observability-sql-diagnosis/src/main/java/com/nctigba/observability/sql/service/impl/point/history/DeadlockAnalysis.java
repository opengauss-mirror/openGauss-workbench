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
 *  DeadlockAnalysis.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/DeadlockAnalysis.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.LockDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.elastic.DeadlockItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.util.PointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * DeadlockAnalysis
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class DeadlockAnalysis implements DiagnosisPointService<LockDTO> {
    @Autowired
    DeadlockItem item;

    @Autowired
    PointUtils pointUtils;

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
        return pointUtils.buildLockAnalysis(
                dataStoreService.getData(item).getCollectionData(),
                "history.DeadlockAnalysis.charName");
    }

    @Override
    public LockDTO getShowData(int taskId) {
        return null;
    }
}
