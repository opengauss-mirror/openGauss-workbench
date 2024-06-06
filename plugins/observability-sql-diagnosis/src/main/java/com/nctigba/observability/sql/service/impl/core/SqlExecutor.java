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
 *  SqlExecutor.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/core/SqlExecutor.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.core;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import com.nctigba.observability.sql.constant.SqlConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.enums.TaskStateEnum;
import com.nctigba.observability.sql.mapper.DiagnosisTaskMapper;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.OptionVO;
import com.nctigba.observability.sql.service.TaskService;
import com.nctigba.observability.sql.service.impl.ClusterManager;
import com.nctigba.observability.sql.util.EbpfUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * SqlExecute
 *
 * @author luomeng
 * @since 2023/7/31
 */
@Service
public class SqlExecutor {
    @Autowired
    private DiagnosisTaskMapper mapper;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    @Lazy
    private TaskService taskService;
    @Autowired
    private EbpfUtils ebpfUtils;

    /**
     * Execute sql
     *
     * @param task task info
     */
    @Async
    public void executeSql(DiagnosisTaskDO task) {
        try {
            task.addRemarks("***start execute sql***");
            task.setState(TaskStateEnum.SQL_RUNNING);
            mapper.updateById(task);
            var rsList = new ArrayList<String>();
            try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId(), task.getDbName(),
                    task.getSchemaName())) {
                Long sessionid = null;
                int count = 0;
                while (count++ < 100) {
                    int rand = RandomUtil.randomInt();
                    var sessionidSql = String.format(SqlConstants.SESSION_ID_SQL, rand, rand, rand, rand);
                    try (var st = conn.createStatement(); var rs = st.executeQuery(sessionidSql)) {
                        if (rs.next()) {
                            sessionid = rs.getLong(1);
                            task.setSessionId(sessionid);
                            task.addRemarks("sessionId:" + sessionid);
                            mapper.updateById(task);
                            break;
                        }
                    }
                }
                if (sessionid == null) {
                    task.setSessionId(0L);
                    task.addRemarks("sessionId catch fail");
                    mapper.updateById(task);
                }
                List<OptionVO> options = databaseOption(task);
                boolean isExplain = options.stream().anyMatch(f -> {
                    if (OptionEnum.IS_EXPLAIN.toString().equals(f.getOption())) {
                        return f.getIsCheck();
                    }
                    return false;
                });
                String checkSql = task.getSql().toUpperCase(Locale.ROOT);
                boolean isCreate = checkSql.startsWith("CREATE");
                boolean isUpdate = checkSql.startsWith("UPDATE");
                boolean isInsert = checkSql.startsWith("INSERT");
                boolean isDelete = checkSql.startsWith("DELETE");
                boolean isModify = ((isUpdate || isDelete || isInsert) && !isExplain);
                boolean isAnalysisExplain = isCreate || isModify;
                if (isAnalysisExplain) {
                    try (var stmt = conn.createStatement()) {
                        stmt.execute(task.getSql());
                    }
                } else {
                    String diagnosisSql = isExplain ? "explain analyze " + task.getSql() : task.getSql();
                    try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(diagnosisSql)) {
                        while (rs.next()) {
                            rsList.add(rs.getString(1));
                        }
                    }
                }
                task.addRemarks("results:" + rsList.size());
                task.setTaskEndTime(new Date());
                task.setSpan(task.getCost());
                mapper.updateById(task);
            } catch (SQLException e) {
                task.addRemarks(TaskStateEnum.SQL_ERROR, e);
                task.setTaskEndTime(new Date());
                task.setSpan(task.getCost());
                mapper.updateById(task);
                return;
            }
            task.addRemarks("after sql caller");
            task.setState(TaskStateEnum.RECEIVING);
            mapper.updateById(task);
            ThreadUtil.execAsync(() -> {
                boolean isSuccess = ebpfUtils.stopMonitor(task);
                if (isSuccess) {
                    task.setCollectPidStatus(1);
                } else {
                    task.setCollectPidStatus(0);
                }
                task.addRemarks("agent pid status:" + isSuccess);
                mapper.updateById(task);
            });
            if (task.getPid() != null) {
                taskService.explainAfter(task, rsList);
            } else {
                task.setState(TaskStateEnum.ERROR);
                mapper.updateById(task);
            }
        } finally {
            mapper.updateById(task);
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
}