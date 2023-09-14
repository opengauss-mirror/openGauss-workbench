/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.core;

import cn.hutool.core.util.RandomUtil;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.constants.history.SqlCommon;
import com.nctigba.observability.sql.mapper.history.HisDiagnosisTaskMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.query.OptionQuery;
import com.nctigba.observability.sql.model.history.result.TaskState;
import com.nctigba.observability.sql.service.ClusterManager;
import com.nctigba.observability.sql.service.history.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * SqlExecute
 *
 * @author luomeng
 * @since 2023/7/31
 */
@Service
public class SqlExecutor {
    @Autowired
    private HisDiagnosisTaskMapper mapper;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    @Lazy
    private TaskService taskService;

    /**
     * Execute sql
     *
     * @param task task info
     */
    @Async
    public void executeSql(HisDiagnosisTask task) {
        try {
            task.addRemarks("***start execute sql***");
            task.setState(TaskState.SQL_RUNNING);
            mapper.updateById(task);
            var rsList = new ArrayList<String>();
            try (var conn = clusterManager.getConnectionByNodeId(task.getNodeId(), task.getDbName())) {
                Long sessionid = null;
                int count = 0;
                while (count++ < 100) {
                    int rand = RandomUtil.randomInt();
                    var sessionidSql = String.format(SqlCommon.SESSION_ID_SQL, rand, rand, rand, rand);
                    try (var st = conn.createStatement(); var rs = st.executeQuery(sessionidSql)) {
                        if (rs.next()) {
                            sessionid = rs.getLong(1);
                            task.setSessionId(sessionid);
                            task.addRemarks("sessionId:" + sessionid);
                            break;
                        }
                    }
                }
                if (sessionid == null) {
                    task.setSessionId(0L);
                    task.addRemarks("sessionId catch fail");
                    mapper.updateById(task);
                }
                List<OptionQuery> options = databaseOption(task);
                boolean isExplain = options.stream().anyMatch(f -> {
                    if (OptionCommon.IS_EXPLAIN.toString().equals(f.getOption())) {
                        return f.getIsCheck();
                    }
                    return false;
                });
                String diagnosisSql = isExplain ? "explain analyze " + task.getSql() : task.getSql();
                try (var stmt = conn.createStatement(); var rs = stmt.executeQuery(diagnosisSql)) {
                    while (rs.next()) {
                        rsList.add(rs.getString(1));
                    }
                }
                task.addRemarks("results:" + rsList.size());
                task.setTaskEndTime(new Date());
                task.setSpan(task.getCost());
                mapper.updateById(task);
            } catch (SQLException e) {
                task.addRemarks(TaskState.SQL_ERROR, e);
                task.setTaskEndTime(new Date());
                task.setSpan(task.getCost());
                mapper.updateById(task);
                return;
            }
            task.addRemarks("after sql caller");
            task.setState(TaskState.RECEIVING);
            mapper.updateById(task);
            taskService.explainAfter(task, rsList);
        } finally {
            mapper.updateById(task);
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
}