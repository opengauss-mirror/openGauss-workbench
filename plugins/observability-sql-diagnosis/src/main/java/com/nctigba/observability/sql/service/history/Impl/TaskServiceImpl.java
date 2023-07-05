/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.Impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.history.HisThresholdMapper;
import com.nctigba.observability.sql.model.history.DataStoreConfig;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.dto.HisDiagnosisTaskDTO;
import com.nctigba.observability.sql.model.history.dto.HisThresholdDTO;
import com.nctigba.observability.sql.model.history.dto.OptionDTO;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.model.history.result.TaskState;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.TaskService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TaskServiceImpl
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private HisDiagnosisTaskMapper taskMapper;
    @Autowired
    private List<HisDiagnosisPointService<?>> pointServiceList;
    @Autowired
    private DataStoreService dataStoreService;
    @Autowired
    private HisThresholdMapper hisThresholdMapper;
    @Autowired
    private HisDiagnosisResultMapper resultMapper;


    @Override
    public Integer add(HisDiagnosisTaskDTO hisDiagnosisTaskDTO) {
        List<OptionQuery> optionDTOList = this.getOption();
        if (!CollectionUtils.isEmpty(hisDiagnosisTaskDTO.getConfigs())) {
            optionDTOList.forEach(f -> hisDiagnosisTaskDTO.getConfigs().forEach(g -> {
                if (g.getOption().equals(f.getOption())) {
                    f.setIsCheck(g.getIsCheck());
                }
            }));
        }
        List<HisDiagnosisThreshold> thresholdList = getThresholds(hisDiagnosisTaskDTO.getThresholds());
        HisDiagnosisTask hisDiagnosisTask = new HisDiagnosisTask();
        hisDiagnosisTask.setClusterId(hisDiagnosisTaskDTO.getClusterId());
        hisDiagnosisTask.setNodeId(hisDiagnosisTaskDTO.getNodeId());
        hisDiagnosisTask.setHisDataStartTime(hisDiagnosisTaskDTO.getHisDataStartTime());
        hisDiagnosisTask.setHisDataEndTime(hisDiagnosisTaskDTO.getHisDataEndTime());
        hisDiagnosisTask.setConfigs(optionDTOList);
        hisDiagnosisTask.setThresholds(thresholdList);
        hisDiagnosisTask.setState(TaskState.CREATE);
        hisDiagnosisTask.setRemarks("***Ready to start diagnosis***");
        hisDiagnosisTask.setTaskStartTime(new Date());
        taskMapper.insert(hisDiagnosisTask);
        return hisDiagnosisTask.getId();
    }

    @Override
    public void start(int taskId, HisDiagnosisTaskDTO taskDTO) {
        List<HisDiagnosisThreshold> thresholdList = getThresholds(taskDTO.getThresholds());
        HisDiagnosisTask task = taskMapper.selectById(taskId);
        task.setThresholds(thresholdList);
        task.setState(TaskState.WAITING);
        taskMapper.updateById(task);
        task.addRemarks("start running diagnosis");
        HashMap<CollectionItem<?>, Integer> hashMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (HisDiagnosisPointService<?> pointService : pointServiceList) {
            String pointName = getClassName(pointService);
            List<String> option = pointService.getOption();
            boolean isRun = isRun(option, taskDTO.getConfigs());
            List<CollectionItem<?>> params = pointService.getSourceDataKeys();
            boolean isDiagnosisType = task.getHisDataEndTime() != null && pointService.getDiagnosisType().equals(
                    DiagnosisTypeCommon.CURRENT);
            if (isRun || CollectionUtils.isEmpty(params) || isDiagnosisType) {
                if (!CollectionUtils.isEmpty(params)) {
                    sb.append(pointService).append(";");
                    HisDiagnosisResult result = new HisDiagnosisResult(task, pointName,
                            HisDiagnosisResult.PointState.NOT_ANALYZED, HisDiagnosisResult.ResultState.NO_ADVICE);
                    resultMapper.insert(result);
                }
                continue;
            }
            for (CollectionItem<?> param : params) {
                hashMap.merge(param, 1, Integer::sum);
            }
        }
        for (CollectionItem<?> item : hashMap.keySet()) {
            DataStoreConfig config = new DataStoreConfig();
            config.setCollectionItem(item);
            Object isExistData = item.queryData(task);
            Object itemData = item.queryData(task);
            if (isExistData == null) {
                pointServiceList.forEach(f -> {
                    if (!CollectionUtils.isEmpty(f.getSourceDataKeys())) {
                        f.getSourceDataKeys().forEach(g -> {
                            if (g == item && !sb.toString().contains(f.toString())) {
                                String pointName = getClassName(f);
                                sb.append(f).append(";");
                                HisDiagnosisResult result = new HisDiagnosisResult(task, pointName,
                                        HisDiagnosisResult.PointState.ABNORMAL,
                                        HisDiagnosisResult.ResultState.NO_ADVICE);
                                resultMapper.insert(result);
                            }
                        });
                    }
                });
                continue;
            } else {
                if (itemData.toString().contains("error")) {
                    pointServiceList.forEach(f -> {
                        if (!CollectionUtils.isEmpty(f.getSourceDataKeys())) {
                            f.getSourceDataKeys().forEach(g -> {
                                if (g == item && !sb.toString().contains(f.toString())) {
                                    String pointName = getClassName(f);
                                    sb.append(f).append(";");
                                    HisDiagnosisResult result = new HisDiagnosisResult(task, pointName,
                                            HisDiagnosisResult.PointState.ABNORMAL,
                                            HisDiagnosisResult.ResultState.NO_ADVICE);
                                    result.setPointSuggestion(itemData.toString());
                                    resultMapper.insert(result);
                                }
                            });
                        }
                    });
                    continue;
                }
            }
            String itemName = getClassName(item);
            task.addRemarks("start collection " + itemName);
            config.setCollectionData(item.collectData(task));
            task.addRemarks("stop collection " + itemName);
            config.setCount(hashMap.get(item));
            List<DataStoreConfig> list = new ArrayList<>();
            list.add(config);
            dataStoreService.storeData(list);
            List<CollectionItem<?>> itemList = dataStoreService.getCollectionItem();
            for (HisDiagnosisPointService<?> pointService : pointServiceList) {
                String pointName = getClassName(pointService);
                if (sb.toString().contains(pointService.toString())) {
                    continue;
                }
                boolean isDataReady = CollectionUtils.isEmpty(pointService.getSourceDataKeys()) || new HashSet<>(
                        itemList).containsAll(pointService.getSourceDataKeys());
                if (isDataReady) {
                    sb.append(pointService).append(";");
                    task.addRemarks("start analysis " + pointName);
                    AnalysisDTO analysisDTO = pointService.analysis(task, dataStoreService);
                    task.addRemarks("stop analysis " + pointName);
                    HisDiagnosisResult result = new HisDiagnosisResult(
                            task, analysisDTO, pointName, HisDiagnosisResult.PointState.NORMAL);
                    resultMapper.insert(result);
                }
            }
        }
        dataStoreService.clearData();
        task.addRemarks("finish diagnosis");
        task.setTaskEndTime(new Date());
        task.setSpan(task.getCost());
        task.setState(TaskState.FINISH);
        taskMapper.updateById(task);
    }

    @Override
    public List<OptionQuery> getOption() {
        List<OptionQuery> list = new ArrayList<>();
        for (HisDiagnosisPointService<?> pointService : pointServiceList) {
            List<String> options = pointService.getOption();
            if (options != null) {
                for (String option : options) {
                    OptionQuery optionDTO = new OptionQuery();
                    optionDTO.setOption(option);
                    optionDTO.setIsCheck(false);
                    optionDTO.setName(OptionCommon.valueOf(option).getName());
                    optionDTO.setSortNo(OptionCommon.valueOf(option).getSortNo());
                    if (CollectionUtils.isEmpty(
                            list.stream().filter(o -> o.getOption().equals(option)).collect(Collectors.toList()))) {
                        list.add(optionDTO);
                    }
                }
            }
        }
        return list;
    }

    private boolean isRun(List<String> options, List<OptionDTO> optionList) {
        // 1 option is empty,return false
        if (CollectionUtils.isEmpty(options)) {
            return false;
        } else {
            // 2 has options,only should run when all options are ture
            // -- 2.1 loop options,check is check or not.If checked then set option to true
            List<OptionDTO> isOption = new ArrayList<>();
            for (String option : options) {
                OptionDTO dto = new OptionDTO();
                dto.setOption(option);
                dto.setIsCheck(false);
                if (!CollectionUtils.isEmpty(optionList)) {
                    optionList.forEach(f -> {
                        if (option.equals(f.getOption()) && f.getIsCheck() != null) {
                            dto.setIsCheck(f.getIsCheck());
                        }
                    });
                }
                isOption.add(dto);
            }
            // -- 2.3 if all options true then return false else return true
            return isOption.stream().anyMatch(f -> !f.getIsCheck());
        }
    }

    private List<HisDiagnosisThreshold> getThresholds(List<HisThresholdDTO> dtoList) {
        List<HisDiagnosisThreshold> thresholdList = hisThresholdMapper.selectList(Wrappers.emptyWrapper());
        if (!CollectionUtils.isEmpty(dtoList)) {
            thresholdList.forEach(f -> dtoList.forEach(g -> {
                if (g.getThreshold().equals(f.getThreshold())) {
                    f.setThresholdValue(g.getThresholdValue());
                }
            }));
        }
        return thresholdList;
    }

    private String getClassName(Object object) {
        String className = object.getClass().getName();
        className = className.substring(className.lastIndexOf(".") + 1);
        return className;
    }
}
