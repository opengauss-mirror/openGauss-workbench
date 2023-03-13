package com.nctigba.datastudio.constants;

public class SqlConstants {
    public static final String POINT = ".";

    public static final String PARENTHESES_LEFT = "(";

    public static final String PARENTHESES_RIGHT = ")";

    public static final String SEMICOLON = ";";

    public static final String QUOTES = "'";

    public static final String QUOTES_SEMICOLON = "';";

    public static final String PARENTHESES_SEMICOLON = ");";

    public static final String QUOTES_PARENTHESES_SEMICOLON = "');";

    public static final String QUOTES_COMMA = "',";

    public static final String COMMA = ",";

    public static final String COMMA_SPACE = ", ";
    public static final String CONFIGURE_TIME = "?connectTimeout=30&socketTimeout=0";

    public static final String TURN_ON_SQL = "select * from dbe_pldebugger.turn_on(";

    public static final String ATTACH_SQL = "select * from dbe_pldebugger.attach('";

    public static final String ADD_BREAKPOINT_SQL = "select * from dbe_pldebugger.add_breakpoint(";

    public static final String INFO_BREAKPOINT_PRE = "select funcoid, lineno + ";

    public static final String INFO_BREAKPOINT_SQL = " as lineno, query, enable from dbe_pldebugger.info_breakpoints();";

    public static final String CONTINUE_SQL = "select * from dbe_pldebugger.continue();";

    public static final String BACKTRACE_SQL_PRE = "select frameno, funcname, lineno + ";

    public static final String BACKTRACE_SQL = " as lineno from dbe_pldebugger.backtrace();";

    public static final String INFO_LOCALS_SQL = "select varname, value, vartype, isconst from dbe_pldebugger.info_locals();";

    public static final String DELETE_BREAKPOINT_SQL = "select * from dbe_pldebugger.delete_breakpoint(";

    public static final String DISABLE_BREAKPOINT_SQL = "select * from dbe_pldebugger.disable_breakpoint(";

    public static final String ENABLE_BREAKPOINT_SQL = "select * from dbe_pldebugger.enable_breakpoint(";

    public static final String STEP_SQL = "select * from dbe_pldebugger.step();";

    public static final String DEBUG_SERVER_INFO_SQL = "select * from dbe_pldebugger.local_debug_server_info();";

    public static final String NEXT_SQL = "select * from dbe_pldebugger.next();";

    public static final String FINISH_SQL = "select * from dbe_pldebugger.finish();";

    public static final String INFO_CODE_SQL = "select * from dbe_pldebugger.info_code(";

    public static final String TURN_OFF_SQL = "select * from dbe_pldebugger.turn_off(";

    public static final String ABORT_SQL = "select * from dbe_pldebugger.abort();";

    public static final String DEBUG_INFO_SQL = "select * from dbe_pldebugger.local_debug_server_info()";

    public static final String GET_TYPE_OID_SQL = "select oid from pg_type where typname = '";

    public static final String GET_OID_NAME_SQL = "select typname from pg_type pt where pt.oid = ";

    public static final String GET_PROC_SQL = "select pg.oid, pg.proname, pg.proallargtypes, pg.proargmodes, pg.proargtypes, pg.proargnames, pg.procost, pg.prorows," +
            "pg.proretset, pg.prosrc, pg.probin, pg.prokind, pg.proargsrc, pg.proisstrict, pg.provolatile, pg.fencedmode, pg.proshippable, pl.lanname, pt.typname \n" +
            "from pg_proc pg\n" +
            "left join pg_language pl on pl.oid = pg.prolang\n" +
            "left join pg_type pt on pt.oid = pg.prorettype\n" +
            "left join pg_namespace pn on pn.oid = pg.pronamespace\n" +
            "where pg.proname = '";

    public static final String GET_PROC_TYPE_SQL = "' and pg.proargtypes = '";
    public static final String GET_PROC_NAME_SQL = "' and pn.nspname = '";
    public static final String GET_URL_JDBC = "jdbc:opengauss://";
    public static final String GET_TYPENAME_SQL = "select a.oid,a.typname from pg_type a where a.oid<9999";
    public static final String GET_DATABASE_SQL = "select datname from pg_database;";
    public static final String GET_SCHEMA_NAME_SQL = "SELECT nspname as schema_name FROM pg_namespace where nspname not in ('blockchain','snapshot','dbe_perf','pkg_service','cstore','pg_toast');";
    public static final String GET_DATA_Connection_SQL = "select id, type , name , driver , ip, port ,dataname , username, userpassword ,webuser from DATABASELINK WHERE";
    public static final String GET_DATA_Connection_NOT_PASSWORD_SQL = "select id, type , name , driver , ip, port ,dataname , username, '' as userpassword ,webuser from DATABASELINK WHERE";
    public static final String GET_DATABASELINK_COUNT_SQL = "select count(1) as count from DATABASELINK where";

    public static final String TABLE_DEF_SQL = "select pg_get_tabledef('";

    public static final String TABLE_DATA_SQL = "select * from ";

    public static final String GET_CONSTRAINT_SQL = "select con.conname, string_agg(att.attname, ',' order by att.attname) as attname,\n" +
            "  con.contype, pg_get_constraintdef(con.oid), con.condeferrable, ns.nspname, 'pg_default' as tablespace\n" +
            "from pg_class cla\n" +
            "inner join pg_attribute att on cla.oid = att.attrelid and att.attnum > 0 \n" +
            "inner join (select oid, condeferrable, unnest(conkey) as conkey, conrelid, contype, conname from pg_constraint) con \n" +
            "  on att.attnum = con.conkey and con.conrelid = cla.oid\n" +
            "inner join pg_namespace ns on cla.relnamespace = ns.oid and ns.nspname = '";

    public static final String RELNAME_CONDITION = "'where cla.relname = '";

    public static final String CONSTRAINT_GROUP_BY_SQL = "' group by con.conname, con.contype, pg_get_constraintdef(con.oid), con.condeferrable, ns.nspname";

    public static final String GET_INDEX_SQL = "select pc.relname,string_agg(att.attname, ',' order by att.attname),\n" +
            "  pi.indisunique, '' as expression, 'pg_default' as tablespace\n" +
            "from PG_INDEX pi \n" +
            "inner join PG_CLASS pc on pi.indexrelid = pc.oid and pc.relkind = 'i'\n" +
            "inner join PG_ATTRIBUTE att on pc.oid = att.attrelid\n" +
            "inner join (select pc.oid from PG_CLASS pc\n" +
            "\t\tinner join pg_namespace pn on pn.oid = pc.relnamespace\n" +
            "\t\twhere pc.relname = '";

    public static final String NSPNAME_CONDITION = "' and pn.nspname = '";

    public static final String INDEX_GROUP_BY_SQL = "') tt\n" + "\t\ton pi.indrelid = tt.oid\n" +
            "group by pc.relname, pi.indisunique";

    public static final String GET_CLASS_OID_SQL = "select cla.oid from PG_CLASS cla left join pg_namespace pn on pn.oid = cla.relnamespace\n" +
            "where cla.relname = '";

    public static final String GET_COLUMN_SQL = "select pa.attname, pt.typname, pa.attnotnull, pd.description\n" +
            "from PG_ATTRIBUTE pa\n" +
            "left join pg_type pt on pt.oid = pa.atttypid\n" +
            "left join PG_DESCRIPTION pd on pd.objsubid = pa.attnum and pd.objoid = ";

    public static final String ATTRELID_CONDITION_SQL = "\nwhere pa.attnum > 0 and pa.attrelid = ";
    public static final String CREATE_SQL = "CREATE ";
    public static final String SEQUENCE_SET_SQL = "SET search_path = ";
    public static final String NO_MINVALUE_SQL = "NO MINVALUE";
    public static final String START_WITH_SQL = "START WITH ";
    public static final String MAXVALUE_SQL = "MAXVALUE ";
    public static final String CYCLE_SQL = "CYCLE";
    public static final String MINVALUE_SQL = "MINVALUE ";
    public static final String INCREMENT_BY_SQL = "INCREMENT BY ";
    public static final String MATERIALIZED_VIEW_SQL = "MATERIALIZED ";
    public static final String COMMON_VIEW_SQL = "OR REPLACE ";
    public static final String VIEW_KEYWORD_SQL = "VIEW ";
    public static final String SELECT_KEYWORD_SQL = "SELECT ";
    public static final String COUNT_SQL = " count(1) as count " ;
    public static final String FROM_CLASS_SQL = " from PG_CLASS c INNER JOIN pg_namespace n ON n.oid = c.relnamespace\n" +
            "and n.nspname = '" ;
    public static final String FROM_CLASS_WHERE_SQL = "' where relname = '" ;
    public static final String RELKIND_SQL = "' and relkind = '" ;
    public static final String SEQUENCE_KEYWORD_SQL = "SEQUENCE ";
    public static final String SYNONYM_KEYWORD_SQL = "SYNONYM ";
    public static final String WITH_KEYWORD_SQL = " WITH ";
    public static final String DATABASE_SQL = "DATABASE ";
    public static final String WITH_ENCODING_SQL = " WITH ENCODING '";
    public static final String LC_COLLATE_SQL = "' LC_COLLATE '";
    public static final String LC_CTYPE_SQL = "' LC_CTYPE '";
    public static final String CONNECTION_LIMIT_SQL = " CONNECTION LIMIT ";
    public static final String DBCOMPATIBILITY_SQL = "' DBCOMPATIBILITY '";
    public static final String REPLACE_KEYWORD_SQL = "OR REPLACE ";
    public static final String START_KEYWORD_SQL = "START WITH ";
    public static final String NO_MINVALUE_KEYWORD_SQL = "NO MINVALUE ";
    public static final String INCREMENT_KEYWORD_SQL = "INCREMENT BY ";
    public static final String MINVALUE_KEYWORD_SQL = "MINVALUE ";
    public static final String FOR_KEYWORD_SQL = "FOR";
    public static final String MAXVALUE_KEYWORD_SQL = "MAXVALUE ";
    public static final String CACHE_KEYWORD_SQL = "CACHE ";
    public static final String CYCLE_KEYWORD_SQL = "CYCLE ";
    public static final String FUNCTION_KEYWORD_SQL = "FUNCTION ";
    public static final String PROCEDURE_KEYWORD_SQL = "PROCEDURE ";
    public static final String OWNED_KEYWORD_SQL = "OWNED BY ";
    public static final String CONNECTIVES_SQL = "AS ";
    public static final String IF_EXISTS_SQL = "IF EXISTS ";
    public static final String DROP_SQL = "DROP ";
    public static final String ALTER_SQL = "ALTER ";
    public static final String RENAME_TO_SQL = " RENAME TO ";
    public static final String LIMIT_SQL = " LIMIT 500";
    public static final String SELECT_SEQUENCE_DDL_SQL = " SELECT sequence_schema,sequence_name,start_value,increment,minimum_value,maximum_value,cycle_option\n" +
            " FROM information_schema.sequences where sequence_schema = '";
    public static final String SELECT_SEQUENCE_DDL_WHERE_SQL = "'and sequence_name ='";
    public static final String SELECT_VIEW_DDL_SQL = " SELECT c.relkind as relkind, n.nspname AS schemaname, c.relname AS matviewname, pg_get_userbyid(c.relowner) AS matviewowner, c.relhasindex AS hasindexes, pg_get_viewdef(c.oid) AS definition FROM pg_class c \n" +
            "LEFT JOIN pg_namespace n ON n.oid = c.relnamespace and n.nspname = '";
    public static final String SELECT_VIEWNAME_DDL_WHERE_SQL = "' where c.relname = '";
    public static final String SELECT_VIEW_DDL_WHERE_SQL = "' and c.relkind in ('m','v')";
    public static final String SELECT_OBJECT_SQL = "select c.relname as relname from pg_class c INNER JOIN pg_namespace n ON n.oid = c.relnamespace and n.nspname = '";
    public static final String SELECT_OBJECT_WHERE_SQL = "' where c.relkind = '";
    public static final String SELECT_OBJECT_WHERE_IN_SQL = "' where c.relkind in ('";
    public static final String SELECT_COLUMN_SQL = "select column_name from information_schema.columns where table_schema ='";
    public static final String SELECT_COLUMN_WHERE_SQL = "' and table_name = '";
    public static final String SELECT_SYNONYM_SQL = "select synname from PG_SYNONYM where synobjschema ='";
    public static final String SELECT_FUNCTION_SQL = "SELECT proname,proargtypes FROM pg_proc\n" +
            "WHERE pronamespace = (SELECT pg_namespace.oid FROM pg_namespace WHERE nspname = '";
    public static final String SYNONYM_ATTRIBUTE_SQL = "select synname,synobjschema,rolname,synobjname from PG_SYNONYM  a inner join pg_authid b on a.synowner = b.oid where synname = '";
    public static final String SYNONYM_COUNT_SQL = "select count(1) count from PG_SYNONYM  where synname = '";
    public static final String DATABASE_ATTRIBUTE_SQL = "select ds.oid,ds.datname,pg_encoding_to_char(ds.encoding) as encoding,ds.datallowconn,\n" +
            "  case when ds.datconnlimit = -1 \n" +
            "       then '-1(不限制)'\n" +
            "       else to_char(ds.datconnlimit)\n" +
            "  end as datconnlimit,ts.spcname,ds.datcollate,ds.datctype from pg_database ds left join pg_tablespace ts on ts.oid = ds.dattablespace where ds.datname = '";
    public static final String DATABASE_UPDATA_ATTRIBUTE_SQL = "select datname as databaseName,pg_encoding_to_char(encoding) as databaseCode,datcompatibility as compatibleType,datcollate as collation,datctype as characterType,datconnlimit as conRestrictions from pg_database where datname = '";
}
