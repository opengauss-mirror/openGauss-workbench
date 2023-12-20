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
 *  CurrentSlowSql.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/CurrentSlowSql.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.PointTypeConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.collection.DatabaseVO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.table.SlowSqlItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * CurrentSlowSql
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class CurrentSlowSql implements DiagnosisPointService<Object> {
    @Autowired
    SlowSqlItem item;

    @Override
    public List<String> getOption() {
        return null;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public String getDiagnosisType() {
        return PointTypeConstants.CURRENT;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        HashMap<String, String> map = new HashMap<>();
        List<DiagnosisThresholdDO> thresholds = task.getThresholds();
        for (DiagnosisThresholdDO threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdConstants.SQL_NUM)) {
                map.put(threshold.getThreshold(), threshold.getThresholdValue());
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<DatabaseVO> databaseVOList = new ArrayList<>();
        list.forEach(data -> {
            if (data instanceof DatabaseVO) {
                databaseVOList.add((DatabaseVO) data);
            }
        });
        AnalysisDTO analysisDTO = new AnalysisDTO();
        if (!CollectionUtils.isEmpty(databaseVOList) && databaseVOList.get(0).getValue().size() > Integer.parseInt(
                map.get(ThresholdConstants.SQL_NUM))) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        for (DatabaseVO dto : databaseVOList) {
            dto.setTableName(LocaleStringUtils.format("history.SLOW_SQL.table"));
        }
        analysisDTO.setPointData(databaseVOList);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    @Override
    public List<?> getShowData(int taskId) {
        return null;
    }
}