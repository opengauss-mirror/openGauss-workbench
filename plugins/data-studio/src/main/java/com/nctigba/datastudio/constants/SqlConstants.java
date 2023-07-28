/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.constants;

/**
 * SqlConstants
 *
 * @since 2023-06-25
 */
public class SqlConstants {
    public static final String POINT = ".";
    public static final String LF = System.getProperty("line.separator");
    public static final String SPACE = " ";

    public static final String LEFT_BRACKET = "(";

    public static final String RIGHT_BRACKET = ")";

    public static final String SEMICOLON = ";";

    public static final String QUOTES = "'";

    public static final String QUOTES_SEMICOLON = "';";

    public static final String QUOTES_LF_COMMA = "," + LF;

    public static final String COMMA = ",";

    public static final String COMMA_SPACE = ", ";

    public static final String COMMENT_ON_COLUMN_SQL = "COMMENT ON COLUMN %s.%s.%s IS '%s';" + LF;
    public static final String CONFIGURE_TIME = "?connectTimeout=30&socketTimeout=0";

    public static final String PROC_SQL = "select pn.nspname, pp.proname, pp.pronargs, pp.proargtypes, "
            + "pp.proallargtypes, pp.proargnames, pp.proargmodes, pp.prokind "
            + "from pg_proc pp left join pg_namespace pn on pp.pronamespace = pn.oid where pp.oid =%s;";

    public static final String QUERY_DEF_SQL = "select definition from PG_GET_FUNCTIONDEF(%s);";

    public static final String QUERY_OID_SQL = "select pp.oid from pg_proc pp "
            + "left join pg_namespace pn on pp.pronamespace = pn.oid where pp.proname = '%s' and pn.nspname = '%s';";

    public static final String TURN_ON_SQL = "select * from dbe_pldebugger.turn_on(%s);";

    public static final String ATTACH_SQL = "select * from dbe_pldebugger.attach('%s',%s);";

    public static final String ADD_BREAKPOINT_SQL = "select * from dbe_pldebugger.add_breakpoint(%s,%s);";

    public static final String INFO_BREAKPOINT_PRE = "select funcoid, lineno + ";

    public static final String INFO_BREAKPOINT_SQL = " as lineno, query, enable from dbe_pldebugger.info_breakpoints();";

    public static final String CONTINUE_SQL = "select * from dbe_pldebugger.continue();";

    public static final String BACKTRACE_SQL_PRE = "select funcoid, frameno, funcname, lineno + ";

    public static final String BACKTRACE_SQL = " as lineno from dbe_pldebugger.backtrace();";

    public static final String INFO_LOCALS_SQL = "select varname, value, vartype,"
            + " isconst from dbe_pldebugger.info_locals();";

    public static final String DELETE_BREAKPOINT_SQL = "select * from dbe_pldebugger.delete_breakpoint(%s);";

    public static final String DISABLE_BREAKPOINT_SQL = "select * from dbe_pldebugger.disable_breakpoint(%s);";

    public static final String ENABLE_BREAKPOINT_SQL = "select * from dbe_pldebugger.enable_breakpoint(%s);";

    public static final String STEP_SQL = "select * from dbe_pldebugger.step();";

    public static final String DEBUG_SERVER_INFO_SQL = "select * from dbe_pldebugger.local_debug_server_info();";

    public static final String NEXT_SQL = "select * from dbe_pldebugger.next();";

    public static final String FINISH_SQL = "select * from dbe_pldebugger.finish();";

    public static final String TURN_OFF_SQL = "select * from dbe_pldebugger.turn_off(%s);";

    public static final String ABORT_SQL = "select * from dbe_pldebugger.abort();";

    public static final String INFO_CODE_SQL = "SELECT * FROM DBE_PLDEBUGGER.info_code(%s);";

    public static final String GET_OID_NAME_SQL = "select typname from pg_type pt where pt.oid in (%s);";
    public static final String GET_URL_JDBC = "jdbc:opengauss://";
    public static final String GET_TYPENAME_SQL = "select a.oid,a.typname from pg_type a where a.oid<9999";
    public static final String GET_DATABASE_SQL = "select datname from pg_database;";
    public static final String GET_SCHEMA_NAME_SQL = "SELECT oid, nspname FROM pg_namespace where nspname not in "
            + "('blockchain','snapshot','dbe_perf','pkg_service','cstore','pg_toast');";
    public static final String GET_DATA_CONNECTION_SQL = "select id, type , name , driver , ip, port ,dataname , "
            + "username, userpassword ,webuser,edition from DATABASELINK WHERE";
    public static final String GET_DATA_CONNECTION_NOT_P_SQL = "select id, type , name , driver , ip, port ,dataname"
            + " , username, '' as userpassword ,webuser,edition from DATABASELINK WHERE";
    public static final String GET_DATABASELINK_COUNT_SQL = "select count(1) as count from DATABASELINK where";

    public static final String TABLE_DDL_SQL = "select pg_get_tabledef('%s.%s');";

    public static final String TABLE_DATA_SQL = "select * from %s.%s";
    public static final String FUNCTION_SQL = "select * from %s(%s)";
    public static final String VIEW_DATA_SQL = "select * from %s.%s LIMIT 300";

    public static final String TABLE_ANALYSE_SQL = "ANALYSE %s.%s ";

    public static final String TABLE_TRUNCATE_SQL = "TRUNCATE TABLE ONLY %s.%s ";

    public static final String VACUUM_SQL = "VACUUM %s.%s ";

    public static final String REINDEX_TABLE_SQL = "REINDEX TABLE %s.%s ";

    public static final String RENAME_TABLE_SQL = "ALTER TABLE %s.%s RENAME TO %s ";

    public static final String SCHEMA_TABLE_SQL = "ALTER TABLE ONLY %s.%s SET SCHEMA %s ";

    public static final String TABLESPACE_TABLE_SQL = "ALTER TABLE %s.%s SET TABLESPACE %s;";

    public static final String DROP_TABLE_SQL = "DROP TABLE %s.%s ;";

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

    public static final String TABLE_DATA_COUNT_SQL = "select reltuples from pg_class cla" + LF
            + "inner join pg_namespace ns on cla.relnamespace = ns.oid and ns.nspname = '%s'" + LF
            + "where relname = '%s'";

    public static final String GET_COLUMN_SQL = "select col.column_name,col.data_type,"
            + "case when col.is_nullable = 'YES' then FALSE  else TRUE end as is_null," + LF
            + "substr(col.column_default,1,instr(col.column_default,'::')-1) as column_default,"
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
            + "where  col.table_schema = '%s' and col.table_name = '%s'";
    public static final String ALTER_TABLE_SQL = "ALTER TABLE ";
    public static final String ALTER_TABLE_COLUMN_DROPQL = "ALTER TABLE %s.%s DROP COLUMN %s;";

    public static final String ALTER_TABLE_COLUMN_ADD_SQL = "ALTER TABLE %s.%s  ADD COLUMN %s ";

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
            + "       split_part(split_part(array_to_string(tbl.reloptions, ','),',', 2),'=',2) as compression ," + LF
            + "       tbl.relhasoids as hashoid," + LF
            + "       d.description as tbl_desc," + LF
            + "       'Y' as partition"
            + "  FROM pg_class tbl" + LF
            + "  LEFT JOIN pg_roles auth on (tbl.relowner = auth.oid)" + LF
            + "  left join pg_description d on (tbl.oid = d.objoid)" + LF
            + "  LEFT JOIN pg_tablespace tblsp ON (tbl.reltablespace = tblsp.oid)" + LF
            + " WHERE tbl.oid = %s";
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
            + "       split_part(split_part(array_to_string(tbl.reloptions, ','),',', 2),'=',2) as compression ," + LF
            + "       tbl.relhasoids as hashoid," + LF
            + "       d.description as tbl_desc" + LF
            + "  FROM pg_class tbl" + LF
            + "  LEFT JOIN pg_roles auth on (tbl.relowner = auth.oid)" + LF
            + "  left join pg_description d on (tbl.oid = d.objoid)" + LF
            + "  LEFT JOIN pg_tablespace tblsp ON (tbl.reltablespace = tblsp.oid)" + LF
            + " WHERE tbl.oid = %s";
    public static final String CREATE_SQL = "CREATE ";
    public static final String CREATE_VIEW_SQL = "CREATE %s VIEW %s.%s "
            + "AS "
            + "%s";
    public static final String CREATE_SYNONYM_SQL = "CREATE SYNONYM %s.%s FOR %s.%s;";
    public static final String CREATE_REPLACE_SYNONYM_SQL = "CREATE OR REPLACE SYNONYM %s.%s FOR %s.%s";
    public static final String CREATE_DATABASE_SQL = "CREATE DATABASE ";
    public static final String CREATE_TABLE_SQL = "CREATE TABLE %s.%s(" + LF;
    public static final String CREATE_TABLE_EXISTS_SQL = "CREATE TABLE IF NOT EXISTS %s.%s(" + LF;
    public static final String CREATE_UNLOGGED_TABLE_SQL = "CREATE UNLOGGED TABLE %s.%s(" + LF;
    public static final String CREATE_UNLOGGED_TABLE_EXISTS_SQL = "CREATE UNLOGGED TABLE IF NOT EXISTS %s.%s(" + LF;
    public static final String PARTITION_BY_SQL = "PARTITION BY ";
    public static final String RANGE_SQL = "RANGE(%s)(" + LF
            + "partition %s values less than (%s) tablespace %s)" + LF;
    public static final String INTERVAL_SQL = "RANGE(%s)\n INTERVAL(%s)(" + LF
            + "partition %s values less than (%s) tablespace %s)" + LF;
    public static final String LIST_SQL = "LIST(%s)(" + LF
            + "partition %s values (%s) tablespace %s";
    public static final String HASH_SQL = "HASH(%s)(" + LF
            + "partition %s tablespace %s";

    public static final String UNIQUE_SQL = "CONSTRAINT %s UNIQUE (%s)";

    public static final String UNIQUE_IMMEDIATE_SQL = "CONSTRAINT %s UNIQUE (%s)  DEFERRABLE INITIALLY IMMEDIATE ";
    public static final String PRIMARY_KEY_SQL = "CONSTRAINT %s PRIMARY KEY (%s)";
    public static final String CHECK_SQL = "CONSTRAINT %s CHECK (%s)";
    public static final String FOREIGN_KEY_SQL = "CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s.%s (%s)";
    public static final String PARTIAL_CLUSTER_KEY_SQL = "CONSTRAINT %s PARTIAL CLUSTER KEY (%s)";
    public static final String WITH_DOUBLE_SQL = "WITH (%s,%s)" + LF;
    public static final String WITH_SQL = "WITH (%s)" + LF;
    public static final String OIDS_SQL = "OIDS=TRUE" + LF;
    public static final String FILLFACTOR_SQL = "FILLFACTOR=%s" + LF;
    public static final String COLUMN_SQL = "WITH (orientation = column)" + LF;
    public static final String TABLESPACE_SQL = "TABLESPACE %s" + LF;
    public static final String COMMENT_TABLE_SQL = "COMMENT ON TABLE %s.%s IS '%s'" + LF;
    public static final String SEQUENCE_SET_SQL = "SET search_path = ";
    public static final String NO_MINVALUE_SQL = "NO MINVALUE";
    public static final String START_WITH_SQL = "START WITH ";
    public static final String MAXVALUE_SQL = "MAXVALUE ";
    public static final String CYCLE_SQL = "CYCLE";
    public static final String MINVALUE_SQL = "MINVALUE ";
    public static final String INCREMENT_BY_SQL = "INCREMENT BY ";
    public static final String MATERIALIZED_VIEW_SQL = "MATERIALIZED ";
    public static final String COMMON_VIEW_SQL = "OR REPLACE ";
    public static final String RENAME_KEYWORD_SQL = " RENAME ";
    public static final String TO_KEYWORD_SQL = " TO ";
    public static final String COLUMN_KEYWORD_SQL = "COLUMN ";
    public static final String TYPE_KEYWORD_SQL = " TYPE ";
    public static final String NULL_KEYWORD_SQL = " NULL";
    public static final String UNIQUE_KEYWORD_SQL = " UNIQUE";
    public static final String NOT_KEYWORD_SQL = " NOT";
    public static final String SET_KEYWORD_SQL = " SET ";
    public static final String DROP_DEFAULT_SQL = " ALTER TABLE %s.%s ALTER COLUMN %s DROP DEFAULT;";
    public static final String ALTER_DEFAULT_SQL = " ALTER TABLE %s.%s ALTER COLUMN %s SET DEFAULT %s;";
    public static final String DEFAULT_KEYWORD_SQL = " DEFAULT ";
    public static final String SELECT_KEYWORD_SQL = "SELECT ";
    public static final String SELECT_SEQUENCE_COUNT_SQL = "SELECT  count(1) as count "
            + " from PG_CLASS c INNER JOIN pg_namespace n ON n.oid = c.relnamespace" + LF
            + "and n.nspname = '%s' where relname = '%s' and relkind = 'S';";
    public static final String FROM_KEYWORD_SQL = " FROM ";
    public static final String SEQUENCE_KEYWORD_SQL = "SEQUENCE ";
    public static final String WITH_ENCODING_SQL = " WITH ENCODING '";
    public static final String LC_COLLATE_SQL = "' LC_COLLATE '";
    public static final String LC_CTYPE_SQL = "' LC_CTYPE '";
    public static final String CONNECTION_LIMIT_SQL = " CONNECTION LIMIT ";
    public static final String DBCOMPATIBILITY_SQL = "' DBCOMPATIBILITY '";
    public static final String START_KEYWORD_SQL = "START WITH ";
    public static final String NO_MINVALUE_KEYWORD_SQL = "NO MINVALUE ";
    public static final String INCREMENT_KEYWORD_SQL = "INCREMENT BY ";
    public static final String MINVALUE_KEYWORD_SQL = "MINVALUE ";
    public static final String MAXVALUE_KEYWORD_SQL = "MAXVALUE ";
    public static final String CACHE_KEYWORD_SQL = "CACHE ";
    public static final String CYCLE_KEYWORD_SQL = "CYCLE ";
    public static final String OWNED_KEYWORD_SQL = "OWNED BY ";
    public static final String DROP_SQL = "DROP ";
    public static final String DROP_VIEW_SQL = "DROP VIEW %s.%s;";
    public static final String DROP_MATERIALIZED_VIEW_SQL = "DROP MATERIALIZED VIEW IF EXISTS %s.%s;";
    public static final String DROP_SYNONYM_SQL = "DROP SYNONYM %s.%s ;";
    public static final String DROP_SEQUENCE_SQL = "DROP SEQUENCE %s.%s;";
    public static final String DROP_FUNCTION_KEYWORD_SQL = "DROP FUNCTION %s.%s ;";
    public static final String DROP_PROCEDURE_KEYWORD_SQL = "DROP PROCEDURE %s.%s ;";
    public static final String DROP_DATABASE_SQL = "DROP DATABASE %s ;";
    public static final String ALTER_RENAME_DATABASE_SQL = "ALTER DATABASE %s RENAME TO %s ;";
    public static final String ALTER_CONNECTION_LIMIT_SQL = "ALTER DATABASE %s  WITH CONNECTION LIMIT %s ;";
    public static final String ALTER_SQL = "ALTER ";

    public static final String CONSTRAINT_DROP_SQL = " ALTER TABLE %s.%s DROP  CONSTRAINT %s";
    public static final String CONSTRAINT_UNIQUE_SQL = " ALTER TABLE %s.%s ADD  CONSTRAINT %s UNIQUE (%s) ";
    public static final String CONSTRAINT_UNIQUE_IMMEDIATE_SQL = " ALTER TABLE %s.%s ADD  "
            + "CONSTRAINT %s UNIQUE (%s)  DEFERRABLE INITIALLY IMMEDIATE  ";
    public static final String CONSTRAINT_PRIMARY_SQL = " ALTER TABLE %s.%s ADD CONSTRAINT %s PRIMARY KEY (%s) ";
    public static final String CONSTRAINT_PRIMARY_IMMEDIATE_SQL = " ALTER TABLE %s.%s ADD CONSTRAINT %s "
            + "PRIMARY KEY (%s)  DEFERRABLE INITIALLY IMMEDIATE  ";
    public static final String CONSTRAINT_CHECK_SQL = "   ALTER TABLE %s.%s ADD CONSTRAINT %s CHECK (%s)  ";
    public static final String CONSTRAINT_PARTIAL_CLUSTER_KEY_SQL = "   ALTER TABLE %s.%s  ADD CONSTRAINT %s  "
            + "PARTIAL  CLUSTER KEY (%s)  ";
    public static final String CONSTRAINT_NO_CHECK_SQL = "   ALTER TABLE %s.%s ADD CONSTRAINT %s  %s  ";

    public static final String CONSTRAINT_FOREIGN_KEY_SQL = " ALTER TABLE %s.%s ADD CONSTRAINT %s "
            + "FOREIGN KEY (%s) REFERENCES %s.%s (%s) ";
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
    public static final String UNIQUE_CONSTRAINT_TABLE_SQL = "select  con.contype from pg_class cla "
            + "inner join pg_attribute att on cla.oid = att.attrelid and att.attnum > 0" + LF
            + "inner join (select oid, condeferrable, unnest(conkey) as conkey, conrelid,"
            + " contype, conname,confrelid ,confkey from pg_constraint) con " + LF
            + "on att.attnum = con.conkey and con.conrelid = cla.oid "
            + "inner join pg_namespace ns on cla.relnamespace = ns.oid and ns.nspname = '%s' " + LF
            + "LEFT JOIN pg_description d ON con.oid = d.objoid  where cla.relname = '%s' and con.contype ='p' ";
    public static final String CONSTRAINT_COMMENT_SQL = " COMMENT ON CONSTRAINT %s ON %s.%s is '%s' ";

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


    public static final String INDEX_DROP_SQL = " DROP INDEX  %s.%s ;";
    public static final String INDEX_CREATE_SQL = " CREATE %s INDEX %s ON %s.%s %s (%s); ";
    public static final String INDEX_COMMENT_SQL = " COMMENT ON INDEX  %s.%s is '%s' ;";
    public static final String INDEX_TABLE_SQL = "select pc.relname,pi.indisunique, am.amname,"
            + " CASE WHEN pg_catalog.pg_get_expr(pi.indexprs, pi.indrelid, true) is null "
            + "THEN string_agg(att.attname, ',' order by att.attname) ELSE null END attname, "
            + " pg_catalog.pg_get_expr(pi.indexprs, pi.indrelid, true) expression,  pg_description.description "
            + "from PG_INDEX pi inner join PG_CLASS pc on pi.indexrelid = pc.oid and pc.relkind = 'i' "
            + "inner join PG_ATTRIBUTE att on pc.oid = att.attrelid "
            + "LEFT JOIN pg_description  ON pi.indexrelid = pg_description.objoid JOIN pg_am am ON pc.relam = am.oid "
            + "inner join (select pc.oid from PG_CLASS pc inner join pg_namespace pn on pn.oid = pc.relnamespace "
            + "where pc.relname = '%s' and pn.nspname = '%s' ) tt on pi.indrelid = tt.oid "
            + "group by pc.relname, pi.indisunique, pi.indexprs, pi.indrelid, am.amname, pg_description.description;";

    public static final String SELECT_SEQUENCE_DDL_SQL = " SELECT sequence_schema,sequence_name,start_value,"
            + "increment,minimum_value,maximum_value,cycle_option" + LF
            + " FROM information_schema.sequences where sequence_schema = '%s'and sequence_name ='%s';";
    public static final String SELECT_VIEW_DDL_SQL = " SELECT c.relkind as relkind, n.nspname AS schemaname, "
            + "c.relname AS matviewname, pg_get_userbyid(c.relowner) AS matviewowner, c.relhasindex AS hasindexes, "
            + "pg_get_viewdef(c.oid) AS definition FROM pg_class c " + LF
            + "LEFT JOIN pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relname = '%s'"
            + " and c.relkind in ('m','v')";
    public static final String SELECT_VIEW_TYPE_SQL = " SELECT c.relkind as relkind FROM pg_class c LEFT JOIN "
            + "pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relname = '%s' and c.relkind in "
            + "('m','v')";
    public static final String SELECT_OBJECT_SQL = "select c.relname as relname from pg_class c INNER JOIN "
            + "pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relkind = '%s';";
    public static final String SELECT_VIEW_SQL = "select c.relname as viewname from pg_class c INNER JOIN "
            + "pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relkind in ('v','m');";
    public static final String SELECT_TABLE_SQL = "select c.relname as relname from pg_class c INNER JOIN "
            + "pg_namespace n ON n.oid = c.relnamespace and n.nspname = '%s' where c.relkind ='r';";
    public static final String SELECT_COLUMN_SQL = "select column_name from information_schema.columns where "
            + "table_schema ='%s' and table_name = '%s';";
    public static final String SELECT_FUNCTION_SQL = "SELECT proname,proargtypes FROM pg_proc" + LF
            + "WHERE pronamespace = (SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '%s');";
    public static final String SYNONYM_ATTRIBUTE_SQL = "select synname,synobjschema,rolname,synobjname from "
            + "PG_SYNONYM  a inner join pg_authid b on a.synowner = b.oid where synname = '%s';";
    public static final String DATABASE_ATTRIBUTE_SQL = "select ds.oid,ds.datname,"
            + "pg_encoding_to_char(ds.encoding) as encoding,ds.datallowconn," + LF
            + "  case when ds.datconnlimit = -1 " + LF
            + "       then '-1(不限制)'" + LF
            + "       else to_char(ds.datconnlimit)" + LF
            + "  end as datconnlimit,ts.spcname,ds.datcollate,ds.datctype from pg_database ds left join pg_tablespace "
            + "ts on ts.oid = ds.dattablespace where ds.datname = '%s';";
    public static final String DATABASE_UPDATA_ATTRIBUTE_SQL = "select datname as databaseName,pg_encoding_to_char"
            + "(encoding) as databaseCode,datcompatibility as compatibleType,datcollate as collation,datctype as "
            + "characterType,datconnlimit as conRestrictions from pg_database where datname = '%s';";

    public static final String QUERY_USERS_SQL = "select rolname from pg_roles;";
    public static final String QUERY_SCHEMA_SQL = "select pn.oid, pn.nspname, pr.rolname, pd.description "
            + "from pg_namespace pn "
            + "left join pg_description pd on pn.oid = pd.objoid left join pg_roles pr "
            + "on pr.oid = pn.nspowner where pn.oid = %s;";
    public static final String CREATE_SCHEMA_SQL = "create schema %s;";
    public static final String CREATE_SCHEMA_DDL_SQL = "create schema %s authorization %s;";
    public static final String ALTER_SCHEMA_NAME_SQL = "alter schema %s rename to %s;";
    public static final String ALTER_SCHEMA_OWNER_SQL = "alter schema %s owner to %s;";
    public static final String UPDATE_DESCRIPTION_SQL = "comment on schema %s  IS '%s';";
    public static final String CREATE_SCHEMA_COMMENT_DDL_SQL = "comment on schema %s IS '%s';";
    public static final String IS_SQL = " IS '";
    public static final String DROP_SCHEMA_SQL = "drop schema %s;";
    public static final String HIS_COVERAGE_OID_SQL = "select ph.*, pp.proname from public.his_coverage ph "
            + "left join pg_proc pp on pp.oid = ph.oid where ph.oid =%s order by cid desc;";
    public static final String DELETE_BY_ID_SQL = "delete public.his_coverage where cid in ('%s');";
    public static final String CREATE_COVERAGE_SQL = "CREATE TABLE IF NOT EXISTS public.his_coverage( " + LF
            + "oid BIGINT, cid BIGINT, coverageLines VARCHAR, remarkLines VARCHAR, " + LF
            + "endTime BIGINT, sourceCode VARCHAR, params VARCHAR, canBreakLine VARCHAR);";
    public static final String INSERT_COVERAGE_SQL = "insert into public.his_coverage VALUES(?,?,?,?,?,?,?,?);";
    public static final String SEQUENCE_SQL = "SELECT start_value, increment_by, max_value, "
            + "min_value, is_called FROM %s;";
    public static final String SET_VALUE_SQL = "SELECT pg_catalog.setVal('%s',%d,%b);";
    public static final String NEXT_VALUE_SQL = "SELECT pg_catalog.nextval('%s');";
}
