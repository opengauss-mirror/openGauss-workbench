/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.FunctionSPObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.service.DatabaseFunctionSPService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

@Slf4j
@Service
public class DatabaseFunctionSPServiceImpl implements DatabaseFunctionSPService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, FunctionSPObjectSQLService> functionSPObjectSQLService;

    @Resource
    public void setfunctionSPObjectSQLService(List<FunctionSPObjectSQLService> SQLServiceList) {
        functionSPObjectSQLService = new HashMap<>();
        for (FunctionSPObjectSQLService s : SQLServiceList) {
            functionSPObjectSQLService.put(s.type(), s);
        }
    }

    @Override
    public String functionDdl(DatabaseFunctionSPDTO request) throws Exception {
        log.info("functionDdl request is: " + request);
        return functionSPObjectSQLService.get(conMap.get(request.getUuid()).getType()).functionDdl(request);
    }

    @Override
    public void dropFunctionSP(DatabaseFunctionSPDTO request) {
        log.info("dropFunctionSP request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sql = functionSPObjectSQLService.get(conMap.get(request.getUuid()).getType()).dropFunctionSP(
                    request);
            statement.execute(sql);
            log.info("dropFunctionSP sql is: " + sql);
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

}
