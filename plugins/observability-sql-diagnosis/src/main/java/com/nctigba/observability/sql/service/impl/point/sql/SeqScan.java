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
 *  SeqScan.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/SeqScan.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.model.vo.point.TableShowDataVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.collection.table.AllFunctionItem;
import com.nctigba.observability.sql.service.impl.collection.table.ExplainItem;
import com.nctigba.observability.sql.util.PointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SeqScan
 *
 * @author luomeng
 * @since 2023/11/7
 */
@Service
public class SeqScan implements DiagnosisPointService<Object> {
    @Autowired
    private ExplainItem item;
    @Autowired
    private AllFunctionItem functionItem;
    @Autowired
    private PointUtils pointUtils;
    @Autowired
    private ClusterManager clusterManager;

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
        list.add(functionItem);
        return list;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        List<String> rsList = new ArrayList<>();
        List<?> rsObject = (List<?>) dataStoreService.getData(item).getCollectionData();
        rsObject.forEach(obj -> {
            if (obj instanceof String) {
                rsList.add((String) obj);
            }
        });
        StringBuilder sb = new StringBuilder();
        rsList.forEach(data -> {
            if (data != null) {
                sb.append(data);
            }
        });
        List<ShowData> tableList = new ArrayList<>();
        TableShowDataVO tableData = pointUtils.generateTableData(rsList, "explain", "explain");
        tableList.add(tableData);
        if (!sb.toString().contains("Seq Scan on") || sb.toString().contains("Partitioned Seq Scan on")) {
            AutoShowDataVO dataVO = new AutoShowDataVO();
            dataVO.setData(tableList);
            analysisDTO.setPointData(dataVO);
            return analysisDTO;
        }
        StringBuilder suggest = new StringBuilder();
        List<?> list = (List<?>) dataStoreService.getData(functionItem).getCollectionData();
        boolean isHint = isSuggestion(task, list, suggest, rsList);
        if (isHint) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        }
        TableShowDataVO suggestData = pointUtils.generateTableData(suggest, "suggest", "suggest");
        tableList.add(suggestData);
        AutoShowDataVO dataVO = new AutoShowDataVO();
        dataVO.setData(tableList);
        analysisDTO.setPointData(dataVO);
        return analysisDTO;
    }

    private boolean isSuggestion(DiagnosisTaskDO task, List<?> list, StringBuilder suggest, List<String> rsList) {
        List<String> conditions = pointUtils.getAllConditions(task.getSql());
        String sql = String.format(SqlConstants.INDEX_LOSE_EFFICACY, pointUtils.getTableName(task.getSql()));
        List<String> funcList = pointUtils.getAllFunction(list);
        boolean isHint = false;
        try (Connection conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             java.sql.Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String column = rs.getString(6);
                if ("false".equals(rs.getString(1))) {
                    isHint = true;
                    suggest.append(column).append(" index is lose efficacy; ");
                }
                boolean isHasArithmetic = isHasArithmetic(rsList, column);
                if (isHasArithmetic) {
                    isHint = true;
                    suggest.append(column).append(" has logical operation; ");
                }
                String columnType = rs.getString(7);
                boolean isExists = conditions.stream().anyMatch(f -> {
                    if (f.contains(column)) {
                        // Determine if there is a type conversion
                        String rightSide = f.substring(f.indexOf("=") + 1).trim();
                        boolean isExist = pointUtils.isExistConversion(rightSide, columnType);
                        if (isExist) {
                            suggest.append(column).append(" has type conversion; ");
                            return true;
                        }
                        // Determine if a function exists
                        for (String func : funcList) {
                            if (f.contains(func)) {
                                suggest.append(column).append(" has function; ");
                                return true;
                            }
                        }
                    }
                    return false;
                });
                if (isExists) {
                    isHint = true;
                }
            }
        } catch (SQLException e) {
            throw new HisDiagnosisException("execute sql failed!");
        }
        return isHint;
    }

    private boolean isHasArithmetic(List<String> rsList, String column) {
        for (int i = 0; i < rsList.size(); i++) {
            String explain = rsList.get(i);
            boolean isHasOperator = explain.contains("Seq Scan on");
            if (isHasOperator && i + 1 < rsList.size()) {
                String pattern = "(?=.*Filter:)(?=.*[<~>])";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(rsList.get(i + 1));
                boolean isHasLike = m.find();
                if (isHasLike && rsList.get(i + 1).contains(column)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
