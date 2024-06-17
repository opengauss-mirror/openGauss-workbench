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
 *  SqlConstants.java
 *
 *  IDENTIFICATION
 *  plugins/data-studio/src/main/java/com/nctigba/datastudio/constants/SqlConstants.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.datastudio.constants;

/**
 * SqlConstants
 *
 * @since 2023-06-25
 */
public class SqlConstants {
    /**
     * point
     */
    public static final String POINT = ".";

    /**
     * LF
     */
    public static final String LF = System.getProperty("line.separator");

    /**
     * space
     */
    public static final String SPACE = " ";

    /**
     * left bracket
     */
    public static final String LEFT_BRACKET = "(";

    /**
     * right bracket
     */
    public static final String RIGHT_BRACKET = ")";

    /**
     * semicolon
     */
    public static final String SEMICOLON = ";";

    /**
     * quotes
     */
    public static final String QUOTES = "'";

    /**
     * quotes lf comma
     */
    public static final String QUOTES_LF_COMMA = "," + LF;

    /**
     * comma
     */
    public static final String COMMA = ",";

    /**
     * AND
     */
    public static final String AND = " and ";

    /**
     * comma space
     */
    public static final String COMMA_SPACE = ", ";

    /**
     * comment on column sql
     */
    public static final String COMMENT_ON_COLUMN_SQL = "COMMENT ON COLUMN %s.%s.%s IS '%s';" + LF;

    /**
     * configure time
     */
    public static final String CONFIGURE_TIME = "?connectTimeout=30&socketTimeout=0";

    /**
     * proc sql
     */
    public static final String PROC_SQL = "select pn.nspname, pp.proname, pp.pronargs, pp.proargtypes, "
            + "pp.proallargtypes, pp.proargnames, pp.proargmodes, pp.prokind "
            + "from pg_proc pp left join pg_namespace pn on pp.pronamespace = pn.oid where pp.oid =%s;";

    /**
     * query oid by oackage
     */
    public static final String QUERY_OID_BY_PACKAGE = "select oid from pg_proc where propackageid in (%s)";

    /**
     * query def sql
     */
    public static final String QUERY_DEF_SQL = "select definition from PG_GET_FUNCTIONDEF(%s);";

    /**
     * query package sql
     */
    public static final String QUERY_PACKAGE_SQL = "select pkgname, nspname, pkgspecsrc, pkgbodydeclsrc "
            + "from gs_package gp left join pg_namespace pn on gp.pkgnamespace = pn.oid "
            + "where pn.nspname = '%s' and gp.oid = %s;";

    /**
     * query oid sql
     */
    public static final String QUERY_OID_SQL = "select pp.oid from pg_proc pp "
            + "left join pg_namespace pn on pp.pronamespace = pn.oid where pp.proname = '%s' and pn.nspname = '%s';";

    /**
     * turn on sql
     */
    public static final String TURN_ON_SQL = "select * from dbe_pldebugger.turn_on(%s);";

    /**
     * attach sql
     */
    public static final String ATTACH_SQL = "select * from dbe_pldebugger.attach('%s',%s);";

    /**
     * add breakpoint sql
     */
    public static final String ADD_BREAKPOINT_SQL = "select * from dbe_pldebugger.add_breakpoint(%s,%s);";

    /**
     * info breakpoint pre
     */
    public static final String INFO_BREAKPOINT_PRE = "select funcoid, lineno + ";

    /**
     * info breakpoint sql
     */
    public static final String INFO_BREAKPOINT_SQL = " as lineno, query, enable from dbe_pldebugger.info_breakpoints();";

    /**
     * continue sql
     */
    public static final String CONTINUE_SQL = "select * from dbe_pldebugger.continue();";

    /**
     * backtrace sql pre
     */
    public static final String BACKTRACE_SQL_PRE = "select funcoid, frameno, funcname, lineno + ";

    /**
     * backtrace sql
     */
    public static final String BACKTRACE_SQL = " as lineno from dbe_pldebugger.backtrace();";

    /**
     * info locals sql
     */
    public static final String INFO_LOCALS_SQL = "select varname, value, vartype,"
            + " isconst from dbe_pldebugger.info_locals();";

    /**
     * delete breakpoint sql
     */
    public static final String DELETE_BREAKPOINT_SQL = "select * from dbe_pldebugger.delete_breakpoint(%s);";

    /**
     * disable breakpoint sql
     */
    public static final String DISABLE_BREAKPOINT_SQL = "select * from dbe_pldebugger.disable_breakpoint(%s);";

    /**
     * enable breakpoint sql
     */
    public static final String ENABLE_BREAKPOINT_SQL = "select * from dbe_pldebugger.enable_breakpoint(%s);";

    /**
     * step sql
     */
    public static final String STEP_SQL = "select * from dbe_pldebugger.step();";

    /**
     * debug server info sql
     */
    public static final String DEBUG_SERVER_INFO_SQL = "select * from dbe_pldebugger.local_debug_server_info();";

    /**
     * next sql
     */
    public static final String NEXT_SQL = "select * from dbe_pldebugger.next();";

    /**
     * finish sql
     */
    public static final String FINISH_SQL = "select * from dbe_pldebugger.finish();";

    /**
     * turn off sql
     */
    public static final String TURN_OFF_SQL = "select * from dbe_pldebugger.turn_off(%s);";

    /**
     * abort sql
     */
    public static final String ABORT_SQL = "select * from dbe_pldebugger.abort();";

    /**
     * info code sql
     */
    public static final String INFO_CODE_SQL = "SELECT * FROM DBE_PLDEBUGGER.info_code(%s);";

    /**
     * get oid name sql
     */
    public static final String GET_OID_NAME_SQL = "select typname from pg_type pt where pt.oid in (%s);";

    /**
     * get url jdbc
     */
    public static final String GET_URL_JDBC = "jdbc:opengauss://";

    /**
     * get typename sql
     */
    public static final String GET_TYPENAME_SQL = "select a.oid,a.typname from pg_type a where a.oid<9999";

    /**
     * get database sql
     */
    public static final String GET_DATABASE_SQL = "select datname from pg_database order by datname;";

    /**
     * get schema name sql
     */
    public static final String GET_SCHEMA_NAME_SQL = "SELECT oid, nspname FROM pg_namespace order by nspname;";

    /**
     * get data connection sql
     */
    public static final String GET_DATA_CONNECTION_SQL = "select id, type , name , driver , ip, port ,dataname , "
            + "username, userpassword ,webuser,edition from DATABASELINK WHERE";

    /**
     * get data connection not p sql
     */
    public static final String GET_DATA_CONNECTION_NOT_P_SQL = "select id, type , name , driver , ip, port ,dataname"
            + " , username, '' as userpassword ,webuser,edition from DATABASELINK WHERE";

    /**
     * get database link count sql
     */
    public static final String GET_DATABASELINK_COUNT_SQL = "select count(1) as count from DATABASELINK where";

    /**
     * table ddl sql
     */
    public static final String TABLE_DDL_SQL = "select pg_get_tabledef('%s.%s');";

    /**
     * table data sql
     */
    public static final String TABLE_DATA_SQL = "select * from %s.%s";

    /**
     * table data limit sql
     */
    public static final String TABLE_DATA_LIMIT_SQL = "select * from %s.%s LIMIT %s,%s";

    /**
     * limit sql
     */
    public static final String LIMIT_SQL = " LIMIT %s,%s";

    /**
     * where sql
     */
    public static final String WHERE_SQL = " WHERE %s";

    /**
     * order sql
     */
    public static final String ORDER_SQL = " ORDER BY %s";

    /**
     * function sql
     */
    public static final String FUNCTION_SQL = "select * from %s(%s)";

    /**
     * view data sql
     */
    public static final String VIEW_DATA_SQL = "select * from %s.%s LIMIT %s,%s";

    /**
     * table analyse sql
     */
    public static final String TABLE_ANALYSE_SQL = "ANALYSE %s.%s ";

    /**
     * table truncata sql
     */
    public static final String TABLE_TRUNCATE_SQL = "TRUNCATE TABLE ONLY %s.%s ";

    /**
     * vacuum sql
     */
    public static final String VACUUM_SQL = "VACUUM %s.%s ";

    /**
     * reindex table sql
     */
    public static final String REINDEX_TABLE_SQL = "REINDEX TABLE %s.%s ";

    /**
     * rename table sql
     */
    public static final String RENAME_TABLE_SQL = "ALTER TABLE %s.%s RENAME TO %s ";

    /**
     * schema table sql
     */
    public static final String SCHEMA_TABLE_SQL = "ALTER TABLE ONLY %s.%s SET SCHEMA %s ";

    /**
     * table space table sql
     */

    public static final String TABLESPACE_TABLE_SQL = "ALTER TABLE %s.%s SET TABLESPACE %s;";

    /**
     * analyse table sql
     */
    public static final String ANALYZE_TABLE_SQL = "ANALYZE %s.%s;";

    /**
     * drop table sql
     */
    public static final String DROP_TABLE_SQL = "DROP TABLE %s.%s ;";

    /**
     * table attribute partition sql
     */
    public static final String TABLE_ATTRIBUTE_PARTITION_SQL = "select  p.relname AS partition_name, "
            + "ARRAY_TO_STRING( p.boundaries,',' )as boundaries, n.spcname AS table_space  "
            + "from pg_class c  "
            + "left join  pg_partition p on p.parentid = c.oid "
            + "left JOIN PG_TABLESPACE n ON n.oid = p.reltablespace "
            + "where  c.parttype = 'p'   and p.parttype = 'p' and p.parentid =  %s ";

    /**
     * table sequence sql
     */
    public static final String TABLE_SEQUENCE_SQL = "select t.tableuser, t.tableName, t.columnName, "
            + "decode(seq.sequence_name, t.relname, seq.sequence_name, '[No_Privilege]') as sequenceName, "
            + "decode(seq.sequence_name, t.relname, seq.sequence_schema, '[No_Privilege]') as sequenceuser, "
            + "decode(seq.sequence_name, t.relname, seq.minimum_value, '[No_Privilege]') as minValue,"
            + " decode(seq.sequence_name, t.relname, seq.maximum_value, '[No_Privilege]') as maxValue, "
            + "decode(seq.sequence_name, t.relname, seq.increment, '[No_Privilege]') as increment" + LF
            + "  from (SELECT tu.rolname as tableuser, tb.relname as tableName, tc.attname as columnName, "
            + "scl.relname, sch.nspname" + LF
            + "          FROM pg_class scl, pg_depend sdp, pg_attrdef sc, pg_attribute tc, pg_class tb,"
            + " pg_roles tu, pg_namespace sch" + LF
            + "         WHERE sch.nspname = '%s'" + LF
            + "           and tb.relname = '%s'" + LF
            + "           AND scl.relnamespace = sch.oid" + LF
            + "           AND scl.relkind = 'S'" + LF
            + "           AND sdp.refobjid = scl.oid" + LF
            + "           AND sc.oid = sdp.objid" + LF
            + "           AND tc.attrelid = sc.adrelid" + LF
            + "           AND tc.attnum = sc.adnum" + LF
            + "           AND tb.oid = tc.attrelid" + LF
            + "           AND tu.oid = tb.relowner ) t" + LF
            + "  LEFT OUTER join information_schema.sequences seq on (t.nspname = seq.sequence_schema "
            + "AND t.relname = seq.sequence_name);";

    /**
     * get column sql
     */
    public static final String GET_COLUMN_SQL = "select col.column_name,col.data_type,"
            + "case when col.is_nullable = 'YES' then FALSE  else TRUE end as is_null," + LF
            + "col.column_default as column_default,"
            + " case when con.conname is null then FALSE ELSE TRUE end as is_only," + LF
            + "case when col.CHARACTER_MAXIMUM_LENGTH is not null then col.CHARACTER_MAXIMUM_LENGTH" + LF
            + "when col.numeric_precision is not null then col.numeric_precision end as precision, "
            + "col.NUMERIC_SCALE, pd.description" + LF
            + "from information_schema.columns col" + LF
            + "inner join pg_class pc on  pc.relname = col.table_name and pc.oid = %s" + LF
            + "left join (select oid, unnest(conkey) as conkey, conrelid,"
            + "contype, conname from pg_constraint) con  " + LF
            + "on  con.conrelid = pc.oid and col.ordinal_position = con.conkey and con.contype = 'u'" + LF
            + "left join PG_DESCRIPTION pd on pd.objsubid = col.ORDINAL_POSITION and objoid = %s" + LF
            + "where  col.table_schema = '%s' and col.table_name = '%s'"
            + "order by col.ordinal_position";

    /**
     * alter table sql
     */
    public static final String ALTER_TABLE_SQL = "ALTER TABLE ";

    /**
     * alter table column drop sql
     */
    public static final String ALTER_TABLE_COLUMN_DROPQL = "ALTER TABLE %s.%s DROP COLUMN %s;";

    /**
     * insert table sql
     */
    public static final String INSERRT_TABLE_SQL = "INSERT INTO %s.%s ( %s ) VALUES ( %s );";

    /**
     * delete table sql
     */
    public static final String DELETE_TABLE_SQL = "DELETE FROM %s.%s WHERE %s ;";

    /**
     * update table sql
     */
    public static final String UPDATE_TABLE_SQL = "UPDATE %s.%s SET  %s WHERE  %s ;";

    /**
     * equal sql
     */
    public static final String EQUAL_SQL = "%s = %s";

    /**
     * is null sql
     */
    public static final String IS_NULL_SQL = "%s IS NULL";

    /**
     * alter table column add sql
     */
    public static final String ALTER_TABLE_COLUMN_ADD_SQL = "ALTER TABLE %s.%s  ADD COLUMN %s ";

    /**
     * table partition attribute sql
     */
    public static final String TABLE_PARTITIONG_ATTRIBUTE_SQL = "SELECT tbl.oid," + LF
            + "       tbl.relname," + LF
            + "       case when tbl.reltablespace = 0 then 'pg_default' else tblsp.spcname end," + LF
            + "       case when tbl.relpersistence = 'g' then true else false end as relpersistence," + LF
            + "       case when tbl.relpersistence = 'u' then 'UNLOGGED' else '正常' end as table_type," + LF
            + "       auth.rolname as owner," + LF
            + "       tbl.relpages pages," + LF
            + "       tbl.reltuples as rows_count," + LF
            + "       tbl.relhasindex as has_index," + LF
            + "       tbl.relisshared as is_shared,split_part("
            + "split_part(array_to_string(tbl.reloptions, ','),',', 1),'=',2) orientation ," + LF
            + "       split_part(split_part(array_to_string(tbl.reloptions, ','),',', 2),'=',2) compression ," + LF
            + "       tbl.relhasoids as hashoid," + LF
            + "       d.description as tbl_desc," + LF
            + "       case when pp.partstrategy = 'l' then 'BY LIST' " + LF
            + "            when pp.partstrategy = 'h' then 'BY HASH' ELSE 'BY RANGE' end as partstrategy," + LF
            + "       pa.attname" + LF
            + "  FROM pg_class tbl" + LF
            + "  LEFT JOIN pg_roles auth on (tbl.relowner = auth.oid)" + LF
            + "  left join pg_description d on (tbl.oid = d.objoid)" + LF
            + "  LEFT JOIN pg_tablespace tblsp ON (tbl.reltablespace = tblsp.oid)" + LF
            + "  left join pg_attribute pa on pa.attrelid = tbl.oid " + LF
            + "  inner join PG_PARTITION pp on CAST(pp.partkey as VARCHAR2) = CAST(pa.attnum as VARCHAR2)" + LF
            + "     and parentid = %s" + LF
            + " WHERE tbl.oid = %s;";

    /**
     * table attribute sql
     */
    public static final String TABLE_ATTRIBUTE_SQL = "SELECT tbl.oid," + LF
            + "       tbl.relname," + LF
            + "       case when tbl.reltablespace = 0 then 'pg_default' else tblsp.spcname end," + LF
            + "       case when tbl.relpersistence = 'g' then true else false end as relpersistence," + LF
            + "       case when tbl.relpersistence = 'u' then 'UNLOGGED' else '正常' end as table_type," + LF
            + "       auth.rolname as owner," + LF
            + "       tbl.relpages pages," + LF
            + "       tbl.reltuples as rows_count," + LF
            + "       tbl.relhasindex as has_index," + LF
            + "       tbl.relisshared as is_shared,split_part("
            + "split_part(array_to_string(tbl.reloptions, ','),',', 1),'=',2) orientation ," + LF
            + "       split_part(split_part(array_to_string(tbl.reloptions, ','),',', 2),'=',2) compression ," + LF
            + "       tbl.relhasoids as hashoid," + LF
            + "       d.description as tbl_desc" + LF
            + "  FROM pg_class tbl" + LF
            + "  LEFT JOIN pg_roles auth on (tbl.relowner = auth.oid)" + LF
            + "  left join pg_description d on (tbl.oid = d.objoid)" + LF
            + "  LEFT JOIN pg_tablespace tblsp ON (tbl.reltablespace = tblsp.oid)" + LF
            + " WHERE tbl.oid = %s";

    /**
     * create sql
     */
    public static final String CREATE_SQL = "CREATE ";

    /**
     * create or replace sql
     */
    public static final String CREATE_OR_REPLACE_SQL = "CREATE OR REPLACE ";

    /**
     * create user sql
     */
    public static final String CREATE_USER_SQL = "CREATE %s WITH %s PASSWORD '%s';";

    /**
     * user sql
     */
    public static final String USER_SQL = "USER %s";

    /**
     * role sql
     */
    public static final String ROLE_SQL = "ROLE %s";

    /**
     * user connection limit sql
     */
    public static final String USER_CONNECTION_LIMIT_SQL = LF + "    CONNECTION LIMIT %s";

    /**
     * valid begin sql
     */
    public static final String VALID_BEGIN_SQL = LF + "    VALID BEGIN '%s'";

    /**
     * valid until sql
     */
    public static final String VALID_UNTIL_SQL = LF + "    VALID UNTIL '%s'";

    /**
     * resource pool sql
     */
    public static final String RESOURCE_POOL_SQL = LF + "    RESOURCE POOL '%s'";

    /**
     * comment role sql
     */
    public static final String COMMENT_ROLE_SQL = LF + "COMMENT ON ROLE %s IS '%s';";

    /**
     * comment tablespace sql
     */
    public static final String COMMENT_TABLESPACE_SQL = LF + "COMMENT ON TABLESPACE %s IS '%s';";

    /**
     * create tablespace sql
     */
    public static final String CREATE_TABLESPACE_SQL = "CREATE TABLESPACE %s ";

    /**
     * cowner tablespace sql
     */
    public static final String OWNER_TABLESPACE_SQL = LF + " OWNER %s ";

    /**
     * cowner tablespace sql
     */
    public static final String ALTER_OWNER_TABLESPACE_SQL = "ALTER TABLESPACE %s OWNER TO %s; ";

    /**
     * maxsize
     */
    public static final String ALTER_MAXSIZE_TABLESPACE_SQL = LF + "ALTER TABLESPACE %s RESIZE MAXSIZE '%s';";

    /**
     * seq_page_cost
     */
    public static final String ALTER_SEQ_PAGE_COST_SQL = LF + "ALTER TABLESPACE %s SET (%s = '%s');";

    /**
     * random_page_cost
     */
    public static final String ALTER_RANDOM_PAGE_COST_SQL = LF + "ALTER TABLESPACE %s SET (%s = '%s');";

    /**
     * cowner tablespace sql
     */
    public static final String ALTER_NAME_TABLESPACE_SQL = "ALTER TABLESPACE %s RENAME TO %s; ";

    /**
     * relative
     */
    public static final String RELATIVE_TABLESPACE_SQL = LF + " RELATIVE ";

    /**
     * location
     */
    public static final String LOCAL_TABLESPACE_SQL = LF + " LOCATION '%s' ";

    /**
     * maxsize
     */
    public static final String MAXSIZE_TABLESPACE_SQL = LF + " MAXSIZE '%s'";

    /**
     * with tablespace sql
     */
    public static final String WITH_TABLESPACE_SQL = LF + " WITH ( %s )";

    /**
     * random page cost tablespace sql
     */
    public static final String RANDOM_PAGE_COST_TABLESPACE_SQL = LF + " random_page_cost = '%s' ";

    /**
     * seq page cost tablespace sql
     */
    public static final String SEQ_PAGE_COST_TABLESPACE_SQL = LF + " seq_page_cost  = '%s' ";

    /**
     * drop tablespace sql
     */
    public static final String DROP_TABLESPACE_SQL = "DROP TABLESPACE %s ";

    /**
     * path tablespace sql
     */
    public static final String PATH_TABLESPACE_SQL = "SELECT * from pg_tablespace_location(%s);";

    /**
     * attribute tablespace sql
     */
    public static final String ATTRIBUTE_TABLESPACE_SQL = "SELECT  " + LF
            + "pt.spcname,pt.spcoptions,pt.spcmaxsize,pt.relative,pa.rolname,pd.Description " + LF
            + "FROM PG_TABLESPACE pt LEFT JOIN PG_SHDescription pd on pd.objoid = pt.oid " + LF
            + "LEFT JOIN PG_AUTHID pa on pa.oid = pt.spcowner" + LF
            + "where pt.oid = %s ;";

    /**
     * create view sql
     */
    public static final String CREATE_VIEW_SQL = "CREATE %s VIEW %s.%s AS %s";

    /**
     * rename view sql
     */
    public static final String RENAME_VIEW_SQL = "ALTER VIEW %s.%s RENAME TO %s;";

    /**
     * update view schema sql
     */
    public static final String UPDATE_VIEW_SCHEMA_SQL = "ALTER VIEW %s.%s SET SCHEMA %s;";

    /**
     * get view column sql
     */
    public static final String GET_VIEW_COLUMN_SQL = "select pa.attname as name, "
            + "format_type(pa.atttypid, atttypmod) AS type, pa.attnotnull as notnull "
            + "from pg_attribute pa left join pg_class pc on pa.attrelid = pc.oid "
            + "left join pg_namespace pn on pc.relnamespace = pn.oid  "
            + "where pn.nspname = '%s' and pc.relname = '%s' order by pa.attnum;";

    /**
     * drop user sql
     */
    public static final String DROP_USER_SQL = LF + "DROP USER %s;";

    /**
     * update user password
     */
    public static final String UPDAT_USER_PASSWORD_SQL = "alter %s %s identified by '%s' replace '%s';";

    /**
     * drop role sql
     */
    public static final String DROP_ROLE_SQL = LF + "DROP ROLE %s;";

    /**
     * alter user date sql
     */
    public static final String ALTER_DATE_SQL = LF + "ALTER %s %s VALID %s '%s';";

    /**
     * alter connection user sql
     */
    public static final String ALTER_CONNECTION_SQL = LF + "ALTER %s %s CONNECTION LIMIT %s;";

    /**
     * alter name user sql
     */
    public static final String ALTER_NAME_SQL = LF + "ALTER %s %s RENAME TO %s;";

    /**
     * alter comment user sql
     */
    public static final String ALTER_COMMENT_SQL = LF + "COMMENT ON %s %s IS '%s';";

    /**
     * alter comment user sql
     */
    public static final String ALTER_POOL_SQL = LF + "ALTER %s %s RESOURCE POOL '%s';";

    /**
     * alter grant user sql
     */
    public static final String GRANT_SQL = LF + "GRANT %s TO %s;";

    /**
     * alter revoke user sql
     */
    public static final String REVOKE_SQL = LF + "REVOKE %s FROM %s;";

    /**
     * alter user power sql
     */
    public static final String ALTER_POWER_SQL = LF + "ALTER %s %s %s;";

    /**
     * synonym sql
     */
    public static final String SYNONYM_SQL = "SYNONYM %s.%s FOR %s.%s;";

    /**
     * create database sql
     */
    public static final String CREATE_DATABASE_SQL = "CREATE DATABASE ";

    /**
     * create table sql
     */
    public static final String CREATE_TABLE_SQL = "CREATE TABLE %s.%s(" + LF;

    /**
     * create table exists sql
     */
    public static final String CREATE_TABLE_EXISTS_SQL = "CREATE TABLE IF NOT EXISTS %s.%s(" + LF;

    /**
     * create un logged table sql
     */
    public static final String CREATE_UNLOGGED_TABLE_SQL = "CREATE UNLOGGED TABLE %s.%s(" + LF;

    /**
     * create un logged table exists sql
     */
    public static final String CREATE_UNLOGGED_TABLE_EXISTS_SQL = "CREATE UNLOGGED TABLE IF NOT EXISTS %s.%s(" + LF;

    /**
     * partition by sql
     */
    public static final String PARTITION_BY_SQL = "PARTITION BY ";

    /**
     * range sql
     */
    public static final String RANGE_SQL = "RANGE(%s)(" + LF
            + "partition %s values less than (%s) tablespace %s)" + LF;

    /**
     * interval sql
     */
    public static final String INTERVAL_SQL = "RANGE(%s) INTERVAL(%s)(" + LF
            + "partition %s values less than (%s) tablespace %s)" + LF;

    /**
     * list sql
     */
    public static final String LIST_SQL = "LIST(%s)(" + LF
            + "partition %s values (%s) tablespace %s)";

    /**
     * hash sql
     */
    public static final String HASH_SQL = "HASH(%s)(" + LF
            + "partition %s tablespace %s)";

    /**
     * unique sql
     */
    public static final String UNIQUE_SQL = "CONSTRAINT \"%s\" UNIQUE (%s)";

    /**
     * unique immediate sql
     */
    public static final String UNIQUE_IMMEDIATE_SQL = "CONSTRAINT \"%s\" UNIQUE (%s)  DEFERRABLE INITIALLY IMMEDIATE ";

    /**
     * primary key sql
     */
    public static final String PRIMARY_KEY_SQL = "CONSTRAINT \"%s\" PRIMARY KEY (%s)";

    /**
     * check sql
     */
    public static final String CHECK_SQL = "CONSTRAINT \"%s\" CHECK (%s)";

    /**
     * foreign key sql
     */
    public static final String FOREIGN_KEY_SQL = "CONSTRAINT \"%s\" FOREIGN KEY (%s) REFERENCES %s.%s (%s)";

    /**
     * partial cluster key sql
     */
    public static final String PARTIAL_CLUSTER_KEY_SQL = "CONSTRAINT \"%s\" PARTIAL CLUSTER KEY (%s)";

    /**
     * with double sql
     */
    public static final String WITH_DOUBLE_SQL = "WITH (%s,%s)" + LF;

    /**
     * with sql
     */
    public static final String WITH_SQL = "WITH (%s)" + LF;

    /**
     * oid sql
     */
    public static final String OIDS_SQL = "OIDS=TRUE" + LF;

    /**
     * fillfactor sql
     */
    public static final String FILLFACTOR_SQL = "FILLFACTOR=%s" + LF;

    /**
     * column sql
     */
    public static final String COLUMN_SQL = "WITH (orientation = column)" + LF;

    /**
     * table space sql
     */
    public static final String TABLESPACE_SQL = "TABLESPACE %s" + LF;

    /**
     * comment table sql
     */
    public static final String COMMENT_TABLE_SQL = "COMMENT ON TABLE %s.%s IS '%s'" + LF;

    /**
     * sequence set sql
     */
    public static final String SEQUENCE_SET_SQL = "SET search_path = ";

    /**
     * no minvalue sql
     */
    public static final String NO_MINVALUE_SQL = "NO MINVALUE";

    /**
     * start with sql
     */
    public static final String START_WITH_SQL = "START WITH ";

    /**
     * maxvalue sql
     */
    public static final String MAXVALUE_SQL = "MAXVALUE ";

    /**
     * cycle sql
     */
    public static final String CYCLE_SQL = "CYCLE";

    /**
     * minvalue sql
     */
    public static final String MINVALUE_SQL = "MINVALUE ";

    /**
     * increment by sql
     */
    public static final String INCREMENT_BY_SQL = "INCREMENT BY ";

    /**
     * materialized view sql
     */
    public static final String MATERIALIZED_VIEW_SQL = "MATERIALIZED ";

    /**
     * common view sql
     */
    public static final String COMMON_VIEW_SQL = "OR REPLACE ";

    /**
     * rename keyword sql
     */
    public static final String RENAME_KEYWORD_SQL = " RENAME ";

    /**
     * to keyword sql
     */
    public static final String TO_KEYWORD_SQL = " TO ";

    /**
     * column keyword sql
     */
    public static final String COLUMN_KEYWORD_SQL = "COLUMN ";

    /**
     * type keyword sql
     */
    public static final String TYPE_KEYWORD_SQL = " TYPE ";

    /**
     * null keyword sql
     */
    public static final String NULL_KEYWORD_SQL = " NULL";

    /**
     * unique keyword sql
     */
    public static final String UNIQUE_KEYWORD_SQL = " UNIQUE";

    /**
     * not keyword sql
     */
    public static final String NOT_KEYWORD_SQL = " NOT";

    /**
     * set keyword sql
     */
    public static final String SET_KEYWORD_SQL = " SET ";

    /**
     * drop default sql
     */
    public static final String DROP_DEFAULT_SQL = " ALTER TABLE %s.%s ALTER COLUMN %s DROP DEFAULT;";

    /**
     * alter default sql
     */
    public static final String ALTER_DEFAULT_SQL = " ALTER TABLE %s.%s ALTER COLUMN %s SET DEFAULT %s;";

    /**
     * default keyword sql
     */
    public static final String DEFAULT_KEYWORD_SQL = " DEFAULT ";

    /**
     * select keyword sql
     */
    public static final String SELECT_KEYWORD_SQL = "SELECT ";

    /**
     * select sequence count sql
     */
    public static final String SELECT_SEQUENCE_COUNT_SQL = "SELECT  count(1) as count "
            + " from PG_CLASS c INNER JOIN pg_namespace n ON n.oid = c.relnamespace" + LF
            + "and n.nspname = '%s' where relname = '%s' and relkind = 'S';";

    /**
     * from keyword sql
     */
    public static final String FROM_KEYWORD_SQL = " FROM ";

    /**
     * sequence keyword sql
     */
    public static final String SEQUENCE_KEYWORD_SQL = "SEQUENCE ";

    /**
     * with encoding sql
     */
    public static final String WITH_ENCODING_SQL = " WITH ENCODING '";

    /**
     * lc collate sql
     */
    public static final String LC_COLLATE_SQL = "' LC_COLLATE '";

    /**
     * lc c type sql
     */
    public static final String LC_CTYPE_SQL = "' LC_CTYPE '";

    /**
     * connection limit
     */
    public static final String CONNECTION_LIMIT_SQL = " CONNECTION LIMIT ";

    /**
     * db compatibility
     */
    public static final String DBCOMPATIBILITY_SQL = "' DBCOMPATIBILITY '";

    /**
     * start keyword sql
     */
    public static final String START_KEYWORD_SQL = "START WITH ";

    /**
     * no minvalue keyword
     */
    public static final String NO_MINVALUE_KEYWORD_SQL = "NO MINVALUE ";

    /**
     * increment keyword sql
     */
    public static final String INCREMENT_KEYWORD_SQL = "INCREMENT BY ";

    /**
     * minvalue keyword sql
     */
    public static final String MINVALUE_KEYWORD_SQL = "MINVALUE ";

    /**
     * maxvalue key word sql
     */
    public static final String MAXVALUE_KEYWORD_SQL = "MAXVALUE ";

    /**
     * cache keyword sql
     */
    public static final String CACHE_KEYWORD_SQL = "CACHE ";

    /**
     * cycle keyword sql
     */
    public static final String CYCLE_KEYWORD_SQL = "CYCLE ";

    /**
     * owned keyword sql
     */
    public static final String OWNED_KEYWORD_SQL = "OWNED BY ";

    /**
     * drop sql
     */
    public static final String DROP_SQL = "DROP ";

    /**
     * drop view sql
     */
    public static final String DROP_VIEW_SQL = "DROP VIEW %s.%s;";

    /**
     * drop materialized view sql
     */
    public static final String DROP_MATERIALIZED_VIEW_SQL = "DROP MATERIALIZED VIEW IF EXISTS %s.%s;";

    /**
     * drop synonym sql
     */
    public static final String DROP_SYNONYM_SQL = "DROP SYNONYM %s.%s ;";

    /**
     * drop sequence sql
     */
    public static final String DROP_SEQUENCE_SQL = "DROP SEQUENCE %s.%s;";

    /**
     * drop function keyword sql
     */
    public static final String DROP_FUNCTION_KEYWORD_SQL = "DROP FUNCTION %s.%s ;";

    /**
     * drop procedure keyword sql
     */
    public static final String DROP_PROCEDURE_KEYWORD_SQL = "DROP PROCEDURE %s.%s ;";

    /**
     * drop package sql
     */
    public static final String DROP_PACKAGE_SQL = "DROP PACKAGE %s.%s ;";

    /**
     * drop database sql
     */
    public static final String DROP_DATABASE_SQL = "DROP DATABASE %s ;";

    /**
     * alter rename database sql
     */
    public static final String ALTER_RENAME_DATABASE_SQL = "ALTER DATABASE %s RENAME TO %s ;";

    /**
     * alter connection limit sql
     */
    public static final String ALTER_CONNECTION_LIMIT_SQL = "ALTER DATABASE %s  WITH CONNECTION LIMIT %s ;";

    /**
     * alter sql
     */
    public static final String ALTER_SQL = "ALTER ";

    /**
     * constraint drop sql
     */
    public static final String CONSTRAINT_DROP_SQL = " ALTER TABLE %s.%s DROP  CONSTRAINT \"%s\"";

    /**
     * constraint unique sql
     */
    public static final String CONSTRAINT_UNIQUE_SQL = " ALTER TABLE %s.%s ADD  CONSTRAINT \"%s\" UNIQUE (%s) ";

    /**
     * constraint unique immediate sql
     */
    public static final String CONSTRAINT_UNIQUE_IMMEDIATE_SQL = " ALTER TABLE %s.%s ADD  "
            + "CONSTRAINT \"%s\" UNIQUE (%s)  DEFERRABLE INITIALLY IMMEDIATE  ";

    /**
     * constraint primary sql
     */
    public static final String CONSTRAINT_PRIMARY_SQL = " ALTER TABLE %s.%s ADD CONSTRAINT \"%s\" PRIMARY KEY (%s) ";

    /**
     * constraint primary immediate sql
     */
    public static final String CONSTRAINT_PRIMARY_IMMEDIATE_SQL = " ALTER TABLE %s.%s ADD CONSTRAINT \"%s\" "
            + "PRIMARY KEY (%s)  DEFERRABLE INITIALLY IMMEDIATE  ";

    /**
     * constraint check sql
     */
    public static final String CONSTRAINT_CHECK_SQL = "   ALTER TABLE %s.%s ADD CONSTRAINT \"%s\" CHECK (%s)  ";

    /**
     * constraint partial cluster key sql
     */
    public static final String CONSTRAINT_PARTIAL_CLUSTER_KEY_SQL = "   ALTER TABLE %s.%s  ADD CONSTRAINT \"%s\"  "
            + "PARTIAL  CLUSTER KEY (%s)  ";

    /**
     * constraint no check sql
     */
    public static final String CONSTRAINT_NO_CHECK_SQL = "   ALTER TABLE %s.%s ADD CONSTRAINT \"%s\"  %s  ";

    /**
     * constraint foreign key sql
     */
    public static final String CONSTRAINT_FOREIGN_KEY_SQL = " ALTER TABLE %s.%s ADD CONSTRAINT \"%s\" "
            + "FOREIGN KEY (%s) REFERENCES %s.%s (%s) ";

    /**
     * constraint table sql
     */
    public static final String CONSTRAINT_TABLE_SQL = "select con.conname, "
            + "string_agg(DISTINCT att.attname, ',' order by att.attname) as attname, con.contype,"
            + " pg_get_constraintdef(con.oid) constraintdef,"
            + " con.condeferrable, d.description,f.nspname AS ref_nspname , f.relname AS ref_tbname , "
            + "string_agg(DISTINCT f.attname, ', ') AS ref_columns "
            + " from pg_class cla inner join pg_attribute att on cla.oid = att.attrelid and att.attnum > 0 "
            + "inner join (select oid, condeferrable, "
            + " unnest(conkey) as conkey, conrelid, contype, conname,confrelid ,confkey "
            + "from pg_constraint) con on att.attnum = con.conkey and con.conrelid "
            + " = cla.oid left JOIN   (SELECT pg_attribute.attnum,"
            + "pg_attribute.attrelid,pg_attribute.attname,relname, nspname FROM pg_class INNER JOIN pg_namespace "
            + " ON pg_namespace.oid = pg_class.relnamespace "
            + "INNER JOIN pg_attribute ON pg_class.oid = pg_attribute.attrelid)  f ON f.attrelid = con.confrelid "
            + " AND f.attnum = ANY ( con.confkey) inner join pg_namespace ns "
            + "on cla.relnamespace = ns.oid and ns.nspname = '%s' LEFT JOIN pg_description d "
            + " ON con.oid = d.objoid  where cla.relname = '%s' "
            + "group by con.conname,con.contype, pg_get_constraintdef(con.oid), con.condeferrable, ns.nspname "
            + " ,d.description,ref_nspname,ref_tbname";

    /**
     * unique constraint table sql
     */
    public static final String UNIQUE_CONSTRAINT_TABLE_SQL = "select  con.contype from pg_class cla "
            + "inner join pg_attribute att on cla.oid = att.attrelid and att.attnum > 0" + LF
            + "inner join (select oid, condeferrable, unnest(conkey) as conkey, conrelid,"
            + " contype, conname,confrelid ,confkey from pg_constraint) con " + LF
            + "on att.attnum = con.conkey and con.conrelid = cla.oid "
            + "inner join pg_namespace ns on cla.relnamespace = ns.oid and ns.nspname = '%s' " + LF
            + "LEFT JOIN pg_description d ON con.oid = d.objoid  where cla.relname = '%s' and con.contype ='p' ";

    /**
     * constraint comment sql
     */
    public static final String CONSTRAINT_COMMENT_SQL = " COMMENT ON CONSTRAINT \"%s\" ON %s.%s is '%s' ";

    /**
     * constraint table column sql
     */
    public static final String CONSTRAINT_TABLE_COLUMN_SQL = "select con.conname" + LF
            + "  from pg_class cla" + LF
            + " inner join pg_attribute att on cla.oid = att.attrelid" + LF
            + "                            and att.attnum > 0" + LF
            + " inner join (select oid, condeferrable, unnest(conkey) as conkey, conrelid, contype, conname" + LF
            + "               from pg_constraint ) con on att.attnum = con.conkey" + LF
            + "                   and con.conrelid = cla.oid" + LF
            + " inner join pg_namespace ns on cla.relnamespace = ns.oid" + LF
            + "                           and ns.nspname = '%s'" + LF
            + "  LEFT JOIN pg_description d ON con.oid = d.objoid" + LF
            + " where cla.relname = '%s'" + LF
            + "   and con.contype = 'u'" + LF
            + "   and att.attname = '%s'" + LF
            + " group by con.conname";

    /**
     * index drop sql
     */
    public static final String INDEX_DROP_SQL = " DROP INDEX  %s.%s ;";

    /**
     * index create sql
     */
    public static final String INDEX_CREATE_SQL = " CREATE %s INDEX %s ON %s.%s %s (%s); ";

    /**
     * index comment sql
     */
    public static final String INDEX_COMMENT_SQL = " COMMENT ON INDEX  %s.%s is '%s' ;";

    /**
     * index table sql
     */
    public static final String INDEX_TABLE_SQL = "select pc.relname,pi.indisunique, am.amname,"
            + " CASE WHEN pg_catalog.pg_get_expr(pi.indexprs, pi.indrelid, true) is null "
            + "THEN string_agg(att.attname, ',' order by att.attname) ELSE null END attname, "
            + " pg_catalog.pg_get_expr(pi.indexprs, pi.indrelid, true) expression,  pg_description.description "
            + "from PG_INDEX pi inner join PG_CLASS pc on pi.indexrelid = pc.oid and pc.relkind in ('i' ,'I') "
            + "inner join PG_ATTRIBUTE att on pc.oid = att.attrelid and att.attname != 'tableoid' "
            + "LEFT JOIN pg_description  ON pi.indexrelid = pg_description.objoid JOIN pg_am am ON pc.relam = am.oid "
            + "inner join (select pc.oid from PG_CLASS pc inner join pg_namespace pn on pn.oid = pc.relnamespace "
            + "where pc.relname = '%s' and pn.nspname = '%s' ) tt on pi.indrelid = tt.oid "
            + "group by pc.relname, pi.indisunique, pi.indexprs, pi.indrelid, am.amname, pg_description.description;";

    /**
     * select sequence ddl sql
     */
    public static final String SELECT_SEQUENCE_DDL_SQL = " SELECT sequence_schema,sequence_name,start_value,"
            + "increment,minimum_value,maximum_value,cycle_option" + LF
            + " FROM information_schema.sequences where sequence_schema = '%s'and sequence_name ='%s';";

    /**
     * select view ddl sql
     */
    public static final String SELECT_VIEW_DDL_SQL = " SELECT c.relkind as relkind, n.nspname AS schemaname, "
            + "c.relname AS matviewname, pg_get_userbyid(c.relowner) AS matviewowner, c.relhasindex AS hasindexes, "
            + "pg_get_viewdef(c.oid) AS definition FROM pg_class c " + LF
            + "LEFT JOIN pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relname = '%s'"
            + " and c.relkind in ('m','v')";

    /**
     * select view type sql
     */
    public static final String SELECT_VIEW_TYPE_SQL = " SELECT c.relkind as relkind FROM pg_class c LEFT JOIN "
            + "pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relname = '%s' and c.relkind in "
            + "('m','v')";

    /**
     * select user sql
     */
    public static final String SELECT_USER_SQL = "SELECT a.oid,a.rolname," + LF
            + "case when a.rolcanlogin = true then 'LOGIN' ELSE '' end rolcanlogin," + LF
            + "case when a.rolinherit = true then 'INHERIT' ELSE '' end rolinherit," + LF
            + "case when a.rolreplication = true then 'REPLICATION' ELSE '' end rolreplication," + LF
            + "case when a.rolcreaterole = true then 'CREATEROLE' ELSE '' end rolcreaterole," + LF
            + "case when a.rolcreatedb = true then 'CREATEDB' ELSE '' end rolcreatedb," + LF
            + "case when a.rolauditadmin = true then 'AUDITADMIN' ELSE '' end rolauditadmin," + LF
            + "case when a.rolsystemadmin = true then 'SYSADMIN' ELSE '' end rolsystemadmin," + LF
            + "a.rolvalidbegin,a.rolvaliduntil,a.rolconnlimit," + LF
            + "a.rolrespool,t1.merolname,t1.grrolname,t2.belong,pd.Description  from PG_ROLES a LEFT join " + LF
            + "(SELECT t2.roleid ,grrolname," + LF
            + " merolname from " + LF
            + "(SELECT b.roleid ,array_agg(c.rolname) grrolname " + LF
            + "from PG_AUTH_MEMBERS b INNER JOIN PG_ROLES c on c.oid = b.member " + LF
            + "where admin_option =false" + LF
            + "GROUP BY b.roleid) t2" + LF
            + "full join (SELECT b.roleid,array_agg(c.rolname) merolname " + LF
            + "from PG_AUTH_MEMBERS b INNER JOIN PG_ROLES c on c.oid = b.member " + LF
            + "where admin_option =true" + LF
            + "GROUP BY b.roleid) t3" + LF
            + "on t2.roleid = t3.roleid) t1" + LF
            + "on a.oid = t1.roleid" + LF
            + "left join PG_SHDescription pd on a.oid = pd.objoid" + LF
            + "LEFT join (SELECT pg_authid.oid AS oid, array_agg(parent.rolname) AS belong" + LF
            + "FROM PG_AUTHID" + LF
            + "INNER JOIN pg_auth_members ON pg_authid.oid = pg_auth_members.member" + LF
            + "INNER JOIN pg_authid AS parent ON pg_auth_members.roleid = parent.oid" + LF
            + "WHERE pg_authid.rolname = '%s'" + LF
            + "GROUP BY 1)t2 on a.oid = t2.oid" + LF
            + "where a.rolname = '%s';";

    /**
     * select user sql
     */
    public static final String SELECT_ROLES_SQL = "SELECT a.oid,a.rolname," + LF
            + "case when a.rolinherit = true then 'INHERIT' ELSE '' end rolinherit," + LF
            + "case when a.rolreplication = true then 'REPLICATION' ELSE '' end rolreplication," + LF
            + "case when a.rolcreaterole = true then 'CREATEROLE' ELSE '' end rolcreaterole," + LF
            + "case when a.rolcreatedb = true then 'CREATEDB' ELSE '' end rolcreatedb," + LF
            + "case when a.rolauditadmin = true then 'AUDITADMIN' ELSE '' end rolauditadmin," + LF
            + "case when a.rolsystemadmin = true then 'SYSADMIN' ELSE '' end rolsystemadmin," + LF
            + "a.rolvalidbegin,a.rolvaliduntil,a.rolconnlimit," + LF
            + "a.rolrespool,t1.merolname,t1.grrolname,t2.belong,pd.Description  from PG_ROLES a LEFT join " + LF
            + "(SELECT t2.roleid ,grrolname," + LF
            + " merolname from " + LF
            + "(SELECT b.roleid ,array_agg(c.rolname) grrolname " + LF
            + "from PG_AUTH_MEMBERS b INNER JOIN PG_ROLES c on c.oid = b.member " + LF
            + "where admin_option =false" + LF
            + "GROUP BY b.roleid) t2" + LF
            + "full join (SELECT b.roleid,array_agg(c.rolname) merolname " + LF
            + "from PG_AUTH_MEMBERS b INNER JOIN PG_ROLES c on c.oid = b.member " + LF
            + "where admin_option =true" + LF
            + "GROUP BY b.roleid) t3" + LF
            + "on t2.roleid = t3.roleid) t1" + LF
            + "on a.oid = t1.roleid" + LF
            + "left join PG_SHDescription pd on a.oid = pd.objoid" + LF
            + "LEFT join (SELECT pg_authid.oid AS oid, array_agg(parent.rolname) AS belong" + LF
            + "FROM PG_AUTHID" + LF
            + "INNER JOIN pg_auth_members ON pg_authid.oid = pg_auth_members.member" + LF
            + "INNER JOIN pg_authid AS parent ON pg_auth_members.roleid = parent.oid" + LF
            + "WHERE pg_authid.rolname = '%s'" + LF
            + "GROUP BY 1)t2 on a.oid = t2.oid" + LF
            + "where a.rolname = '%s';";

    /**
     * select object select
     */
    public static final String SELECT_OBJECT_SQL = "select c.relname as relname from pg_class c INNER JOIN "
            + "pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relkind = '%s';";

    /**
     * select view sql
     */
    public static final String SELECT_VIEW_SQL = "select c.relname as viewname from pg_class c INNER JOIN "
            + "pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relkind in ('v','m');";

    /**
     * select table sql
     */
    public static final String SELECT_TABLE_SQL = "select c.relname as relname from pg_class c INNER JOIN "
            + "pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relkind ='r';";

    /**
     * select column sql
     */
    public static final String SELECT_COLUMN_SQL = "select column_name from information_schema.columns where "
            + "table_schema ='%s' and table_name = '%s';";

    /**
     * select function sql
     */
    public static final String SELECT_FUNCTION_SQL = "SELECT proname,proargtypes FROM pg_proc" + LF
            + "WHERE pronamespace = (SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '%s');";

    /**
     * synonym attribute sql
     */
    public static final String SYNONYM_ATTRIBUTE_SQL = "select synname,synobjschema,rolname,synobjname from "
            + "PG_SYNONYM  a inner join pg_authid b on a.synowner = b.oid where synname = '%s';";

    /**
     * database attribute sql
     */
    public static final String DATABASE_ATTRIBUTE_SQL = "select ds.oid,ds.datname,"
            + "pg_encoding_to_char(ds.encoding) as encoding,ds.datallowconn," + LF
            + "  case when ds.datconnlimit = -1 " + LF
            + "       then '-1(不限制)'" + LF
            + "       else to_char(ds.datconnlimit)" + LF
            + "  end as datconnlimit,ts.spcname,ds.datcollate,ds.datctype from pg_database ds left join pg_tablespace "
            + "ts on ts.oid = ds.dattablespace where ds.datname = '%s';";

    /**
     * database update attribute sql
     */
    public static final String DATABASE_UPDATA_ATTRIBUTE_SQL = "select datname as databaseName,pg_encoding_to_char"
            + "(encoding) as databaseCode,datcompatibility as compatibleType,datcollate as collation,datctype as "
            + "characterType,datconnlimit as conRestrictions from pg_database where datname = '%s';";

    /**
     * query users sql
     */
    public static final String QUERY_USERS_SQL = "select rolname from pg_roles;";

    /**
     * query schema sql
     */
    public static final String QUERY_SCHEMA_SQL = "select pn.oid, pn.nspname, pr.rolname, pd.description "
            + "from pg_namespace pn "
            + "left join pg_description pd on pn.oid = pd.objoid left join pg_roles pr "
            + "on pr.oid = pn.nspowner where pn.oid = %s;";

    /**
     * create schema sql
     */
    public static final String CREATE_SCHEMA_SQL = "create schema %s;";

    /**
     * create schema ddl sql
     */
    public static final String CREATE_SCHEMA_DDL_SQL = "create schema %s authorization %s;";

    /**
     * alter schema name sql
     */
    public static final String ALTER_SCHEMA_NAME_SQL = "alter schema %s rename to %s;";

    /**
     * alter schema owner sql
     */
    public static final String ALTER_SCHEMA_OWNER_SQL = "alter schema %s owner to %s;";

    /**
     * update description sql
     */
    public static final String UPDATE_DESCRIPTION_SQL = "comment on schema %s  IS '%s';";

    /**
     * create schema comment ddl sql
     */
    public static final String CREATE_SCHEMA_COMMENT_DDL_SQL = "comment on schema %s IS '%s';";

    /**
     * drop schema sql
     */
    public static final String DROP_SCHEMA_SQL = "drop schema %s;";

    /**
     * query coverage param
     */
    public static final String QUERY_COVERAGE_PARAM = "select setting from pg_settings "
            + "where name = 'enable_proc_coverage';";

    /**
     * his coverage oid sql
     */
    public static final String HIS_COVERAGE_OID_SQL = "select * from coverage.proc_coverage where pro_oid = %s;";

    /**
     * delete by id sql
     */
    public static final String DELETE_BY_ID_SQL = "delete coverage.proc_coverage where coverage_id in (%s);";

    /**
     * sequence sql
     */
    public static final String SEQUENCE_SQL = "SELECT start_value, increment_by, max_value, "
            + "min_value, is_called FROM %s;";

    /**
     * set value sql
     */
    public static final String SET_VALUE_SQL = "SELECT pg_catalog.setVal('%s',%d,%b);";

    /**
     * current value sql
     */
    public static final String CUR_VALUE_SQL = "select last_value from %s;";

    /**
     * count sql
     */
    public static final String COUNT_SQL = "select count(*) as count from %s.%s";

    /**
     * course sql
     */
    public static final String COURSE_SQL = "CURSOR %s NO SCROLL FOR %s";

    /**
     * fetch sql
     */
    public static final String FETCH_SQL = "FETCH FORWARD %d FROM %s";

    /**
     * query foreign server sql
     */
    public static final String QUERY_FOREIGN_SERVER_SQL = "select srvname, fdwname from pg_foreign_server pfs "
            + "left join pg_namespace pn on pfs.srvowner = pn.nspowner "
            + "left join pg_foreign_data_wrapper pfdw on pfdw.oid = pfs.srvfdw "
            + "where pn.nspname = '%s';";

    /**
     * query extension sql
     */
    public static final String QUERY_EXTENSION_SQL = "select * from pg_foreign_data_wrapper;";

    /**
     * create extension sql
     */
    public static final String CREATE_EXTENSION_SQL = "CREATE EXTENSION IF NOT EXISTS ";

    /**
     * create foreign server sql
     */
    public static final String CREATE_FOREIGN_SERVER_SQL = "CREATE SERVER %s FOREIGN DATA WRAPPER %s "
            + "OPTIONS (host '%s', port '%s', dbname '%s');";

    /**
     * mapping name sql
     */
    public static final String MAPPING_NAME_SQL = "select usename from pg_user_mappings where srvname = '%s';";

    /**
     * create mapping sql
     */
    public static final String CREATE_MAPPING_SQL = "CREATE USER MAPPING FOR %s SERVER %s "
            + "OPTIONS (user '%s', password '%s');";

    /**
     * far option sql
     */
    public static final String FAR_OPTION_SQL = "OPTIONS (schema_name '%s', table_name '%s');" + LF;

    /**
     * delete foreign table
     */
    public static final String DELETE_FOREIGN_TABLE_SQL = "DROP FOREIGN TABLE %s.%s;";

    /**
     * delete foreign mapping
     */
    public static final String DELETE_FOREIGN_MAPPING_SQL = "DROP USER MAPPING for %s SERVER %s;";

    /**
     * delete foreign server
     */
    public static final String DELETE_FOREIGN_SERVER_SQL = "DROP SERVER %s;";

    /**
     * foreign table attribute sql
     */
    public static final String FOREIGN_TABLE_ATTRIBUTE_SQL = "select pc.relname, pfdw.fdwname, pfs.srvname, "
            + "pn.nspname, pfs.srvoptions, pft.ftoptions, pd.description "
            + "from pg_class pc "
            + "left join pg_namespace pn on pn.oid = pc.relnamespace "
            + "left join pg_foreign_table pft on pft.ftrelid = pc.relfilenode "
            + "left join pg_foreign_server pfs on pfs.oid = pft.ftserver "
            + "left join pg_foreign_data_wrapper pfdw on pfdw.oid = pfs.srvfdw "
            + "left join pg_description pd on pd.objoid = pc.relfilenode and objsubid = 0 "
            + "where pc.relname = '%s' and pc.relkind = 'f' and pn.nspname = '%s';";

    /**
     * foreign table description
     */
    public static final String TABLE_DESCRIPTION_SQL = "COMMENT ON FOREIGN TABLE %s.%s IS '%s';";

    /**
     * query trigger sql
     */
    public static final String QUERY_TRIGGER_SQL = "select pt.oid as toid,pc.oid, pt.tgname, pt.tgenabled, pc.relkind, "
            + "pc.relname,pt.tgtype, pt.tgattr, pp.proname, regexp_replace(substring(pg_get_triggerdef(pt.oid) from "
            + "'WHEN \\(\\(.*\\)\\)'), '^WHEN \\(\\((.*)\\)\\)$', '\\1') AS tgqual, pd.description "
            + "from pg_trigger pt "
            + "left join pg_class pc on pc.oid = pt.tgrelid "
            + "left join pg_proc pp on pp.oid = pt.tgfoid "
            + "left join pg_description pd on pd.objoid = pt.oid "
            + "where pt.tgname = '%s' and pc.relname = '%s';";

    /**
     * query column name sql
     */
    public static final String QUERY_COLUMN_NAME_SQL = "select attname from pg_attribute "
            + "where attrelid = %s and attnum in (%s);";

    /**
     * rename trigger sql
     */
    public static final String RENAME_TRIGGER_SQL = "ALTER TRIGGER %s ON %s.%s RENAME TO %s;";

    /**
     * query trigger function sql
     */
    public static final String QUERY_TRIGGER_FUNCTION_SQL = "select pg.proname from pg_proc pg "
            + "left join pg_type pt on pt.oid = pg.prorettype "
            + "left join pg_namespace pn on pn.oid = pg.pronamespace "
            + "where pt.typname = 'trigger' and pn.nspname = '%s' order by pg.proname;";

    /**
     * drop trigger sql
     */
    public static final String DROP_TRIGGER_SQL = "DROP TRIGGER %s ON %s.%s;";

    /**
     * enable trigger sql
     */
    public static final String ENABLE_TRIGGER_SQL = "ALTER TABLE %s.%s ENABLE TRIGGER %s;";

    /**
     * disable trigger sql
     */
    public static final String DISABLE_TRIGGER_SQL = "ALTER TABLE %s.%s DISABLE TRIGGER %s;";

    /**
     * trigger ddl sql
     */
    public static final String TRIGGER_DDL_SQL = "select * from pg_get_triggerdef(%s);";

    /**
     * query job list sql
     */
    public static final String QUERY_JOB_LIST_SQL = "select pj.job_id, what as job_content, dbname as database_name, "
            + "job_status, interval, start_date, next_run_date, failure_count, failure_msg, last_end_date, "
            + "last_suc_date, log_user as creator, priv_user as executor "
            + "from pg_job pj left join pg_job_proc pjp on pj.job_id = pjp.job_id "
            + "order by what desc limit %s, %s;";

    /**
     * query job count sql
     */
    public static final String QUERY_JOB_COUNT_SQL = "select count(1) from pg_job;";

    /**
     * query job sql
     */
    public static final String QUERY_JOB_SQL = "select pj.job_id, what as job_content, "
            + "to_char(next_run_date,'yyyy-MM-dd HH24:mi:ss')  as next_run_date, interval "
            + "from pg_job pj left join pg_job_proc pjp on pj.job_id = pjp.job_id where pj.job_id = %s;";

    /**
     * create job sql
     */
    public static final String CREATE_JOB_SQL = "select pkg_service.job_submit(null, '%s', "
            + "to_timestamp('%s', 'yyyy-MM-dd HH24:mi:ss'), '%s');";

    /**
     * update job sql
     */
    public static final String UPDATE_JOB_SQL = "select pkg_service.job_update(%s, "
            + "to_timestamp('%s', 'yyyy-MM-dd HH24:mi:ss'), '%s', '%s');";

    /**
     * delete job sql
     */
    public static final String DELETE_JOB_SQL = "select pkg_service.job_cancel(%s);";

    /**
     * enable job sql
     */
    public static final String ENABLE_JOB_SQL = "select pkg_service.job_finish(%s, false, sysdate);";

    /**
     * delete job sql
     */
    public static final String DISABLE_JOB_SQL = "select pkg_service.job_finish(%s, true, sysdate);";
}
