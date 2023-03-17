package com.nctigba.observability.instance.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.common.web.exception.CustomException;
import com.nctigba.common.web.result.AppResult;
import com.nctigba.observability.instance.model.monitoring.MonitoringParam;
import com.nctigba.observability.instance.service.MonitoringService;

import lombok.RequiredArgsConstructor;

/**
 * Monitoring data
 *
 * @author zhanggr@vastdata.com.cn
 * @since 2022/9/5 17:04
 */
@RestController
@RequestMapping("/observability/v1/monitoring")
@RequiredArgsConstructor
public class MonitoringController {
    private final MonitoringService monitoringService;
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(100));

    private static final List<String> names = Arrays.asList("cpu", "memory", "disk_read", "disk_written",
            "network_transmit", "network_receive", "load5", "run_time", "mem", "cpu_cnt", "uname", "os");
    private static final List<String> metrics = Arrays.asList(
            "(avg(sum(irate(node_cpu_seconds_total{mode!=\"idle\",instance=\"ogbrench\"}[5m]))by (cpu))) * 100",
            "(1 - (node_memory_MemAvailable_bytes{instance=\"ogbrench\"} / (node_memory_MemTotal_bytes{instance=\"ogbrench\"}))) * 100",
            "max(rate(node_disk_read_bytes_total{instance=\"ogbrench\"}[5m])) by (instance)",
            "max(rate(node_disk_written_bytes_total{instance=\"ogbrench\"}[5m])) by (instance)",
            "max(rate(node_network_transmit_bytes_total{instance=\"ogbrench\"}[5m])*8) by (instance)",
            "max(rate(node_network_receive_bytes_total{instance=\"ogbrench\"}[5m])*8) by (instance)",
            "node_load5{instance=\"ogbrench\"}",
            "sum(time() - node_boot_time_seconds{instance=\"ogbrench\"})by(instance)",
            "node_memory_MemTotal_bytes{instance=\"ogbrench\"}",
            "count(node_cpu_seconds_total{mode='system',instance=\"ogbrench\"}) by (instance)",
            "node_uname_info{instance=\"ogbrench\"}", "node_os_info{instance=\"ogbrench\"}",
            "sum(avg(node_filesystem_size_bytes{fstype=~\"xfs|ext.*\",instance=\"ogbrench\"})by(device,instance))");
    private static final List<String> databaseNames = Arrays.asList("CPU_TIME", "NET_SEND_TIME", "DATA_IO_TIME",
            "gauss_wait_events_value", "totalCoreNum", "total5mLoad", "totalAverageUtilization1", "diskIOUsage", "systemUsage", "userUsage", "totalUsage", "totalMemory", "usedMemory",
            "totalAverageUtilization2", "totalDisks", "totalNumber", "totalAverageUtilization3", "read1", "write1", "read2", "write2", "upload", "download", "TCP_alloc", "CurrEstab",
            "Tcp_OutSegs", "Tcp_InSegs", "UDP_inuse", "TCP_tw", "Tcp_RetransSegs", "Sockets_used", "transactionRollbackNum", "transactionCommitments",
            "transactionAndRollbackTotal", "queryTransactions", "currentIdleConnections", "currentActiveConnections", "currentConnections", "totalConnections", "slowSqlNum", "longTransactions", "sqlResponseTime80", "sqlResponseTime95",
            "accessExclusiveLock", "accessShareLock", "ExclusiveLock", "ShareUpdateExclusiveLock",
            "ShareRowExclusiveLock", "RowShareLock", "RowExclusiveLock", "ShareLock", "queryCacheHitRate", "databaseCacheHitRate", "readPhysicalFileBlockNum",
            "writePhysicalFileBlockNum", "lastBatchDirtyPageNum", "currentRemainingDirtyPages");
    private static final List<String> databaseMetrics = Arrays.asList(
            "increase(gauss_instance_time_value{instance='ogbrench',stat_name='CPU_TIME'}[5m])",
            "increase(gauss_instance_time_value{instance='ogbrench',stat_name='NET_SEND_TIME'}[5m])",
            "increase(gauss_instance_time_value{instance='ogbrench',stat_name='DATA_IO_TIME'}[5m])",
            "gauss_wait_events_value{instance='ogbrench'}",
            "count(node_cpu_seconds_total{mode='system',instance='ogbrench'}) by (instance)",
            "sum(node_load5{instance='ogbrench'})",
            "avg(rate(node_cpu_seconds_total{mode!='idle',instance='ogbrench'}[5m])) by (instance) * 100",
            "(avg(sum(irate(node_cpu_seconds_total{mode='iowait',instance='ogbrench'}[5m]))by (cpu))) * 100",
            "(avg(sum(irate(node_cpu_seconds_total{mode='system',instance='ogbrench'}[5m]))by (cpu))) * 100",
            "(avg(sum(irate(node_cpu_seconds_total{mode='user',instance='ogbrench'}[5m]))by (cpu))) * 100",
            "(avg(sum(irate(node_cpu_seconds_total{mode!='idle',instance='ogbrench'}[5m]))by (cpu))) * 100",
            "node_memory_MemTotal_bytes{instance='ogbrench'}",
            "node_memory_MemTotal_bytes{instance='ogbrench'} - node_memory_MemAvailable_bytes{instance='ogbrench'}",
            "(node_memory_MemTotal_bytes{instance='ogbrench'} - node_memory_MemAvailable_bytes{instance='ogbrench'})/node_memory_MemTotal_bytes{instance='ogbrench'} * 100",
            "sum(avg(node_filesystem_size_bytes{fstype=~'xfs|ext.*',instance='ogbrench'})by(device,instance))",
            "sum(avg(node_filesystem_size_bytes{fstype=~'xfs|ext.*',instance='ogbrench'})by(device,instance)) - sum(avg(node_filesystem_free_bytes{fstype=~'xfs|ext.*',instance='ogbrench'})by(device,instance))",
            "(sum(avg(node_filesystem_size_bytes{fstype=~'xfs|ext.*',instance='ogbrench'})by(device,instance)) - sum(avg(node_filesystem_free_bytes{fstype=~'xfs|ext.*',instance='ogbrench'})by(device,instance))) /(sum(avg(node_filesystem_avail_bytes{fstype=~'xfs|ext.*',instance='ogbrench'})by(device,instance))+(sum(avg(node_filesystem_size_bytes{fstype=~'xfs|ext.*',instance='ogbrench'})by(device,instance)) - sum(avg(node_filesystem_free_bytes{fstype=~'xfs|ext.*',instance='ogbrench'})by(device,instance)))) * 100",
            "sum(rate(node_disk_reads_completed_total{instance='ogbrench'}[5m]))",
            "sum(rate(node_disk_writes_completed_total{instance='ogbrench'}[5m]))",
            "sum(rate(node_disk_read_bytes_total{instance='ogbrench'}[5m]))",
            "sum(rate(node_disk_written_bytes_total{instance='ogbrench'}[5m]))",
            "sum(rate(node_network_transmit_bytes_total{instance='ogbrench'}[5m])*8)",
            "sum(rate(node_network_receive_bytes_total{instance='ogbrench'}[5m])*8)",
            "node_sockstat_TCP_alloc{instance='ogbrench'}", "node_netstat_Tcp_CurrEstab{instance='ogbrench'}",
            "rate(node_netstat_Tcp_OutSegs{instance='ogbrench'}[5m])",
            "rate(node_netstat_Tcp_InSegs{instance='ogbrench'}[5m])", "node_sockstat_UDP_inuse{instance='ogbrench'}",
            "node_sockstat_TCP_tw{instance='ogbrench'}", "rate(node_netstat_Tcp_RetransSegs{instance='ogbrench'}[5m])",
            "node_sockstat_sockets_used{instance='ogbrench'}",
            "sum(irate(pg_stat_database_xact_rollback{instance='ogbrench'}[5m]))",
            "sum(irate(pg_stat_database_xact_commit{instance='ogbrench'}[5m]))",
            "sum(irate(pg_stat_database_xact_rollback{instance='ogbrench'}[5m])) + sum(irate(pg_stat_database_xact_commit{instance='ogbrench'}[5m]))",
            "sum(rate(gauss_workload_sql_count_select_count{instance='ogbrench'}[5m]))",
            "sum(pg_stat_activity_count{instance='ogbrench',state='idle'})",
            "sum(pg_stat_activity_count{instance='ogbrench',state='active'})",
            "sum(pg_stat_activity_count{instance='ogbrench'})", "pg_settings_max_connections{instance='ogbrench'}",
            "sum(pg_stat_activity_max_tx_duration{instance='ogbrench',state!='idle'}) > bool 3",
            "sum(pg_stat_activity_max_tx_duration{instance='ogbrench',state!='idle'}) > bool 30",
            "gauss_statement_responsetime_percentile_p80{instance='ogbrench'}",
            "gauss_statement_responsetime_percentile_p95{instance='ogbrench'}",
            "sum(pg_lock_count{mode='AccessExclusiveLock',instance='ogbrench'})",
            "sum(pg_lock_count{mode='AccessShareLock',instance='ogbrench'})",
            "sum(pg_lock_count{mode='ExclusiveLock',instance='ogbrench'})",
            "sum(pg_lock_count{mode='ShareUpdateExclusiveLock',instance='ogbrench'})",
            "sum(pg_lock_count{mode='ShareRowExclusiveLock',instance='ogbrench'})",
            "sum(pg_lock_count{mode='RowShareLock',instance='ogbrench'})",
            "sum(pg_lock_count{mode='RowExclusiveLock',instance='ogbrench'})",
            "sum(pg_lock_count{mode='ShareLock',instance='ogbrench'})",
            "(gauss_query_statement_cache_hit_rate{instance='ogbrench'}) * 100",
            "(sum(pg_stat_database_blks_hit{instance='ogbrench'}) / (sum(pg_stat_database_blks_hit{instance='ogbrench'}) + sum(pg_stat_database_blks_read{instance='ogbrench'}))) * 100",
            "rate(gauss_summary_file_iostat_total_phyblkrd{instance='ogbrench'}[5m])",
            "rate(gauss_summary_file_iostat_total_phyblkwrt{instance='ogbrench'}[5m])",
            "sum(gauss_global_pagewriter_status_pgwr_last_flush_num{instance='ogbrench'})",
            "sum(gauss_global_pagewriter_status_remain_dirty_page_num{instance='ogbrench'})");

    @PostMapping("/point")
    public AppResult point(@RequestBody MonitoringParam monitoringParam) {
        // The specified time point can only be returned in table data format
        return AppResult.ok("").addData(monitoringService.getPointMonitoringData(monitoringParam));
    }

    @PostMapping("/range")
    public AppResult range(@RequestBody MonitoringParam monitoringParam) {
        return AppResult.ok("").addData(monitoringService.getRangeMonitoringData(monitoringParam));
    }

    @GetMapping("/server-metric")
    public AppResult pointMetrics(MonitoringParam monitoringParam) {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (String metric : metrics) {
            if (i > 6) {
                break;
            }
            handlePoint(map, monitoringParam, metric, i++);
        }
        return AppResult.ok("").addData(map);
    }

    @GetMapping("/server-info")
    public AppResult pointInfo(MonitoringParam monitoringParam) {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (String metric : metrics) {
            if (i < 7) {
                i++;
                continue;
            }
            handlePoint(map, monitoringParam, metric, i++);
        }
        return AppResult.ok("").addData(map);
    }

    private void handlePoint(Map<String, Object> map, MonitoringParam monitoringParam, String metric, int i) {
        monitoringParam.setQuery(metric.replace("ogbrench", monitoringParam.getId()));
        Map<String, Object> pointMonitoringData = monitoringService.getPointMonitoringData(monitoringParam);
        if (pointMonitoringData.containsKey("value")) {
            map.put(names.get(i), pointMonitoringData.get("value"));
        } else {
            map.putAll(pointMonitoringData);
        }
    }

    public interface MyRunnable extends Runnable {
        MyRunnable setName(String name);
    }

    @GetMapping("/database-metrics")
    public AppResult getDatabaseMetrics(MonitoringParam monitoringParam) {
        Map<String, Object> map = new HashMap<>();
        int i = 0;
        CountDownLatch countDownLatch = new CountDownLatch(databaseMetrics.size());
        for (String metric : databaseMetrics) {
            pool.submit(new MyRunnable() {
                String name;

                @Override
                public MyRunnable setName(String name) {
                    this.name = name;
                    return this;
                }

                @Override
                public void run() {
                    try {
                        monitoringParam.setQuery(metric.replace("ogbrench", monitoringParam.getId()));
                        if ("gauss_wait_events_value".equals(name)) {
                            monitoringParam.setLegendName("event");
                        }
                        map.put(name, monitoringService.getRangeMonitoringData(monitoringParam).get(0));
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            }.setName(databaseNames.get(i++)));
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new CustomException(e.getMessage());
        }
        return AppResult.ok("").addData(map);
    }
}