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
 *  SqlRewrite.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/SqlRewrite.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.JdbcConstants;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.collection.DatabaseVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.model.vo.point.TableShowDataColumnVO;
import com.nctigba.observability.sql.model.vo.point.TableShowDataVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.table.AggFunctionItem;
import com.nctigba.observability.sql.util.PointUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SqlRewrite
 *
 * @author luomeng
 * @since 2023/11/7
 */
@Service
@Slf4j
public class SqlRewrite implements DiagnosisPointService<Object> {
    @Autowired
    private AggFunctionItem item;
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
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        if (!task.getSql().contains("from")) {
            return analysisDTO;
        }
        List<?> list = (List<?>) dataStoreService.getData(item).getCollectionData();
        StringBuilder sb = getAllFunction(list);
        String pattern = sb.deleteCharAt(sb.length() - 1).toString();
        String firstSql = task.getSql();
        String querySql = firstSql.toLowerCase(Locale.ROOT);
        int start = querySql.indexOf("select");
        int end = querySql.indexOf("from");
        String aggString = querySql.substring(start + 6, end);
        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(aggString);
        String rewriteSql = null;
        if (m.find() && querySql.contains("order by")) {
            int sIndex = querySql.indexOf("order by");
            int eIndex = querySql.indexOf(")", sIndex);
            String tmpString = firstSql.substring(sIndex, eIndex);
            if (querySql.substring(sIndex).contains(")")) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                rewriteSql = firstSql.replace(tmpString, "");
            }
        }
        String regex = "([a-zA-Z_]\\w*)\\s*[-+*/]\\s*(\\d+)\\s*=\\s*(\\d+)";
        List<String> conditions = pointUtils.getAllConditions(firstSql);
        if (!CollectionUtils.isEmpty(conditions)) {
            for (String condition : conditions) {
                if (condition.matches(regex)) {
                    analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
                    String tmpSql = Objects.requireNonNullElse(rewriteSql, firstSql);
                    String formatSql = SQLUtils.format(tmpSql, JdbcConstants.MYSQL, new SQLUtils.FormatOption(true));
                    rewriteSql = formatSql.replace(condition, convertEquation(condition));
                }
            }
        }
        AutoShowDataVO dataVO = assemblyData(firstSql, rewriteSql);
        analysisDTO.setPointData(dataVO);
        return analysisDTO;
    }

    private StringBuilder getAllFunction(List<?> list) {
        List<DatabaseVO> databaseVOList = new ArrayList<>();
        list.forEach(data -> {
            if (data instanceof DatabaseVO) {
                databaseVOList.add((DatabaseVO) data);
            }
        });
        StringBuilder sb = new StringBuilder("\\b");
        for (DatabaseVO databaseVO : databaseVOList) {
            List<Object> dataList = (List<Object>) databaseVO.getValue().get(0);
            dataList.forEach(data -> {
                if (data instanceof HashMap) {
                    Map<?, ?> map = (Map<?, ?>) data;
                    map.forEach((k, v) -> {
                        sb.append(v.toString());
                        sb.append("\\([^)]*\\)|");
                    });
                }
            });
        }
        return sb;
    }

    private AutoShowDataVO assemblyData(String firstSql, String rewriteSql) {
        List<Map<String, Object>> firstDataList = new ArrayList<>();
        Map<String, Object> firstHashMap = new HashMap<>();
        firstHashMap.put("sql", firstSql);
        firstDataList.add(firstHashMap);
        List<Map<String, Object>> rewriteDataList = new ArrayList<>();
        Map<String, Object> rewriteHashMap = new HashMap<>();
        rewriteHashMap.put("sql", rewriteSql);
        rewriteDataList.add(rewriteHashMap);
        List<TableShowDataColumnVO> columnList = new ArrayList<>();
        TableShowDataColumnVO columnVO = new TableShowDataColumnVO();
        columnVO.setKey("sql");
        columnVO.setName("sql");
        columnList.add(columnVO);
        TableShowDataVO firstData = new TableShowDataVO();
        firstData.setDataName("first");
        firstData.setData(firstDataList);
        firstData.setColumns(columnList);
        TableShowDataVO rewriteData = new TableShowDataVO();
        rewriteData.setDataName("rewrite");
        rewriteData.setData(rewriteDataList);
        rewriteData.setColumns(columnList);
        List<ShowData> tableList = new ArrayList<>();
        tableList.add(firstData);
        tableList.add(rewriteData);
        AutoShowDataVO dataVO = new AutoShowDataVO();
        dataVO.setData(tableList);
        return dataVO;
    }

    private String convertEquation(String input) {
        String[] parts;
        String variable;
        int number1;
        int number2;
        int result;
        if (input.contains("+")) {
            parts = input.split("[+=]");
            variable = parts[0];
            number1 = Integer.parseInt(parts[1].trim());
            number2 = Integer.parseInt(parts[2].trim());
            result = number2 - number1;
        } else if (input.contains("-")) {
            parts = input.split("[-=]");
            variable = parts[0];
            number1 = Integer.parseInt(parts[1].trim());
            number2 = Integer.parseInt(parts[2].trim());
            result = number2 + number1;
        } else if (input.contains("*")) {
            parts = input.split("[*=]");
            variable = parts[0];
            number1 = Integer.parseInt(parts[1].trim());
            number2 = Integer.parseInt(parts[2].trim());
            result = number2 / number1;
        } else if (input.contains("/")) {
            parts = input.split("[/=]");
            variable = parts[0];
            number1 = Integer.parseInt(parts[1].trim());
            number2 = Integer.parseInt(parts[2].trim());
            result = number2 * number1;
        } else {
            return "Invalid equation format";
        }
        return variable + "=" + result;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
