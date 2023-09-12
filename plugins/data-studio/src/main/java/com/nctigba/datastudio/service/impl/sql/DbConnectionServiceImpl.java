/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.GainObjectSQLService;
import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.dao.DatabaseConnectionDAO;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayIdSchemaQuery;
import com.nctigba.datastudio.service.DataListByJdbcService;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.util.DebugUtils;
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

/**
 * DbConnectionServiceImpl
 *
 * @since 2023-6-26
 */
@Slf4j
@Service
public class DbConnectionServiceImpl implements DbConnectionService {

    @Resource
    private DatabaseConnectionDAO databaseConnectionDAO;
    @Resource
    private ConnectionMapDAO connectionMapDAO;
    @Resource
    private MetaDataByJdbcService metaDataByJdbcService;

    @Resource
    private DataListByJdbcService dataListByJdbcService;

    private Map<String, GainObjectSQLService> gainObjectSQLService;

    /**
     * set gain object sql service
     *
     * @param SQLServiceList SQLServiceList
     */
    @Resource
    public void setGainObjectSQLService(List<GainObjectSQLService> SQLServiceList) {
        gainObjectSQLService = new HashMap<>();
        for (GainObjectSQLService service : SQLServiceList) {
            gainObjectSQLService.put(service.type(), service);
        }
    }

    @Override
    public DatabaseConnectionDO addDatabaseConnection(DbConnectionCreateDTO request) {
        if (databaseConnectionDAO.getJudgeName(request.getName(), request.getWebUser()) == 0) {
            DatabaseConnectionDO conn = request.toDatabaseConnection();
            conn.setEdition(test(request));
            databaseConnectionDAO.insertTable(conn);
            DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(),
                    request.getWebUser());
            String uuid = UUID.randomUUID().toString();
            dataList.setConnectionid(uuid);
            ConnectionDTO connectionDTO = new ConnectionDTO();
            connectionDTO.setIpConnectionDTO(dataList);
            ConnectionMapDAO.setConMap(uuid, connectionDTO);
            dataList.setPassword("");
            return dataList;
        } else {
            return loginDatabaseConnection(request);
        }
    }

    @Override
    public void deleteDatabaseConnectionList(String id) {
        databaseConnectionDAO.deleteTable(Integer.parseInt(id));
    }

    @Override
    public DatabaseConnectionDO databaseAttributeConnection(String id) {
        DatabaseConnectionDO databaseConnectionEntity = databaseConnectionDAO.getAttributeById(id, "A");
        databaseConnectionEntity.setPassword("");
        return databaseConnectionEntity;
    }

    @Override
    public List<DatabaseConnectionDO> databaseConnectionList(String webUser) {
        return databaseConnectionDAO.selectTable(webUser);
    }

    @Override
    public DatabaseConnectionDO updateDatabaseConnection(DbConnectionCreateDTO request) throws SQLException {
            DatabaseConnectionDO conn = request.toDatabaseConnection();
            conn.setEdition(test(request));
            connectionMapDAO.deleteConnection(request.getConnectionid());
            databaseConnectionDAO.updateTable(conn);
            DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(),
                    request.getWebUser());
            String uuid = UUID.randomUUID().toString();
            dataList.setConnectionid(uuid);
            ConnectionDTO connectionDTO = new ConnectionDTO();
            DatabaseConnectionUrlDO databaseConnectionUrlDO = databaseConnectionDAO.getByName(request.getName(),
                    request.getWebUser());
            connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
            ConnectionMapDAO.setConMap(uuid, connectionDTO);
            return dataList;
    }

    @Override
    public DatabaseConnectionDO loginDatabaseConnection(DbConnectionCreateDTO request) {
        DatabaseConnectionDO conn = request.toDatabaseConnection();
        conn.setEdition(test(request));
        databaseConnectionDAO.updateTable(conn);
        DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(),
                request.getWebUser());
        String uuid = UUID.randomUUID().toString();
        dataList.setConnectionid(uuid);
        ConnectionDTO connectionDTO = new ConnectionDTO();
        DatabaseConnectionUrlDO databaseConnectionUrlDO = databaseConnectionDAO.getByName(request.getName(),
                request.getWebUser());
        connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        return dataList;
    }

    @Override
    public List<DataListDTO> schemaObjectList(DatabaseMetaarrayIdSchemaQuery schema) {
            if (!conMap.containsKey(schema.getUuid())) {
                throw new CustomException(LocaleString.transLanguage("1004"));
            }
            ConnectionDTO connectionDTO = conMap.get(schema.getUuid());
            List<DataListDTO> listDataList = new ArrayList<>();
        DataListDTO dataList = null;
        try {
            dataList = dataListByJdbcService.dataListQuerySQL(
                    connectionDTO.getUrl(),
                    connectionDTO.getDbUser(),
                    connectionDTO.getDbPassword(),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType())
                            .tableSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType())
                            .viewSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType())
                            .fun_prosSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType())
                            .sequenceSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType())
                            .synonymSql(DebugUtils.needQuoteName(schema.getSchema())),
                    schema.getSchema()
            );
            log.info("77777 response is: " + dataList);
        } catch (SQLException | InterruptedException e) {
            throw new CustomException(e.getMessage());
        }
        connectionDTO.updateConnectionDTO(connectionDTO);
            ConnectionMapDAO.setConMap(schema.getUuid(), connectionDTO);
            listDataList.add(dataList);
            return listDataList;
    }

    @Override
    public String test(DbConnectionCreateDTO request) {
        log.info("{}", request);
        log.info("{}", gainObjectSQLService.keySet());
        return metaDataByJdbcService.versionSQL(
                GET_URL_JDBC + request.getIp() + ":" + request.getPort() + "/" + request.getDataName() + CONFIGURE_TIME,
                request.getUserName(),
                request.getPassword(),
                gainObjectSQLService.get(request.getType()).databaseVersion()
        );
    }

}