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
 *  RandomPageCost.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/RandomPageCost.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.DiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.collection.DatabaseVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.collection.table.RandomPageCostItem;
import com.nctigba.observability.sql.util.DbUtils;
import com.nctigba.observability.sql.util.PointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * RandomPageCost
 *
 * @author luomeng
 * @since 2023/11/7
 */
@Service
public class RandomPageCost implements DiagnosisPointService<AutoShowDataVO> {
    @Autowired
    private RandomPageCostItem item;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private DiagnosisResultMapper resultMapper;
    @Autowired
    private DbUtils dbUtils;
    @Autowired
    private DiagnosisTaskMapper taskMapper;
    @Autowired
    private PointUtils pointUtils;

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
        HashMap<String, String> thresholdMap = new HashMap<>();
        List<DiagnosisThresholdDO> thresholds = task.getThresholds();
        for (DiagnosisThresholdDO threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdConstants.RANDOM_PAGE_COST)) {
                thresholdMap.put(threshold.getThreshold(), threshold.getThresholdValue());
            }
        }
        if (thresholdMap.isEmpty()) {
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
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        List<Object> dataList = databaseVOList.get(0).getValue();
        List<?> mapList = (List<?>) dataList.get(0);
        mapList.forEach(data -> {
            if (data instanceof HashMap) {
                HashMap<?, ?> map = (HashMap<?, ?>) data;
                if (!thresholdMap.get(ThresholdConstants.RANDOM_PAGE_COST).equals(map.get("setting"))) {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                }
            }
        });
        return analysisDTO;
    }

    @Override
    public AutoShowDataVO getShowData(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        Object resultObj = dbUtils.rangQuery(
                SqlConstants.RANDOM_PAGE_COST, null, null, task.getNodeId());
        List<?> resultList = new ArrayList<>();
        if (resultObj instanceof ArrayList) {
            resultList = (List<?>) resultObj;
        }
        List<ShowData> tableList = pointUtils.getTableData(resultList, "RandomPageCost");
        AutoShowDataVO dataVO = new AutoShowDataVO();
        dataVO.setData(tableList);
        return dataVO;
    }
}

