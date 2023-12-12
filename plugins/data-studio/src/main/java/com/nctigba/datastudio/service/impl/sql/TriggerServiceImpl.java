/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.TriggerSqlService;
import com.nctigba.datastudio.model.query.TriggerQuery;
import com.nctigba.datastudio.service.TriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * TriggerServiceImpl
 *
 * @since 2023-10-19
 */
@Slf4j
@Service
public class TriggerServiceImpl implements TriggerService {
    private Map<String, TriggerSqlService> triggerSqlService;

    /**
     * set trigger service
     *
     * @param sqlServiceList sqlServiceList
     */
    @Resource
    public void setGainObjectSQLService(List<TriggerSqlService> sqlServiceList) {
        triggerSqlService = new HashMap<>();
        for (TriggerSqlService service : sqlServiceList) {
            triggerSqlService.put(service.type(), service);
        }
    }

    @Override
    public List<String> queryFunction(TriggerQuery query) throws SQLException {
        return triggerSqlService.get(comGetUuidType(query.getUuid())).queryFunction(query);
    }

    @Override
    public void createFunction(TriggerQuery query) throws SQLException {
        triggerSqlService.get(comGetUuidType(query.getUuid())).createFunction(query);
    }

    @Override
    public String ddlPreview(TriggerQuery query) {
        return triggerSqlService.get(comGetUuidType(query.getUuid())).ddlPreview(query);
    }

    @Override
    public void create(TriggerQuery query) throws SQLException {
        triggerSqlService.get(comGetUuidType(query.getUuid())).create(query);
    }

    @Override
    public Map<String, Map<String, Object>> query(TriggerQuery query) throws SQLException {
        return triggerSqlService.get(comGetUuidType(query.getUuid())).query(query);
    }

    @Override
    public void rename(TriggerQuery query) throws SQLException {
        triggerSqlService.get(comGetUuidType(query.getUuid())).rename(query);
    }

    @Override
    public void edit(TriggerQuery query) throws SQLException {
        triggerSqlService.get(comGetUuidType(query.getUuid())).edit(query);
    }

    @Override
    public void delete(TriggerQuery query) throws SQLException {
        triggerSqlService.get(comGetUuidType(query.getUuid())).delete(query);
    }

    @Override
    public void enable(TriggerQuery query) throws SQLException {
        triggerSqlService.get(comGetUuidType(query.getUuid())).enable(query);
    }

    @Override
    public void disable(TriggerQuery query) throws SQLException {
        triggerSqlService.get(comGetUuidType(query.getUuid())).disable(query);
    }

    @Override
    public Map<String, String> showDdl(TriggerQuery query) throws SQLException {
        return triggerSqlService.get(comGetUuidType(query.getUuid())).showDdl(query);
    }
}
