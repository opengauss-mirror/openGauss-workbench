/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package com.nctigba.observability.instance.config;

import cn.hutool.core.io.FileUtil;
import com.nctigba.observability.instance.constants.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Component;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ParamInfoInitConfig {
    public static final String PARAMINFO = "paramInfo";
    public static final String PARAMVALUEINFO = "paramValueInfo";
    private static final Map<String, Connection> map = new HashMap<>();
    private static final String PARAMINFOPATH = "data" + File.separatorChar + "NewParamInfo.db";
    private static final String PARAMVALUEINFOPATH = "data" + File.separatorChar + "NewParamValueInfo.db";

    public static Connection getCon(String key) {
        if (!map.containsKey(key)) {
            synchronized (map) {
                if (!map.containsKey(key)) {
                    try {
                        init();
                    } catch (IOException e) {
                        throw new CustomException("sqlite error", e);
                    }
                }
            }
        }
        return map.get(key);
    }

    private static final String[] paramInfos = {
            "CREATE TABLE param_info (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, paramType TEXT,"
                    + " paramName TEXT, parameterCategory TEXT, valueRange TEXT, paramDetail TEXT, suggestValue TEXT,"
                    + " defaultValue TEXT,"
                    + " unit TEXT, suggestExplain TEXT, diagnosisRule TEXT);",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"recovery_max_workers\",\"\",\"\",\"\",\"\",\"1\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"recovery_parse_workers\",\"\",\"\",\"\",\"\",\"1\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"recovery_redo_workers\",\"\",\"\",\"\",\"\",\"1\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"recovery_min_apply_delay\",\"\",\"\",\"\",\"\",\"1\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"wal_level\",\"\",\"\",\"\",\"\",\"hot_standby\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"synchronous_commit\",\"\",\"\",\"\",\"\",\"on\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"wal_buffers\",\"\",\"\",\"\","
                    + "\"12800-128000\",\"2048\",\"\","
                    + "\"\",\"actualValue>=12800 && actualValue<=128000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"wal_writer_delay\",\"\",\"\",\"\",\"\",\"200\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"commit_delay\",\"\",\"\",\"\",\"\",\"0\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"commit_siblings\",\"\",\"\",\"\",\"\",\"5\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"wal_flush_timeout\",\"\",\"\",\"\",\"\",\"2\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"recovery_time_target\",\"\",\"\",\"\",\"\",\"0\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"wal_flush_delay\",\"\",\"\",\"\",\"\",\"1\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_max_tw_buckets\",\"\",\"\",\"\","
                    + "\"10000\",\"180000\",\"\",\"\","
                    + "\"actualValue<=10000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_tw_reuse\",\"\",\"\",\"\",\"1\",\"0\",\"\","
                    + "\"\",\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_tw_recycle\",\"\",\"\",\"\",\"1\",\"0\",\"\",\"\","
                    + "\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"max_process_memory\",\"\",\"\","
                    + "\"\",\"2*1024*1024～INT_MAX\",\"12582912\",\"\","
                    + "\"\",\"actualValue>=2*1024*1024\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"work_mem\",\"\",\"\","
                    + "\"\",\"\",\"\",\"\",\"\",\"actualValue>=64 && actualValue<=2147483647\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"pagewriter_sleep\",\"\",\"\","
                    + "\"\",\"0～3600000\",\"2000\",\"\",\"\","
                    + "\"actualValue>0 && actualValue<3600000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.ip_local_port_range\",\"\",\"\",\"\","
                    + "\"26000-65535\",\"32768-61000\",\"\",\"\",\"actualValue=='26000 65535'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_keepalive_time\",\"\",\"\",\"\",\"30\",\"7200\","
                    + "\"\",\"\",\"actualValue==30\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_keepalive_probes\",\"\",\"\",\"\",\"9\",\"9\","
                    + "\"\",\"\",\"actualValue==9\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_keepalive_intvl\",\"\",\"\",\"\",\"30\",\"75\","
                    + "\"\",\"\",\"actualValue==30\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_retries1\",\"\",\"\",\"\",\"5\",\"3\","
                    + "\"\",\"\",\"actualValue==5\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_syn_retries\",\"\",\"\",\"\","
                    + "\"5\",\"5\",\"\",\"\",\"actualValue==5\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_synack_retries\",\"\",\"\",\"\","
                    + "\"5\",\"5\",\"\",\"\",\"actualValue==5\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_retries2\",\"\",\"\",\"\",\"12\",\"15\","
                    + "\"\",\"\",\"actualValue==12\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"vm.overcommit_memory\",\"\",\"\",\"\",\"0\",\"0\","
                    + "\"\",\"\",\"actualValue==0\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_rmem\",\"\",\"\","
                    + "\"\",\"8192 250000 16777216\",\"4096 87380 174760（4k）\","
                    + "\"\",\"\",\"actualValue=='8192 250000 16777216'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_wmem\",\"\",\"\",\"\","
                    + "\"8192 250000 16777216\",\"4096 16384 131072（4k）\",\"\",\"\","
                    + "\"actualValue=='8192 250000 16777216'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.core.wmem_max\",\"\",\"\",\"\","
                    + "\"21299200\",\"129024\",\"\",\"\",\"actualValue==21299200\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.core.rmem_max\",\"\",\"\",\"\","
                    + "\"21299200\",\"129024\",\"\",\"\",\"actualValue==21299200\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.core.wmem_default\",\"\",\"\",\"\","
                    + "\"21299200\",\"129024\",\"\",\"\",\"actualValue==21299200\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.core.rmem_default\",\"\",\"\",\"\","
                    + "\"21299200\",\"129024\",\"\",\"\",\"actualValue==21299200\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"kernel.sem\",\"\",\"\",\"\",\"250 6400000 1000 25600\","
                    + "\"250 32000 32 128\",\"\",\"\",\"actualValue=='250 6400000 1000 25600'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"vm.min_free_kbytes\",\"\",\"\",\"\","
                    + "\"5%\",\"724\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.core.somaxconn\",\"\",\"\",\"\","
                    + "\"65535\",\"128\",\"\",\"\",\"actualValue==65535\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_syncookies\",\"\",\"\",\"\",\"1\",\"0\","
                    + "\"\",\"\",\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.core.netdev_max_backlog\",\"\",\"\",\"\",\"65535\",\"1000\","
                    + "\"\",\"\",\"actualValue==65535\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_max_syn_backlog\",\"\",\"\",\"\",\"65535\",\"1024\","
                    + "\"\",\"\",\"actualValue==65535\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_fin_timeout\",\"\",\"\",\"\",\"60\","
                    + "\"60\",\"\",\"\",\"actualValue==60\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"kernel.shmall\",\"\",\"\",\"\",\"1152921504606840000\","
                    + "\"2097152\",\"\",\"\",\"actualValue==1152921504606840000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"kernel.shmmax\",\"\",\"\",\"\","
                    + "\"18446744073709500000\",\"33554432\",\"\",\"\",\"actualValue==18446744073709500000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_sack\",\"\",\"\",\"\","
                    + "\"1\",\"1\",\"\",\"\",\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"net.ipv4.tcp_timestamps\",\"\",\"\",\"\",\"1\",\"1\","
                    + "\"\",\"\",\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"vm.extfrag_threshold\",\"\",\"\",\"\",\"500\",\"500\","
                    + "\"\",\"\",\"actualValue==500\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"vm.overcommit_ratio\",\"\",\"\",\"\","
                    + "\"90\",\"50\",\"%\",\"\",\"actualValue==90\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"OS\",\"MTU\",\"network\",\"\",\"\",\"8192\",\"1500\","
                    + "\"\",\"\",\"actualValue==8192\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"bgwriter_delay\",\"\",\"\",\"\",\"\",\"2\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"pagewriter_thread_num\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"max_io_capacity\",\"\",\"\",\"\",\"\",\"512000\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"log_min_duration_statement\",\"\",\"\",\"\",\"\",\"1800000\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"log_duration\",\"\",\"\",\"\",\"\",\"on\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"track_stmt_stat_level\",\"\",\"\",\"\",\"\",\"OFF,L0\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"track_stmt_retention_time\",\"\",\"\",\"\",\"\",\"3600,604800\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"enable_thread_pool\",\"\",\"\",\"\",\"\",\"off\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"thread_pool_attr\",\"\",\"\",\"\",\"\",\"16, 2, (nobind)\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"log_statement\",\"\",\"\",\"\",\"\",\"none\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"log_error_verbosity\",\"\",\"\",\"\",\"\",\"default\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"log_min_messages\",\"\",\"\",\"\",\"\",\"warning\",\"\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO_SQL
                    + " values(\"DB\",\"log_min_error_statement\",\"\",\"\",\"\",\"\",\"error\",\"\"," + "\"\",\"\");"
    };

    private static final String[] paramValueVnfo = {
            "CREATE TABLE param_value_info (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, sid INTEGER,"
                    + " instance TEXT, actualValue TEXT);"
    };

    private static void init() throws IOException {
        map.put(PARAMINFO, initSqlite(PARAMINFOPATH, paramInfos));
        map.put(PARAMVALUEINFO, initSqlite(PARAMVALUEINFOPATH, paramValueVnfo));
    }

    private static Connection initSqlite(String path, String[] sqls) throws IOException {
        File f = FileUtil.file(path);
        log.info("sqlite:" + f.getCanonicalPath());
        if (!f.exists()) {
            var parent = f.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            boolean b = f.createNewFile();
            if (!b) {
                log.info("createNewFile fail:" + path);
            }
            var sqLiteDataSource = new SQLiteDataSource();
            sqLiteDataSource.setUrl(JDBC.PREFIX + f.getCanonicalPath());
            try {
                var conn = sqLiteDataSource.getConnection();
                for (String sql : sqls) {
                    try {
                        conn.createStatement().execute(sql);
                    } catch (SQLException e) {
                    }
                }
                return conn;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        var sqLiteDataSource = new SQLiteDataSource();
        sqLiteDataSource.setUrl(JDBC.PREFIX + f.getCanonicalPath());
        try {
            return sqLiteDataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}