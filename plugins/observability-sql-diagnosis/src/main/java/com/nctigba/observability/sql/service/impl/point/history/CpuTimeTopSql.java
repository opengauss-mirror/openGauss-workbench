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
 *  CpuTimeTopSql.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/history/CpuTimeTopSql.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.history;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.collection.DatabaseVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.table.CpuTimeTopSqlItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * CpuTimeTopSql
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class CpuTimeTopSql implements DiagnosisPointService<Object> {
    @Autowired
    CpuTimeTopSqlItem item;
    @Autowired
    private DiagnosisTaskMapper taskMapper;

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
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        List<DatabaseVO> databaseVOList = new ArrayList<>();
        list.forEach(data -> {
            if (data instanceof DatabaseVO) {
                databaseVOList.add((DatabaseVO) data);
            }
        });
        if (!CollectionUtils.isEmpty(databaseVOList)) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DISPLAY);
        return analysisDTO;
    }

    @Override
    public List<?> getShowData(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        List<DatabaseVO> dataList = new ArrayList<>();
        for (CollectionItem<?> item : getSourceDataKeys()) {
            Object object = item.queryData(task);
            if (object instanceof String) {
                List<String> message = new ArrayList<>();
                message.add(String.valueOf(object));
                return message;
            } else {
                List<?> list = (List<?>) item.queryData(task);
                List<DatabaseVO> databaseVOList = new ArrayList<>();
                list.forEach(data -> {
                    if (data instanceof DatabaseVO) {
                        databaseVOList.add((DatabaseVO) data);
                    }
                });
                if (CollectionUtils.isEmpty(databaseVOList)) {
                    continue;
                }
                dataList.addAll(databaseVOList);
            }
        }
        for (DatabaseVO dto : dataList) {
            if (dto.getSqlName() != null && dto.getSqlName().getString("__name__").equals(
                    SqlConstants.CPU_TIME_TOP_SQL)) {
                dto.setTableName(LocaleStringUtils.format("history.CPU_TIME_TOP_SQL.table"));
            }
        }
        return dataList;
    }
}