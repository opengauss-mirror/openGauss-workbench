/*
 * Copyright (c) 2025 Huawei Technologies Co.,Ltd.
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

package org.opengauss.admin.plugin.constants;

/**
 * TranscribeReplayConstants
 *
 * @since 2025-02-10
 */

public interface TranscribeReplayConstants {
    /**
     * TARGET_DB
     */
    String TARGET_DB = "openGauss";

    /**
     * TRANSCRIBE
     */
    String TRANSCRIBE = "transcribe";

    /**
     * REPLAY
     */
    String REPLAY = "replay";

    /**
     * TRANSCRIBE_REPLAY
     */
    String TRANSCRIBE_REPLAY = "transcribe_replay";

    /**
     * TOOL_PATH
     */
    String TOOL_PATH = "transcribe-replay-tool";

    /**
     * JAR_NAME
     */
    String JAR_NAME = "/transcribe-replay-tool-%s.jar";

    /**
     * FAIL_SQL_FILE_NAME
     */
    String FAIL_SQL_FILE_NAME = "/fail_sql_%s.json";

    /**
     * FAIL_FILE_SIZE
     */
    int FAIL_FILE_SIZE = 100;

    /**
     * TOTAL_NUM
     */
    String TOTAL_NUM = "Total Number: (\\d+)";

    /**
     * SLOW_SQL_NUM
     */
    String SLOW_SQL_NUM = "Slow Sql Number: (\\d+)";

    /**
     * FAIL_SQL_NUM
     */
    String FAIL_SQL_NUM = "Fail Number: (\\d+)";

    /**
     * PARSE_FILE
     */
    String PARSE_RESULT_FILE = "parse_result.log";

    /**
     * REPLAY_FILE
     */
    String REPLAY_RESULT_FILE = "replay_result.log";

    /**
     * PARSE_FILE
     */
    String PARSE_PROCESS_FILE = "parse-process.txt";

    /**
     * REPLAY_FILE
     */
    String REPLAY_PROCESS_FILE = "replay-process.txt";

    /**
     * DURATION_FILE
     */
    String DURATION_FILE = "duration.json";

    /**
     * SQL_TRANSCRIBE_MODE
     */
    String SQL_TRANSCRIBE_MODE = "sql.transcribe.mode";

    /**
     * PCAP_FILE_PATH
     */
    String PCAP_FILE_PATH = "tcpdump-files";

    /**
     * JSON_PATH
     */
    String JSON_PATH = "sql-files";

    /**
     * TCPDUMP_NETWORK_INTERFACE
     */
    String TCPDUMP_NETWORK_INTERFACE = "tcpdump.network.interface";

    /**
     * TCPDUMP_CAPTURE_DURATION
     */
    String TCPDUMP_CAPTURE_DURATION = "tcpdump.capture.duration";

    /**
     * TCPDUMP_FILE_NAME
     */
    String TCPDUMP_FILE_NAME = "tcpdump.file.name";

    /**
     * TCPDUMP_FILE_SIZE
     */
    String TCPDUMP_FILE_SIZE = "tcpdump.file.size";

    /**
     * TCPDUMP_FILE_PATH
     */
    String TCPDUMP_FILE_PATH = "tcpdump.file.path";

    /**
     * FILE_COUNT_LIMIT
     */
    String FILE_COUNT_LIMIT = "file.count.limit";

    /**
     * TCPDUMP_PLUGIN_PATH
     */
    String TCPDUMP_PLUGIN_PATH = "tcpdump.plugin.path";

    /**
     * TCPDUMP_DATABASE_PORT
     */
    String TCPDUMP_DATABASE_PORT = "tcpdump.database.port";

    /**
     * TCPDUMP_FILE_ID
     */
    String TCPDUMP_FILE_ID = "tcpdump.file.id";

    /**
     * ATTACH_PROCESS_PID
     */
    String ATTACH_PROCESS_PID = "attach.process.pid";

    /**
     * ATTACH_TARGET_SCHEMA
     */
    String ATTACH_TARGET_SCHEMA = "attach.target.schema";

    /**
     * ATTACH_CAPTURE_DURATION
     */
    String ATTACH_CAPTURE_DURATION = "attach.capture.duration";

    /**
     * SQL_FILE_NAME
     */
    String SQL_FILE_NAME = "sql.file.name";

    /**
     * SQL_FILE_SIZE
     */
    String SQL_FILE_SIZE = "sql.file.size";

    /**
     * ATTACH_PLUGIN_PATH
     */
    String ATTACH_PLUGIN_PATH = "attach.plugin.path";

    /**
     * SQL_FILE_PATH
     */
    String SQL_FILE_PATH = "sql.file.path";

    /**
     * SHOULD_CHECK_SYSTEM
     */
    String SHOULD_CHECK_SYSTEM = "should.check.system";

    /**
     * MAX_CPU_THRESHOLD
     */
    String MAX_CPU_THRESHOLD = "max.cpu.threshold";

    /**
     * MAX_MEMORY_THRESHOLD
     */
    String MAX_MEMORY_THRESHOLD = "max.memory.threshold";

    /**
     * MAX_DISK_THRESHOLD
     */
    String MAX_DISK_THRESHOLD = "max.disk.threshold";

    /**
     * SHOULD_SEND_FILE
     */
    String SHOULD_SEND_FILE = "should.send.file";

    /**
     * REMOTE_RETRY_COUNT
     */
    String REMOTE_RETRY_COUNT = "remote.retry.count";

    /**
     * REMOTE_RECEIVER_PASSWORD
     */
    String REMOTE_RECEIVER_PASSWORD = "remote.receiver.password";

    /**
     * REMOTE_FILE_PATH
     */
    String REMOTE_FILE_PATH = "remote.file.path";

    /**
     * REMOTE_RECEIVER_NAME
     */
    String REMOTE_RECEIVER_NAME = "remote.receiver.name";

    /**
     * REMOTE_NODE_IP
     */
    String REMOTE_NODE_IP = "remote.node.ip";

    /**
     * REMOTE_NODE_PORT
     */
    String REMOTE_NODE_PORT = "remote.node.port";

    /**
     * GENERAL_DATABASE_USERNAME
     */
    String GENERAL_DATABASE_USERNAME = "general.database.username";

    /**
     * GENERAL_DATABASE_PASSWORD
     */
    String GENERAL_DATABASE_PASSWORD = "general.database.password";

    /**
     * GENERAL_SQL_BATCH
     */
    String GENERAL_SQL_BATCH = "general.sql.batch";

    /**
     * GENERAL_START_TIME
     */
    String GENERAL_START_TIME = "general.start.time";

    /**
     * SQL_TABLE_DROP
     */
    String SQL_TABLE_DROP = "sql.table.drop";

    /**
     * GENERAL_DATABASE_IP
     */
    String GENERAL_DATABASE_IP = "general.database.ip";

    /**
     * GENERAL_DATABASE_PORT
     */
    String GENERAL_DATABASE_PORT = "general.database.port";

    /**
     * SQL_DATABASE_IP
     */
    String SQL_DATABASE_IP = "sql.database.ip";

    /**
     * SQL_DATABASE_PORT
     */
    String SQL_DATABASE_PORT = "sql.database.port";

    /**
     * SQL_DATABASE_USERNAME
     */
    String SQL_DATABASE_USERNAME = "sql.database.username";

    /**
     * SQL_DATABASE_NAME
     */
    String SQL_DATABASE_NAME = "sql.database.name";

    /**
     * SQL_DATABASE_PASSWORD
     */
    String SQL_DATABASE_PASSWORD = "sql.database.password";

    /**
     * SQL_TABLE_NAME
     */
    String SQL_TABLE_NAME = "sql.table.name";

    /**
     * PARSE_SELECT_RESULT
     */
    String PARSE_SELECT_RESULT = "parse.select.result";

    /**
     * SELECT_RESULT_PATH
     */
    String SELECT_RESULT_PATH = "select.result.path";

    /**
     * RESULT_FILE_NAME
     */
    String RESULT_FILE_NAME = "result.file.name";

    /**
     * RESULT_FILE_SIZE
     */
    String RESULT_FILE_SIZE = "result.file.size";

    /**
     * SQL_STORAGE_MODE
     */
    String SQL_STORAGE_MODE = "sql.storage.mode";

    /**
     * TCPDUMP_DATABASE_TYPE
     */
    String TCPDUMP_DATABASE_TYPE = "tcpdump.database.type";

    /**
     * QUEUE_SIZE_LIMIT
     */
    String QUEUE_SIZE_LIMIT = "queue.size.limit";

    /**
     * PACKET_BATCH_SIZE
     */
    String PACKET_BATCH_SIZE = "packet.batch.size";

    /**
     * TCPDUMP_DATABASE_IP
     */
    String TCPDUMP_DATABASE_IP = "tcpdump.database.ip";

    /**
     * SQL_REPLAY_MULTIPLE
     */
    String SQL_REPLAY_MULTIPLE = "sql.replay.multiple";

    /**
     * SQL_REPLAY_ONLY_QUERY
     */
    String SQL_REPLAY_ONLY_QUERY = "sql.replay.only.query";

    /**
     * SQL_REPLAY_STRATEGY
     */
    String SQL_REPLAY_STRATEGY = "sql.replay.strategy";

    /**
     * SQL_REPLAY_PARALLEL_MAX_POOL_SIZE
     */
    String SQL_REPLAY_PARALLEL_MAX_POOL_SIZE = "sql.replay.parallel.max.pool.size";

    /**
     * REPLAY_MAX_TIME
     */
    String REPLAY_MAX_TIME = "replay.max.time";

    /**
     * SQL_REPLAY_SLOW_SQL_RULE
     */
    String SQL_REPLAY_SLOW_SQL_RULE = "sql.replay.slow.sql.rule";

    /**
     * SQL_REPLAY_SLOW_TIME_DIFFERENCE_THRESHOLD
     */
    String SQL_REPLAY_SLOW_TIME_DIFFERENCE_THRESHOLD = "sql.replay.slow.time.difference.threshold";

    /**
     * SQL_REPLAY_SLOW_SQL_DURATION_THRESHOLD
     */
    String SQL_REPLAY_SLOW_SQL_DURATION_THRESHOLD = "sql.replay.slow.sql.duration.threshold";

    /**
     * SQL_REPLAY_SLOW_TOP_NUMBER
     */
    String SQL_REPLAY_SLOW_TOP_NUMBER = "sql.replay.slow.top.number";

    /**
     * SQL_REPLAY_DRAW_INTERVAL
     */
    String SQL_REPLAY_DRAW_INTERVAL = "sql.replay.draw.interval";

    /**
     * SQL_REPLAY_SESSION_WHITE_LIST
     */
    String SQL_REPLAY_SESSION_WHITE_LIST = "sql.replay.session.white.list";

    /**
     * SQL_REPLAY_SESSION_BLACK_LIST
     */
    String SQL_REPLAY_SESSION_BLACK_LIST = "sql.replay.session.black.list";

    /**
     * SQL_REPLAY_DATABASE_USERNAME
     */
    String SQL_REPLAY_DATABASE_USERNAME = "sql.replay.database.username";

    /**
     * SQL_REPLAY_DATABASE_PASSWORD
     */
    String SQL_REPLAY_DATABASE_PASSWORD = "sql.replay.database.password";

    /**
     * SQL_REPLAY_SLOW_SQL_CSV_DIR
     */
    String SQL_REPLAY_SLOW_SQL_CSV_DIR = "sql.replay.slow.sql.csv.dir";

    /**
     * SQL_REPLAY_DATABASE_IP
     */
    String SQL_REPLAY_DATABASE_IP = "sql.replay.database.ip";

    /**
     * SQL_REPLAY_DATABASE_PORT
     */
    String SQL_REPLAY_DATABASE_PORT = "sql.replay.database.port";

    /**
     * SQL_REPLAY_DATABASE_SCHEMA_MAP
     */
    String SQL_REPLAY_DATABASE_SCHEMA_MAP = "sql.replay.database.schema.map";
}
