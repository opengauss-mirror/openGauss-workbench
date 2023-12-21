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
 *  DatabaseFunctionSPServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/DatabaseFunctionSPServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.FunctionSPObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseFunctionSPDTO;
import com.nctigba.datastudio.model.query.PackageQuery;
import com.nctigba.datastudio.service.DatabaseFunctionSPService;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * DatabaseFunctionSPServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class DatabaseFunctionSPServiceImpl implements DatabaseFunctionSPService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, FunctionSPObjectSQLService> functionSPObjectSQLService;

    /**
     * set function object sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setFunctionSPObjectSQLService(List<FunctionSPObjectSQLService> SQLServiceList) {
        functionSPObjectSQLService = new HashMap<>();
        for (FunctionSPObjectSQLService service : SQLServiceList) {
            functionSPObjectSQLService.put(service.type(), service);
        }
    }

    @Override
    public String functionDdl(DatabaseFunctionSPDTO request) throws SQLException {
        log.info("functionDdl request is: " + request);
        return functionSPObjectSQLService.get(comGetUuidType(request.getUuid())).functionDdl(request);
    }

    @Override
    public void dropFunctionSP(DatabaseFunctionSPDTO request) {
        log.info("dropFunctionSP request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sql = functionSPObjectSQLService.get(comGetUuidType(request.getUuid())).dropFunctionSP(
                    request);
            statement.execute(sql);
            log.info("dropFunctionSP sql is: " + sql);
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void dropPackage(PackageQuery request) {
        log.info("DatabaseFunctionSPServiceImpl dropPackage request: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String sql = functionSPObjectSQLService.get(comGetUuidType(request.getUuid())).dropPackage(request);
            log.info("DatabaseFunctionSPServiceImpl dropPackage sql: " + sql);
            statement.execute(sql);
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }
}
