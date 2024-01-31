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
 *  TaskServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/core/TaskServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.core;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.sql.constant.CollectionTypeConstants;
import com.nctigba.observability.sql.constant.DiagnosisTypeConstants;
import com.nctigba.observability.sql.constant.PointTypeConstants;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.enums.DatabaseParamEnum;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.enums.OsParamEnum;
import com.nctigba.observability.sql.enums.TaskStateEnum;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.DiagnosisResultMapper;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.mapper.HisThresholdMapper;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.mapper.param.ParamInfoMapper;
import com.nctigba.observability.sql.model.dto.DiagnosisTaskDTO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.model.entity.NctigbaEnvDO;
import com.nctigba.observability.sql.model.entity.ParamInfoDO;
import com.nctigba.observability.sql.model.query.TaskQuery;
import com.nctigba.observability.sql.model.vo.DataStoreVO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.model.vo.point.ThresholdVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.TaskService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.service.impl.collection.ebpf.EbpfCollectionItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
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
    private DiagnosisTaskMapper taskMapper;
    @Autowired
    private SqlValidator sqlValidator;
    @Autowired
    private List<DiagnosisPointService<?>> pointServiceList;
    @Autowired
    private DataStoreService dataStoreService;
    @Autowired
    private HisThresholdMapper hisThresholdMapper;
    @Autowired
    private DiagnosisResultMapper resultMapper;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    @Lazy
    private SqlExecutor sqlExecutor;
    @Autowired
    private List<EbpfCollectionItem> ebpfItemList;
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    private ParamInfoMapper paramInfoMapper;

    @Override
    public Integer add(DiagnosisTaskDTO taskDTO) {
        String clusterId = clusterManager.getOpsClusterIdByNodeId(taskDTO.getNodeId());
        taskDTO.setClusterId(clusterId);
        List<OptionVO> optionDTOList = this.getOption(taskDTO.getDiagnosisType());
        if (!CollectionUtils.isEmpty(taskDTO.getConfigs())) {
            optionDTOList.forEach(f -> taskDTO.getConfigs().forEach(g -> {
                if (g.getOption().equals(f.getOption())) {
                    f.setIsCheck(g.getIsCheck());
                }
            }));
        }
        optionDTOList.sort(Comparator.comparing(OptionVO::getSortNo));
        List<DiagnosisThresholdDO> thresholdList = getThresholds(taskDTO.getDiagnosisType(), taskDTO.getThresholds());
        DiagnosisTaskDO diagnosisTaskDO = new DiagnosisTaskDO(taskDTO, optionDTOList, thresholdList);
        taskMapper.insert(diagnosisTaskDO);
        return diagnosisTaskDO.getId();
    }

    private void initDiagnosisData(DiagnosisTaskDO task) {
        List<DiagnosisResultDO> resultList = new ArrayList<>();
        HashMap<String, String> pointMap = obtainAllPoint(task.getDiagnosisType());
        for (String point : pointMap.keySet()) {
            String pointType = pointMap.get(point);
            DiagnosisResultDO initData = new DiagnosisResultDO(task, point,
                    Enum.valueOf(DiagnosisResultDO.PointType.class, pointType),
                    DiagnosisResultDO.PointState.INITIALIZE, DiagnosisResultDO.ResultState.NO_ADVICE);
            resultList.add(initData);
        }
        if (DiagnosisTypeConstants.SQL.equals(task.getDiagnosisType())) {
            List<ParamInfoDO> paramInfoDOList = paramInfoMapper.selectList(null);
            for (ParamInfoDO info : paramInfoDOList) {
                String paramName = info.getParamName();
                String nodeName = initParamName(paramName);
                DiagnosisResultDO initData = new DiagnosisResultDO(task, nodeName,
                        DiagnosisResultDO.PointState.INITIALIZE, DiagnosisResultDO.ResultState.NO_ADVICE);
                resultList.add(initData);
            }
        }
        resultMapper.batchInert(resultList);
        String topologyMap = initTopologyMap(task.getDiagnosisType());
        task.setTopologyMap(topologyMap);
        taskMapper.updateById(task);
    }

    private String initTopologyMap(String diagnosisType) {
        String fileName = DiagnosisTypeConstants.HISTORY.equals(
                diagnosisType) ? "/hisTreeNode.txt" : "/sqlTreeNode.txt";
        StringBuilder sb = new StringBuilder();
        try (InputStream is = this.getClass().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                var lines = reader.readLine();
                if (StringUtils.isBlank(lines)) {
                    continue;
                }
                sb.append(lines);
                sb.append(System.getProperty("line.separator"));
                String[] line = lines.split(" ");
                String lineValue = line[0];
                int length = lineValue.split("-").length;
                String value = lineValue.substring(0, length - 1);
                if (lines.contains("OsParam")) {
                    List<ParamInfoDO> paramInfoDOList = paramInfoMapper.selectList(
                            Wrappers.lambdaQuery(ParamInfoDO.class).eq(
                                    ParamInfoDO::getParamType,
                                    "OS"));
                    for (ParamInfoDO info : paramInfoDOList) {
                        String paramName = info.getParamName();
                        OsParamEnum[] osFields = OsParamEnum.values();
                        String pointName = "";
                        for (OsParamEnum osField : osFields) {
                            if (osField.getParamName().equals(paramName)) {
                                pointName = osField.toString();
                            }
                        }
                        sb.append(value).append("-").append(pointName).append(" DIAGNOSIS");
                        sb.append(System.getProperty("line.separator"));
                    }
                } else if (lines.contains("DatabaseParam")) {
                    List<ParamInfoDO> paramInfoList = paramInfoMapper.selectList(
                            Wrappers.lambdaQuery(ParamInfoDO.class).eq(
                                    ParamInfoDO::getParamType,
                                    "DB"));
                    for (ParamInfoDO info : paramInfoList) {
                        String paramName = info.getParamName();
                        DatabaseParamEnum[] dbFields = DatabaseParamEnum.values();
                        String pointName = "";
                        for (DatabaseParamEnum dbField : dbFields) {
                            if (dbField.getParamName().equals(paramName)) {
                                pointName = dbField.toString();
                            }
                        }
                        sb.append(value).append("-").append(pointName);
                        sb.append(System.getProperty("line.separator"));
                    }
                }
            }
        } catch (IOException e) {
            throw new HisDiagnosisException("error:", e);
        }
        return sb.toString();
    }

    private String initParamName(String paramName) {
        DatabaseParamEnum[] dbFields = DatabaseParamEnum.values();
        String nodeName = null;
        for (DatabaseParamEnum dbField : dbFields) {
            if (dbField.getParamName().equals(paramName)) {
                nodeName = dbField.toString();
            }
        }
        OsParamEnum[] osFields = OsParamEnum.values();
        for (OsParamEnum osField : osFields) {
            if (osField.getParamName().equals(paramName)) {
                nodeName = osField.toString();
            }
        }
        return nodeName;
    }

    @Override
    public void start(int taskId) {
        DiagnosisTaskDO task = taskMapper.selectById(taskId);
        task.setTaskStartTime(new Date());
        task.addRemarks("start running diagnosis");
        taskMapper.updateById(task);
        initDiagnosisData(task);
        List<OptionVO> options = databaseOption(task);
        List<DiagnosisThresholdDO> thresholds = databaseThreshold(task);
        task.setThresholds(thresholds);
        boolean isAgent = bccBeforeStart(task);
        if (DiagnosisTypeConstants.SQL.equals(task.getDiagnosisType())) {
            try {
                sqlValidator.beforeStart(task);
            } catch (HisDiagnosisException e) {
                throw new HisDiagnosisException("error:", e);
            }
            task.addRemarks("bcc diagnosis:" + isAgent);
            boolean isExist = options.stream().anyMatch(f -> OptionEnum.IS_BCC.toString().equals(f.getOption()));
            if (isExist) {
                options.forEach(f -> {
                    if (OptionEnum.IS_BCC.toString().equals(f.getOption())) {
                        f.setIsCheck(isAgent);
                    }
                });
            } else {
                OptionVO query = new OptionVO();
                query.setOption(String.valueOf(OptionEnum.IS_BCC));
                query.setIsCheck(isAgent);
                options.add(query);
            }
            ThreadUtil.execAsync(() -> sqlExecutor.executeSql(task));
            for (int i = 0; i < 10; i++) {
                if (task.getSessionId() == null) {
                    ThreadUtil.sleep(500L);
                }
            }
            task.addRemarks("execute SQL while starting diagnosis");
            Integer pid = obtainPid(task);
            task.addRemarks("get pid:" + pid);
            task.setPid(pid);
            setDebugQueryId(task);
            taskMapper.updateById(task);
        }
        HashMap<String, String> pointMap = obtainAllPoint(task.getDiagnosisType());
        List<String> pointList = new ArrayList<>(pointMap.keySet());
        StringBuffer sb = new StringBuffer();
        HashMap<CollectionItem<?>, Integer> hashMap = new HashMap<>();
        for (DiagnosisPointService<?> pointService : pointServiceList) {
            String pointName = getClassName(pointService);
            if (!pointList.contains(pointName)) {
                continue;
            }
            List<String> option = pointService.getOption();
            boolean isRun = isRun(option, options);
            List<CollectionItem<?>> params = pointService.getSourceDataKeys();
            boolean isDiagnosisType = false;
            if (DiagnosisTypeConstants.HISTORY.equals(task.getDiagnosisType())) {
                isDiagnosisType = task.getHisDataEndTime() != null && pointService.getDiagnosisType().equals(
                        PointTypeConstants.CURRENT);
            }
            if (isRun || CollectionUtils.isEmpty(params) || isDiagnosisType) {
                if (!CollectionUtils.isEmpty(params)) {
                    sb.append(pointService).append(";");
                    boolean isBcc = options.stream().anyMatch(f -> {
                        if (OptionEnum.IS_BCC.toString().equals(f.getOption())) {
                            return f.getIsCheck();
                        }
                        return false;
                    });
                    DiagnosisResultDO.PointState state;
                    if (!CollectionUtils.isEmpty(option) && option.contains(OptionEnum.IS_BCC.toString()) && !isBcc) {
                        state = DiagnosisResultDO.PointState.NOT_SATISFIED_DIAGNOSIS;
                    } else {
                        state = DiagnosisResultDO.PointState.NOT_MATCH_OPTION;
                    }
                    DiagnosisResultDO result = new DiagnosisResultDO(task, pointName,
                            state, DiagnosisResultDO.ResultState.NO_ADVICE);
                    resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                                    DiagnosisResultDO::getTaskId, result.getTaskId())
                            .eq(DiagnosisResultDO::getPointName, pointName));
                    if ("OsParam".equals(pointName) || "DatabaseParam".equals(pointName)) {
                        String paramType = "OsParam".equals(pointName) ? "OS" : "DB";
                        updateParamChildNode(task, paramType, state);
                    }
                }
                continue;
            }
            for (CollectionItem<?> param : params) {
                if (CollectionTypeConstants.AFTER.equals(param.getCollectionType())) {
                    continue;
                }
                hashMap.merge(param, 1, Integer::sum);
            }
        }
        task.addRemarks("start analysis ...");
        taskMapper.updateById(task);
        ExecutorService executor = ThreadUtil.newExecutor();
        for (CollectionItem<?> item : hashMap.keySet()) {
            boolean isBcc = pointServiceList.stream().anyMatch(
                    f -> !CollectionUtils.isEmpty(f.getOption()) && f.getOption().contains(
                            OptionEnum.IS_BCC.toString()) && f.getSourceDataKeys().contains(
                            item) && !sb.toString().contains(f.toString()));
            if (isBcc && isAgent) {
                try {
                    item.collectData(task);
                    continue;
                } catch (Exception e) {
                    task.addRemarks("call ebpf error:" + e);
                    taskMapper.updateById(task);
                }
            }
            String itemName = getClassName(item);
            executor.execute(() -> {
                task.addRemarks("start check collection " + itemName);
                try {
                    Object isExistData = item.queryData(task);
                    if (isExistData == null) {
                        updateResult(item, sb, task, "no data");
                    } else {
                        if (isExistData.toString().contains("collection error:")) {
                            updateResult(item, sb, task, isExistData.toString());
                        }
                    }
                } catch (Exception e) {
                    updateResult(item, sb, task, e.getMessage());
                }
                task.addRemarks("stop check collection " + itemName);
                taskMapper.updateById(task);
                DataStoreVO config = new DataStoreVO();
                config.setCollectionItem(item);
                task.addRemarks("start collection " + itemName);
                Object collectionData;
                try {
                    collectionData = item.collectData(task);
                } catch (Exception e) {
                    collectionData = "error:" + e.getMessage();
                }
                config.setCollectionData(collectionData);
                task.addRemarks("stop collection " + itemName);
                taskMapper.updateById(task);
                config.setCount(hashMap.get(item));
                List<DataStoreVO> list = new ArrayList<>();
                list.add(config);
                dataStoreService.storeData(list);
                List<CollectionItem<?>> itemList = dataStoreService.getCollectionItem();
                taskMapper.updateById(task);
                for (DiagnosisPointService<?> pointService : pointServiceList) {
                    String pointName = getClassName(pointService);
                    if (!pointList.contains(pointName)) {
                        continue;
                    }
                    if (sb.toString().contains(pointService.toString())) {
                        continue;
                    }
                    if (!CollectionUtils.isEmpty(pointService.getOption()) && pointService.getOption().contains(
                            OptionEnum.IS_BCC.toString())) {
                        continue;
                    }
                    if (!CollectionUtils.isEmpty(pointService.getOption()) && pointService.getOption().contains(
                            OptionEnum.IS_EXPLAIN.toString())) {
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
                                DiagnosisResultDO result = new DiagnosisResultDO(
                                        task, analysisDTO, pointName, DiagnosisResultDO.PointState.SUCCEED);
                                resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                                                DiagnosisResultDO::getTaskId, result.getTaskId())
                                        .eq(DiagnosisResultDO::getPointName, pointName));
                            }
                        } catch (Exception e) {
                            task.addRemarks("stop analysis " + pointName);
                            DiagnosisResultDO result = new DiagnosisResultDO(task, pointName,
                                    DiagnosisResultDO.PointState.ANALYSIS_EXCEPTION,
                                    DiagnosisResultDO.ResultState.NO_ADVICE);
                            result.setPointSuggestion("Analysis exception:" + e.getMessage());
                            resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                                            DiagnosisResultDO::getTaskId, result.getTaskId())
                                    .eq(DiagnosisResultDO::getPointName, pointName));
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
        if (!DiagnosisTypeConstants.SQL.equals(task.getDiagnosisType())) {
            dataStoreService.clearData();
        }
        if (DiagnosisTypeConstants.HISTORY.equals(task.getDiagnosisType())) {
            task.setState(TaskStateEnum.FINISH);
        } else {
            task.setState(checkDiagnosisResult(task));
        }
        task.addRemarks("finish diagnosis");
        task.setTaskEndTime(new Date());
        task.setSpan(task.getCost());
        taskMapper.updateById(task);
    }

    private void updateResult(CollectionItem<?> item, StringBuffer sb, DiagnosisTaskDO task, String message) {
        pointServiceList.forEach(f -> {
            if (!CollectionUtils.isEmpty(f.getSourceDataKeys())) {
                f.getSourceDataKeys().forEach(g -> {
                    if (g == item && !sb.toString().contains(f.toString())) {
                        String pointName = getClassName(f);
                        sb.append(f).append(";");
                        DiagnosisResultDO.PointState state;
                        if ("no data".equals(message)) {
                            state = DiagnosisResultDO.PointState.NOT_HAVE_DATA;
                        } else {
                            state = DiagnosisResultDO.PointState.COLLECT_EXCEPTION;
                        }
                        DiagnosisResultDO result = new DiagnosisResultDO(task, pointName,
                                state, DiagnosisResultDO.ResultState.NO_ADVICE);
                        result.setPointSuggestion("Collection exception:" + message);
                        resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                                        DiagnosisResultDO::getTaskId, result.getTaskId())
                                .eq(DiagnosisResultDO::getPointName, pointName));
                    }
                });
            }
        });
    }

    private void updateParamChildNode(DiagnosisTaskDO task, String paramType, DiagnosisResultDO.PointState state) {
        List<ParamInfoDO> paramInfoDOList = paramInfoMapper.selectList(
                Wrappers.lambdaQuery(ParamInfoDO.class).eq(ParamInfoDO::getParamType, paramType));
        List<String> list = new ArrayList<>();
        for (ParamInfoDO info : paramInfoDOList) {
            String type = String.valueOf(info.getParamType());
            String paramName = info.getParamName();
            String pointName = null;
            if ("OS".equals(type)) {
                OsParamEnum[] osFields = OsParamEnum.values();
                for (OsParamEnum osField : osFields) {
                    if (osField.getParamName().equals(paramName)) {
                        pointName = osField.toString();
                    }
                }
            } else {
                DatabaseParamEnum[] dbFields = DatabaseParamEnum.values();
                for (DatabaseParamEnum dbField : dbFields) {
                    if (dbField.getParamName().equals(paramName)) {
                        pointName = dbField.toString();
                    }
                }
            }
            list.add(pointName);
        }
        for (String pointName : list) {
            DiagnosisResultDO result = new DiagnosisResultDO(
                    task, pointName, state, DiagnosisResultDO.ResultState.NO_ADVICE);
            resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                            DiagnosisResultDO::getTaskId, result.getTaskId())
                    .eq(DiagnosisResultDO::getPointName, pointName));
        }
    }

    private List<OptionVO> databaseOption(DiagnosisTaskDO task) {
        List<OptionVO> options = new ArrayList<>();
        List<?> objects = task.getConfigs();
        for (Object ob : objects) {
            if (ob instanceof LinkedHashMap) {
                LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>) ob;
                OptionVO optionVO = new OptionVO();
                Object optionObj = hashMap.get("option");
                if (optionObj instanceof String) {
                    optionVO.setOption((String) optionObj);
                }
                Object checkObj = hashMap.get("isCheck");
                if (checkObj instanceof Boolean) {
                    optionVO.setIsCheck((Boolean) checkObj);
                }
                options.add(optionVO);
            }
        }
        return options;
    }

    private List<DiagnosisThresholdDO> databaseThreshold(DiagnosisTaskDO task) {
        List<DiagnosisThresholdDO> thresholds = new ArrayList<>();
        List<?> objects = task.getThresholds();
        for (Object ob : objects) {
            if (ob instanceof LinkedHashMap) {
                LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>) ob;
                DiagnosisThresholdDO threshold = new DiagnosisThresholdDO();
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
    public List<OptionVO> getOption(String diagnosisType) {
        HashMap<String, String> pointMap = obtainAllPoint(diagnosisType);
        List<String> pointList = new ArrayList<>(pointMap.keySet());
        List<OptionVO> list = new ArrayList<>();
        for (DiagnosisPointService<?> pointService : pointServiceList) {
            String pointName = getClassName(pointService);
            if (!pointList.contains(pointName)) {
                continue;
            }
            List<String> options = pointService.getOption();
            if (!CollectionUtils.isEmpty(options)) {
                for (String option : options) {
                    if (OptionEnum.IS_BCC.toString().equals(option)) {
                        continue;
                    }
                    OptionVO optionDTO = new OptionVO();
                    optionDTO.setOption(option);
                    optionDTO.setIsCheck(false);
                    optionDTO.setName(OptionEnum.valueOf(option).getName());
                    optionDTO.setSortNo(OptionEnum.valueOf(option).getSortNo());
                    if (CollectionUtils.isEmpty(
                            list.stream().filter(o -> o.getOption().equals(option)).collect(Collectors.toList()))) {
                        list.add(optionDTO);
                    }
                }
            }
        }
        list.sort(Comparator.comparing(OptionVO::getSortNo));
        return list;
    }

    @Override
    public IPage<DiagnosisTaskDO> selectByPage(TaskQuery query) {
        return taskMapper.selectPage(
                query.iPage(),
                Wrappers.lambdaQuery(DiagnosisTaskDO.class).eq(StringUtils.isNotBlank(query.getClusterId()),
                        DiagnosisTaskDO::getClusterId, query.getClusterId()).eq(
                        StringUtils.isNotBlank(query.getNodeId()), DiagnosisTaskDO::getNodeId, query.getNodeId()).eq(
                        StringUtils.isNotBlank(query.getDbName()), DiagnosisTaskDO::getDbName, query.getDbName()).eq(
                        StringUtils.isNotBlank(query.getSqlId()), DiagnosisTaskDO::getSqlId, query.getSqlId()).eq(
                        StringUtils.isNotBlank(query.getDiagnosisType()), DiagnosisTaskDO::getDiagnosisType,
                        query.getDiagnosisType()).and(
                        StringUtils.isNotBlank(query.getName()),
                        c -> c.like(DiagnosisTaskDO::getTaskName, query.getName()).or().like(
                                DiagnosisTaskDO::getSql, query.getName().replaceAll(" ", "%"))).ge(
                        query.getStartTime() != null, DiagnosisTaskDO::getCreateTime, query.getStartTime()).le(
                        query.getEndTime() != null, DiagnosisTaskDO::getCreateTime, query.getEndTime()).orderByDesc(
                        DiagnosisTaskDO::getId));
    }

    @Override
    public void delete(int taskId) {
        taskMapper.deleteById(taskId);
    }

    @Override
    public void explainAfter(DiagnosisTaskDO task, ArrayList<String> rsList) {
        task.addRemarks("***start analysis explain***");
        List<OptionVO> options = databaseOption(task);
        boolean isExplain = explainBeforeStart(task) || task.getSessionId() == 0L;
        HashMap<CollectionItem<?>, Integer> hashMap = getCollectionMap(options, isExplain);
        storeCollectionData(task, rsList, hashMap);
        for (DiagnosisPointService<?> pointService : pointServiceList) {
            String pointName = getClassName(pointService);
            List<String> option = pointService.getOption();
            boolean isRun = isRun(option, options);
            if (!CollectionUtils.isEmpty(option) && String.valueOf(OptionEnum.IS_EXPLAIN).equals(option.get(0))) {
                task.addRemarks("start analysis:" + pointName);
                if (!isExplain) {
                    updateExplainChildNode(task, DiagnosisResultDO.PointState.NOT_SATISFIED_DIAGNOSIS);
                    updateExplainRelatedNode(pointName, task, DiagnosisResultDO.PointState.NOT_SATISFIED_DIAGNOSIS);
                } else if (isRun) {
                    updateExplainChildNode(task, DiagnosisResultDO.PointState.NOT_MATCH_OPTION);
                    updateExplainRelatedNode(pointName, task, DiagnosisResultDO.PointState.NOT_MATCH_OPTION);
                } else {
                    updateExplainChildNode(task, DiagnosisResultDO.PointState.SUCCEED);
                    explainAnalysis(pointService, task, pointName);
                }
                task.addRemarks("stop analysis:" + pointName);
            }
        }
        dataStoreService.clearData();
        task.addRemarks("***end analysis explain***");
        task.setState(checkDiagnosisResult(task));
        task.setTaskEndTime(new Date());
        task.setSpan(task.getCost());
        taskMapper.updateById(task);
    }

    private void updateExplainRelatedNode(String pointName, DiagnosisTaskDO task, DiagnosisResultDO.PointState state) {
        if (!"ObjectInfoCheck".equals(pointName)) {
            DiagnosisResultDO result = new DiagnosisResultDO(
                    task, pointName, state, DiagnosisResultDO.ResultState.NO_ADVICE);
            resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                            DiagnosisResultDO::getTaskId, result.getTaskId())
                    .eq(DiagnosisResultDO::getPointName, pointName));
        }
    }

    private void explainAnalysis(DiagnosisPointService<?> pointService, DiagnosisTaskDO task, String pointName) {
        try {
            AnalysisDTO analysisDTO = pointService.analysis(task, dataStoreService);
            if (!"ObjectInfoCheck".equals(pointName)) {
                DiagnosisResultDO result = new DiagnosisResultDO(
                        task, analysisDTO, pointName, DiagnosisResultDO.PointState.SUCCEED);
                resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                                DiagnosisResultDO::getTaskId, result.getTaskId())
                        .eq(DiagnosisResultDO::getPointName, pointName));
            }
        } catch (Exception e) {
            DiagnosisResultDO result = new DiagnosisResultDO(task, pointName,
                    DiagnosisResultDO.PointState.ANALYSIS_EXCEPTION,
                    DiagnosisResultDO.ResultState.NO_ADVICE);
            result.setPointSuggestion("Analysis exception:" + e.getMessage());
            resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                            DiagnosisResultDO::getTaskId, result.getTaskId())
                    .eq(DiagnosisResultDO::getPointName, pointName));
        }
    }

    private HashMap<CollectionItem<?>, Integer> getCollectionMap(List<OptionVO> options, boolean isExplain) {
        HashMap<CollectionItem<?>, Integer> hashMap = new HashMap<>();
        for (DiagnosisPointService<?> pointService : pointServiceList) {
            List<String> option = pointService.getOption();
            boolean isRun = isRun(option, options);
            List<CollectionItem<?>> params = pointService.getSourceDataKeys();
            if (!CollectionUtils.isEmpty(option) && String.valueOf(OptionEnum.IS_EXPLAIN).equals(option.get(0))) {
                if (!isExplain || isRun) {
                    continue;
                }
                params.forEach(f -> {
                    if (CollectionTypeConstants.AFTER.equals(f.getCollectionType())) {
                        hashMap.merge(f, 1, Integer::sum);
                    }
                });
            }
        }
        return hashMap;
    }

    private void storeCollectionData(DiagnosisTaskDO task, ArrayList<String> rsList,
            HashMap<CollectionItem<?>, Integer> hashMap) {
        List<DataStoreVO> list = new ArrayList<>();
        for (CollectionItem<?> item : hashMap.keySet()) {
            String itemName = getClassName(item);
            DataStoreVO config = new DataStoreVO();
            config.setCollectionItem(item);
            config.setCount(hashMap.get(item));
            if ("ExplainItem".equals(itemName)) {
                config.setCollectionData(rsList);
            } else {
                Object collectData = item.collectData(task);
                config.setCollectionData(collectData);
            }
            list.add(config);
        }
        dataStoreService.storeData(list);
    }

    private void updateExplainChildNode(DiagnosisTaskDO task, DiagnosisResultDO.PointState state) {
        String[] pointNames = {"ObjectInfoCheck", "ObjectRecommendedToUpdateStatistics",
                "PlanRecommendedToCreateIndex",
                "PlanChangedToPartitionTable", "PlanRecommendedToQueryBasedOnPartition",
                "PlanRecommendedToDoVacuumCleaning",
                "PlanRecommendedToOptimizeStatementsOrAddWorkMemSize"};
        for (String pointName : pointNames) {
            DiagnosisResultDO result = new DiagnosisResultDO(
                    task, pointName, state, DiagnosisResultDO.ResultState.NO_ADVICE);
            resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                            DiagnosisResultDO::getTaskId, result.getTaskId())
                    .eq(DiagnosisResultDO::getPointName, pointName));
        }
    }

    @Override
    public void bccResult(String taskId, String pointName, MultipartFile file) {
        DiagnosisTaskDO task = null;
        if (StringUtils.isNumeric(taskId)) {
            task = taskMapper.selectById(taskId);
        }
        if (task == null) {
            return;
        }
        task.addRemarks("received pointName:" + pointName);
        taskMapper.updateById(task);
        List<DataStoreVO> list = new ArrayList<>();
        DataStoreVO config = generateCollectData(task.getDiagnosisType(), pointName, file);
        list.add(config);
        task.addRemarks("collectionItem:" + list.get(0).getCollectionItem().getClass().getName());
        dataStoreService.storeData(list);
        long size = file == null || file.isEmpty() ? 0 : file.getSize();
        log.info("bccTest:" + pointName + ":" + size);
        for (DiagnosisPointService<?> pointService : pointServiceList) {
            String className = getClassName(pointService);
            if (!CollectionUtils.isEmpty(pointService.getSourceDataKeys())
                    && pointService.getSourceDataKeys().contains(config.getCollectionItem())) {
                if (size > 0) {
                    try {
                        List<CollectionItem<?>> itemList = dataStoreService.getCollectionItem();
                        List<CollectionItem<?>> collectionItemList = pointService.getSourceDataKeys();
                        boolean isDataReady =
                                CollectionUtils.isEmpty(collectionItemList) || new HashSet<>(itemList).containsAll(
                                        collectionItemList);
                        if (isDataReady) {
                            AnalysisDTO analysisDTO = pointService.analysis(task, dataStoreService);
                            if (analysisDTO.getPointData() != null && "osParam".equals(analysisDTO.getPointData())) {
                                continue;
                            }
                            DiagnosisResultDO result = new DiagnosisResultDO(
                                    task, analysisDTO, className, DiagnosisResultDO.PointState.SUCCEED);
                            resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                                            DiagnosisResultDO::getTaskId, result.getTaskId())
                                    .eq(DiagnosisResultDO::getPointName, className));
                        }
                    } catch (Exception e) {
                        DiagnosisResultDO result = new DiagnosisResultDO(task, className,
                                DiagnosisResultDO.PointState.ANALYSIS_EXCEPTION,
                                DiagnosisResultDO.ResultState.NO_ADVICE);
                        result.setPointSuggestion("Analysis exception:" + e.getMessage());
                        resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                                        DiagnosisResultDO::getTaskId, result.getTaskId())
                                .eq(DiagnosisResultDO::getPointName, className));
                    }
                } else {
                    DiagnosisResultDO result = new DiagnosisResultDO(task, className,
                            DiagnosisResultDO.PointState.NOT_HAVE_DATA, DiagnosisResultDO.ResultState.NO_ADVICE);
                    resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                                    DiagnosisResultDO::getTaskId, result.getTaskId())
                            .eq(DiagnosisResultDO::getPointName, className));
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

    private void updateOsParamChildNode(DiagnosisTaskDO task) {
        List<ParamInfoDO> paramInfoDOList = paramInfoMapper.selectList(
                Wrappers.lambdaQuery(ParamInfoDO.class).eq(
                        ParamInfoDO::getParamType,
                        "OS"));
        List<String> list = new ArrayList<>();
        for (ParamInfoDO info : paramInfoDOList) {
            String paramName = info.getParamName();
            String pointName = null;
            OsParamEnum[] osFields = OsParamEnum.values();
            for (OsParamEnum osField : osFields) {
                if (osField.getParamName().equals(paramName)) {
                    pointName = osField.toString();
                }
            }
            list.add(pointName);
        }
        for (String pointName : list) {
            DiagnosisResultDO result = new DiagnosisResultDO(task, pointName,
                    DiagnosisResultDO.PointState.NOT_HAVE_DATA, DiagnosisResultDO.ResultState.NO_ADVICE);
            resultMapper.update(result, Wrappers.<DiagnosisResultDO>lambdaQuery().eq(
                            DiagnosisResultDO::getTaskId, result.getTaskId())
                    .eq(DiagnosisResultDO::getPointName, pointName));
        }
    }

    private DataStoreVO generateCollectData(String diagnosisType, String pointName, MultipartFile file) {
        int itemCount = 0;
        DataStoreVO config = new DataStoreVO();
        ebpfItemList.forEach(f -> {
            if (f.getHttpParam().equals(pointName)) {
                config.setCollectionItem(f);
                config.setCollectionData(file);
            }
        });
        HashMap<String, String> pointMap = obtainAllPoint(diagnosisType);
        List<String> pointList = new ArrayList<>(pointMap.keySet());
        for (DiagnosisPointService<?> pointService : pointServiceList) {
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

    private TaskStateEnum checkDiagnosisResult(DiagnosisTaskDO task) {
        DiagnosisTaskDO newTask = taskMapper.selectById(task.getId());
        if (TaskStateEnum.SQL_ERROR.equals(newTask.getState())) {
            return TaskStateEnum.SQL_ERROR;
        }
        Long count = paramInfoMapper.selectCount(null);
        HashMap<String, String> pointMap = obtainAllPoint(task.getDiagnosisType());
        List<String> pointList = new ArrayList<>(pointMap.keySet());
        List<DiagnosisResultDO> resultList = resultMapper.selectList(
                Wrappers.<DiagnosisResultDO>lambdaQuery().eq(DiagnosisResultDO::getTaskId, task.getId())
                        .in(
                                DiagnosisResultDO::getPointState, DiagnosisResultDO.PointState.SUCCEED,
                                DiagnosisResultDO.PointState.NOT_MATCH_OPTION,
                                DiagnosisResultDO.PointState.NOT_SATISFIED_DIAGNOSIS,
                                DiagnosisResultDO.PointState.NOT_HAVE_DATA));
        if (resultList.size() == count + pointList.size()) {
            return TaskStateEnum.FINISH;
        }
        List<DiagnosisResultDO> exceptionResult = resultMapper.selectList(
                Wrappers.<DiagnosisResultDO>lambdaQuery().eq(DiagnosisResultDO::getTaskId, task.getId())
                        .in(
                                DiagnosisResultDO::getPointState, DiagnosisResultDO.PointState.COLLECT_EXCEPTION,
                                DiagnosisResultDO.PointState.ANALYSIS_EXCEPTION));
        if (exceptionResult.size() > 0) {
            return TaskStateEnum.ERROR;
        }
        return TaskStateEnum.RECEIVING;
    }

    @Override
    public DiagnosisTaskDO selectById(int taskId) {
        return taskMapper.selectById(taskId);
    }

    private boolean isRun(List<String> options, List<OptionVO> optionList) {
        // 1 option is empty,return false
        if (CollectionUtils.isEmpty(options)) {
            return false;
        } else {
            // 2 has options,only should run when all options are ture
            // -- 2.1 loop options,check is check or not.If checked then set option to true
            List<OptionVO> isOption = new ArrayList<>();
            for (String option : options) {
                OptionVO dto = new OptionVO();
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

    private List<DiagnosisThresholdDO> getThresholds(String diagnosisType, List<ThresholdVO> dtoList) {
        List<DiagnosisThresholdDO> thresholdList = hisThresholdMapper.selectList(
                Wrappers.<DiagnosisThresholdDO>lambdaQuery().eq(
                        DiagnosisThresholdDO::getDiagnosisType,
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

    private boolean explainBeforeStart(DiagnosisTaskDO task) {
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId())) {
            try (var stmt = conn.createStatement();
                 var rs = stmt.executeQuery(SqlConstants.DEBUG_QUERY_CHECK)) {
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

    private boolean bccBeforeStart(DiagnosisTaskDO task) {
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId());
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(SqlConstants.DB_THREADS_CONF)) {
            if (rs.next() && rs.getString(1).equals("on")) {
                return false;
            }
        } catch (SQLException e) {
            task.addRemarks("bcc check threads config failed", e);
            taskMapper.updateById(task);
        }
        var env = envMapper.selectOne(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getNodeid, task.getNodeId()).eq(
                        NctigbaEnvDO::getType,
                        NctigbaEnvDO.envType.AGENT));
        return env != null;
    }

    private void setDebugQueryId(DiagnosisTaskDO task) {
        task.addRemarks("catching DEBUG_QUERY_ID");
        taskMapper.updateById(task);
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId())) {
            Long debugQueryId = null;
            int counter = 0;
            while (debugQueryId == null && counter++ < 1000) {
                try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(
                        String.format(SqlConstants.DEBUG_QUERY_ID_SQL, task.getSessionId()))) {
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
        String fileName = DiagnosisTypeConstants.HISTORY.equals(
                diagnosisType) ? "/hisTreeNode.txt" : "/sqlTreeNode.txt";
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

    private Integer obtainPid(DiagnosisTaskDO task) {
        task.addRemarks("sessionId:" + task.getSessionId());
        Integer lwpid = null;
        try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId())) {
            var watch = new StopWatch();
            watch.start();
            // catch pid
            while (lwpid == null) {
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery("SELECT b.lwpid from pg_stat_activity a,dbe_perf.os_threads b "
                        + "where a.state != 'idle' and a.pid=b.pid and a.sessionid = '" + task.getSessionId()
                        + "'");
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
}