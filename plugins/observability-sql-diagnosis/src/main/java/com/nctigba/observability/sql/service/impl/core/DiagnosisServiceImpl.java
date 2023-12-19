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
 *  DiagnosisServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/core/DiagnosisServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.core;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.constant.DiagnosisTypeConstants;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.DiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.HisThresholdMapper;
import com.nctigba.observability.sql.model.dto.TreeNodeDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.vo.TreeNodeVO;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.DiagnosisService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DiagnosisServiceImpl
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Slf4j
@Service
public class DiagnosisServiceImpl implements DiagnosisService {
    @Autowired
    private DiagnosisResultMapper resultMapper;
    @Autowired
    private List<DiagnosisPointService<?>> pointServiceList;
    @Autowired
    private LocaleStringUtils localeToString;
    @Autowired
    private HisThresholdMapper hisThresholdMapper;
    @Autowired
    private DiagnosisTaskMapper taskMapper;

    @Override
    public TreeNodeDTO getTopologyMap(int taskId, boolean isAll, String diagnosisType) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        List<DiagnosisResultDO> resultList = resultMapper.selectList(
                Wrappers.<DiagnosisResultDO>lambdaQuery().eq(DiagnosisResultDO::getTaskId, taskId));
        TreeNodeDTO treeNodeDTO = this.createTreeNode(resultList, task);
        TreeNodeDTO hisTreeNodeDTO = this.refreshTreeNode(treeNodeDTO);
        hisTreeNodeDTO.setIsHidden(false);
        return hisTreeNodeDTO;
    }

    @Override
    public Object getNodeDetail(int taskId, String pointName, String diagnosisType) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new HisDiagnosisException("taskId is not exists!");
        }
        DiagnosisResultDO result = resultMapper.selectOne(
                Wrappers.<DiagnosisResultDO>lambdaQuery().eq(DiagnosisResultDO::getPointName, pointName).eq(
                        DiagnosisResultDO::getTaskId, taskId));
        if (result == null) {
            return "No data found!";
        }
        Object object = null;
        for (DiagnosisPointService<?> pointService : pointServiceList) {
            String className = pointService.getClass().getName();
            if (className.substring(className.lastIndexOf(".") + 1).equals(pointName)) {
                object = pointService.getShowData(taskId);
            }
        }
        if (result.getPointData() == null) {
            result.setData(object);
        } else {
            // 2023.11.27 Latest Unified Return AutoShowDataVO
            String stringData = result.getPointData().toString();
            if (stringData.contains("autoShowData")) {
                result.setData(result.getPointData());
            } else {
                List<Object> objectList = new ArrayList<>();
                if (object != null) {
                    objectList.add(object);
                }
                objectList.add(result.getPointData());
                result.setData(objectList);
            }
        }
        HashMap<String, String> map = getAllThreshold(task.getThresholds());
        DiagnosisResultDO toResult = localeToString.trapLanguage(result);
        List<DiagnosisThresholdDO> thresholds = hisThresholdMapper.selectList(Wrappers.emptyWrapper());
        for (DiagnosisThresholdDO threshold : thresholds) {
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

    private HashMap<String, String> getAllThreshold(List<?> thresholdValue) {
        HashMap<String, String> map = new HashMap<>();
        for (Object ob : thresholdValue) {
            if (ob instanceof LinkedHashMap) {
                LinkedHashMap<String, String> hashMap = (LinkedHashMap<String, String>) ob;
                map.put(hashMap.get("threshold"), hashMap.get("thresholdValue"));
            }
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        return map;
    }

    private TreeNodeDTO createTreeNode(List<DiagnosisResultDO> resultList, DiagnosisTaskDO task) {
        List<String> list = new ArrayList<>();
        String[] nodeLines = task.getTopologyMap().split(System.getProperty("line.separator"));
        for (String lines : nodeLines) {
            String[] line = lines.split(" ");
            list.add(line[0]);
        }
        List<TreeNodeVO> treeNodeVOList = getTreeNode(list);
        var nodeMap = new HashMap<String, TreeNodeDTO>();
        for (TreeNodeVO treeNodeVO : treeNodeVOList) {
            String title;
            if (DiagnosisTypeConstants.HISTORY.equals(task.getDiagnosisType())) {
                title = LocaleStringUtils.format("history." + treeNodeVO.getNodeName() + ".title");
            } else {
                title = LocaleStringUtils.format(treeNodeVO.getNodeName() + ".title");
            }
            TreeNodeDTO initNode = new TreeNodeDTO(title,
                    treeNodeVO.getNodeName(), null, null, true);
            nodeMap.put(treeNodeVO.getNodeName(), initNode);
            if (!CollectionUtils.isEmpty(resultList)) {
                resultList.forEach(f -> {
                    if (treeNodeVO.getNodeName().equals(f.getPointName())) {
                        boolean isSuggestion = DiagnosisResultDO.ResultState.NO_ADVICE.equals(f.getIsHint());
                        TreeNodeDTO treeNodeDTO =
                                new TreeNodeDTO(f.getPointTitle(), treeNodeVO.getNodeName(), f.getPointType(),
                                        f.getPointState(), isSuggestion);
                        nodeMap.put(treeNodeVO.getNodeName(), treeNodeDTO);
                    }
                });
            }
        }
        TreeNodeDTO base = null;
        for (TreeNodeVO treeNodeVO : treeNodeVOList) {
            if ("0".equals(treeNodeVO.getParentNode())) {
                base = nodeMap.get(treeNodeVO.getNodeName());
            } else {
                nodeMap.get(treeNodeVO.getParentNode()).appendChild(nodeMap.get(treeNodeVO.getNodeName()));
            }
        }
        return base;
    }

    private List<TreeNodeVO> getTreeNode(List<String> list) {
        List<TreeNodeVO> treeNodeVOList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            TreeNodeVO treeNodeVO = new TreeNodeVO();
            for (int j = i; j >= 0; j--) {
                int iDepth = list.get(i).split("-").length;
                int jDepth = list.get(j).split("-").length;
                String parentNode = list.get(j).replace("-", "");
                if (iDepth == 2) {
                    treeNodeVO.setParentNode("0");
                    break;
                } else if (jDepth == iDepth - 1) {
                    treeNodeVO.setParentNode(parentNode);
                    break;
                }
            }
            String nodeName = list.get(i).replace("-", "");
            treeNodeVO.setNodeName(nodeName);
            treeNodeVOList.add(treeNodeVO);
        }
        return treeNodeVOList;
    }

    private TreeNodeDTO refreshTreeNode(TreeNodeDTO treeNodeDTO) {
        List<TreeNodeDTO> list = treeNodeDTO.getChild();
        if (CollectionUtils.isEmpty(list)) {
            return treeNodeDTO;
        }
        int count = 0;
        for (TreeNodeDTO treeNodeDTO1 : list) {
            if (this.refreshTreeNode(treeNodeDTO1).getIsHidden()) {
                count++;
            }
        }
        boolean isTypeEmpty = CollectionUtils.isEmpty(list.stream().filter(
                f -> f.getPointType() != null && f.getPointType().equals(DiagnosisResultDO.PointType.DISPLAY)).collect(
                Collectors.toList()));
        if (count == list.size() && (treeNodeDTO.getPointType() != null && isTypeEmpty
                && !treeNodeDTO.getPointType().equals(
                DiagnosisResultDO.PointType.DIAGNOSIS))) {
            treeNodeDTO.setIsHidden(true);
        } else if (count != list.size() && isTypeEmpty && (treeNodeDTO.getPointType() != null
                && !treeNodeDTO.getPointType().equals(DiagnosisResultDO.PointType.DIAGNOSIS))) {
            treeNodeDTO.setIsHidden(false);
        } else {
            log.info("Continue to the next cycle!");
        }
        return treeNodeDTO;
    }
}