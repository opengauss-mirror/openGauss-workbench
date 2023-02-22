package com.nctigba.observability.instance.model.param;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum DatabaseParamData {
    MaxProcessMemory("数据库","max_process_memory","设置一个数据库节点可用的最大物理内存",
            "2*1024*1024～INT_MAX","12582912","KB","数据库节点上该数值需要根据系统物理内存及单节点部署主数据库节点个数决定"),
    WorkMem("数据库","work_mem","",
            "","","KB","判断执行作业可下盘算子是否已使用内存量触发下盘点"),
    PagewriterSleep("数据库","pagewriter_sleep","",
            "0～3600000","2000ms","毫秒","设置用于增量检查点打开后，pagewrite线程每隔pagewriter_sleep的时间刷一批脏页下盘"),
    BgwriterDelay("数据库","bgwriter_delay","",
            "10~10000","2s","毫秒","设置后端写进程写“脏”共享缓冲区之间的时间间隔"),
    BgwriterThreadNum("数据库","bgwriter_thread_num","",
            "","","",""),
    MaxIoCapacity("数据库","max_io_capacity","设置后端写进程批量刷页每秒的IO上限",
            "30720~10485760","512000","KB",""),
    LogMinDurationStatement("数据库","log_min_duration_statement","当某条语句的持续时间大于或者等于特定的毫秒数时，log_min_duration_statement参数用于控制记录每条完成语句的持续时间",
            "","30min","毫秒",""),
    LogDuration("数据库","log_duration","控制记录每个已完成SQL语句的执行时间",
            "","on","布尔型",""),
    TrackStmtStatLevel("数据库","track_stmt_stat_level","控制语句执行跟踪的级别",
            "","OFF,L0","字符型",""),
    TrackStmtRetentionTime("数据库","track_stmt_retention_time","组合参数，控制全量/慢SQL记录的保留时间",
            "","3600,604800","字符型",""),
    EnableThreadPool("数据库","enable_thread_pool","控制是否使用线程池功能",
            "","off","布尔型",""),
    ThreadPoolAttr("数据库","thread_pool_attr","用于控制线程池功能的详细属性",
            "","16, 2, (nobind)","字符型",""),
    LogStatement("数据库","log_statement","控制记录SQL语句",
            "","none","枚举类型",""),
    LogErrorVerbosity("数据库","log_error_verbosity","控制服务器日志中每条记录的消息写入的详细度",
            "","default","枚举类型",""),
    LogMinMessages("数据库","log_min_messages","控制写到服务器日志文件中的消息级别",
            "","warning","枚举类型",""),
    LogMinErrorStatement("数据库","log_min_error_statement","控制在服务器日志中记录错误的SQL语句",
            "","error","枚举类型","");
    private String classify;
    private String paramName;
    private String paramDetail;
    private String suggestValue;
    private String defaultValue;
    private String unit;
    private String suggestExplain;

    DatabaseParamData(String classify, String paramName, String paramDetail, String suggestValue, String defaultValue, String unit, String suggestExplain) {
        this.classify=classify;
        this.paramName=paramName;
        this.paramDetail=paramDetail;
        this.suggestValue=suggestValue;
        this.defaultValue=defaultValue;
        this.unit=unit;
        this.suggestExplain=suggestExplain;
    }
}
