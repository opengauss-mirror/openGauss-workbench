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
     * SKIP_NUM
     */
    String SKIP_NUM = "Skip Number: (\\d+)";

    /**
     * PROCESS_FILE
     */
    String PROCESS_FILE = "process.json";

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
}
