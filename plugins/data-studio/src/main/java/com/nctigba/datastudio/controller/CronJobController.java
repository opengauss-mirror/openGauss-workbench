/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.controller;

import com.nctigba.datastudio.model.query.CronJobQuery;
import com.nctigba.datastudio.service.CronJobService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Map;

/**
 * CronJobController
 *
 * @since 2023-11-08
 */
@RestController
@RequestMapping(value = "/dataStudio/web/v1")
public class CronJobController {
    @Resource
    private CronJobService cronJobService;

    /**
     * query cron job list
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/job/queryList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> queryList(@RequestBody CronJobQuery request) throws SQLException {
        return cronJobService.queryList(request);
    }

    /**
     * query cron job by id
     *
     * @param request request
     * @return Map
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/job/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> query(@RequestBody CronJobQuery request) throws SQLException {
        return cronJobService.query(request);
    }

    /**
     * create cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/job/create", produces = MediaType.APPLICATION_JSON_VALUE)
    public void create(@RequestBody CronJobQuery request) throws SQLException {
        cronJobService.create(request);
    }

    /**
     * edit cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/job/edit", produces = MediaType.APPLICATION_JSON_VALUE)
    public void edit(@RequestBody CronJobQuery request) throws SQLException {
        cronJobService.edit(request);
    }

    /**
     * delete cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/job/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public void delete(@RequestBody CronJobQuery request) throws SQLException {
        cronJobService.delete(request);
    }

    /**
     * enable cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/job/enable", produces = MediaType.APPLICATION_JSON_VALUE)
    public void enable(@RequestBody CronJobQuery request) throws SQLException {
        cronJobService.enable(request);
    }

    /**
     * disable cron job
     *
     * @param request request
     * @throws SQLException SQLException
     */
    @PostMapping(value = "/job/disable", produces = MediaType.APPLICATION_JSON_VALUE)
    public void disable(@RequestBody CronJobQuery request) throws SQLException {
        cronJobService.disable(request);
    }
}
