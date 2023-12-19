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
 *  Hint.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/Hint.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Hint
 *
 * @author luomeng
 * @since 2023/11/7
 */
@Service
public class Hint implements DiagnosisPointService<Object> {
    @Override
    public List<String> getOption() {
        return null;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        return null;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        List<String> list = new ArrayList<>();
        String[] sqlList = task.getSql().toLowerCase(Locale.ROOT).split("select");
        for (String sql : sqlList) {
            if (sql.trim().startsWith("/*")) {
                list.add(sql.substring(sql.indexOf("/*") + 2, sql.indexOf("*/") - 1));
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            }
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("content", list);
        dataList.add(hashMap);
        List<TableShowDataColumnVO> columnList = new ArrayList<>();
        TableShowDataColumnVO columnVO = new TableShowDataColumnVO();
        columnVO.setKey("content");
        columnVO.setName("content");
        columnList.add(columnVO);
        TableShowDataVO tableData = new TableShowDataVO();
        tableData.setDataName("Hint");
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
