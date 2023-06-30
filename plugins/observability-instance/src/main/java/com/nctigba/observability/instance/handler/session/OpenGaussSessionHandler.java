/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.handler.session;

import com.alibaba.fastjson.JSONObject;
import com.nctigba.common.web.exception.InstanceException;
import com.nctigba.observability.instance.constants.CommonConstants;
import com.nctigba.observability.instance.constants.DatabaseType;
import com.nctigba.observability.instance.dto.session.DetailStatisticDto;
import com.nctigba.observability.instance.model.InstanceNodeInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.nctigba.common.web.exception.InstanceExceptionMsgEnum.SESSION_DETAIL_GENERAL_MESSAGE;


@Component
@RequiredArgsConstructor
@Slf4j
public class OpenGaussSessionHandler implements SessionHandler {

    private static final String TEST_SQL = "select 1";
    private static final String CHECK_SESSION_IS_WAIT_SQL = "select count(waiting) as count from pg_stat_activity "
            + "where sessionid = ? and waiting";
    private static final String SESSION_GENERAL_MES_SQL =
            "select a.state, a.sessionid,  a.datname, a.usename, a.resource_pool, b.lwtid , "
                    + "TO_CHAR(a.backend_start,'YYYY-MM-DD HH:MI:SS') as backend_start, "
                    + "TO_CHAR((now()- a.backend_start),'HH24:MI:SS') as backend_runtime, "
                    + "a.client_addr, a.application_name, a.client_hostname, a.client_port, "
                    + "TO_CHAR(a.xact_start ,'YYYY-MM-DD HH:MI:SS') as xact_start, "
                    + "TO_CHAR(a.query_start ,'YYYY-MM-DD HH:MI:SS') as query_start, "
                    + "a.datname, a.query_id, a.query "
                    + "from pg_stat_activity a left join pg_thread_wait_status b on a.sessionid=b.sessionid "
                    + "where a.sessionid = ?";
    private static final String SESSION_BLOCK_MES_SQL =
            "select a.block_sessionid, pg_relation_filepath(b.relation) as filepath, "
                    + "b.page, b.tuple, b.bucket, a.wait_status, a.wait_event, a.lockmode, "
                    + "(select d.nspname || '.' || c.relname from pg_class c left join pg_namespace d  "
                    + "on c.relnamespace = d.oid  "
                    + "where c.oid = b.relation) as namespace_relation "
                    + "from pg_thread_wait_status a left join pg_locks b on a.sessionid = b.sessionid "
                    + "where a.sessionid = ? and not granted";
    private static final String SESSION_STATISTIC_SQL = "select stat_name,value from GS_SESSION_TIME "
            + "where sessid like '%' || ?";
    private static final String SESSION_RUNTIME_SQL = "select statname,value from GS_SESSION_STAT "
            + "where sessid like '%' || ?";
    private static final String SESSION_WAITING_REC_SQL =
            "select sample_time,wait_status,event,lockmode,locktag_decode(locktag) as locktag  " +
                    "from GS_ASP where sessionid = ? order by sample_time desc limit 10";
    private static final String SESSION_BLOCK_TREE = "with recursive tmp_lock as ( "
            + "    select distinct "
            + "           w.sessionid as id, "
            + "           r.sessionid as parentid, "
            + "           w.wait_status as wait_status, "
            + "           w.wait_event as wait_event, "
            + "           w.lockmode as lockmode         "
            + "    from ( "
            + "          select a.sessionid,a.locktype,a.database, "
            + "                 a.relation,a.page,a.tuple,a.classid, "
            + "                 a.objid,a.objsubid,a.pid,a.virtualtransaction,a.virtualxid, "
            + "                 a.transactionid, b.query as query, "
            + "                 b.xact_start,b.query_start,b.usename,b.datname ,c.wait_status,c.wait_event,c.lockmode "
            + "           from pg_locks a  "
            + "           left join pg_stat_activity b on a.sessionid=b.sessionid "
            + "           left join pg_thread_wait_status c on b.sessionid=c.sessionid "
            + "           where not a.granted  "
            + "          ) w, "
            + "         ( "
            + "          select a.sessionid,a.locktype,a.database, "
            + "                 a.relation,a.page,a.tuple,a.classid, "
            + "                 a.objid,a.objsubid,a.pid,a.virtualtransaction,a.virtualxid, "
            + "                 a.transactionid, b.query as query, "
            + "                 b.xact_start,b.query_start,b.usename,b.datname ,c.wait_status,c.wait_event,c.lockmode "
            + "           from pg_locks a  "
            + "           left join pg_stat_activity b on a.sessionid=b.sessionid "
            + "           left join pg_thread_wait_status c on b.sessionid=c.sessionid "
            + "           where a.granted  "
            + "          ) r  "
            + "    where 1=1 "
            + "      and r.locktype is not distinct from w.locktype  "
            + "      and r.database is not distinct from w.database  "
            + "      and r.relation is not distinct from w.relation  "
            + "      and r.page is not distinct from w.page  "
            + "      and r.tuple is not distinct from w.tuple  "
            + "      and r.classid is not distinct from w.classid  "
            + "      and r.objid is not distinct from w.objid  "
            + "      and r.objsubid is not distinct from w.objsubid  "
            + "      and r.transactionid is not distinct from w.transactionid  "
            + "      and r.sessionid <> w.sessionid "
            + "),tmp0 as ( "
            + "  select * "
            + "    from tmp_lock tl "
            + "   union all "
            + "  select t1.parentid,0::int4,p.wait_status,p.wait_event,null "
            + "    from tmp_lock t1 left join pg_thread_wait_status p on t1.parentid=p.sessionid  "
            + "   where 1=1 "
            + "     and t1.parentid not in (select id from tmp_lock) "
            + "), "
            + "  tmp3 as ( "
            + "  SELECT array[id]::text[] as pathid,1 as depth,id,parentid,wait_status,wait_event,lockmode "
            + "    FROM tmp0 "
            + "   where 1=1 "
            + "     and parentid=0 "
            + "   union "
            + "  SELECT t0.pathid||array[t1.id]::text[] as pathid,t0.depth+1 as depth,t1.id,t1.parentid, "
            + "         t1.wait_status,t1.wait_event,t1.lockmode "
            + "    FROM tmp0 t1,   "
            + "         tmp3 t0 "
            + "   where 1=1 "
            + "     and t1.parentid=t0.id "
            + ") "
            + "select distinct  "
            + "       '/'||array_to_string(a0.pathid,'/') as pathid, "
            + "       a0.depth, "
            + "       a0.id,a0.parentid, a0.pathid[1] as tree_id, "
            + "       lpad(a0.id::text, 2*a0.depth-1+length(a0.id::text),' ') as tree, "
            + "       a2.datname,a2.usename,a2.application_name,a2.client_addr,a2.state, "
            + "       TO_CHAR(a2.backend_start,'YYYY-MM-DD HH:MI:SS') as backend_start,a2.query,  "
            + "       a0.wait_status,a0.wait_event,a0.lockmode "
            + "  from tmp3 a0 "
            + "       left outer join (select distinct '/'||id||'/' as prefix_id,id "
            + "                        from tmp0 "
            + "             where 1=1 ) a1 "
            + "                  on position( a1.prefix_id in '/'||array_to_string(a0.pathid,'/')||'/' ) >0 "
            + "     left outer join pg_stat_activity a2 "
            + "                  on a0.id = a2.sessionid "
            + "order by '/'||array_to_string(a0.pathid,'/'),a0.depth;";
    private final String SESSION_SIMPLE_MES_SQL = "select a.max_conn, b.active, c.waiting, d.max_runtime from "
            + "(select setting::int max_conn, 1 as key from pg_settings where name='max_connections') a "
            + "left join  "
            + "(select count(*) as active, 1 as key from pg_stat_activity where state = 'active') b "
            + "on a.key=b.key "
            + " left join "
            + " (select count(*) as waiting, 1 as key from pg_stat_activity where waiting is true) c "
            + " on b.key=c.key "
            + " left join "
            + " (select extract(EPOCH from(max(now() - backend_start)))::INTEGER as max_runtime, 1 as key from "
            + "pg_stat_activity where application_name not in "
            + "('WLMArbiter','workload','WorkloadMonitor','WDRSnapshot','JobScheduler','PercentileJob'"
            + ",'statement flush thread','Asp','ApplyLauncher')) d "
            + " on c.key = d.key";
    private final String LONG_TXC_SQL =
            "SELECT pid,sessionid,usename,datname,application_name,client_addr,query, xact_start,  "
                    + "now() - xact_start xact_duration, query_start, now() - query_start query_duration, STATE  "
                    + "FROM pg_stat_activity WHERE STATE <>'idle' and application_name not in  "
                    + "('WLMArbiter','workload',"
                    + "'WorkloadMonitor','WDRSnapshot','JobScheduler','PercentileJob','statement flush thread','Asp',"
                    + "'ApplyLauncher') "
                    + "and now()-xact_start > interval '30 SECOND' "
                    + "ORDER BY xact_start;";

    @Override
    public String getDatabaseType() {
        return DatabaseType.DEFAULT.getDbType();
    }

    @Override
    public Connection getConnection(InstanceNodeInfo nodeInfo) {
        String driver = "org.opengauss.Driver";
        String jdbcUrl = "jdbc:opengauss://" + nodeInfo.getIp() + ":" + nodeInfo.getPort() + "/" + nodeInfo.getDbName();
        try {
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(jdbcUrl, nodeInfo.getDbUser(), nodeInfo.getDbUserPassword());
            if (testConnection(conn)) {
                return conn;
            }
        } catch (Exception e) {
            log.error("get connection fail:{}", e.getMessage());
            throw new InstanceException(e.getMessage());
        }
        return null;
    }

    @Override
    public void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new InstanceException("", e);
            }
        }
    }

    @Override
    public JSONObject detailGeneral(Connection conn, String sessionid) {
        JSONObject res = new JSONObject();
        try {
            if (conn == null) {
                throw new InstanceException(CommonConstants.CONNECTION_FAIL);
            }
            PreparedStatement generalMesStm = conn.prepareStatement(SESSION_GENERAL_MES_SQL);
            generalMesStm.setLong(1, Long.parseLong(sessionid));
            List<JSONObject> generalMesList = executeQuery(generalMesStm);
            if (generalMesList.size() == 0) {
                throw new InstanceException(SESSION_DETAIL_GENERAL_MESSAGE);
            }
            res.putAll(generalMesList.get(0));
            if (!checkSessionIsWaiting(conn, sessionid)) {
                return res;
            }
            PreparedStatement blockStm = conn.prepareStatement(SESSION_BLOCK_MES_SQL);
            blockStm.setLong(1, Long.parseLong(sessionid));
            List<JSONObject> blockList = executeQuery(blockStm);
            if (blockList.size() != 0) {
                res.putAll(blockList.get(0));
                //throw new InstanceException(SESSION_DETAIL_BLOCK_MESSAGE, blockList.size());
            }
        } catch (SQLException e) {
            log.error("get session_general_mes list fail", e);
            throw new InstanceException(e.getMessage(), e);
        }
        return res;
    }

    @Override
    public List<DetailStatisticDto> detailStatistic(Connection conn, String sessionid) {
        ArrayList<DetailStatisticDto> list = new ArrayList<>();
        if (conn == null) {
            throw new InstanceException(CommonConstants.CONNECTION_FAIL);
        }
        try {
            PreparedStatement statisticStm = conn.prepareStatement(SESSION_STATISTIC_SQL);
            statisticStm.setLong(1, Long.parseLong(sessionid));
            List<JSONObject> statisticList = executeQuery(statisticStm);
            PreparedStatement runtimeStm = conn.prepareStatement(SESSION_RUNTIME_SQL);
            runtimeStm.setLong(1, Long.parseLong(sessionid));
            List<JSONObject> runtimeList = executeQuery(runtimeStm);
            List<DetailStatisticDto> list1 = statisticList.stream().map(ob -> DetailStatisticDto
                    .builder().type(DetailStatisticDto.Type.STATUS).name(ob.getString("stat_name"))
                    .value(ob.getString("value")).build()).collect(Collectors.toList());
            List<DetailStatisticDto> list2 = runtimeList.stream().map(ob -> DetailStatisticDto
                    .builder().type(DetailStatisticDto.Type.RUNTIME).name(ob.getString("statname"))
                    .value(ob.getString("value")).build()).collect(Collectors.toList());
            list.addAll(list1);
            list.addAll(list2);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<JSONObject> detailWaiting(Connection conn, String sessionid) {
        List<JSONObject> list = new ArrayList<>();
        if (conn == null) {
            throw new InstanceException(CommonConstants.CONNECTION_FAIL);
        }
        try {
            if (!checkSessionIsWaiting(conn, sessionid)) {
                return list;
            }
            PreparedStatement stm = conn.prepareStatement(SESSION_WAITING_REC_SQL);
            stm.setLong(1, Long.parseLong(sessionid));
            list = executeQuery(stm);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public List<JSONObject> detailBlockTree(Connection conn, String sessionid) {
        List<JSONObject> queryList;
        if (conn == null) {
            throw new InstanceException(CommonConstants.CONNECTION_FAIL);
        }
        try {
            PreparedStatement stm = conn.prepareStatement(SESSION_BLOCK_TREE);
            queryList = executeQuery(stm);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (StringUtils.isNotEmpty(sessionid)) {
            Set<String> treeIdSet = queryList.stream().filter(obj -> obj.getString("pathid").contains(sessionid))
                    .map(obj -> obj.getString("tree_id")).collect(Collectors.toSet());
            queryList = queryList.stream().filter(obj -> treeIdSet.contains(obj.getString("tree_id")))
                    .collect(Collectors.toList());
        }
        return toTreeData(queryList);
    }

    @Override
    public JSONObject simpleStatistic(Connection conn) {
        List<JSONObject> queryList;
        if (conn == null) {
            throw new InstanceException(CommonConstants.CONNECTION_FAIL);
        }
        try {
            PreparedStatement stm = conn.prepareStatement(SESSION_SIMPLE_MES_SQL);
            queryList = executeQuery(stm);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return queryList.get(0);
    }

    @Override
    public List<JSONObject> longTxc(Connection conn) {
        List<JSONObject> queryList;
        if (conn == null) {
            throw new InstanceException(CommonConstants.CONNECTION_FAIL);
        }
        try {
            PreparedStatement stm = conn.prepareStatement(LONG_TXC_SQL);
            queryList = executeQuery(stm);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return queryList;
    }

    private List<JSONObject> executeQuery(PreparedStatement stm) throws SQLException {
        List<JSONObject> resList = new ArrayList<>();
        ResultSet rs = stm.executeQuery();
        while (rs.next()) {
            JSONObject object = new JSONObject();
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                object.put(rs.getMetaData().getColumnLabel(i), rs.getString(i));
            }
            resList.add(object);
        }
        return resList;
    }

    private boolean testConnection(Connection conn) {
        if (ObjectUtils.isNotEmpty(conn)) {
            try (PreparedStatement preparedStatement = conn.prepareStatement(TEST_SQL)) {
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    return true;
                }
            } catch (Exception e) {
                log.error("test connection fail:{}", e.getMessage());
                throw new InstanceException(e.getMessage());
            }
        }
        return false;
    }


    private List<JSONObject> toTreeData(List<JSONObject> list) {
        //Classify by parentid
        Map<String, List<JSONObject>> map = new HashMap<>();
        for (JSONObject object : list) {
            object.put("children", new ArrayList<JSONObject>());
            if (!map.containsKey(object.getString("parentid"))) {
                LinkedList<JSONObject> obs = new LinkedList<>();
                obs.add(object);
                map.put(object.getString("parentid"), obs);
            } else {
                List<JSONObject> obs = map.get(object.getString("parentid"));
                obs.add(object);
            }
        }
        // Get top parent list, wrapper the final tree result
        List<JSONObject> topParentList = map.getOrDefault("0", new ArrayList<>());
        // Get all child values recursively
        recursionTreeChild(map, topParentList);

        return topParentList;
    }


    @SuppressWarnings("unchecked")
    public void recursionTreeChild(Map<String, List<JSONObject>> map, List<JSONObject> parentList) {
        for (JSONObject object : parentList) {
            if (map.containsKey(object.getString("id"))) {
                List<JSONObject> newChildren = map.get(object.getString("id"));
                List<JSONObject> allChildren = (List<JSONObject>) object.get("children");
                allChildren.addAll(newChildren);
                recursionTreeChild(map, allChildren);
            }
        }
    }

    private boolean checkSessionIsWaiting(Connection conn, String sessionid) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(CHECK_SESSION_IS_WAIT_SQL);
        statement.setLong(1, Long.parseLong(sessionid));
        List<JSONObject> query = executeQuery(statement);
        return query.get(0).getIntValue("count") != 0;
    }
}