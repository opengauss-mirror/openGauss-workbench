package com.nctigba.observability.instance.handler.topsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.opengauss.admin.common.exception.CustomException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nctigba.observability.instance.constants.DatabaseType;
import com.nctigba.observability.instance.dto.topsql.TopSQLListReq;
import com.nctigba.observability.instance.model.ExecutionPlan;
import com.nctigba.observability.instance.model.IndexAdvice;
import com.nctigba.observability.instance.model.InstanceNodeInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * TopSQL business of openGauss
 * </p>
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/15 14:39
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenGaussTopSQLHandler implements TopSQLHandler {
    private static final String TEST_SQL = "select 1";
    private static final String TOP_SQL_LIST_SQL = "select unique_query_id,debug_query_id,db_name,schema_name,user_name,application_name,start_time,finish_time,db_time,cpu_time,execution_time from dbe_perf.statement_history where debug_query_id != 0 and finish_time >= ? and finish_time <= ? order by %s desc,execution_time desc,cpu_time desc,db_time desc limit 10";
    // private static final String TOP_SQL_LIST_SQL = "select unique_query_id,debug_query_id,db_name,schema_name,user_name,application_name,start_time,finish_time,db_time,cpu_time,execution_time from dbe_perf.statement_history where debug_query_id != 0 and finish_time >= ? and finish_time <= ? order by %s desc,execution_time desc,cpu_time desc,db_time desc";
    private static final String PRE_CHECK_SET_SQL = "select name,setting from pg_settings where name in('enable_stmt_track','enable_resource_track','track_stmt_stat_level')";
    private static final String STATISTICAL_INFO_SQL =
            "select query,debug_query_id,unique_query_id,db_name,schema_name,\n" +
                    "substring(start_time,0,20) start_time,substring(finish_time,0,20) finish_time,\n" +
                    "user_name,application_name,client_addr||':'||client_port socket,\n" +
                    "n_returned_rows,n_tuples_fetched,n_tuples_returned,n_tuples_inserted,n_tuples_updated,n_tuples_deleted,lock_count,lock_wait_count,lock_max_count,\n" +
                    "(case when n_blocks_fetched=0 then '-' else substring((n_blocks_hit/n_blocks_fetched)*100,0,6)||'%' end) as blocks_hit_rate,\n" +
                    "net_send_info::json->'size' net_send_info_size,net_recv_info::json->'size' net_recv_info_size,\n" +
                    "net_stream_send_info::json->'size' net_stream_send_info_size,net_stream_recv_info::json->'size' net_stream_recv_info_size,\n" +
                    "net_send_info::json->'n_calls' net_send_info_calls,net_recv_info::json->'n_calls' net_recv_info_calls,\n" +
                    "net_stream_send_info::json->'n_calls' net_stream_send_info_calls,net_stream_recv_info::json->'n_calls' net_stream_recv_info_calls,\n" +
                    "net_send_info::json->'time' net_send_info_time,net_recv_info::json->'time' net_recv_info_time,\n" +
                    "net_stream_send_info::json->'time' net_stream_send_info_time,net_stream_recv_info::json->'time' net_stream_recv_info_time,\n" +
                    "n_soft_parse,n_hard_parse,db_time,cpu_time,(db_time-cpu_time) wait_time,lock_time,lock_wait_time,\n" +
                    "execution_time,parse_time,plan_time,rewrite_time,pl_execution_time,pl_compilation_time,data_io_time\n" +
                    "from dbe_perf.statement_history\n" +
                    "where debug_query_id=?";
    private static final String EXECUTION_PLAN_SQL = "select query_plan,query from dbe_perf.statement_history where debug_query_id=?;";
    private static final String SELECT_QUERY_SQL = "select query from dbe_perf.statement_history where debug_query_id=?";
    private static final String TABLE_METADATA_SQL =
            "select row_to_json(t) from (select schemaname,t1.relname,pg_relation_size(relid) object_size, relkind object_type,n_live_tup,n_dead_tup,\n" +
                    "case when n_live_tup+n_dead_tup=0 then '0.00%' else round(n_dead_tup*100/(n_dead_tup+n_live_tup),2)||'%' end dead_tup_ratio,\n" +
                    "last_vacuum,last_autovacuum,last_analyze,last_autoanalyze\n" +
                    "from pg_catalog.pg_stat_all_tables t1\n" +
                    "left join pg_catalog.pg_class t2 on t1.relid = t2.oid\n" +
                    "where t1.relname=?)t";
    private static final String INDEX_SQL =
            "select row_to_json(t) from (select c2.relname,i.indisprimary,i.indisunique,i.indisclustered,i.indisvalid,\n" +
                    "i.indisreplident,pg_catalog.pg_get_indexdef(i.indexrelid,0,true) as def\n" +
                    "from pg_catalog.pg_class c, pg_catalog.pg_class c2,pg_catalog.pg_index i\n" +
                    "where c.relname=? and c.oid=i.indrelid and c2.oid=i.indexrelid) t";
    private static final String TABLE_STRUCTURE_SQL =
            "select row_to_json(t) from (select a.attnum,a.attname,t.typname,a.attlen,a.attnotnull,b.description\n" +
                    "from pg_catalog.pg_class c,pg_catalog.pg_attribute a\n" +
                    "left outer join pg_catalog.pg_description b on a.attrelid=b.objoid and a.attnum=b.objsubid,pg_catalog.pg_type t\n" +
                    "where c.relname=? and a.attnum>0 and a.attrelid=c.oid and a.atttypid=t.oid\n" +
                    "order by a.attnum) t";
    private static final String WORK_MEM_SQL = "select setting from pg_settings WHERE name = 'work_mem'";
    private static final String PARTITION_LIST_SQL = "select partstrategy, partkey, relpages, reltuples, relallvisible, interval from pg_partition WHERE parttype = 'r'";
    private int totalPlanRows = 0;
    private int totalPlanWidth = 0;
    private List<String> objectNameList = new ArrayList<>();

    private boolean testConnection (Connection conn) {
        if (ObjectUtils.isNotEmpty(conn)) {
            try(PreparedStatement preparedStatement = conn.prepareStatement(TEST_SQL)) {
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    return true;
                }
            } catch (Exception e) {
                log.error("test connection fail:{}", e.getMessage());
                throw new CustomException(e.getMessage());
            }
        }
        return false;
    }

    @Override
    public Connection getConnection (InstanceNodeInfo nodeInfo) {
        String driver = "org.opengauss.Driver";
        String jdbcUrl = "jdbc:opengauss://" + nodeInfo.getIp() + ":" + nodeInfo.getPort() + "/"+ nodeInfo.getDbName();
        //String key = nodeInfo.getIp() + "_" + nodeInfo.getPort() + "_" + nodeInfo.getDbName() + "_" + nodeInfo.getDbUser();
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(jdbcUrl, nodeInfo.getDbUser(), nodeInfo.getDbUserPassword());
            if (testConnection(conn)) {
                return conn;
            }
        } catch (Exception e) {
            log.error("get connection fail:{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
        return null;
    }

    @Override
    public String getDatabaseType() {
        return DatabaseType.DEFAULT.getDbType();
    }

    @Override
    public List<JSONObject> getTopSQLList(InstanceNodeInfo nodeInfo, TopSQLListReq topSQLListReq) {
        // nodeInfo.setDbUserPassword(RSAUtil.decrypt(nodeInfo.getDbUserPassword()));
        List<JSONObject> list = new ArrayList<>();
        try(Connection conn = getConnection(nodeInfo)) {
            if (conn == null) {
                throw new CustomException("get database connection fail");
            }
            // param pre check
            if (topSqlListPreCheck(conn)) {
                return null;
            }
            try (PreparedStatement preparedStatement = conn.prepareStatement(String.format(TOP_SQL_LIST_SQL, StringUtils.isBlank(topSQLListReq.getOrderField()) ? "execution_time" : topSQLListReq.getOrderField()))) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(topSQLListReq.getStartTime()));
                preparedStatement.setTimestamp(2, Timestamp.valueOf(topSQLListReq.getFinishTime()));
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        JSONObject object = new JSONObject();
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            object.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
                        }
                        list.add(object);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("get TopSQL list fail:{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
        return list;
    }

    /**
     * pre-check top sql list job
     * @param connection connection info
     */
    private boolean topSqlListPreCheck(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(PRE_CHECK_SET_SQL)) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    String setting = rs.getString(2);
                    // "track_stmt_stat_level".equalsIgnoreCase(name) && StringUtils.startsWith(setting, "OFF") || "off".equalsIgnoreCase(setting)
                    if (
                            ("enable_resource_track".equalsIgnoreCase(name) && "off".equalsIgnoreCase(setting)) ||
                                    ("enable_stmt_track".equalsIgnoreCase(name) && "off".equalsIgnoreCase(setting)) ||
                                    ("track_stmt_stat_level".equalsIgnoreCase(name) && dealTrackStmtStatLevel(setting))
                    ) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            log.error("pre check fail:{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
        return false;
    }

    private boolean dealTrackStmtStatLevel (String setting) {
        if (StringUtils.isEmpty(setting) || !setting.contains(",")) {
            return true;
        }
        String[] settingArr = setting.split(",");
        if (settingArr[0].equalsIgnoreCase("off")) {
            return true;
        }
        return false;
    }

    @Override
    public JSONObject getStatisticalInfo(InstanceNodeInfo nodeInfo, String sqlId) {
        // nodeInfo.setDbUserPassword(RSAUtil.decrypt(nodeInfo.getDbUserPassword()));
        JSONObject result = new JSONObject();
        // get connection then execute query
        try (Connection connection = getConnection(nodeInfo)) {
            if (connection == null) {
                throw new CustomException("get database connection fail");
            }
            // get prepared statement then put parameters in
            try (PreparedStatement preparedStatement = connection.prepareStatement(STATISTICAL_INFO_SQL)) {
                preparedStatement.setString(1, sqlId);
                // get result from result set
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            result.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("get TopSQL statistical info fail:{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
        return result;
    }

    public String getPeakMemory(String planStr) {
        if (StringUtils.isEmpty(planStr)) {
            return "";
        }
        if (planStr.contains("Peak Memory")) {
            String[] planArr = planStr.split("Peak Memory:");
            if (planArr.length > 1) {
                return planArr[1].substring(0, planArr[1].indexOf("(KB)"));
            }
        }
        return "";
    }

    public String getWorkMem(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(WORK_MEM_SQL)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "";
    }

    public List<JSONObject> getRowsDiff (String planStr) {
        List<JSONObject> result = new ArrayList<>();
        List<String> planStrList = Arrays.asList(planStr.split("\n"));
        LinkedList<String> modifyLines = new LinkedList<>(planStrList);
        try {
            for (String item : planStrList) {
                if ((item.contains("cost=") && !item.contains("Result")) || item.contains("Hash Cond") || item.contains("->")) {
                    continue;
                }
                modifyLines.remove(item);
            }
            for (String mItem : modifyLines) {
                String[] mItemArr = mItem.split("rows=");
                if (mItem.length() <= 2) {
                    continue;
                }
                // there are two rows
                JSONObject jsonObject = new JSONObject();
                String firstStr = mItemArr[0];
                if (!firstStr.contains("(cost=")) {
                    continue;
                }
                if (firstStr.contains("->")) {
                    jsonObject.put("stepName", firstStr.substring(firstStr.indexOf("->") + 2, firstStr.indexOf("(cost=")).trim());
                } else {
                    jsonObject.put("stepName", firstStr.substring(0, firstStr.indexOf("(cost=")).trim());
                }
                jsonObject.put("estimateRows", mItemArr[1].substring(0, mItemArr[1].indexOf(" ")));
                jsonObject.put("actualRows", mItemArr[2].substring(0, mItemArr[2].indexOf(" ")));
                result.add(jsonObject);
            }
            return result;
        } catch (Exception e) {
            log.error("get Rows diff fail:{}", e.getMessage());
            return result;
        }
    }

    @Override
    public JSONObject getExecutionPlan(InstanceNodeInfo nodeInfo, String sqlId, String type) {
        // nodeInfo.setDbUserPassword(RSAUtil.decrypt(nodeInfo.getDbUserPassword()));
        // store return result
        JSONObject parsedResult = new JSONObject();
        // get connection then execute query
        try (Connection connection = getConnection(nodeInfo)) {
            if (connection == null) {
                throw new CustomException("get database connection fail");
            }
            // pre-check track_stmt_stat_leve full sql level at least L1
            if (executionPlanPreCheck(connection)) {
                return null;
            }
            // get prepared statement
            try (PreparedStatement preparedStatement = connection.prepareStatement(EXECUTION_PLAN_SQL)) {
                preparedStatement.setString(1, sqlId);
                // native plan
                if ("native".equals(type)) {
                    JSONArray nativePlanArray = new JSONArray();
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        while (rs.next()) {
                            JSONObject jsonObject = new JSONObject();
                            String planStr = rs.getString(1);
                            jsonObject.put("rowsDiff", getRowsDiff(planStr));
                            jsonObject.put("queryPlan", planStr);
                            jsonObject.put("peakMem", getPeakMemory(planStr));
                            jsonObject.put("workMem", getWorkMem(connection));
                            nativePlanArray.add(jsonObject);
                        }
                    }
                    parsedResult.put("plan", nativePlanArray);
                    return parsedResult;
                }
                // json plan
                // get result from result set
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        List<String> splitLines = new ArrayList<>();
                        String executionPlan = rs.getString(1);
                        if (StringUtils.isBlank(executionPlan)) {
                            throw new CustomException("failGetExecutionPlan");
                        } else {
                            splitLines = Arrays.asList(executionPlan.split("\n"));
                        }
                        LinkedList<String> modifySplitLines = new LinkedList<>(splitLines);
                        // remove non-operation or non-condition lines
                        for (String line : splitLines) {
                            if ((line.contains("cost=") && !line.contains("Result"))|| line.contains("Hash Cond")) {
                                continue;
                            }
                            modifySplitLines.remove(line);
                        }
                        if (modifySplitLines.size() == 0) {
                            throw new CustomException("failResolveExecutionPlan");
                        }
                        // get base execution plan object
                        totalPlanWidth = 0;
                        totalPlanRows = 0;
                        ExecutionPlan plan = processExecutionPlanString(modifySplitLines.get(0));
                        // get tree shape execution plan object
                        assert plan != null;
                        processExecutionPlan(modifySplitLines.subList(1, modifySplitLines.size()), 0, plan, plan.getChildren());
                        // put plan node
                        JSONObject result = JSONObject.parseObject(JSON.toJSONString(plan));
                        JSONArray data = new JSONArray();
                        data.add(result);
                        parsedResult.put("data", data);
                        // put total rows and width
                        JSONObject total = new JSONObject();
                        total.put("totalPlanRows", totalPlanRows);
                        total.put("totalPlanWidth", totalPlanWidth);
                        parsedResult.put("total", total);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("get TopSQL execute plan fail:{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
        return parsedResult;
    }

    /**
     * pre-check execution plan job
     * @param connection connection info
     */
    private boolean executionPlanPreCheck(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(PRE_CHECK_SET_SQL)) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    String setting = rs.getString(2);
                    if ("track_stmt_stat_level".equalsIgnoreCase(name)) {
                        if (StringUtils.startsWith(setting, "OFF") || StringUtils.startsWith(setting, "L0")) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("pre check fail:{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
        return false;
    }

    /**
     * generate execution plan tree shape json object
     *
     * @param lines          lines of execution plan and condition
     * @param previousIndent last level indent length
     * @param plan           last level plan
     * @param children      last level plan children
     */
    public void processExecutionPlan(List<String> lines, int previousIndent, ExecutionPlan plan, List<ExecutionPlan> children) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // process condition
            if (line.contains("Hash Cond")) {
                String[] split = line.split(": ");
                plan.setJoinType(split[1]);
                continue;
            }
            // skip processed lines
            if (line.isEmpty()) {
                continue;
            }
            String[] indentPlanSplit = line.split("->");
            int currentIndent = indentPlanSplit[0].length();
            // when current indent greater than previous indent, add new children node and go into next tree level
            if (currentIndent > previousIndent) {
                lines.set(i, "");
                ExecutionPlan subPlan = processExecutionPlanString(line);
                children.add(subPlan);
                assert subPlan != null;
                processExecutionPlan(lines.subList(i + 1, lines.size()), currentIndent, subPlan, subPlan.getChildren());
            }
            // when current indent less than or equals previous indent, return to last level
            if (currentIndent <= previousIndent) {
                return;
            }
        }
    }

    /**
     * parse execution plan line string into object
     *
     * @param line execution plan line string
     * @return execution plan line object
     */
    public ExecutionPlan processExecutionPlanString(String line) {
        if (line.contains("Hash Cond")) {
            return null;
        }
        ExecutionPlan plan = new ExecutionPlan();
        // check if first line
        String planString;
        if (line.contains("->")) {
            String[] indentPlanSplit = line.split("->");
            planString = indentPlanSplit[1];
        } else {
            planString = line;
        }
        String[] operationParameterSplit = planString.split("\\(");
        // set operation name and alias name
        String operation = operationParameterSplit[0];
        if (operation.contains(" on ")) {
            String[] split = operation.split(" on ");
            plan.setNodeType(split[0].trim());
            // only show object name, remove alias name
            String objectName = split[1];
            String alias;
            if (objectName.contains(" ")) {
                String[] aliasSplit = objectName.split(" ");
                alias = aliasSplit[0];
            } else {
                alias = objectName;
            }
            plan.setAlias(alias);
            if (!objectNameList.contains(alias)) {
                objectNameList.add(alias);
            }
        } else {
            plan.setNodeType(operation.trim());
        }
        // set parameters
        String parameters = operationParameterSplit[1].replace(")", "");
        String[] parametersSplit = parameters.split(" ");
        for (String parameterSplit:parametersSplit) {
            if (StringUtils.startsWith(parameterSplit, "cost")) {
                // set start cost and total cost
                String[] costSplit = parameterSplit.split("=");
                String[] startCostTotalCostSplit = costSplit[1].split("\\.\\.");
                plan.setStartupCost(Double.parseDouble(startCostTotalCostSplit[0]));
                plan.setTotalCost(Double.parseDouble(startCostTotalCostSplit[1]));
            } else if (StringUtils.startsWith(parameterSplit, "rows=")) {
                // set plan rows
                String[] rowSplit = parameterSplit.split("=");
                int rowsNum = Integer.parseInt(rowSplit[1]);
                plan.setPlanRows(rowsNum);
                totalPlanRows += rowsNum;
            } else if (StringUtils.startsWith(parameterSplit, "width=")) {
                // set plan width
                String[] widthSplit = parameterSplit.split("=");
                int widthNum = Integer.parseInt(widthSplit[1]);
                plan.setPlanWidth(widthNum);
                totalPlanWidth += widthNum;
            }
        }
        return plan;
    }

    @Override
    public List<JSONObject> getPartitionList(InstanceNodeInfo nodeInfo, String sqlId) {
        // nodeInfo.setDbUserPassword(RSAUtil.decrypt(nodeInfo.getDbUserPassword()));
        List<JSONObject> results = new ArrayList<>();
        try (Connection connection = getConnection(nodeInfo)) {
            if (connection == null) {
                throw new CustomException("get database connection fail");
            }
            // get prepared statement then put parameters in
            try (PreparedStatement preparedStatement = connection.prepareStatement(PARTITION_LIST_SQL)) {
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("partstrategy", rs.getString(1));
                        jsonObject.put("partkey", rs.getString(2));
                        jsonObject.put("relpages", rs.getString(3));
                        jsonObject.put("reltuples", rs.getString(4));
                        jsonObject.put("relallvisible", rs.getString(5));
                        jsonObject.put("interval", rs.getString(6));
                        results.add(jsonObject);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("get TopSQL Partition List fail:{}", e.getMessage());
            return results;
        }
        return results;
    }

    @Override
    public List<String> getIndexAdvice(InstanceNodeInfo nodeInfo, String sqlId) {
        // nodeInfo.setDbUserPassword(RSAUtil.decrypt(nodeInfo.getDbUserPassword()));
        List<String> results = new ArrayList<>();
        List<IndexAdvice> advices = new ArrayList<>();
        String queryText = "";
        // get connection then execute query
        try (Connection connection = getConnection(nodeInfo)) {
            if (connection == null) {
                throw new CustomException("get database connection fail");
            }
            // get prepared statement then put parameters in
            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_QUERY_SQL)) {
                preparedStatement.setString(1, sqlId);
                // get query text from result set
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                            queryText = rs.getString(i);
                        }
                    }
                }
            }
            // check if queryText is empty
            if (queryText.isEmpty()) {
                throw new CustomException("get query text fail");
            }
            String completeQueryText = "select * from gs_index_advise('" + queryText.replace("\n", " ").replace("'", "''") + "')";
            log.info("get complete query text: {}", completeQueryText);
            // get prepared statement
            try (PreparedStatement preparedStatement = connection.prepareStatement(completeQueryText)) {
                // get index advice from result set
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        IndexAdvice advice = new IndexAdvice();
                        advice.setSchema(rs.getString(1));
                        advice.setTable(rs.getString(2));
                        advice.setColumn(rs.getString(3));
                        advice.setIndexType(rs.getString(4));
                        advices.add(advice);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("get TopSQL index advice fail:{}", e.getMessage());
            results.add("No index suggestions");
            return results;
        }
        processAdvice(results, advices);
        return results;
    }

    /**
     * generate readable index advice
     * @param results return result
     * @param advices list of index advice
     */
    private void processAdvice(List<String> results, List<IndexAdvice> advices) {
        String indexTemplate = "建议为%s模式下的%t表的%c列创建索引";
        String multiColumnIndexTemplate = "建议为%s模式下的%t表的%c创建复合索引";
        // return specific message when get no index advice
        if (advices.isEmpty()) {
            results.add("No index suggestions");
            return;
        }
        // process index advice for every returned line
        for (IndexAdvice advice : advices) {
            String column = advice.getColumn();
            if (StringUtils.isNotEmpty(column)) {
                String result = column.contains(",") ? multiColumnIndexTemplate : indexTemplate;
                result = result.replace("%s", advice.getSchema());
                result = result.replace("%t", advice.getTable());
                result = result.replace("%c", advice.getColumn());
                results.add(result);
            }
        }
    }

    @Override
    public JSONObject getObjectInfo(InstanceNodeInfo nodeInfo, String sqlId) {
        // init objectNameList via get execution plan
        this.objectNameList = new ArrayList<>();
        if (null == this.getExecutionPlan(nodeInfo, sqlId, "")) {
            throw new CustomException("failGetExecutionPlan");
        }
        List<String> curObjectNameList = this.objectNameList;
        LinkedList<String> modifyObjectNameList = new LinkedList<>(curObjectNameList);
        // get connection then execute query
        JSONObject results = new JSONObject();
        JSONObject tableMetadata = new JSONObject();
        JSONObject tableStructure = new JSONObject();
        JSONObject tableIndex = new JSONObject();
        try (Connection connection = getConnection(nodeInfo)) {
            if (connection == null) {
                throw new CustomException("get database connection fail");
            }
            // query table metadata
            try (PreparedStatement preparedStatement = connection.prepareStatement(TABLE_METADATA_SQL)) {
                for (String name : curObjectNameList) {
                    preparedStatement.setString(1, name);
                    // get result from result set
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        while (rs.next()) {
                            JSONObject result = JSONObject.parseObject(rs.getString(1));
                            tableMetadata.put(name, result);
                        }
                    } catch (SQLException e) {
                        modifyObjectNameList.remove(name);
                    }
                }
            }
            // query table structure
            try (PreparedStatement preparedStatement = connection.prepareStatement(TABLE_STRUCTURE_SQL)) {
                for (String name : curObjectNameList) {
                    preparedStatement.setString(1, name);
                    // get result from result set
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        JSONArray resultArray = new JSONArray();
                        while (rs.next()) {
                            resultArray.add(JSONObject.parseObject(rs.getString(1)));
                        }
                        tableStructure.put(name, resultArray);
                    } catch (SQLException e) {
                        modifyObjectNameList.remove(name);
                    }
                }
            }
            // query index info
            try (PreparedStatement preparedStatement = connection.prepareStatement(INDEX_SQL)) {
                for (String name : curObjectNameList) {
                    preparedStatement.setString(1, name);
                    // get result from result set
                    try (ResultSet rs = preparedStatement.executeQuery()) {
                        JSONArray parsedResult = new JSONArray();
                        while (rs.next()) {
                            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                                parsedResult.add(JSONObject.parseObject(rs.getString(1)));
                            }
                        }
                        tableIndex.put(name, parsedResult);
                    } catch (SQLException e) {
                        modifyObjectNameList.remove(name);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("get TopSQL object information fail:{}", e.getMessage());
            throw new CustomException(e.getMessage());
        }
        results.put("object_name_list", modifyObjectNameList);
        results.put("table_metadata", tableMetadata);
        results.put("table_structure", tableStructure);
        results.put("table_index", tableIndex);
        return results;
    }
}
