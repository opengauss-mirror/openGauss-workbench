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
 *  DbConnectionServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/service/impl/sql/DbConnectionServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.compatible.GainObjectSQLService;
import com.nctigba.datastudio.dao.ConnectionMapDAO;
import com.nctigba.datastudio.dao.DatabaseConnectionDAO;
import com.nctigba.datastudio.enums.ParamTypeEnum;
import com.nctigba.datastudio.model.dto.ConnectionDTO;
import com.nctigba.datastudio.model.dto.ConnectionTimeLengthDTO;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.dto.GetConnectionAttributeDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayIdSchemaQuery;
import com.nctigba.datastudio.service.DataListByJdbcService;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.MetaDataByJdbcService;
import com.nctigba.datastudio.utils.ConnectionUtils;
import com.nctigba.datastudio.utils.DebugUtils;
import com.nctigba.datastudio.utils.ExecuteUtils;
import com.nctigba.datastudio.utils.LocaleStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.nctigba.datastudio.constants.CommonConstants.FDW_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.PARTTYPE;
import static com.nctigba.datastudio.constants.CommonConstants.PKG_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_ARG_TYPES;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.PRO_PACKAGE_ID;
import static com.nctigba.datastudio.constants.CommonConstants.REL_NAME;
import static com.nctigba.datastudio.constants.SqlConstants.CONFIGURE_TIME;
import static com.nctigba.datastudio.constants.SqlConstants.GET_TYPENAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;
import static com.nctigba.datastudio.constants.SqlConstants.SPACE;
import static com.nctigba.datastudio.dao.ConnectionMapDAO.conMap;
import static com.nctigba.datastudio.utils.DebugUtils.comGetUuidType;

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
            conn.setEdition(getVersion(request));
            databaseConnectionDAO.insertTable(conn);
            DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(),
                    request.getWebUser());
            String uuid;
            if (StringUtils.isNotEmpty(request.getConnectionid())) {
                uuid = request.getConnectionid();
            } else {
                uuid = UUID.randomUUID().toString();
            }
            dataList.setConnectionid(uuid);
            ConnectionDTO connectionDTO = new ConnectionDTO();
            connectionDTO.setIpConnectionDTO(dataList);
            ConnectionMapDAO.setConMap(uuid, connectionDTO);
            dataList.setPassword("");
            dataList.setIsRememberPassword(request.getIsRememberPassword());
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
    public DatabaseConnectionDO databaseAttributeConnection(GetConnectionAttributeDTO request) {
        DatabaseConnectionDO databaseConnectionEntity = databaseConnectionDAO.getAttributeById(request.getId(),
                request.getWebUser());
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
        conn.setEdition(getVersion(request));
        connectionMapDAO.deleteConnection(request.getConnectionid());
        databaseConnectionDAO.updateTable(conn);
        DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(),
                request.getWebUser());
        String uuid;
        if (StringUtils.isNotEmpty(request.getConnectionid())) {
            uuid = request.getConnectionid();
        } else {
            uuid = UUID.randomUUID().toString();
        }
        dataList.setConnectionid(uuid);
        ConnectionDTO connectionDTO = new ConnectionDTO();
        DatabaseConnectionUrlDO databaseConnectionUrlDO = databaseConnectionDAO.getByName(request.getName(),
                request.getWebUser());
        connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        dataList.setIsRememberPassword(request.getIsRememberPassword());
        return dataList;
    }

    @Override
    public DatabaseConnectionDO loginDatabaseConnection(DbConnectionCreateDTO request) {
        DatabaseConnectionDO conn = request.toDatabaseConnection();
        conn.setEdition(getVersion(request));
        databaseConnectionDAO.updateTable(conn);
        DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(),
                request.getWebUser());
        String uuid;
        if (StringUtils.isNotEmpty(request.getConnectionid())) {
            uuid = request.getConnectionid();
        } else {
            uuid = UUID.randomUUID().toString();
        }
        dataList.setConnectionid(uuid);
        ConnectionDTO connectionDTO = new ConnectionDTO();
        DatabaseConnectionUrlDO databaseConnectionUrlDO = databaseConnectionDAO.getByName(request.getName(),
                request.getWebUser());
        connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        dataList.setIsRememberPassword(request.getIsRememberPassword());
        return dataList;
    }

    @Override
    public DatabaseConnectionDO loginConnection(DbConnectionCreateDTO request) {
        DatabaseConnectionDO dataList;
        if ("y".equals(request.getIsRememberPassword())) {
            dataList = databaseConnectionDAO.getAttributeByName(request.getName(),
                    request.getWebUser());
            dataList.setConnectionid(request.getConnectionid());
            ConnectionDTO connectionDTO = new ConnectionDTO();
            DatabaseConnectionUrlDO databaseConnectionUrlDO = databaseConnectionDAO.getByName(request.getName(),
                    request.getWebUser());
            connectionDTO.setConnectionDTO(databaseConnectionUrlDO);
            ConnectionMapDAO.setConMap(request.getConnectionid(), connectionDTO);
        } else {
            dataList = loginDatabaseConnection(request);
        }
        dataList.setIsRememberPassword(request.getIsRememberPassword());
        return dataList;
    }

    @Override
    public void timeLength(ConnectionTimeLengthDTO request) {
        ConnectionDTO connectionDTO = conMap.get(request.getUuid());
        connectionDTO.setTimeLength(request.getTimeLength());
        ConnectionMapDAO.setConMap(request.getUuid(), connectionDTO);
    }

    @Override
    public Integer getTimeLength(String uuid) {
        ConnectionDTO connectionDTO = conMap.get(uuid);
        return connectionDTO.getTimeLength();
    }

    @Override
    public List<DataListDTO> schemaObjectList(DatabaseMetaArrayIdSchemaQuery schema) {
        if (!conMap.containsKey(schema.getUuid())) {
            throw new CustomException(LocaleStringUtils.transLanguage("1004"));
        }
        ConnectionDTO connectionDTO = conMap.get(schema.getUuid());
        List<DataListDTO> listDataList = new ArrayList<>();
        DataListDTO dataList;
        try {
            dataList = dataListByJdbcService.dataListQuerySQL(
                    connectionDTO.getUrl(),
                    connectionDTO.getDbUser(),
                    connectionDTO.getDbPassword(),
                    gainObjectSQLService.get(comGetUuidType(schema.getUuid()))
                            .tableSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(comGetUuidType(schema.getUuid()))
                            .viewSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(comGetUuidType(schema.getUuid()))
                            .funProSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(comGetUuidType(schema.getUuid()))
                            .sequenceSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(comGetUuidType(schema.getUuid()))
                            .synonymSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(comGetUuidType(schema.getUuid()))
                            .foreignTableSql(DebugUtils.needQuoteName(schema.getSchema())),
                    gainObjectSQLService.get(comGetUuidType(schema.getUuid()))
                            .triggerSql(DebugUtils.needQuoteName(schema.getSchema())),
                    schema.getSchema()
            );
        } catch (SQLException | InterruptedException e) {
            throw new CustomException(e.getMessage());
        }
        connectionDTO.updateConnectionDTO(connectionDTO);
        ConnectionMapDAO.setConMap(schema.getUuid(), connectionDTO);
        listDataList.add(dataList);
        return listDataList;
    }

    @Override
    public List<Map<String, Object>> schemaObjects(DatabaseMetaArrayIdSchemaQuery query) throws SQLException {
        log.info("DbConnectionServiceImpl schemaObjects query: " + query);
        String uuid = query.getUuid();
        if (!conMap.containsKey(uuid)) {
            throw new CustomException(LocaleStringUtils.transLanguage("1004"));
        }

        List<Map<String, Object>> list = new ArrayList<>();
        ConnectionDTO connectionDTO = conMap.get(uuid);
        try (
                Connection connection = ConnectionUtils.connectGet(
                        connectionDTO.getUrl(), connectionDTO.getDbUser(), connectionDTO.getDbPassword());
                Statement statement = connection.createStatement();
        ) {
            list = parseByType(query, uuid, list, statement);
        }

        connectionDTO.updateConnectionDTO(connectionDTO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        log.info("DbConnectionServiceImpl schemaObjects list: " + list);
        return list;
    }

    @Override
    public Map<String, Integer> schemaObjectCount(DatabaseMetaArrayIdSchemaQuery query) throws SQLException {
        log.info("DbConnectionServiceImpl schemaObjectCount query: " + query);
        String uuid = query.getUuid();
        if (!conMap.containsKey(uuid)) {
            throw new CustomException(LocaleStringUtils.transLanguage("1004"));
        }

        Map<String, Integer> map = new HashMap<>();
        ConnectionDTO connectionDTO = conMap.get(uuid);
        try (
                Connection connection = ConnectionUtils.connectGet(
                        connectionDTO.getUrl(), connectionDTO.getDbUser(), connectionDTO.getDbPassword());
        ) {
            GainObjectSQLService service = gainObjectSQLService.get(comGetUuidType(uuid));
            String schema = DebugUtils.needQuoteName(query.getSchema());
            Object tableCount = ExecuteUtils.executeGetOne(connection, service.tableCountSql(schema));
            Object funProCount = ExecuteUtils.executeGetOne(connection, service.funProCountSql(schema));
            Object sequenceCount = ExecuteUtils.executeGetOne(connection, service.sequenceCountSql(schema));
            Object viewCount = ExecuteUtils.executeGetOne(connection, service.viewCountSql(schema));
            Object synonymCount = ExecuteUtils.executeGetOne(connection, service.synonymCountSql(schema));
            Object foreignTableCount = ExecuteUtils.executeGetOne(connection, service.foreignTableCountSql(schema));
            Object triggerCount = ExecuteUtils.executeGetOne(connection, service.triggerCountSql(schema));
            map.put("table", DebugUtils.changeTypeToInteger(tableCount));
            map.put("function", DebugUtils.changeTypeToInteger(funProCount));
            map.put("sequence", DebugUtils.changeTypeToInteger(sequenceCount));
            map.put("view", DebugUtils.changeTypeToInteger(viewCount));
            map.put("synonym", DebugUtils.changeTypeToInteger(synonymCount));
            map.put("foreignTable", DebugUtils.changeTypeToInteger(foreignTableCount));
            map.put("trigger", DebugUtils.changeTypeToInteger(triggerCount));
        }

        connectionDTO.updateConnectionDTO(connectionDTO);
        ConnectionMapDAO.setConMap(uuid, connectionDTO);
        log.info("DbConnectionServiceImpl schemaObjectCount map: " + map);
        return map;
    }

    private List<Map<String, Object>> parseByType(
            DatabaseMetaArrayIdSchemaQuery query, String uuid, List<Map<String, Object>> list,
            Statement statement) throws SQLException {
        String schema = DebugUtils.needQuoteName(query.getSchema());
        GainObjectSQLService service = gainObjectSQLService.get(comGetUuidType(uuid));
        switch (query.getType()) {
            case "table":
                ResultSet tableResultSet = statement.executeQuery(service.tableSql(schema));
                list = parseTableResult(tableResultSet);
                break;
            case "function":
                ResultSet functionTypeResultSet = statement.executeQuery(GET_TYPENAME_SQL);
                Map<String, String> typeMap = parseTypeResultSet(functionTypeResultSet);
                ResultSet functionResultSet = statement.executeQuery(service.funProSql(schema));
                list = parseFunctionResult(typeMap, functionResultSet);
                break;
            case "sequence":
                ResultSet sequenceResultSet = statement.executeQuery(service.sequenceSql(schema));
                list = parseSequenceResult(sequenceResultSet);
                break;
            case "view":
                ResultSet viewResultSet = statement.executeQuery(service.viewSql(schema));
                list = parseViewResult(viewResultSet);
                break;
            case "synonym":
                ResultSet synonymResultSet = statement.executeQuery(service.synonymSql(schema));
                list = parseSynonymResult(synonymResultSet);
                break;
            case "foreignTable":
                ResultSet foreignTableResultSet = statement.executeQuery(service.foreignTableSql(schema));
                list = parseForeignTableResult(foreignTableResultSet);
                break;
            case "trigger":
                ResultSet triggerResultSet = statement.executeQuery(service.triggerSql(schema));
                list = parseTriggerResult(triggerResultSet);
                break;
            default:
                break;
        }
        return list;
    }

    private List<Map<String, Object>> parseTableResult(ResultSet resultSet) throws SQLException {
        log.info("DbConnectionServiceImpl parseTableResult start: ");
        List<Map<String, Object>> list = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put(OID, resultSet.getString(OID));
            map.put(NAME, resultSet.getString("tablename"));
            map.put(PARTTYPE, resultSet.getString(PARTTYPE));
            list.add(map);
        }
        log.info("DbConnectionServiceImpl parseTableResult list: " + list);
        return list;
    }

    private Map<String, String> parseTypeResultSet(ResultSet resultSet) throws SQLException {
        Map<String, String> typeMap = new HashMap<>();
        while (resultSet.next()) {
            typeMap.put(resultSet.getString(OID), resultSet.getString("typname"));
        }
        return typeMap;
    }

    private List<Map<String, Object>> parseFunctionResult(
            Map<String, String> typeMap, ResultSet resultSet) throws SQLException {
        log.info("DbConnectionServiceImpl parseFunctionResult start: ");
        Map<String, List<Map<String, String>>> packageMap = new HashMap<>();
        List<Map<String, Object>> list = parseResult(typeMap, resultSet, packageMap);

        for (String key : packageMap.keySet()) {
            List<Map<String, String>> childrenList = packageMap.get(key);
            Map<String, Object> map = new HashMap<>();
            map.put(OID, childrenList.get(0).get(PRO_PACKAGE_ID));
            map.put(NAME, childrenList.get(0).get(PKG_NAME));
            map.put("isPackage", true);
            map.put("children", childrenList);
            list.add(map);
        }
        log.info("DbConnectionServiceImpl parseTableResult list: " + list);
        return list;
    }

    private List<Map<String, Object>> parseResult(
            Map<String, String> typeMap, ResultSet resultSet,
            Map<String, List<Map<String, String>>> packageMap) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        while (resultSet.next()) {
            String proArgTypes = resultSet.getString(PRO_ARG_TYPES);
            String[] split = proArgTypes.split(SPACE);
            StringBuilder asd = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                if (!StringUtils.isEmpty(split[i])) {
                    asd.append(ParamTypeEnum.parseType(typeMap.get(split[i])));
                    if (split.length - 1 != i) {
                        asd.append(",");
                    }
                }
            }

            String proPackageId = resultSet.getString(PRO_PACKAGE_ID);
            String oid = resultSet.getString(OID);
            String proName = DebugUtils.needQuoteName(resultSet.getString(PRO_NAME));
            Map<String, Object> map = new HashMap<>();
            if ("0".equals(proPackageId)) {
                map.put(OID, oid);
                map.put(NAME, proName + "(" + asd + ")");
                map.put("isPackage", false);
                list.add(map);
            } else {
                Map<String, String> childrenMap = new HashMap<>();
                childrenMap.put(OID, oid);
                childrenMap.put(NAME, proName);
                childrenMap.put(PRO_PACKAGE_ID, proPackageId);
                childrenMap.put(PKG_NAME, resultSet.getString(PKG_NAME));
                if (packageMap.containsKey(proPackageId)) {
                    List<Map<String, String>> childrenList = packageMap.get(proPackageId);
                    childrenList.add(childrenMap);
                } else {
                    List<Map<String, String>> childrenList = new ArrayList<>();
                    childrenList.add(childrenMap);
                    packageMap.put(proPackageId, childrenList);
                }
            }
        }
        log.info("DbConnectionServiceImpl parseResult list: " + list);
        return list;
    }

    private List<Map<String, Object>> parseSequenceResult(ResultSet resultSet) throws SQLException {
        log.info("DbConnectionServiceImpl parseSequenceResult start: ");
        List<Map<String, Object>> list = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put(OID, resultSet.getString(OID));
            map.put(NAME, resultSet.getString(REL_NAME));
            list.add(map);
        }
        log.info("DbConnectionServiceImpl parseSequenceResult list: " + list);
        return list;
    }

    private List<Map<String, Object>> parseViewResult(ResultSet resultSet) throws SQLException {
        log.info("DbConnectionServiceImpl parseViewResult start: ");
        List<Map<String, Object>> list = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put(OID, resultSet.getString(OID));
            map.put(NAME, resultSet.getString("viewname"));
            list.add(map);
        }
        log.info("DbConnectionServiceImpl parseViewResult list: " + list);
        return list;
    }

    private List<Map<String, Object>> parseSynonymResult(ResultSet resultSet) throws SQLException {
        log.info("DbConnectionServiceImpl parseSynonymResult start: ");
        List<Map<String, Object>> list = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put(OID, resultSet.getString(OID));
            map.put(NAME, resultSet.getString("synname"));
            list.add(map);
        }
        log.info("DbConnectionServiceImpl parseSynonymResult list: " + list);
        return list;
    }

    private List<Map<String, Object>> parseForeignTableResult(ResultSet resultSet) throws SQLException {
        log.info("DbConnectionServiceImpl parseForeignTableResult start: ");
        List<Map<String, Object>> list = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put(OID, resultSet.getString(OID));
            map.put(NAME, resultSet.getString("tablename"));
            map.put(PARTTYPE, resultSet.getString(PARTTYPE));
            map.put(FDW_NAME, resultSet.getString(FDW_NAME));
            list.add(map);
        }
        log.info("DbConnectionServiceImpl parseForeignTableResult list: " + list);
        return list;
    }

    private List<Map<String, Object>> parseTriggerResult(ResultSet resultSet) throws SQLException {
        log.info("DbConnectionServiceImpl parseTriggerResult start: ");
        List<Map<String, Object>> list = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put(OID, resultSet.getString(OID));
            map.put(NAME, resultSet.getString("tgname"));
            map.put("tableName", resultSet.getString(REL_NAME));
            map.put("isTableTrigger", "r".equals(resultSet.getString("relkind")));

            String tgEnabled = resultSet.getString("tgenabled");
            if (tgEnabled.equals("D")) {
                map.put("enabled", false);
            } else {
                map.put("enabled", true);
            }
            list.add(map);
        }
        log.info("DbConnectionServiceImpl parseTriggerResult list: " + list);
        return list;
    }


    private String getVersion(DbConnectionCreateDTO request) {
        return metaDataByJdbcService.versionSQL(
                GET_URL_JDBC + request.getIp() + ":" + request.getPort() + "/" + request.getDataName()
                        + CONFIGURE_TIME,
                request.getUserName(),
                request.getPassword(),
                gainObjectSQLService.get(request.getType()).databaseVersion()
        );
    }

    @Override
    public Long test(DbConnectionCreateDTO request) {
        log.info("test {}", request);
        try{
            long start = System.currentTimeMillis();
            Connection connection = ConnectionUtils.connectGet(
                    GET_URL_JDBC + request.getIp() + ":" + request.getPort() + "/" + request.getDataName()
                            + CONFIGURE_TIME,
                    request.getUserName(),
                    request.getPassword()
            );
            long time = System.currentTimeMillis() - start;
            connection.close();
            return time;
        }catch (SQLException e) {
            throw new CustomException(e.getMessage());
        }
    }
}