/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.CronJobSqlService;
import com.nctigba.datastudio.model.query.CronJobQuery;
import com.nctigba.datastudio.service.CronJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * CronJobServiceImpl
 *
 * @since 2023-11-08
 */
@Slf4j
@Service
public class CronJobServiceImpl implements CronJobService {
    private Map<String, CronJobSqlService> cronJobSqlService;

    /**
     * set cron job sql service
     *
     * @param sqlServicesList sqlServicesList
     */
    @Resource
    public void setGainObjectSQLService(List<CronJobSqlService> sqlServicesList) {
        cronJobSqlService = new HashMap<>();
        for (CronJobSqlService service : sqlServicesList) {
            cronJobSqlService.put(service.type(), service);
        }
    }

    @Override
    public Map<String, Object> queryList(CronJobQuery request) throws SQLException {
        return cronJobSqlService.get(comGetUuidType(request.getUuid())).queryList(request);
    }

    @Override
    public Map<String, Object> query(CronJobQuery request) throws SQLException {
        return cronJobSqlService.get(comGetUuidType(request.getUuid())).query(request);
    }

    @Override
    public void create(CronJobQuery request) throws SQLException {
        cronJobSqlService.get(comGetUuidType(request.getUuid())).create(request);
    }

    @Override
    public void edit(CronJobQuery request) throws SQLException {
        cronJobSqlService.get(comGetUuidType(request.getUuid())).edit(request);
    }

    @Override
    public void delete(CronJobQuery request) throws SQLException {
        cronJobSqlService.get(comGetUuidType(request.getUuid())).delete(request);
    }

    @Override
    public void enable(CronJobQuery request) throws SQLException {
        cronJobSqlService.get(comGetUuidType(request.getUuid())).enable(request);
    }

    @Override
    public void disable(CronJobQuery request) throws SQLException {
        cronJobSqlService.get(comGetUuidType(request.getUuid())).disable(request);
    }
}
