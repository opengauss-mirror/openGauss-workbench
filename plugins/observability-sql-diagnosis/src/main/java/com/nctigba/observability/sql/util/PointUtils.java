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
 *  PointUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/util/PointUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nctigba.observability.sql.constant.PrometheusConstants;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.HisThresholdMapper;
import com.nctigba.observability.sql.model.dto.point.AspAnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.collection.DatabaseVO;
import com.nctigba.observability.sql.model.vo.collection.PrometheusVO;
import com.nctigba.observability.sql.model.vo.point.ExecPlanDetailVO;
import com.nctigba.observability.sql.model.vo.point.PlanVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.model.vo.point.TableShowDataColumnVO;
import com.nctigba.observability.sql.model.vo.point.TableShowDataVO;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PointUtil
 *
 * @author luomeng
 * @since 2023/6/25
 */
@Component
@Slf4j
public class PointUtils {
    private int totalPlanRows = 0;

    private int totalPlanWidth = 0;

    @Autowired
    private HisThresholdMapper hisThresholdMapper;

    /**
     * Get asp time slot
     *
     * @param prometheusVOList Prometheus data
     * @return list
     */
    public List<AspAnalysisDTO> aspTimeSlot(List<PrometheusVO> prometheusVOList) {
        List<Integer> timeList = new ArrayList<>();
        for (PrometheusVO data : prometheusVOList) {
            JSONArray values = data.getValues();
            for (Object value : values) {
                JSONArray timeJson = new JSONArray();
                if (value instanceof JSONArray) {
                    timeJson = (JSONArray) value;
                }
                Object time = timeJson.get(0);
                timeList.add(Integer.parseInt(time.toString()));
            }
        }
        int mCount = PrometheusConstants.MINUTE / Integer.parseInt(PrometheusConstants.STEP) - 1;
        int cursor;
        List<AspAnalysisDTO> dtoList = new ArrayList<>();
        for (int i = 0; i < timeList.size(); i = cursor) {
            int count = 0;
            for (int j = i + 1; j < timeList.size(); j++) {
                if (timeList.get(i) == timeList.get(j) - Integer.parseInt(PrometheusConstants.STEP) * (j - i)) {
                    count++;
                }
            }
            if (count < mCount && count > 0) {
                AspAnalysisDTO dto = new AspAnalysisDTO(timeList.get(i), timeList.get(i + count));
                dtoList.add(dto);
            } else if (count == 0) {
                AspAnalysisDTO dto = new AspAnalysisDTO(timeList.get(i), timeList.get(i));
                dtoList.add(dto);
            } else {
                log.info("not exists slot");
            }
            cursor = i + count + 1;
        }
        return dtoList;
    }

    /**
     * Prometheus' data transform to list
     *
     * @param list Prometheus data
     * @return list
     */
    public List<PrometheusVO> dataToObject(List<?> list) {
        List<PrometheusVO> prometheusVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return prometheusVOList;
        }
        PrometheusVO prometheusVO = new PrometheusVO();
        PrometheusVO objectData = new PrometheusVO();
        if (list.get(0) instanceof PrometheusVO) {
            objectData = (PrometheusVO) list.get(0);
        }
        prometheusVO.setMetric(objectData.getMetric());
        JSONArray jsonArray = new JSONArray();
        for (Object object : list) {
            if (object instanceof PrometheusVO) {
                jsonArray.addAll(((PrometheusVO) object).getValues());
            }
        }
        prometheusVO.setValues(jsonArray);
        prometheusVOList.add(prometheusVO);
        return prometheusVOList;
    }

    /**
     * ThresholdDO info transform to maps
     *
     * @param thresholds ThresholdDO info
     * @return HashMap
     */
    public HashMap<String, String> thresholdMap(List<DiagnosisThresholdDO> thresholds) {
        LambdaQueryWrapper<DiagnosisThresholdDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(DiagnosisThresholdDO::getThresholdType);
        List<DiagnosisThresholdDO> thresholdList = hisThresholdMapper.selectList(queryWrapper);
        HashMap<String, String> map = new HashMap<>();
        for (DiagnosisThresholdDO threshold : thresholdList) {
            thresholds.forEach(f -> {
                if (f.getThreshold().equals(threshold.getThreshold())) {
                    threshold.setThresholdValue(f.getThresholdValue());
                }
            });
            map.put(threshold.getThreshold(), threshold.getThresholdValue());
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        return map;
    }

    /**
     * Get cpu core num
     *
     * @param list prometheus data
     * @return num
     */
    public int getCpuCoreNum(List<?> list) {
        int coreNum = 8;
        if (CollectionUtils.isEmpty(list)) {
            return coreNum;
        }
        List<Object> objects = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof PrometheusVO) {
                objects = ((PrometheusVO) object).getValues();
                break;
            }
        }
        if (!CollectionUtils.isEmpty(objects)) {
            return coreNum;
        }
        Object objectList = objects.get(0);
        if (objectList instanceof List<?>) {
            List<?> valueList = (List<?>) objectList;
            if (!CollectionUtils.isEmpty(valueList)) {
                String value = valueList.get(1).toString();
                coreNum = Integer.parseInt(value);
            }
        }
        return coreNum;
    }

    /**
     * Get exec planVO
     *
     * @param planVO exec planVO
     * @return object
     */
    public ExecPlanDetailVO getExecPlan(PlanVO planVO) {
        ExecPlanDetailVO planDetailDTO = new ExecPlanDetailVO();
        planDetailDTO.setData(planVO);
        JSONObject total = new JSONObject();
        if (planVO != null) {
            sumTotalPlan(planVO);
        }
        total.put("totalPlanRows", totalPlanRows);
        total.put("totalPlanWidth", totalPlanWidth);
        planDetailDTO.setTotal(total);
        return planDetailDTO;
    }

    private void sumTotalPlan(PlanVO planVO) {
        totalPlanRows += planVO.getPlanRows();
        totalPlanWidth += planVO.getPlanWidth();
        if (planVO.getPlanVOS() != null) {
            for (PlanVO chilePlanVO : planVO.getPlanVOS()) {
                sumTotalPlan(chilePlanVO);
            }
        }
    }

    /**
     * Parsing SQL to Obtain Table Names
     *
     * @param sql diagnosis sql
     * @return string
     */
    public String getTableName(String sql) {
        if (!sql.contains("from")) {
            return "table name not exists!";
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(new StringReader(sql));
            StringBuilder sb = new StringBuilder();
            if (statement instanceof Select) {
                Select selectStatement = (Select) statement;
                TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
                for (String table : tableList) {
                    sb.append("'");
                    sb.append(table);
                    sb.append("'");
                    sb.append(",");
                }
                if (sb.charAt(sb.length() - 1) == ',') {
                    sb.deleteCharAt(sb.length() - 1);
                }
            }
            return sb.toString();
        } catch (JSQLParserException e) {
            return "";
        }
    }

    /**
     * Parsing SQL to Obtain all conditions
     *
     * @param sql diagnosis sql
     * @return List<String>
     */
    public List<String> getAllConditions(String sql) {
        List<String> list = new ArrayList<>();
        try {
            Statement statement = CCJSqlParserUtil.parse(new StringReader(sql));
            if (statement instanceof Select) {
                Select selectStatement = (Select) statement;
                PlainSelect plainSelect = new PlainSelect();
                SelectBody selectBody = selectStatement.getSelectBody();
                if (selectBody instanceof PlainSelect) {
                    plainSelect = (PlainSelect) selectBody;
                }
                FromItem fromItem = plainSelect.getFromItem();
                if (fromItem instanceof SubSelect) {
                    SubSelect subSelect = (SubSelect) fromItem;
                    SelectBody subSelectBody = subSelect.getSelectBody();
                    if (subSelectBody instanceof PlainSelect) {
                        plainSelect = (PlainSelect) subSelectBody;
                    }
                }
                Expression expression = plainSelect.getWhere();
                if (expression != null) {
                    list = recursionExpression(expression);
                }
            }
            return list;
        } catch (JSQLParserException e) {
            return list;
        }
    }

    private List<String> recursionExpression(Expression expression) {
        List<String> conditions = new ArrayList<>();
        if (expression instanceof AndExpression) {
            AndExpression andExpression = (AndExpression) expression;
            conditions.addAll(recursionExpression(andExpression.getLeftExpression()));
            conditions.addAll(recursionExpression(andExpression.getRightExpression()));
        } else if (expression instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) expression;
            if (equalsTo.getRightExpression() instanceof SubSelect) {
                SubSelect rightExpression = (SubSelect) equalsTo.getRightExpression();
                PlainSelect plainSelect = new PlainSelect();
                SelectBody selectBody = rightExpression.getSelectBody();
                if (selectBody instanceof PlainSelect) {
                    plainSelect = (PlainSelect) selectBody;
                }
                Expression ex = plainSelect.getWhere();
                conditions.addAll(recursionExpression(ex));
            } else {
                conditions.add(expression.toString());
            }
        } else if (expression instanceof OrExpression) {
            OrExpression orExpression = (OrExpression) expression;
            conditions.addAll(recursionExpression(orExpression.getLeftExpression()));
            conditions.addAll(recursionExpression(orExpression.getRightExpression()));
        } else {
            conditions.add(expression.toString());
        }
        return conditions;
    }

    /**
     * Get table data
     *
     * @param list      data
     * @param chartName Chart name
     * @return List<ShowData>
     */
    public List<ShowData> getTableData(List<?> list, String chartName) {
        DatabaseVO databaseVO = new DatabaseVO();
        if (!CollectionUtils.isEmpty(list)) {
            Object obj = list.get(0);
            if (obj instanceof DatabaseVO) {
                databaseVO = (DatabaseVO) obj;
            }
        }
        List<TableShowDataColumnVO> columnList = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (databaseVO.getValue() != null) {
            List<?> resultData = (List<?>) databaseVO.getValue().get(0);
            resultData.forEach(f -> {
                Map<String, Object> hashMap = new HashMap<>();
                Map<String, String> map = (Map<String, String>) f;
                for (String key : map.keySet()) {
                    TableShowDataColumnVO columnVO = new TableShowDataColumnVO();
                    columnVO.setKey(key);
                    columnVO.setName(key);
                    if (!columnList.contains(columnVO)) {
                        columnList.add(columnVO);
                    }
                    hashMap.put(key, map.get(key));
                }
                dataList.add(hashMap);
            });
        }
        TableShowDataVO data = new TableShowDataVO();
        data.setDataName(chartName);
        data.setData(dataList);
        data.setColumns(columnList);
        List<ShowData> tableList = new ArrayList<>();
        tableList.add(data);
        return tableList;
    }


    /**
     * Generate table data,only supports one column
     *
     * @param rsList     data
     * @param columnName column name
     * @param dataName   data name
     * @return TableShowDataVO
     */
    public TableShowDataVO generateTableData(Object rsList, String columnName, String dataName) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (rsList instanceof ArrayList) {
            List<?> rsObject = (List<?>) rsList;
            rsObject.forEach(obj -> {
                if (obj instanceof String) {
                    Map<String, Object> hashMap = new HashMap<>();
                    hashMap.put(columnName, obj);
                    dataList.add(hashMap);
                }
            });
        } else {
            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put(columnName, rsList);
            dataList.add(hashMap);
        }
        List<TableShowDataColumnVO> columnList = new ArrayList<>();
        TableShowDataColumnVO columnVO = new TableShowDataColumnVO();
        columnVO.setKey(columnName);
        columnVO.setName(columnName);
        columnList.add(columnVO);
        TableShowDataVO tableData = new TableShowDataVO();
        tableData.setDataName(dataName);
        tableData.setData(dataList);
        tableData.setColumns(columnList);
        return tableData;
    }

    /**
     * Get database all function
     *
     * @param list data
     * @return List<String>
     */
    public List<String> getAllFunction(List<?> list) {
        List<String> functionList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list)) {
            List<DatabaseVO> databaseVOList = new ArrayList<>();
            list.forEach(data -> {
                if (data instanceof DatabaseVO) {
                    databaseVOList.add((DatabaseVO) data);
                }
            });
            for (DatabaseVO databaseVO : databaseVOList) {
                Object dataObject = databaseVO.getValue().get(0);
                List<?> dataList = (List<?>) dataObject;
                dataList.forEach(data -> {
                    if (data instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) data;
                        map.forEach((k, v) -> functionList.add(v.toString()));
                    }
                });
            }
        }
        return functionList;
    }

    /**
     * Is it a type conversion
     *
     * @param rightSide  data
     * @param columnType data
     * @return List<String>
     */
    public boolean isExistConversion(String rightSide, String columnType) {
        boolean isExist = false;
        switch (columnType) {
            case "bigint":
                if (!isBigInteger(rightSide)) {
                    isExist = true;
                }
                break;
            case "integer":
                if (!isInteger(rightSide)) {
                    isExist = true;
                }
                break;
            case "float4":
                if (!isFloat(rightSide)) {
                    isExist = true;
                }
                break;
            case "decimal":
                if (!isDouble(rightSide)) {
                    isExist = true;
                }
                break;
            default:
                break;
        }
        return isExist;
    }

    private boolean isBigInteger(String s) {
        try {
            new BigInteger(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isFloat(String s) {
        try {
            Float.parseFloat(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
