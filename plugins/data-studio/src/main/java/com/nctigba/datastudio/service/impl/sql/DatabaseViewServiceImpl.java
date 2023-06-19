/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.ViewObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseSelectViewDTO;
import com.nctigba.datastudio.model.dto.DatabaseViewDdlDTO;
import com.nctigba.datastudio.service.DatabaseViewService;
import com.nctigba.datastudio.util.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

@Slf4j
@Service
public class DatabaseViewServiceImpl implements DatabaseViewService {
    @Autowired
    private ConnectionConfig connectionConfig;
    private Map<String, ViewObjectSQLService> viewObjectSQLService;

    @Resource
    public void setViewObjectSQLService(List<ViewObjectSQLService> SQLServiceList) {
        viewObjectSQLService = new HashMap<>();
        for (ViewObjectSQLService s : SQLServiceList) {
            viewObjectSQLService.put(s.type(), s);
        }
    }

    @Override
    public String createViewDDL(DatabaseCreateViewDTO request) throws Exception {
        log.info("createViewDDL request is: " + request);
        String ddl = viewObjectSQLService.get(conMap.get(request.getUuid()).getType()).splicingViewDDL(request);
        log.info("createViewDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String returnViewDDL(DatabaseViewDdlDTO request) throws Exception {
        log.info("returnViewDDL request is: " + request);
        return viewObjectSQLService.get(conMap.get(request.getUuid()).getType()).returnDatabaseViewDDL(request);

    }

    @Override
    public void createView(DatabaseCreateViewDTO request) {
        log.info("createView request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = viewObjectSQLService.get(conMap.get(request.getUuid()).getType()).splicingViewDDL(request);
            statement.execute(ddl);
            log.info("createView response is: " + ddl);
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dropView(DatabaseViewDdlDTO request) {
        log.info("dropView request is: " + request);
        try (

                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            statement.execute(
                    viewObjectSQLService.get(conMap.get(request.getUuid()).getType()).returnDropViewSQL(request));
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> selectView(DatabaseSelectViewDTO request) {
        log.info("selectView request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(viewObjectSQLService.get(
                        conMap.get(request.getUuid()).getType()).returnSelectViewSQL(request))
        ) {
            Map<String, Object> resultMap = DebugUtils.parseResultSet(resultSet);
            log.info("selectView sql is: " + viewObjectSQLService.get(
                    conMap.get(request.getUuid()).getType()).returnSelectViewSQL(request));
            log.info("selectView response is: " + resultMap);
            return resultMap;
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }
}
