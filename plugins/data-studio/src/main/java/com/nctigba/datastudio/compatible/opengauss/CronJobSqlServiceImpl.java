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
 *  CronJobSqlServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/CronJobSqlServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.CronJobSqlService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.CronJobQuery;
import com.nctigba.datastudio.utils.ExecuteUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.SqlConstants.CREATE_JOB_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DELETE_JOB_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.DISABLE_JOB_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.ENABLE_JOB_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_JOB_COUNT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_JOB_LIST_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.QUERY_JOB_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.UPDATE_JOB_SQL;

/**
 * CronJobSqlServiceImpl
 *
 * @since 2023-11-08
 */
@Slf4j
@Service
public class CronJobSqlServiceImpl implements CronJobSqlService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public Map<String, Object> queryList(CronJobQuery request) throws SQLException {
        log.info("CronJobSqlServiceImpl queryList request: " + request);
        Integer pageSize = request.getPageSize();
        String sql = String.format(QUERY_JOB_LIST_SQL, (request.getPageNum() - 1) * pageSize, pageSize);
        log.info("CronJobSqlServiceImpl queryList sql: " + sql);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid())
        ) {
            List<String> keyList = new ArrayList<>() {
                {
                    this.add("job_id");
                    this.add("job_content");
                    this.add("database_name");
                    this.add("job_status");
                    this.add("interval");
                    this.add("start_date");
                    this.add("next_run_date");
                    this.add("failure_count");
                    this.add("failure_msg");
                    this.add("last_end_date");
                    this.add("last_suc_date");
                    this.add("creator");
                    this.add("executor");
                }
            };

            List<Map<String, Object>> list = ExecuteUtils.executeGetMap(connection, sql, keyList);
            log.info("CronJobSqlServiceImpl queryList list: " + list);

            Object obj = ExecuteUtils.executeGetOne(connection, QUERY_JOB_COUNT_SQL);
            int count = 0;
            if (obj instanceof Long) {
                count = Math.toIntExact((Long) obj);
            }
            Map<String, Object> map = new HashMap<>();
            map.put("pageSize", pageSize);
            map.put("pageNum", request.getPageNum());
            map.put("pageTotal", count % pageSize == 0 ? count / pageSize : count / pageSize + 1);
            map.put("data", list);
            return map;
        }
    }

    @Override
    public Map<String, Object> query(CronJobQuery request) throws SQLException {
        log.info("CronJobSqlServiceImpl query request: " + request);
        String sql = String.format(QUERY_JOB_SQL, request.getJobId());
        log.info("CronJobSqlServiceImpl query sql: " + sql);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
        ) {
            List<String> keyList = new ArrayList<>() {
                {
                    this.add("job_id");
                    this.add("job_content");
                    this.add("interval");
                    this.add("next_run_date");
                }
            };

            List<Map<String, Object>> list = ExecuteUtils.executeGetMap(connection, sql, keyList);
            log.info("CronJobSqlServiceImpl queryList list: " + list);
            return list.get(0);
        }
    }

    @Override
    public void create(CronJobQuery request) throws SQLException {
        log.info("CronJobSqlServiceImpl create request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid())
        ) {
            String sql = String.format(CREATE_JOB_SQL, request.getJobContent().replace("'", "''"),
                    request.getNextRunDate(), request.getInterval().replace("'", "''"));
            ExecuteUtils.execute(connection, sql);
        }
    }

    @Override
    public void edit(CronJobQuery request) throws SQLException {
        log.info("CronJobSqlServiceImpl edit request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid())
        ) {
            String sql = String.format(UPDATE_JOB_SQL, request.getJobId(),
                    request.getNextRunDate(), request.getInterval().replace("'", "''"),
                    request.getJobContent().replace("'", "''"));
            ExecuteUtils.execute(connection, sql);
        }
    }

    @Override
    public void delete(CronJobQuery request) throws SQLException {
        log.info("CronJobSqlServiceImpl delete request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid())
        ) {
            ExecuteUtils.execute(connection, String.format(DELETE_JOB_SQL, request.getJobId()));
        }
    }

    @Override
    public void enable(CronJobQuery request) throws SQLException {
        log.info("CronJobSqlServiceImpl enable request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid())
        ) {
            ExecuteUtils.execute(connection, String.format(ENABLE_JOB_SQL, request.getJobId()));
        }
    }

    @Override
    public void disable(CronJobQuery request) throws SQLException {
        log.info("CronJobSqlServiceImpl disable request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid())
        ) {
            ExecuteUtils.execute(connection, String.format(DISABLE_JOB_SQL, request.getJobId()));
        }
    }
}
