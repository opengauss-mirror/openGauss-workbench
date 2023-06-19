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
import com.nctigba.datastudio.util.LocaleString;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;

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

    @Resource
    public void setGainObjectSQLService(List<GainObjectSQLService> SQLServiceList) {
        gainObjectSQLService = new HashMap<>();
        for (GainObjectSQLService s : SQLServiceList) {
            gainObjectSQLService.put(s.type(), s);
        }
    }

    @Override
    public DatabaseConnectionDO addDatabaseConnection(DbConnectionCreateDTO request) {
        if (databaseConnectionDAO.getJudgeName(request.getName(), request.getWebUser()) == 0) {
            DatabaseConnectionDO conn = request.toDatabaseConnection();
            try {
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return loginDatabaseConnection(request);
        }
    }

    @Override
    public void deleteDatabaseConnectionList(String id) {
        try {
            databaseConnectionDAO.deleteTable(Integer.parseInt(id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DatabaseConnectionDO databaseAttributeConnection(String id) {
        try {
            DatabaseConnectionDO atabaseConnectionEntity = databaseConnectionDAO.getAttributeById(id, "A");
            atabaseConnectionEntity.setPassword("");
            return atabaseConnectionEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DatabaseConnectionDO> databaseConnectionList(String webUser) {
        try {
            List<DatabaseConnectionDO> databaseConnectionEntity = new ArrayList<>();
            databaseConnectionEntity = databaseConnectionDAO.selectTable(webUser);
            return databaseConnectionEntity;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DatabaseConnectionDO updateDatabaseConnection(DbConnectionCreateDTO request) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DatabaseConnectionDO loginDatabaseConnection(DbConnectionCreateDTO request) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<DataListDTO> schemaObjectList(DatabaseMetaarrayIdSchemaQuery schema) {
        try {
            if (!conMap.containsKey(schema.getUuid())) {
                throw new CustomException(LocaleString.transLanguage("1004"));
            }
            ConnectionDTO connectionDTO = conMap.get(schema.getUuid());
            List<DataListDTO> listDataList = new ArrayList<>();
            DataListDTO dataList = dataListByJdbcService.dataListQuerySQL(
                    connectionDTO.getUrl(),
                    connectionDTO.getDbUser(),
                    connectionDTO.getDbPassword(),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType()).tableSql(schema.getSchema()),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType()).viewSql(schema.getSchema()),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType()).fun_prosSql(schema.getSchema()),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType()).sequenceSql(schema.getSchema()),
                    gainObjectSQLService.get(conMap.get(schema.getUuid()).getType()).synonymSql(schema.getSchema()),
                    schema.getSchema()
            );
            connectionDTO.updataConnectionDTO(connectionDTO);
            ConnectionMapDAO.setConMap(schema.getUuid(), connectionDTO);
            listDataList.add(dataList);
            return listDataList;
        } catch (Exception e) {
            log.info(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String test(DbConnectionCreateDTO request) throws Exception {
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