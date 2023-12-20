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
 *  DatabaseParamEnum.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/enums/DatabaseParamEnum.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.enums;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum DatabaseParamEnum {
    MaxProcessMemory("max_process_memory"),
    WorkMem("work_mem"),
    PagewriterSleep("pagewriter_sleep"),
    BgwriterDelay("bgwriter_delay"),
    BgwriterThreadNum("pagewriter_thread_num"),
    MaxIoCapacity("max_io_capacity"),
    LogMinDurationStatement("log_min_duration_statement"),
    LogDuration("log_duration"),
    TrackStmtStatLevel("track_stmt_stat_level"),
    TrackStmtRetentionTime("track_stmt_retention_time"),
    EnableThreadPool("enable_thread_pool"),
    ThreadPoolAttr("thread_pool_attr"),
    LogStatement("log_statement"),
    LogErrorVerbosity("log_error_verbosity"),
    LogMinMessages("log_min_messages"),
    LogMinErrorStatement("log_min_error_statement"),
    AUTOVACUUM_MODE("autovacuum_mode");
    private String paramName;

    DatabaseParamEnum(String paramName) {
        this.paramName=paramName;
    }
}
