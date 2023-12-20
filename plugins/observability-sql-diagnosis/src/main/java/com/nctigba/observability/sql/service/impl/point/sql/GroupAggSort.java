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
 *  GroupAggSort.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/GroupAggSort.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.model.vo.point.TableShowDataColumnVO;
import com.nctigba.observability.sql.model.vo.point.TableShowDataVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.table.ExplainItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GroupAggSort
 *
 * @author luomeng
 * @since 2023/11/7
 */
@Service
public class GroupAggSort implements DiagnosisPointService<Object> {
    private static final String THRESHOLD_VALUE = "GroupAggregate";

    @Autowired
    private ExplainItem item;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionEnum.IS_EXPLAIN));
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
        List<String> rsList = new ArrayList<>();
        List<?> rsObject = (List<?>) dataStoreService.getData(item).getCollectionData();
        rsObject.forEach(obj -> {
            if (obj instanceof String) {
                rsList.add((String) obj);
            }
        });
        StringBuilder sb = new StringBuilder();
        rsList.forEach(sb::append);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        String explain = sb.toString();
        if (explain.contains(THRESHOLD_VALUE) && explain.contains("sort")) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("explain", rsList);
        dataList.add(hashMap);
        List<TableShowDataColumnVO> columnList = new ArrayList<>();
        TableShowDataColumnVO columnVO = new TableShowDataColumnVO();
        columnVO.setKey("explain");
        columnVO.setName("explain");
        columnList.add(columnVO);
        TableShowDataVO tableData = new TableShowDataVO();
        tableData.setDataName("GroupAggSort");
        tableData.setData(dataList);
        tableData.setColumns(columnList);
        List<ShowData> tableList = new ArrayList<>();
        tableList.add(tableData);
        AutoShowDataVO dataVO = new AutoShowDataVO();
        dataVO.setData(tableList);
        analysisDTO.setPointData(dataVO);
        return analysisDTO;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
