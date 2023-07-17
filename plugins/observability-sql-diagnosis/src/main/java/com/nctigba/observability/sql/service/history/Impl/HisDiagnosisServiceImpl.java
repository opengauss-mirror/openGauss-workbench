/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.Impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.history.HisThresholdMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.result.HisTreeNode;
import com.nctigba.observability.sql.model.history.result.Node;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.HisDiagnosisService;
import com.nctigba.observability.sql.util.LocaleString;
import com.nctigba.observability.sql.util.PointUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    @Autowired
    private PointUtil pointUtil;

    @Override
    public HisTreeNode getTopologyMap(int taskId, boolean isAll) {
        List<HisDiagnosisResult> resultList = resultMapper.selectList(
                Wrappers.<HisDiagnosisResult>lambdaQuery().eq(HisDiagnosisResult::getTaskId, taskId));
        HisTreeNode treeNode = this.createHisTreeNode(resultList);
        HisTreeNode hisTreeNode = this.refreshTreeNode(treeNode);
        hisTreeNode.setIsHidden(false);
        return hisTreeNode;
    }

    @Override
    public Object getNodeDetail(int taskId, String pointName) {
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

    @Override
    public Object getAllPoint(int taskId) {
        return resultMapper.selectList(
                Wrappers.<HisDiagnosisResult>lambdaQuery().eq(HisDiagnosisResult::getTaskId, taskId));
    }

    private HisTreeNode createHisTreeNode(List<HisDiagnosisResult> resultList) {
        try (InputStream is = this.getClass().getResourceAsStream("/treeNode.txt");
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
                node.setNodeName(list.get(i).replace("-", ""));
                for (int j = i; j >= 0; j--) {
                    int iDepth = list.get(i).split("-").length;
                    int jDepth = list.get(j).split("-").length;
                    String parentNode = list.get(j).replace("-", "");
                    if (iDepth == 2) {
                        node.setParentNode("0");
                        break;
                    } else if (iDepth == 3) {
                        if (jDepth == 2) {
                            node.setParentNode(parentNode);
                            break;
                        }
                    } else if (iDepth == 4) {
                        if (jDepth == 3) {
                            node.setParentNode(parentNode);
                            break;
                        }
                    } else if (iDepth == 5) {
                        if (jDepth == 4) {
                            node.setParentNode(parentNode);
                            break;
                        }
                    } else if (iDepth == 6) {
                        if (jDepth == 5) {
                            node.setParentNode(parentNode);
                            break;
                        }
                    }
                }
                nodeList.add(node);
            }
            var nodeMap = new HashMap<String, HisTreeNode>();
            for (Node node : nodeList) {
                HisTreeNode treeNode = new HisTreeNode(LocaleString.format("history." + node.getNodeName() + ".title"),
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
}
