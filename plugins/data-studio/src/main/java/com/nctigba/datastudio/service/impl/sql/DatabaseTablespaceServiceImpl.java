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
 *  DatabaseTablespaceServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/DatabaseTablespaceServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.TableSpaceObjectSqlService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.dto.DatabaseCreateTablespaceDTO;
import com.nctigba.datastudio.model.dto.DatabaseDorpTablespaceDTO;
import com.nctigba.datastudio.model.dto.DatabaseTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.RequestTablespaceAttributeDTO;
import com.nctigba.datastudio.model.dto.UpdateTablespaceAttributeDTO;
import com.nctigba.datastudio.service.DatabaseTablespaceService;
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

import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

/**
 * DatabaseTablespaceServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class DatabaseTablespaceServiceImpl implements DatabaseTablespaceService {
    @Autowired
    private ConnectionConfig connectionConfig;

    private Map<String, TableSpaceObjectSqlService> tablespaceObjectSQLServiceMap;

    /**
     * set synonym object sql service
     *
     * @param sqlServiceList sqlServiceList
     */
    @Resource
    public void setSynonymObjectSQLService(List<TableSpaceObjectSqlService> sqlServiceList) {
        tablespaceObjectSQLServiceMap = new HashMap<>();
        for (TableSpaceObjectSqlService service : sqlServiceList) {
            tablespaceObjectSQLServiceMap.put(service.type(), service);
        }
    }

    @Override
    public String createTablespaceDDL(DatabaseCreateTablespaceDTO request) {
        log.info("createTablespaceDDL request is: " + request);
        String ddl =
                tablespaceObjectSQLServiceMap.get(comGetUuidType(request.getUuid())).createTablespaceDDL(request);
        log.info("createTablespaceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public void createTablespace(DatabaseCreateTablespaceDTO request) {
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = tablespaceObjectSQLServiceMap.get(comGetUuidType(request.getUuid()))
                    .createTablespaceDDL(request);
            statement.execute(ddl);
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public void dropTablespaceDDL(DatabaseDorpTablespaceDTO request) {
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = tablespaceObjectSQLServiceMap.get(comGetUuidType(request.getUuid()))
                    .dropTablespaceDDL(request.getTablespaceName());
            statement.execute(ddl);
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public DatabaseTablespaceAttributeDTO tablespaceAttribute(RequestTablespaceAttributeDTO request) {
            return tablespaceObjectSQLServiceMap.get(comGetUuidType(request.getUuid()))
                    .tablespaceAttribute(request);
    }

    @Override
    public void tablespaceUpdate(UpdateTablespaceAttributeDTO request) {
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            String ddl = tablespaceObjectSQLServiceMap.get(comGetUuidType(request.getUuid()))
                    .tablespaceUpdateAttributeDdl(request);
            statement.execute(ddl);
        } catch (SQLException e) {
            log.info(e.toString());
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public String tablespaceUpdateAttributeDdl(UpdateTablespaceAttributeDTO request) {
        return tablespaceObjectSQLServiceMap.get(comGetUuidType(request.getUuid()))
                .tablespaceUpdateAttributeDdl(request);
    }
}
