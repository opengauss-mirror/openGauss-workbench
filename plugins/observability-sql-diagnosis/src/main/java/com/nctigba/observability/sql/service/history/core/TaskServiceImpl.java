/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.core;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CollectionTypeCommon;
import com.nctigba.observability.sql.constants.history.DiagnosisTypeCommon;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.PointTypeCommon;
import com.nctigba.observability.sql.constants.history.SqlCommon;
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
import com.nctigba.observability.sql.model.history.query.TaskQuery;
import com.nctigba.observability.sql.model.history.result.TaskState;
import com.nctigba.observability.sql.model.param.DatabaseParamData;
import com.nctigba.observability.sql.model.param.OsParamData;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.TaskService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.EbpfCollectionItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.sqlite.JDBC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TaskServiceImpl
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Slf4j
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
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    @Lazy
    private SqlExecutor sqlExecutor;
    @Autowired
    private List<EbpfCollectionItem> ebpfItemList;

    @Override
    public Integer add(HisDiagnosisTaskDTO taskDTO) {
        String clusterId = clusterManager.getOpsClusterIdByNodeId(taskDTO.getNodeId());
        taskDTO.setClusterId(clusterId);
        List<OptionQuery> optionDTOList = this.getOption(taskDTO.getDiagnosisType());
        if (!CollectionUtils.isEmpty(taskDTO.getConfigs())) {
            optionDTOList.forEach(f -> taskDTO.getConfigs().forEach(g -> {
                if (g.getOption().equals(f.getOption())) {
                    f.setIsCheck(g.getIsCheck());
                }
            }));
        }
        optionDTOList.sort(Comparator.comparing(OptionQuery::getSortNo));
        List<HisDiagnosisThreshold> thresholdList = getThresholds(taskDTO.getDiagnosisType(), taskDTO.getThresholds());
        HisDiagnosisTask hisDiagnosisTask = new HisDiagnosisTask(taskDTO, optionDTOList, thresholdList);
        taskMapper.insert(hisDiagnosisTask);
        return hisDiagnosisTask.getId();
    }

    private void initDiagnosisData(HisDiagnosisTask task) {
        List<HisDiagnosisResult> resultList = new ArrayList<>();
        HashMap<String, String> pointMap = obtainAllPoint(task.getDiagnosisType());
        for (String point : pointMap.keySet()) {
            String pointType = pointMap.get(point);
            HisDiagnosisResult initData = new HisDiagnosisResult(task, point,
                    Enum.valueOf(HisDiagnosisResult.PointType.class, pointType),
                    HisDiagnosisResult.PointState.INITIALIZE, HisDiagnosisResult.ResultState.NO_ADVICE);
            resultList.add(initData);
        }
        if (DiagnosisTypeCommon.SQL.equals(task.getDiagnosisType())) {
            String selectSql = "select * from param_info;";
            try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
                 ResultSet result = statement.executeQuery(selectSql)) {
                while (result.next()) {
                    String paramName = result.getString(3);
                    String nodeName = initParamName(paramName);
                    HisDiagnosisResult initData = new HisDiagnosisResult(task, nodeName,
                            HisDiagnosisResult.PointState.INITIALIZE, HisDiagnosisResult.ResultState.NO_ADVICE);
                    resultList.add(initData);
                }
            } catch (SQLException e) {
                throw new HisDiagnosisException("error:", e);
            }
        }
        resultMapper.batchInert(resultList);
    }

    private String initParamName(String paramName) {
        DatabaseParamData[] dbFields = DatabaseParamData.values();
        String nodeName = null;
        for (DatabaseParamData dbField : dbFields) {
            if (dbField.getParamName().equals(paramName)) {
                nodeName = dbField.toString();
            }
        }
        OsParamData[] osFields = OsParamData.values();
        for (OsParamData osField : osFields) {
            if (osField.getParamName().equals(paramName)) {
                nodeName = osField.toString();
            }
        }
        return nodeName;
    }

    @Override
    public void start(int taskId) {
        HisDiagnosisTask task = taskMapper.selectById(taskId);
        task.setTaskStartTime(new Date());
        task.addRemarks("start running diagnosis");
        task.setState(TaskState.WAITING);
        taskMapper.updateById(task);
        initDiagnosisData(task);
        List<OptionQuery> options = databaseOption(task);
        List<HisDiagnosisThreshold> thresholds = databaseThreshold(task);
        task.setThresholds(thresholds);
        if (DiagnosisTypeCommon.SQL.equals(task.getDiagnosisType())) {
            boolean isBcc = bccBeforeStart(task);
            task.addRemarks("bcc diagnosis:" + isBcc);
            boolean isExist = options.stream().anyMatch(f -> OptionCommon.IS_BCC.toString().equals(f.getOption()));
            if (isExist) {
                options.forEach(f -> {
                    if (OptionCommon.IS_BCC.toString().equals(f.getOption())) {
                        f.setIsCheck(isBcc);
                    }
                });
            } else {
                OptionQuery query = new OptionQuery();
                query.setOption(String.valueOf(OptionCommon.IS_BCC));
                query.setIsCheck(isBcc);
                options.add(query);
            }
            ThreadUtil.execAsync(() -> sqlExecutor.executeSql(task));
            ThreadUtil.sleep(500L);
            task.addRemarks("execute SQL while starting diagnosis");
            Integer pid = obtainPid(task);
            task.addRemarks("get pid:" + pid);
            task.setPid(pid);
            setDebugQueryId(task);
            taskMapper.updateById(task);
        }
        HashMap<String, String> pointMap = obtainAllPoint(task.getDiagnosisType());
        List<String> pointList = new ArrayList<>(pointMap.keySet());
        StringBuilder sb = new StringBuilder();
        HashMap<CollectionItem<?>, Integer> hashMap = new HashMap<>();
        for (HisDiagnosisPointService<?> pointService : pointServiceList) {
            String pointName = getClassName(pointService);
            if (!pointList.contains(pointName)) {
                continue;
            }
            List<String> option = pointService.getOption();
            boolean isRun = isRun(option, options);
            List<CollectionItem<?>> params = pointService.getSourceDataKeys();
            boolean isDiagnosisType = false;
            if (DiagnosisTypeCommon.HISTORY.equals(task.getDiagnosisType())) {
                isDiagnosisType = task.getHisDataEndTime() != null && pointService.getDiagnosisType().equals(
                        PointTypeCommon.CURRENT);
            }
            if (isRun || CollectionUtils.isEmpty(params) || isDiagnosisType) {
                if (!CollectionUtils.isEmpty(params)) {
                    sb.append(pointService).append(";");
                    boolean isBcc = options.stream().anyMatch(f -> {
                        if (OptionCommon.IS_BCC.toString().equals(f.getOption())) {
                            return f.getIsCheck();
                        }
                        return false;
                    });
                    HisDiagnosisResult.PointState state;
                    if (option.contains(OptionCommon.IS_BCC.toString()) && isBcc) {
                        state = HisDiagnosisResult.PointState.NOT_SATISFIED_DIAGNOSIS;
                    } else {
                        state = HisDiagnosisResult.PointState.NOT_MATCH_OPTION;
                    }
                    HisDiagnosisResult result = new HisDiagnosisResult(task, pointName,
                            state, HisDiagnosisResult.ResultState.NO_ADVICE);
                    resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                    HisDiagnosisResult::getTaskId, result.getTaskId())
                            .eq(HisDiagnosisResult::getPointName, pointName));
                    if ("OsParam".equals(pointName) || "DatabaseParam".equals(pointName)) {
                        updateParamChildNode(task);
                    }
                }
                continue;
            }
            for (CollectionItem<?> param : params) {
                hashMap.merge(param, 1, Integer::sum);
            }
        }
        task.addRemarks("start analysis ...");
        taskMapper.updateById(task);
        ExecutorService executor = ThreadUtil.newExecutor();
        for (CollectionItem<?> item : hashMap.keySet()) {
            if (CollectionTypeCommon.BEFORE.equals(item.getCollectionType()) || CollectionTypeCommon.AFTER.equals(
                    item.getCollectionType())) {
                boolean isBcc = pointServiceList.stream().anyMatch(
                        f -> !CollectionUtils.isEmpty(f.getOption()) && f.getOption().contains(
                                OptionCommon.IS_BCC.toString()) && f.getSourceDataKeys().contains(
                                item) && !sb.toString().contains(f.toString()));
                if (isBcc) {
                    try {
                        item.collectData(task);
                    } catch (Exception e) {
                        task.addRemarks("call ebpf error:" + e);
                        taskMapper.updateById(task);
                    }
                }
                continue;
            }
            String itemName = getClassName(item);
            executor.execute(() -> {
                task.addRemarks("start check collection " + itemName);
                Object isExistData = item.queryData(task);
                task.addRemarks("stop check collection " + itemName);
                taskMapper.updateById(task);
                if (isExistData == null) {
                    pointServiceList.forEach(f -> {
                        if (!CollectionUtils.isEmpty(f.getSourceDataKeys())) {
                            f.getSourceDataKeys().forEach(g -> {
                                if (g == item && !sb.toString().contains(f.toString())) {
                                    String pointName = getClassName(f);
                                    sb.append(f).append(";");
                                    HisDiagnosisResult result = new HisDiagnosisResult(task, pointName,
                                            HisDiagnosisResult.PointState.NOT_HAVE_DATA,
                                            HisDiagnosisResult.ResultState.NO_ADVICE);
                                    resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                                    HisDiagnosisResult::getTaskId, result.getTaskId())
                                            .eq(HisDiagnosisResult::getPointName, pointName));
                                }
                            });
                        }
                    });
                } else {
                    if (isExistData.toString().contains("error")) {
                        pointServiceList.forEach(f -> {
                            if (!CollectionUtils.isEmpty(f.getSourceDataKeys())) {
                                f.getSourceDataKeys().forEach(g -> {
                                    if (g == item && !sb.toString().contains(f.toString())) {
                                        String pointName = getClassName(f);
                                        sb.append(f).append(";");
                                        HisDiagnosisResult result = new HisDiagnosisResult(task, pointName,
                                                HisDiagnosisResult.PointState.COLLECT_EXCEPTION,
                                                HisDiagnosisResult.ResultState.NO_ADVICE);
                                        result.setPointSuggestion(isExistData.toString());
                                        resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                                        HisDiagnosisResult::getTaskId, result.getTaskId())
                                                .eq(HisDiagnosisResult::getPointName, pointName));
                                    }
                                });
                            }
                        });
                    }
                }
                DataStoreConfig config = new DataStoreConfig();
                config.setCollectionItem(item);
                task.addRemarks("start collection " + itemName);
                config.setCollectionData(item.collectData(task));
                task.addRemarks("stop collection " + itemName);
                taskMapper.updateById(task);
                config.setCount(hashMap.get(item));
                List<DataStoreConfig> list = new ArrayList<>();
                list.add(config);
                dataStoreService.storeData(list);
                List<CollectionItem<?>> itemList = dataStoreService.getCollectionItem();
                taskMapper.updateById(task);
                for (HisDiagnosisPointService<?> pointService : pointServiceList) {
                    String pointName = getClassName(pointService);
                    if (!pointList.contains(pointName)) {
                        continue;
                    }
                    if (sb.toString().contains(pointService.toString())) {
                        continue;
                    }
                    if (!CollectionUtils.isEmpty(pointService.getOption()) && pointService.getOption().contains(
                            OptionCommon.IS_BCC.toString())) {
                        continue;
                    }
                    List<CollectionItem<?>> collectionItemList = pointService.getSourceDataKeys();
                    boolean isDataReady =
                            CollectionUtils.isEmpty(collectionItemList) || new HashSet<>(itemList).containsAll(
                                    collectionItemList);
                    if (isDataReady) {
                        sb.append(pointService).append(";");
                        task.addRemarks("start analysis " + pointName);
                        try {
                            AnalysisDTO analysisDTO = pointService.analysis(task, dataStoreService);
                            task.addRemarks("stop analysis " + pointName);
                            if (!"DatabaseParam".equals(pointName)) {
                                HisDiagnosisResult result = new HisDiagnosisResult(
                                        task, analysisDTO, pointName, HisDiagnosisResult.PointState.SUCCEED);
                                resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                                HisDiagnosisResult::getTaskId, result.getTaskId())
                                        .eq(HisDiagnosisResult::getPointName, pointName));
                            }
                        } catch (Exception e) {
                            task.addRemarks("stop analysis " + pointName);
                            HisDiagnosisResult result = new HisDiagnosisResult(task, pointName,
                                    HisDiagnosisResult.PointState.ANALYSIS_EXCEPTION,
                                    HisDiagnosisResult.ResultState.NO_ADVICE);
                            resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                            HisDiagnosisResult::getTaskId, result.getTaskId())
                                    .eq(HisDiagnosisResult::getPointName, pointName));
                        }
                    }
                }
            });
        }
        executor.shutdown();
        try {
            boolean isFinish = executor.awaitTermination(5, TimeUnit.MINUTES);
            if (isFinish) {
                task.addRemarks("analysis finish");
            }
        } catch (InterruptedException e) {
            throw new HisDiagnosisException("Exception:" + e);
        }
        dataStoreService.clearData();
        if (DiagnosisTypeCommon.HISTORY.equals(task.getDiagnosisType())) {
            task.setState(TaskState.FINISH);
        } else {
            task.setState(checkDiagnosisResult(task));
        }
        task.addRemarks("finish diagnosis");
        task.setTaskEndTime(new Date());
        task.setSpan(task.getCost());
        taskMapper.updateById(task);
    }

    private void updateParamChildNode(HisDiagnosisTask task) {
        String selectSql = "select * from param_info;";
        List<String> list = new ArrayList<>();
        try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
             ResultSet result = statement.executeQuery(selectSql)) {
            while (result.next()) {
                String type = result.getString(2);
                String paramName = result.getString(3);
                String pointName = null;
                if ("OS".equals(type)) {
                    OsParamData[] osFields = OsParamData.values();
                    for (OsParamData osField : osFields) {
                        if (osField.getParamName().equals(paramName)) {
                            pointName = osField.toString();
                        }
                    }
                } else {
                    DatabaseParamData[] dbFields = DatabaseParamData.values();
                    for (DatabaseParamData dbField : dbFields) {
                        if (dbField.getParamName().equals(paramName)) {
                            pointName = dbField.toString();
                        }
                    }
                }
                list.add(pointName);
            }
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
        for (String pointName : list) {
            HisDiagnosisResult result = new HisDiagnosisResult(task, pointName,
                    HisDiagnosisResult.PointState.NOT_MATCH_OPTION, HisDiagnosisResult.ResultState.NO_ADVICE);
            resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                            HisDiagnosisResult::getTaskId, result.getTaskId())
                    .eq(HisDiagnosisResult::getPointName, pointName));
        }
    }

    private List<OptionQuery> databaseOption(HisDiagnosisTask task) {
        List<OptionQuery> options = new ArrayList<>();
        List<?> objects = task.getConfigs();
        for (Object ob : objects) {
            if (ob instanceof LinkedHashMap) {
                LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>) ob;
                OptionQuery optionQuery = new OptionQuery();
                Object optionObj = hashMap.get("option");
                if (optionObj instanceof String) {
                    optionQuery.setOption((String) optionObj);
                }
                Object checkObj = hashMap.get("isCheck");
                if (checkObj instanceof Boolean) {
                    optionQuery.setIsCheck((Boolean) checkObj);
                }
                options.add(optionQuery);
            }
        }
        return options;
    }

    private List<HisDiagnosisThreshold> databaseThreshold(HisDiagnosisTask task) {
        List<HisDiagnosisThreshold> thresholds = new ArrayList<>();
        List<?> objects = task.getThresholds();
        for (Object ob : objects) {
            if (ob instanceof LinkedHashMap) {
                LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>) ob;
                HisDiagnosisThreshold threshold = new HisDiagnosisThreshold();
                Object thresholdObj = hashMap.get("threshold");
                if (thresholdObj instanceof String) {
                    threshold.setThreshold((String) thresholdObj);
                }
                Object valueObj = hashMap.get("thresholdValue");
                if (valueObj instanceof String) {
                    threshold.setThresholdValue((String) valueObj);
                }
                Object nameObj = hashMap.get("thresholdName");
                if (nameObj instanceof String) {
                    threshold.setThresholdName((String) nameObj);
                }
                Object detailObj = hashMap.get("thresholdDetail");
                if (detailObj instanceof String) {
                    threshold.setThresholdDetail((String) detailObj);
                }
                Object typeObj = hashMap.get("thresholdType");
                if (typeObj instanceof String) {
                    threshold.setThresholdType((String) typeObj);
                }
                Object idObj = hashMap.get("id");
                if (idObj instanceof Integer) {
                    threshold.setId((Integer) idObj);
                }
                Object clusterIdObj = hashMap.get("clusterId");
                if (clusterIdObj instanceof String) {
                    threshold.setClusterId((String) clusterIdObj);
                }
                Object nodeIdObj = hashMap.get("nodeId");
                if (nodeIdObj instanceof String) {
                    threshold.setNodeId((String) nodeIdObj);
                }
                Object unitObj = hashMap.get("thresholdUnit");
                if (unitObj instanceof String) {
                    threshold.setThresholdUnit((String) unitObj);
                }
                thresholds.add(threshold);
            }
        }
        return thresholds;
    }

    @Override
    public List<OptionQuery> getOption(String diagnosisType) {
        HashMap<String, String> pointMap = obtainAllPoint(diagnosisType);
        List<String> pointList = new ArrayList<>(pointMap.keySet());
        List<OptionQuery> list = new ArrayList<>();
        for (HisDiagnosisPointService<?> pointService : pointServiceList) {
            String pointName = getClassName(pointService);
            if (!pointList.contains(pointName)) {
                continue;
            }
            List<String> options = pointService.getOption();
            if (!CollectionUtils.isEmpty(options)) {
                for (String option : options) {
                    if (OptionCommon.IS_BCC.toString().equals(option)) {
                        continue;
                    }
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
        list.sort(Comparator.comparing(OptionQuery::getSortNo));
        return list;
    }

    @Override
    public IPage<HisDiagnosisTask> selectByPage(TaskQuery query) {
        return taskMapper.selectPage(
                query.iPage(),
                Wrappers.lambdaQuery(HisDiagnosisTask.class).eq(StringUtils.isNotBlank(query.getClusterId()),
                        HisDiagnosisTask::getClusterId, query.getClusterId()).eq(
                        StringUtils.isNotBlank(query.getNodeId()), HisDiagnosisTask::getNodeId, query.getNodeId()).eq(
                        StringUtils.isNotBlank(query.getDbName()), HisDiagnosisTask::getDbName, query.getDbName()).eq(
                        StringUtils.isNotBlank(query.getSqlId()), HisDiagnosisTask::getSqlId, query.getSqlId()).eq(
                        StringUtils.isNotBlank(query.getDiagnosisType()), HisDiagnosisTask::getDiagnosisType,
                        query.getDiagnosisType()).and(
                        StringUtils.isNotBlank(query.getTaskName()),
                        c -> c.like(HisDiagnosisTask::getTaskName, query.getTaskName()).or().like(
                                HisDiagnosisTask::getSql, query.getTaskName().replaceAll(" ", "%"))).ge(
                        query.getStartTime() != null, HisDiagnosisTask::getCreateTime, query.getStartTime()).le(
                        query.getEndTime() != null, HisDiagnosisTask::getCreateTime, query.getEndTime()).orderByDesc(
                        HisDiagnosisTask::getId));
    }

    @Override
    public void delete(int taskId) {
        taskMapper.deleteById(taskId);
    }

    @Override
    public void explainAfter(HisDiagnosisTask task, ArrayList<String> rsList) {
        task.addRemarks("***start analysis explain***");
        List<OptionQuery> options = databaseOption(task);
        boolean isExplain = explainBeforeStart(task) || task.getSessionId() == 0L;
        for (HisDiagnosisPointService<?> pointService : pointServiceList) {
            List<String> option = pointService.getOption();
            boolean isRun = isRun(option, options);
            if (!CollectionUtils.isEmpty(option) && OptionCommon.IS_EXPLAIN.toString().equals(option.get(0))) {
                if (!isExplain) {
                    updateExplainChildNode(task, HisDiagnosisResult.PointState.NOT_SATISFIED_DIAGNOSIS);
                } else if (isRun) {
                    updateExplainChildNode(task, HisDiagnosisResult.PointState.NOT_MATCH_OPTION);
                } else {
                    DataStoreConfig config = new DataStoreConfig();
                    config.setCollectionItem(pointService.getSourceDataKeys().get(0));
                    config.setCollectionData(rsList);
                    config.setCount(1);
                    List<DataStoreConfig> list = new ArrayList<>();
                    list.add(config);
                    dataStoreService.storeData(list);
                    updateExplainChildNode(task, HisDiagnosisResult.PointState.SUCCEED);
                    pointService.analysis(task, dataStoreService);
                }
            }
        }
        task.addRemarks("***end analysis explain***");
        task.setState(checkDiagnosisResult(task));
        task.setTaskEndTime(new Date());
        task.setSpan(task.getCost());
        taskMapper.updateById(task);
    }

    private void updateExplainChildNode(HisDiagnosisTask task, HisDiagnosisResult.PointState state) {
        String[] pointNames = {"ObjectInfoCheck", "ObjectRecommendedToUpdateStatistics",
                "PlanRecommendedToCreateIndex",
                "PlanChangedToPartitionTable", "PlanRecommendedToQueryBasedOnPartition",
                "PlanRecommendedToDoVacuumCleaning",
                "PlanRecommendedToOptimizeStatementsOrAddWorkMemSize"};
        for (String pointName : pointNames) {
            HisDiagnosisResult result = new HisDiagnosisResult(
                    task, pointName, state, HisDiagnosisResult.ResultState.NO_ADVICE);
            resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                            HisDiagnosisResult::getTaskId, result.getTaskId())
                    .eq(HisDiagnosisResult::getPointName, pointName));
        }
    }

    @Override
    public void bccResult(String taskId, String pointName, MultipartFile file) {
        HisDiagnosisTask task = null;
        if (StringUtils.isNumeric(taskId)) {
            task = taskMapper.selectById(taskId);
        }
        if (task == null) {
            return;
        }
        task.addRemarks("received pointName:" + pointName);
        taskMapper.updateById(task);
        List<DataStoreConfig> list = new ArrayList<>();
        DataStoreConfig config = generateCollectData(task.getDiagnosisType(), pointName, file);
        list.add(config);
        task.addRemarks("collectionItem:" + list.get(0).getCollectionItem().getClass().getName());
        dataStoreService.storeData(list);
        long size = file == null || file.isEmpty() ? 0 : file.getSize();
        log.info("bccTest:" + pointName + ":" + size);
        for (HisDiagnosisPointService<?> pointService : pointServiceList) {
            String className = getClassName(pointService);
            if (!CollectionUtils.isEmpty(pointService.getSourceDataKeys())
                    && pointService.getSourceDataKeys().contains(config.getCollectionItem())) {
                if (size > 0) {
                    try {
                        AnalysisDTO analysisDTO = pointService.analysis(task, dataStoreService);
                        if (analysisDTO.getPointData() != null && "osParam".equals(analysisDTO.getPointData())) {
                            continue;
                        }
                        HisDiagnosisResult result = new HisDiagnosisResult(
                                task, analysisDTO, className, HisDiagnosisResult.PointState.SUCCEED);
                        resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                        HisDiagnosisResult::getTaskId, result.getTaskId())
                                .eq(HisDiagnosisResult::getPointName, className));
                    } catch (Exception e) {
                        HisDiagnosisResult result = new HisDiagnosisResult(task, className,
                                HisDiagnosisResult.PointState.ANALYSIS_EXCEPTION,
                                HisDiagnosisResult.ResultState.NO_ADVICE);
                        resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                        HisDiagnosisResult::getTaskId, result.getTaskId())
                                .eq(HisDiagnosisResult::getPointName, className));
                    }
                } else {
                    HisDiagnosisResult result = new HisDiagnosisResult(task, className,
                            HisDiagnosisResult.PointState.NOT_HAVE_DATA, HisDiagnosisResult.ResultState.NO_ADVICE);
                    resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                                    HisDiagnosisResult::getTaskId, result.getTaskId())
                            .eq(HisDiagnosisResult::getPointName, className));
                    if ("osParam".equals(pointName)) {
                        updateOsParamChildNode(task);
                    }
                }
            }
        }
        task.addRemarks("bcc filesize:" + size + ",finish analysis::" + pointName);
        task.setState(checkDiagnosisResult(task));
        task.setTaskEndTime(new Date());
        task.setSpan(task.getCost());
        taskMapper.updateById(task);
    }

    private void updateOsParamChildNode(HisDiagnosisTask task) {
        String selectSql = "select * from param_info where paramType='OS';";
        List<String> list = new ArrayList<>();
        try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
             ResultSet result = statement.executeQuery(selectSql)) {
            while (result.next()) {
                String paramName = result.getString(3);
                String pointName = null;
                OsParamData[] osFields = OsParamData.values();
                for (OsParamData osField : osFields) {
                    if (osField.getParamName().equals(paramName)) {
                        pointName = osField.toString();
                    }
                }
                list.add(pointName);
            }
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
        for (String pointName : list) {
            HisDiagnosisResult result = new HisDiagnosisResult(task, pointName,
                    HisDiagnosisResult.PointState.NOT_HAVE_DATA, HisDiagnosisResult.ResultState.NO_ADVICE);
            resultMapper.update(result, Wrappers.<HisDiagnosisResult>lambdaQuery().eq(
                            HisDiagnosisResult::getTaskId, result.getTaskId())
                    .eq(HisDiagnosisResult::getPointName, pointName));
        }
    }

    private DataStoreConfig generateCollectData(String diagnosisType, String pointName, MultipartFile file) {
        int itemCount = 0;
        DataStoreConfig config = new DataStoreConfig();
        ebpfItemList.forEach(f -> {
            if (f.getHttpParam().equals(pointName)) {
                config.setCollectionItem(f);
                config.setCollectionData(file);
            }
        });
        HashMap<String, String> pointMap = obtainAllPoint(diagnosisType);
        List<String> pointList = new ArrayList<>(pointMap.keySet());
        for (HisDiagnosisPointService<?> pointService : pointServiceList) {
            if (!pointList.contains(getClassName(pointService))) {
                continue;
            }
            List<CollectionItem<?>> params = pointService.getSourceDataKeys();
            if (!CollectionUtils.isEmpty(params) && params.contains(config.getCollectionItem())) {
                itemCount++;
            }
        }
        config.setCount(itemCount);
        return config;
    }

    private TaskState checkDiagnosisResult(HisDiagnosisTask task) {
        String selectSql = "select count(*) from param_info;";
        int count = 0;
        try (Connection connect = connectSqlite(); Statement statement = connect.createStatement();
             ResultSet result = statement.executeQuery(selectSql)) {
            if (result.next()) {
                count = result.getInt(1);
            }
        } catch (SQLException e) {
            throw new HisDiagnosisException("error:", e);
        }
        HashMap<String, String> pointMap = obtainAllPoint(task.getDiagnosisType());
        List<String> pointList = new ArrayList<>(pointMap.keySet());
        List<HisDiagnosisResult> resultList = resultMapper.selectList(
                Wrappers.<HisDiagnosisResult>lambdaQuery().eq(HisDiagnosisResult::getTaskId, task.getId())
                        .in(HisDiagnosisResult::getPointState, HisDiagnosisResult.PointState.SUCCEED,
                                HisDiagnosisResult.PointState.NOT_MATCH_OPTION,
                                HisDiagnosisResult.PointState.NOT_SATISFIED_DIAGNOSIS,
                                HisDiagnosisResult.PointState.NOT_HAVE_DATA));
        if (resultList.size() == count + pointList.size()) {
            return TaskState.FINISH;
        }
        HisDiagnosisTask newTask = taskMapper.selectById(task.getId());
        return TaskState.SQL_ERROR.equals(newTask.getState()) ? TaskState.SQL_ERROR : TaskState.RECEIVING;
    }

    @Override
    public HisDiagnosisTask selectById(int taskId) {
        return taskMapper.selectById(taskId);
    }

    private boolean isRun(List<String> options, List<OptionQuery> optionList) {
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

    private List<HisDiagnosisThreshold> getThresholds(String diagnosisType, List<HisThresholdDTO> dtoList) {
        List<HisDiagnosisThreshold> thresholdList = hisThresholdMapper.selectList(
                Wrappers.<HisDiagnosisThreshold>lambdaQuery().eq(
                        HisDiagnosisThreshold::getDiagnosisType,
                        diagnosisType));
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

    private boolean explainBeforeStart(HisDiagnosisTask task) {
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId())) {
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery(SqlCommon.DEBUG_QUERY_CHECK)) {
                if (rs.next() && !rs.getString(1).equals("on")) {
                    return false;
                }
            }
        } catch (SQLException e) {
            task.addRemarks("DEBUG_QUERY_ID check failed", e);
            taskMapper.updateById(task);
        }
        return true;
    }

    private boolean bccBeforeStart(HisDiagnosisTask task) {
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(SqlCommon.DB_THREADS_CONF)) {
            if (rs.next() && rs.getString(1).equals("on")) {
                return false;
            }
        } catch (SQLException e) {
            task.addRemarks("bcc check threads config failed", e);
            taskMapper.updateById(task);
        }
        return true;
    }

    private void setDebugQueryId(HisDiagnosisTask task) {
        task.addRemarks("catching DEBUG_QUERY_ID");
        taskMapper.updateById(task);
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId())) {
            Long debugQueryId = null;
            int counter = 0;
            while (debugQueryId == null && counter++ < 1000) {
                try (var stmt = conn.createStatement();
                     var rs = stmt.executeQuery(String.format(SqlCommon.DEBUG_QUERY_ID_SQL, task.getSessionId()))) {
                    if (rs.next()) {
                        debugQueryId = rs.getLong(1);
                    } else {
                        Thread.sleep(10L);
                    }
                }
            }
            task.setDebugQueryId(debugQueryId);
            task.addRemarks("DEBUG_QUERY_ID:" + debugQueryId);
        } catch (InterruptedException e) {
            task.addRemarks("DEBUG_QUERY_ID catch failer");
            Thread.currentThread().interrupt();
        } catch (SQLException e) {
            task.addRemarks("DEBUG_QUERY_ID catch failer");
        } finally {
            taskMapper.updateById(task);
        }
    }

    private HashMap<String, String> obtainAllPoint(String diagnosisType) {
        String fileName = DiagnosisTypeCommon.HISTORY.equals(diagnosisType) ? "/hisTreeNode.txt" : "/sqlTreeNode.txt";
        HashMap<String, String> map = new HashMap<>();
        try (InputStream is = this.getClass().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                var lines = reader.readLine();
                if (StringUtils.isBlank(lines)) {
                    continue;
                }
                String[] line = lines.split(" ");
                map.put(line[0].replace("-", ""), line[1]);
            }
        } catch (IOException e) {
            throw new HisDiagnosisException("error:", e);
        }
        return map;
    }

    private Integer obtainPid(HisDiagnosisTask task) {
        task.addRemarks("sessionId:" + task.getSessionId());
        Integer lwpid = null;
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId())) {
            var watch = new StopWatch();
            watch.start();
            // catch pid
            while (lwpid == null) {
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT b.lwpid from pg_stat_activity a,dbe_perf.os_threads b "
                        + "where a.state != 'idle' and a.pid=b.pid and a.sessionid = '" + task.getSessionId() + "'");
                if (rs.next()) {
                    lwpid = rs.getInt(1);
                }
                watch.stop();
                watch.start();
                if (watch.getTaskCount() > 10000) {
                    break;
                }
                if (lwpid == null) {
                    Thread.sleep(10L);
                }
            }
            task.addRemarks("pid catch times：" + watch.getTaskCount());
            task.setPid(lwpid);
            task.addRemarks("pid:" + lwpid);
            if (lwpid == null) {
                task.addRemarks("pid catch fail");
                taskMapper.updateById(task);
                throw new HisDiagnosisException("pid catch fail");
            }
            taskMapper.updateById(task);
        } catch (SQLException e) {
            task.addRemarks("db permission fail");
            taskMapper.updateById(task);
            throw new HisDiagnosisException("db permission fail" + e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            taskMapper.updateById(task);
            throw new HisDiagnosisException("error:" + e);
        }
        return lwpid;
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