/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2025-2025. All rights reserved.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.common.constant;

import java.util.List;

/**
 * AgentConstants
 *
 * @author: wangchao
 * @date: 2025/4/18 11:40
 * @since 7.0.0-RC2
 **/
public interface AgentConstants {
    /**
     * 状态相关常量
     */
    interface Status {
        /**
         * heartbeat status up
         */
        String HEARTBEAT_STATUS_UP = "UP";

        /**
         * heartbeat status down
         */
        String HEARTBEAT_STATUS_DOWN = "DOWN";
    }

    /**
     * 空值与默认值相关常量
     */
    interface Default {
        /**
         * empty string
         */
        String EMPTY = "";

        /**
         * none
         */
        String NONE = "none";
    }

    /**
     * 字段与列表相关常量
     */
    interface Field {
        /**
         * id
         */
        String ID = "id";

        /**
         * batch id
         */
        String BATCH_ID = "batch_id";

        /**
         * task id
         */
        String TASK_ID = "task_id";

        /**
         * agent id
         */
        String AGENT_ID = "agent_id";

        /**
         * cluster node id
         */
        String CLUSTER_NODE_ID = "cluster_node_id";

        /**
         * cluster id
         */
        String CREATE_TIME = "create_time";

        /**
         * common field list
         */
        List<String> COMMON_FIELD_LIST = List.of(ID, TASK_ID, AGENT_ID, CLUSTER_NODE_ID, CREATE_TIME);

        /**
         * fingerprint table required field
         */
        String FINGERPRINT_TABLE_REQUIRED_FIELD = "fingerprint_id";

        /**
         * row id
         */
        String ROW_ID = "row_id";
    }

    /**
     * 监控任务模板相关常量
     */
    interface TaskTemplate {
        /**
         * host dynamic metrics collect_metric
         */
        String HOST_DYNAMIC_METRICS = "host_dynamic_metrics";

        /**
         * mysql monitor task template
         */
        String MYSQL_MONITOR_TASK_TEMPLATE = "mysql_instance_monitor";

        /**
         * opengauss monitor task template
         */
        String OPENGAUSS_MONITOR_TASK_TEMPLATE = "opengauss_instance_monitor";
    }

    /**
     * OpenGauss 监控指标相关常量
     */
    interface OpenGaussMetric {
        /**
         * opengauss monitor metric lock count
         */
        String OPENGAUSS_LOCK_COUNT = "opengauss.pg.locks.count";

        /**
         * opengauss monitor metric session count
         */
        String OPENGAUSS_SESSION_COUNT = "opengauss.pg.session.count";

        /**
         * opengauss monitor metric connection count
         */
        String OPENGAUSS_CONNECTION_COUNT = "opengauss.pg.connect.count";
    }

    /**
     * MySQL 监控指标相关常量
     */
    interface MySqlMetric {
        /**
         * mysql monitor metric role master or slave or cascade
         */
        String MYSQL_ROLE = "mysql.jdbc.monitor.role";

        /**
         * mysql monitor metric connection num
         */
        String MYSQL_CONNECTION_NUM = "mysql.jdbc.monitor.conn.num";

        /**
         * mysql monitor metric qps
         */
        String MYSQL_QPS = "mysql.jdbc.monitor.qps";

        /**
         * mysql monitor metric tps
         */
        String MYSQL_TPS = "mysql.jdbc.monitor.tps";

        /**
         * mysql monitor metric memory used
         */
        String MYSQL_MEMORY_USED = "mysql.jdbc.monitor.memory.used";

        /**
         * mysql monitor metric table space used
         */
        String MYSQL_TABLE_SPACE_USED = "mysql.jdbc.monitor.table.space.used";
    }

    /**
     * host metric 监控指标相关常量
     */
    interface HostMetric {
        /**
         * system cpu usage
         */
        String SYSTEM_CPU_USAGE = "system.cpu.usage";

        /**
         * system memory usage
         */
        String SYSTEM_MEMORY_USAGE = "system.memory.usage";

        /**
         * system memory total
         */
        String SYSTEM_MEMORY_TOTAL = "system.memory.total";

        /**
         * system memory available
         */
        String SYSTEM_MEMORY_AVAILABLE = "system.memory.available";

        /**
         * system network bytes received
         */
        String SYSTEM_NETWORK_RECEIVED = "system.net.bytes.received";

        /**
         * system network bytes sent
         */
        String SYSTEM_NETWORK_SENT = "system.net.bytes.sent";

        /**
         * system disk fs total
         */
        String SYSTEM_DISK_TOTAL = "system.disk.fs.total";

        /**
         * system disk fs used
         */
        String SYSTEM_DISK_USED = "system.disk.fs.used";

        /**
         * system disk fs free
         */
        String SYSTEM_DISK_FREE = "system.disk.fs.free";

        /**
         * system disk fs usage
         */
        String SYSTEM_DISK_USAGE = "system.disk.fs.usage";

        /**
         * system cpu info
         */
        String CPU_NAME = "system.cpu.name";

        /**
         * system cpu arch
         */
        String CPU_ARCH = "system.cpu.arch";

        /**
         * system cpu core num
         */
        String CPU_CORE_NUM = "system.cpu.core.count";

        /**
         * system cpu max frequency
         */
        String CPU_FREQUENCY = "system.cpu.max.freq";

        /**
         * system cpu min frequency regex
         */
        String CPU_FREQ_REGEX = "-?\\d+(\\.\\d+)?";

        /**
         * system os name
         */
        String OS_NAME = "system.os.name";

        /**
         * system os version
         */
        String OS_VERSION = "system.os.version";

        /**
         * system cpu usage
         */
        String CPU_USING = "system.cpu.usage";

        /**
         * migration Host
         */
        String MIGRATION_HOST = "migrationHost";

        /**
         * host network tx | rx
         */
        String NET_MONITOR = "netMonitor";
    }
}
