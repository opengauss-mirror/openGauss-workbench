/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.history.HisThresholdMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.result.HisTreeNode;
import com.nctigba.observability.sql.model.history.result.Node;
import com.nctigba.observability.sql.model.param.DatabaseParamData;
import com.nctigba.observability.sql.model.param.OsParamData;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.HisDiagnosisService;
import com.nctigba.observability.sql.util.LocaleString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sqlite.JDBC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HisDiagnosisServiceImpl
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class HisDiagnosisServiceImpl implements HisDiagnosisService {
    @Autowired
    private HisDiagnosisResultMapper resultMapper;
    @Autowired
    private List<HisDiagnosisPointService<?>> pointServiceList;
    @Autowired
    private LocaleString localeToString;
    @Autowired
    private HisThresholdMapper hisThresholdMapper;
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;

    @Override
    public HisTreeNode getTopologyMap(int taskId, boolean isAll, String diagnosisType) {
        List<HisDiagnosisResult> resultList = resultMapper.selectList(
                Wrappers.<HisDiagnosisResult>lambdaQuery().eq(HisDiagnosisResult::getTaskId, taskId));
        HisTreeNode treeNode = this.createHisTreeNode(resultList, diagnosisType);
        HisTreeNode hisTreeNode = this.refreshTreeNode(treeNode);
        hisTreeNode.setIsHidden(false);
        return hisTreeNode;
    }

    @Override
    public Object getNodeDetail(int taskId, String pointName, String diagnosisType) {
        HisDiagnosisTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        HashMap<String, String> map = new HashMap<>();
        List<?> thresholdValue = task.getThresholds();
        for (Object ob : thresholdValue) {
            if (ob instanceof LinkedHashMap) {
                LinkedHashMap<String, String> hashMap = (LinkedHashMap<String, String>) ob;
                map.put(hashMap.get("threshold"), hashMap.get("thresholdValue"));
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        HisDiagnosisResult result = resultMapper.selectOne(
                Wrappers.<HisDiagnosisResult>lambdaQuery().eq(HisDiagnosisResult::getPointName, pointName).eq(
                        HisDiagnosisResult::getTaskId, taskId));
        if (result == null) {
            return "No data found!";
        }
        Object object = null;
        for (HisDiagnosisPointService<?> pointService : pointServiceList) {
            String className = pointService.getClass().getName();
            if (className.substring(className.lastIndexOf(".") + 1).equals(pointName)) {
                object = pointService.getShowData(taskId);
            }
        }
        if (result.getPointData() == null) {
            result.setData(object);
        } else {
            List<Object> objectList = new ArrayList<>();
            if (object != null) {
                objectList.add(object);
            }
            objectList.add(result.getPointData());
            result.setData(objectList);
        }
        HisDiagnosisResult toResult = localeToString.trapLanguage(result);
        List<HisDiagnosisThreshold> thresholds = hisThresholdMapper.selectList(Wrappers.emptyWrapper());
        for (HisDiagnosisThreshold threshold : thresholds) {
            threshold.setThresholdValue(map.get(threshold.getThreshold()));
            if (toResult.getPointSuggestion() != null) {
                String suggestion = toResult.getPointSuggestion().replace(
                        threshold.getThreshold(),
                        threshold.getThresholdValue() + threshold.getThresholdUnit());
                toResult.setPointSuggestion(suggestion);
            }
        }
        return toResult;
    }

    private HisTreeNode createHisTreeNode(List<HisDiagnosisResult> resultList, String diagnosisType) {
        String fileName = DiagnosisTypeCommon.HISTORY.equals(diagnosisType) ? "/hisTreeNode.txt" : "/sqlTreeNode.txt";
        try (InputStream is = this.getClass().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            List<String> list = new ArrayList<>();
            while (reader.ready()) {
                var line = reader.readLine();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                list.add(line);
            }
            List<Node> nodeList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                Node node = new Node();
                for (int j = i; j >= 0; j--) {
                    int iDepth = list.get(i).split("-").length;
                    int jDepth = list.get(j).split("-").length;
                    String parentNode = list.get(j).replace("-", "");
                    if (iDepth == 2) {
                        node.setParentNode("0");
                        break;
                    } else if (jDepth == iDepth - 1) {
                        node.setParentNode(parentNode);
                        break;
                    }
                }
                String nodeName = list.get(i).replace("-", "");
                if (nodeName.equals("OsParam")) {
                    node.setNodeName(nodeName);
                    nodeList.add(node);
                    String selectSql = "select * from param_info where paramType='OS';";
                    try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
                         ResultSet result = statement.executeQuery(selectSql)) {
                        while (result.next()) {
                            String paramName = result.getString(3);
                            OsParamData[] osFields = OsParamData.values();
                            String pointName = null;
                            for (OsParamData osField : osFields) {
                                if (osField.getParamName().equals(paramName)) {
                                    pointName = osField.toString();
                                }
                            }
                            Node osNode = new Node();
                            osNode.setNodeName(pointName);
                            osNode.setParentNode(nodeName);
                            nodeList.add(osNode);
                        }
                    } catch (SQLException e) {
                        throw new HisDiagnosisException("error:", e);
                    }
                } else if (nodeName.equals("DatabaseParam")) {
                    node.setNodeName(nodeName);
                    nodeList.add(node);
                    String selectSql = "select * from param_info where paramType='DB';";
                    try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
                         ResultSet result = statement.executeQuery(selectSql)) {
                        while (result.next()) {
                            String paramName = result.getString(3);
                            DatabaseParamData[] dbFields = DatabaseParamData.values();
                            String pointName = null;
                            for (DatabaseParamData dbField : dbFields) {
                                if (dbField.getParamName().equals(paramName)) {
                                    pointName = dbField.toString();
                                }
                            }
                            Node databaseNode = new Node();
                            databaseNode.setNodeName(pointName);
                            databaseNode.setParentNode(nodeName);
                            nodeList.add(databaseNode);
                        }
                    } catch (SQLException e) {
                        throw new HisDiagnosisException("error:", e);
                    }
                } else {
                    node.setNodeName(nodeName);
                    nodeList.add(node);
                }
            }
            var nodeMap = new HashMap<String, HisTreeNode>();
            for (Node node : nodeList) {
                String title;
                if (DiagnosisTypeCommon.HISTORY.equals(diagnosisType)) {
                    title = LocaleString.format("history." + node.getNodeName() + ".title");
                } else {
                    title = LocaleString.format(node.getNodeName() + ".title");
                }
                HisTreeNode treeNode = new HisTreeNode(title,
                        node.getNodeName(), null, null, true);
                nodeMap.put(node.getNodeName(), treeNode);
                if (!CollectionUtils.isEmpty(resultList)) {
                    for (HisDiagnosisResult result : resultList) {
                        if (node.getNodeName().equals(result.getPointName())) {
                            boolean isSuggestion = HisDiagnosisResult.ResultState.NO_ADVICE.equals(result.getIsHint());
                            HisTreeNode hisTreeNode =
                                    new HisTreeNode(result.getPointTitle(), node.getNodeName(), result.getPointType(),
                                            result.getPointState(), isSuggestion);
                            nodeMap.put(node.getNodeName(), hisTreeNode);
                        }
                    }
                }
            }
            HisTreeNode base = null;
            for (Node node : nodeList) {
                if ("0".equals(node.getParentNode())) {
                    base = nodeMap.get(node.getNodeName());
                } else {
                    nodeMap.get(node.getParentNode()).appendChild(nodeMap.get(node.getNodeName()));
                }
            }
            return base;
        } catch (IOException e) {
            throw new HisDiagnosisException("error:", e);
        }
    }

    private HisTreeNode refreshTreeNode(HisTreeNode treeNode) {
        List<HisTreeNode> list = treeNode.getChild();
        if (CollectionUtils.isEmpty(list)) {
            return treeNode;
        }
        int count = 0;
        for (HisTreeNode treeNode1 : list) {
            if (this.refreshTreeNode(treeNode1).getIsHidden()) {
                count++;
            }
        }
        boolean isTypeEmpty = CollectionUtils.isEmpty(list.stream().filter(
                f -> f.getPointType() != null && f.getPointType().equals(HisDiagnosisResult.PointType.DISPLAY)).collect(
                Collectors.toList()));
        if (count == list.size() && (treeNode.getPointType() != null && isTypeEmpty && !treeNode.getPointType().equals(
                HisDiagnosisResult.PointType.DIAGNOSIS))) {
            treeNode.setIsHidden(true);
        } else if (count != list.size() && isTypeEmpty && (treeNode.getPointType() != null
                && !treeNode.getPointType().equals(HisDiagnosisResult.PointType.DIAGNOSIS))) {
            treeNode.setIsHidden(false);
        }
        return treeNode;
    }

    private static synchronized Connection connectSqlite() {
        Connection conn;
        try {
            conn = DriverManager.getConnection(JDBC.PREFIX + "data/paramDiagnosisInfo.db");
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
        return conn;
    }
}