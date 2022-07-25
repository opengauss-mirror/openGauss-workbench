package com.nctigba.datastudio.service.impl.sql;

import com.nctigba.datastudio.dao.DatabaseConnectionDAO;
import com.nctigba.datastudio.model.DbswitchException;
import com.nctigba.datastudio.model.dto.DataListDTO;
import com.nctigba.datastudio.model.dto.DbConnectionCreateDTO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionDO;
import com.nctigba.datastudio.model.entity.DatabaseConnectionUrlDO;
import com.nctigba.datastudio.model.query.DatabaseMetaarrayIdSchemaQuery;
import com.nctigba.datastudio.service.DataListByJdbcService;
import com.nctigba.datastudio.service.DbConnectionService;
import com.nctigba.datastudio.service.MetaDataByJdbcService;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.nctigba.datastudio.constants.SqlConstants.GET_SCHEMA_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_URL_JDBC;
import static com.nctigba.datastudio.constants.SqlConstants.QUOTES_PARENTHESES_SEMICOLON;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_OBJECT_WHERE_IN_SQL;

@Service
public class DbConnectionServiceImpl implements DbConnectionService {

    @Resource
    private DatabaseConnectionDAO databaseConnectionDAO;

    @Resource
    private MetaDataByJdbcService metaDataByJdbcService;

    @Resource
    private DataListByJdbcService dataListByJdbcService;

    @Override
    public DatabaseConnectionDO addDatabaseConnection(DbConnectionCreateDTO request) {
        if (!Objects.nonNull(databaseConnectionDAO.getByName(request.getName(), "A"))) {

            DatabaseConnectionDO conn = request.toDatabaseConnection();
            try {
                test(request);

                databaseConnectionDAO.insertTable(conn);
                DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(), "A");
                return dataList;
            } catch (Exception e) {
                throw new CustomException(e.getMessage());
            }
        } else {
            return updateDatabaseConnection(request);
        }
    }

    @Override
    public void deleteDatabaseConnection(String id) {
        try {
            databaseConnectionDAO.deleteTable(Integer.parseInt(id));
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public DatabaseConnectionDO databaseAttributeConnection(String id) {
        try {
            DatabaseConnectionDO atabaseConnectionEntity = databaseConnectionDAO.getAttributeById(id, "A");
            atabaseConnectionEntity.setPassword("");
            return atabaseConnectionEntity;
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public List<DatabaseConnectionDO> databaseConnectionList(String webUser) {
        try {
            List<DatabaseConnectionDO> databaseConnectionEntity = new ArrayList<>();
            databaseConnectionEntity = databaseConnectionDAO.selectTable(webUser);
            return databaseConnectionEntity;
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public DatabaseConnectionDO updateDatabaseConnection(DbConnectionCreateDTO request) {
        try {
            DatabaseConnectionDO conn = request.toDatabaseConnection();
            test(request);
            databaseConnectionDAO.updateTable(conn);
            DatabaseConnectionDO dataList = databaseConnectionDAO.getAttributeByName(request.getName(), "A");
            return dataList;
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
    }

    @Override
    public List<DataListDTO> dataList(String id) {

        try {
            DatabaseConnectionUrlDO dbConn = getDatabaseConnectionById(Integer.valueOf(id), "A");
            List<DataListDTO> listDataList = new ArrayList<>();
            List<String> list = dataListByJdbcService.schemaListQuerySQL(
                    dbConn.getUrl(),
                    dbConn.getUserName(),
                    dbConn.getPassword(),
                    GET_SCHEMA_NAME_SQL
            );
            for (int i = 0; i < list.size(); i++) {
                DataListDTO dataList = new DataListDTO();
                dataList = dataListByJdbcService.dataListQuerySQL(
                        dbConn.getUrl(),
                        dbConn.getUserName(),
                        dbConn.getPassword(),
                        "SELECT tablename FROM pg_tables where schemaname ='" + list.get(i) + "';",
                        "select c.relname as viewname from pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace and n.nspname = '" + list.get(i) + SELECT_OBJECT_WHERE_IN_SQL + "v','m" + QUOTES_PARENTHESES_SEMICOLON,
                        "SELECT proname,proargtypes FROM pg_proc\n" +
                                "WHERE pronamespace = (SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '" + list.get(i) + "')\n",

                        "select c.relname as relname from\n" +
                                "pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace \n" +
                                "and n.nspname = '" + list.get(i) + "' where c.relkind = 'S'",
                        "select synname from PG_SYNONYM where synobjschema ='" + list.get(i) + "'",
                        list.get(i)
                );
                listDataList.add(dataList);
            }
            return listDataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }


    @Override
    public List<DataListDTO> schemaObjectList(DatabaseMetaarrayIdSchemaQuery schema) {
        try {
            DatabaseConnectionUrlDO dbConn = getDatabaseConnectionByName(schema.getConnectionName(), schema.getWebUser());
            List<DataListDTO> listDataList = new ArrayList<>();
                DataListDTO dataList = new DataListDTO();
                dataList = dataListByJdbcService.dataListQuerySQL(
                        dbConn.getUrl(),
                        dbConn.getUserName(),
                        dbConn.getPassword(),
                        "SELECT tablename FROM pg_tables where schemaname ='" + schema.getSchema() + "';",
                        "select c.relname as viewname from pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace and n.nspname = '" + schema.getSchema() + SELECT_OBJECT_WHERE_IN_SQL + "v','m" + QUOTES_PARENTHESES_SEMICOLON,
                        "SELECT proname,proargtypes FROM pg_proc\n" +
                                "WHERE pronamespace = (SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '" + schema.getSchema() + "')\n",

                        "select c.relname as relname from\n" +
                                "pg_class c LEFT JOIN pg_namespace n ON n.oid = c.relnamespace \n" +
                                "and n.nspname = '" + schema.getSchema() + "' where c.relkind = 'S'",
                        "select synname from PG_SYNONYM where synobjschema ='" + schema.getSchema() + "'",
                        schema.getSchema()
                );
                listDataList.add(dataList);
            return listDataList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(e.getMessage());
        }
    }
    @Override
    public void test(DbConnectionCreateDTO request) throws Exception {
        metaDataByJdbcService.testQuerySQL(
                GET_URL_JDBC + request.getIp() + ":" + request.getPort() + "/" + request.getDataName() + "?connectTimeout=30&socketTimeout=0",
                request.getUserName(),
                request.getPassword(),
                "SELECT 1"
        );
    }

    public DatabaseConnectionUrlDO getDatabaseConnectionById(Integer id, String webUser) {
        DatabaseConnectionUrlDO dbConn = databaseConnectionDAO.getById(id, webUser);
        if (Objects.isNull(dbConn)) {
            throw new DbswitchException( "not found id=" + id);
        }

        return dbConn;
    }

    public DatabaseConnectionUrlDO getDatabaseConnectionByName(String name, String webUser) {
        DatabaseConnectionUrlDO dbConn = databaseConnectionDAO.getByName(name, webUser);
        if (Objects.isNull(dbConn)) {
            throw new DbswitchException( "not found name=" + name);
        }

        return dbConn;
    }
}
