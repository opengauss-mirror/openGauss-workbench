package com.nctigba.observability.sql.model.param;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum DatabaseParamData {
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
    LogMinErrorStatement("log_min_error_statement");
    private String paramName;

    DatabaseParamData(String paramName) {
        this.paramName=paramName;
    }
}
