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
 *  GainObjectSQLServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/compatible/opengauss/GainObjectSQLServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.compatible.opengauss;

import com.nctigba.datastudio.compatible.GainObjectSQLService;
import com.nctigba.datastudio.config.ConnectionConfig;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayColumnQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArrayQuery;
import com.nctigba.datastudio.model.query.DatabaseMetaArraySchemaQuery;
import com.nctigba.datastudio.utils.DebugUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nctigba.datastudio.constants.CommonConstants.NAME;
import static com.nctigba.datastudio.constants.CommonConstants.NSP_NAME;
import static com.nctigba.datastudio.constants.CommonConstants.OID;
import static com.nctigba.datastudio.constants.CommonConstants.OPENGAUSS;
import static com.nctigba.datastudio.constants.CommonConstants.REL_NAME;
import static com.nctigba.datastudio.constants.SqlConstants.GET_DATABASE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.GET_SCHEMA_NAME_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.LF;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_COLUMN_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_FUNCTION_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_OBJECT_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_TABLE_SQL;
import static com.nctigba.datastudio.constants.SqlConstants.SELECT_VIEW_SQL;

/**
 * GainObjectSQLService achieve
 *
 * @since 2023-06-26
 */
@Slf4j
@Service
public class GainObjectSQLServiceImpl implements GainObjectSQLService {
    @Autowired
    private ConnectionConfig connectionConfig;

    @Override
    public String type() {
        return OPENGAUSS;
    }

    @Override
    public String databaseList() {
        String ddl;
        ddl = GET_DATABASE_SQL;
        log.info("databaseListDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public List<Map<String, String>> schemaList(DatabaseMetaArraySchemaQuery request) throws SQLException {
        log.info("schemaList request is: " + request);
        List<Map<String, String>> schemaList = new ArrayList<>();
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(GET_SCHEMA_NAME_SQL)
        ) {
            while (resultSet.next()) {
                Map<String, String> map = new HashMap<>();
                map.put(OID, resultSet.getString(OID));
                map.put(NAME, resultSet.getString(NSP_NAME));
                schemaList.add(map);
            }
            log.info("schemaList response is: " + schemaList);
            return schemaList;
        }
    }

    @Override
    public List<String> objectList(DatabaseMetaArrayQuery request) throws SQLException {
        log.info("objectList request is: " + request);
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement()
        ) {
            List<String> objectList = new ArrayList<>();
            if (request.getObjectType().equals("ALL")) {
                try (
                        ResultSet resultSet = statement.executeQuery(
                                String.format(SELECT_FUNCTION_SQL, DebugUtils.needQuoteName(request.getSchema())))
                ) {
                    while (resultSet.next()) {
                        objectList.add(resultSet.getString("proname"));
                    }
                }
                try (
                        ResultSet resultSetView = statement.executeQuery(
                                String.format(SELECT_VIEW_SQL, DebugUtils.needQuoteName(request.getSchema())))
                ) {
                    while (resultSetView.next()) {
                        objectList.add(resultSetView.getString("viewname"));
                    }
                }
                try (
                        ResultSet resultSetTable = statement.executeQuery(
                                String.format(SELECT_TABLE_SQL, DebugUtils.needQuoteName(request.getSchema())))
                ) {
                    while (resultSetTable.next()) {
                        objectList.add(resultSetTable.getString(REL_NAME));
                    }
                }
                return objectList;
            } else if (request.getObjectType().equals("FUN_PRO")) {
                try (
                        ResultSet resultSet = statement.executeQuery(
                                String.format(SELECT_FUNCTION_SQL, DebugUtils.needQuoteName(request.getSchema())))
                ) {
                    while (resultSet.next()) {
                        objectList.add(resultSet.getString("proname"));
                    }
                    return objectList;
                }
            } else if (request.getObjectType().equals("VIEW")) {
                try (
                        ResultSet resultSet = statement.executeQuery(
                                String.format(SELECT_VIEW_SQL, DebugUtils.needQuoteName(request.getSchema())))
                ) {
                    while (resultSet.next()) {
                        objectList.add(resultSet.getString("viewname"));
                    }
                    return objectList;
                }
            } else {
                try (
                        ResultSet resultSet = statement.executeQuery(
                                String.format(SELECT_OBJECT_SQL, DebugUtils.needQuoteName(request.getSchema()),
                                        DebugUtils.needQuoteName(request.getObjectType())))
                ) {
                    while (resultSet.next()) {
                        objectList.add(resultSet.getString(REL_NAME));
                    }
                    log.info("objectList response is: " + objectList);
                    return objectList;
                }
            }
        }
    }

    @Override
    public List<String> tableColumnList(DatabaseMetaArrayColumnQuery request) throws SQLException {
        log.info("tableColumnList request is: " + request);
        List<String> columnList = new ArrayList<>();
        try (
                Connection connection = connectionConfig.connectDatabase(request.getUuid());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        String.format(SELECT_COLUMN_SQL, DebugUtils.needQuoteName(request.getSchema()),
                                request.getObjectName()))
        ) {
            while (resultSet.next()) {
                columnList.add(resultSet.getString("column_name"));
            }
            log.info("tableColumnList response is: " + columnList);
            return columnList;
        }
    }

    @Override
    public String databaseVersion() {
        String ddl;
        ddl = " select version();";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableSql(String schema) {
        String ddl;
        ddl = "select x.oid,relname as tablename,parttype" + LF
                + "  from (select tbl.oid as oid, tbl.relname relname,"
                + "         case when parttype='n' then 'n' else 'y' end as parttype  " + LF
                + "         from pg_class tbl" + LF
                + "         inner join pg_namespace ns on tbl.relnamespace = ns.oid" + LF
                + "         where tbl.relkind = 'r'" + LF
                + "           and ns.nspname = '" + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "' ) x" + LF
                + " where has_table_privilege(x.oid, 'SELECT')"
                + " order by relname";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tableCountSql(String schema) {
        String ddl;
        ddl = "select count(1)" + LF
                + "  from (select tbl.oid as oid, tbl.relname relname,"
                + "         case when parttype='n' then 'n' else 'y' end as parttype  " + LF
                + "         from pg_class tbl" + LF
                + "         inner join pg_namespace ns on tbl.relnamespace = ns.oid" + LF
                + "         where tbl.relkind = 'r'" + LF
                + "           and ns.nspname = '" + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "' ) x" + LF
                + " where has_table_privilege(x.oid, 'SELECT')";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String foreignTableSql(String schema) {
        String ddl;
        ddl = "select x.oid,relname as tablename,parttype,fdwname" + LF
                + "  from (select tbl.oid as oid, tbl.relname relname,fdwname,"
                + "         case when parttype='n' then 'n' else 'y' end as parttype  " + LF
                + "         from pg_class tbl" + LF
                + "         inner join pg_namespace ns on tbl.relnamespace = ns.oid" + LF
                + "         inner join pg_foreign_table pft on pft.ftrelid = tbl.relfilenode" + LF
                + "         inner join pg_foreign_server pfs on pfs.oid = pft.ftserver" + LF
                + "         inner join pg_foreign_data_wrapper pfdw on pfdw.oid = pfs.srvfdw" + LF
                + "         where tbl.relkind = 'f'" + LF
                + "           and ns.nspname = '" + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "' ) x" + LF
                + " where has_table_privilege(x.oid, 'SELECT')"
                + " order by relname";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String foreignTableCountSql(String schema) {
        String ddl;
        ddl = "select count(1)" + LF
                + "  from (select tbl.oid as oid, tbl.relname relname,fdwname,"
                + "         case when parttype='n' then 'n' else 'y' end as parttype  " + LF
                + "         from pg_class tbl" + LF
                + "         inner join pg_namespace ns on tbl.relnamespace = ns.oid" + LF
                + "         inner join pg_foreign_table pft on pft.ftrelid = tbl.relfilenode" + LF
                + "         inner join pg_foreign_server pfs on pfs.oid = pft.ftserver" + LF
                + "         inner join pg_foreign_data_wrapper pfdw on pfdw.oid = pfs.srvfdw" + LF
                + "         where tbl.relkind = 'f'" + LF
                + "           and ns.nspname = '" + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "' ) x" + LF
                + " where has_table_privilege(x.oid, 'SELECT');";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String triggerSql(String schema) {
        String ddl;
        ddl = "select pt.oid, pt.tgname, pt.tgenabled, pc.relname, pc.relkind from pg_trigger pt " + LF
                + "left join pg_class pc on pc.oid = pt.tgrelid " + LF
                + "left join pg_namespace pn on pn.nspowner = pc.relowner" + LF
                + "where pn.nspname = '" + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "' order by pt.tgname";
        log.info("triggerSql ddl is: " + ddl);
        return ddl;
    }

    @Override
    public String triggerCountSql(String schema) {
        String ddl;
        ddl = "select count(1) from pg_trigger pt " + LF
                + "left join pg_class pc on pc.oid = pt.tgrelid " + LF
                + "left join pg_namespace pn on pn.nspowner = pc.relowner" + LF
                + "where pn.nspname = '" + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "';";
        log.info("triggerSql ddl is: " + ddl);
        return ddl;
    }

    @Override
    public String viewSql(String schema) {
        String ddl;
        ddl = "select c.oid,c.relname as viewname from pg_class c INNER JOIN pg_namespace n "
                + "ON n.oid = c.relnamespace and n.nspname = '"
                + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "' where c.relkind in ('v','m" + "') order by c.relname;";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String viewCountSql(String schema) {
        String ddl;
        ddl = "select count(1) from pg_class c INNER JOIN pg_namespace n "
                + "ON n.oid = c.relnamespace and n.nspname = '"
                + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "' where c.relkind in ('v','m" + "');";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String funProSql(String schema) {
        String ddl;
        ddl = "SELECT pp.oid,pp.proname,pp.proargtypes,pp.propackageid,gp.pkgname FROM pg_proc pp "
                + "left join gs_package gp on gp.oid = pp.propackageid WHERE pronamespace = "
                + "(SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '"
                + DebugUtils.containsSqlInjection(schema) + "') order by pp.proname";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String funProCountSql(String schema) {
        String ddl;
        ddl = "select sum(count) as count from ("
                + "  select count(distinct propackageid) FROM pg_proc pp "
                + "  left join gs_package gp on gp.oid = pp.propackageid WHERE pronamespace = "
                + "  (SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '"
                + DebugUtils.containsSqlInjection(schema) + "') and pp.propackageid != 0 "
                + "  union all\n"
                + "  select count(1) FROM pg_proc pp\n"
                + "  left join gs_package gp on gp.oid = pp.propackageid WHERE pronamespace = "
                + "  (SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '"
                + DebugUtils.containsSqlInjection(schema) + "') and pp.propackageid = 0 "
                + ");";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String sequenceSql(String schema) {
        String ddl;
        ddl = "select c.oid,c.relname as relname from" + LF
                + "pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace " + LF
                + "and n.nspname = '" + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "' where c.relkind = 'S'" + LF
                + " order by c.relname";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String sequenceCountSql(String schema) {
        String ddl;
        ddl = "select count(1) from" + LF
                + "pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace " + LF
                + "and n.nspname = '" + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema))
                + "' where c.relkind = 'S';";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String synonymSql(String schema) {
        String ddl;
        ddl = "select pgs.oid,synname from PG_SYNONYM pgs, pg_namespace pgn" + LF
                + "   where pgn.oid = pgs.synnamespace" + LF
                + "     and pgn.nspname  ='"
                + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema)) + "'" + LF
                + " order by synname";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String synonymCountSql(String schema) {
        String ddl;
        ddl = "select count(1) from PG_SYNONYM pgs, pg_namespace pgn" + LF
                + "   where pgn.oid = pgs.synnamespace" + LF
                + "     and pgn.nspname  ='"
                + DebugUtils.containsSqlInjection(DebugUtils.needQuoteName(schema)) + "';";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String baseTypeListSQL() {
        String ddl;
        ddl = "select distinct(pg_catalog.format_type(typ.oid,typ.typtypmod)) as type" + LF
                + "from pg_type typ left join pg_description des on (typ.oid = des.objoid) " + LF
                + "where typ.typnamespace in (select oid from pg_namespace where nspname in " + LF
                + "('information_schema', 'pg_catalog'))" + LF
                + "and typtype = 'b'" + LF
                + "order by 1";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tablespaceListSQL() {
        String ddl;
        ddl = "select spcname from pg_tablespace order by spcname";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String tablespaceOidListSQL() {
        String ddl;
        ddl = "select oid,spcname from pg_tablespace order by spcname";
        log.info("splicingSequenceDDL response is: " + ddl);
        return ddl;
    }

    @Override
    public String userSql() {
        return "SELECT rolname,case when rolcanlogin = true then '1' ELSE '0' end as rolcanlogin,"
                + "oid FROM pg_catalog.pg_roles WHERE rolsuper = false order by rolname";
    }

    @Override
    public String resourceListSQL() {
        return "select respool_name from PG_RESOURCE_POOL";
    }
}
