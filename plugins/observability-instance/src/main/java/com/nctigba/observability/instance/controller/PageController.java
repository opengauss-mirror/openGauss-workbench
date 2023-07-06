/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.instance.constants.MetricsLine;
import com.nctigba.observability.instance.constants.MetricsValue;
import com.nctigba.observability.instance.mapper.DbConfigMapper;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.util.Language;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/instanceMonitoring/api/v1/")
@RequiredArgsConstructor
public class PageController extends ControllerConfig {
    private static final Enum<?>[] MEMORY = {
            MetricsLine.MEMORY_USED,
            MetricsLine.MEMORY_DB_USED,
            MetricsValue.MEM_TOTAL,
            MetricsValue.MEM_USED,
            MetricsValue.MEM_FREE,
            MetricsValue.MEM_CACHE,
            MetricsValue.SWAP_TOTAL,
            MetricsValue.SWAP_USED,
            MetricsValue.SWAP_FREE,
            MetricsLine.MEMORY_SWAP
    };
    private static final MetricsLine[] IO = {
            MetricsLine.IOPS_R,
            MetricsLine.IOPS_W,
            MetricsLine.IO_DISK_READ_BYTES_PER_SECOND,
            MetricsLine.IO_DISK_WRITE_BYTES_PER_SECOND,
            MetricsLine.IO_QUEUE_LENGTH,
            MetricsLine.IO_UTIL,
            MetricsLine.IO_AVG_REPONSE_TIME_READ,
            MetricsLine.IO_AVG_REPONSE_TIME_WRITE,
            MetricsLine.IO_AVG_REPONSE_TIME_RW
    };

    private static final MetricsValue[] IO_TABLE = {
            MetricsValue.IO_TPS,
            MetricsValue.IO_RD,
            MetricsValue.IO_WT,
            MetricsValue.IO_AVGRQ_SZ,
            MetricsValue.IO_AVGQU_SZ,
            MetricsValue.IO_AWAIT,
            MetricsValue.IO_UTIL
    };
    private static final MetricsLine[] NETWORK = {
            MetricsLine.NETWORK_IN,
            MetricsLine.NETWORK_OUT,
            MetricsLine.NETWORK_LOST_PACKAGE,
            MetricsLine.NETWORK_TCP_ALLOC,
            MetricsLine.NETWORK_CURRESTAB,
            MetricsLine.NETWORK_TCP_INSEGS,
            MetricsLine.NETWORK_TCP_OUTSEGS,
            MetricsLine.NETWORK_TCP_SOCKET,
            MetricsLine.NETWORK_UDP_SOCKET
    };

    private static final MetricsValue[] NETWORK_TABLE = {
            MetricsValue.NETWORK_RXPCK,
            MetricsValue.NETWORK_TXPCK,
            MetricsValue.NETWORK_RX,
            MetricsValue.NETWORK_TX,
            MetricsValue.NETWORK_RXERR,
            MetricsValue.NETWORK_TXERR,
            MetricsValue.NETWORK_RXDROP,
            MetricsValue.NETWORK_TXDROP,
            MetricsValue.NETWORK_RXFIFO,
            MetricsValue.NETWORK_TXFIFO
    };
    private static final MetricsLine[] INSTANCE = {
            MetricsLine.INSTANCE_TPS_COMMIT,
            MetricsLine.INSTANCE_TPS_ROLLBACK,
            MetricsLine.INSTANCE_TPS_CR,
            MetricsLine.INSTANCE_QPS,
            MetricsLine.INSTANCE_DB_CONNECTION_ACTIVE,
            MetricsLine.INSTANCE_DB_CONNECTION_IDLE,
            MetricsLine.INSTANCE_DB_CONNECTION_CURR,
            MetricsLine.INSTANCE_DB_CONNECTION_TOTAL,
            MetricsLine.INSTANCE_DB_SLOWSQL
    };
    private static final MetricsLine[] WAIT_EVENT = {
            MetricsLine.WAIT_EVENT_COUNT
    };

    private final MetricsService metricsService;
    private final DbConfigMapper dbConfigMapper;
    private final ClusterManager clusterManager;
    private final MessageSource messageSource;

    @GetMapping("memory")
    public Map<String, Object> memory(String id, Long start, Long end, Integer step) {
        HashMap<String, Object> batch = metricsService.listBatch(MEMORY, id, start, end, step);
        try {
            clusterManager.setCurrentDatasource(id, null);
            // memory node detail
            List<Map<String, Object>> memoryNodeDetail = dbConfigMapper.memoryNodeDetail();
            memoryNodeDetail.forEach(map -> {
                var str = map.get("memorytype").toString();
                map.put("desc", messageSource.getMessage("memory.node." + str, null, str, Language.getLocale()));
            });
            batch.put("memoryNodeDetail", memoryNodeDetail);
            // memory config detail
            List<Map<String, Object>> memoryConfig = dbConfigMapper.memoryConfig();
            memoryConfig.forEach(map -> {
                var str = map.get("name").toString();
                map.put("desc", messageSource.getMessage("memory.config." + str, null, str, Language.getLocale()));
            });
            batch.put("memoryConfig", memoryConfig);
        } finally {
            clusterManager.pool();
        }
        return AjaxResult.success(batch);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("io")
    public Map<String, Object> io(String id, Long start, Long end, Integer step) {
        HashMap<String, Object> io = metricsService.listBatch(IO, id, start, end, step);
        HashMap<String, Object> table = metricsService.listBatch(IO_TABLE, id, start, end, step);
        HashMap<String, Object> lines = new HashMap<>();
        for (MetricsValue metric : IO_TABLE) {
            var map = (HashMap<String, Object>) table.get(metric.name());
            if (map == null) {
                continue;
            }
            map.forEach((k, v) -> {
                if (!lines.containsKey(k)) {
                    HashMap<String, Object> line = new HashMap<>();
                    line.put("device", k);
                    lines.put(k, line);
                }
                ((HashMap<String, Object>) lines.get(k)).put(metric.name(), v);
            });
        }
        io.put("table", lines.values().stream().collect(Collectors.toList()));
        return AjaxResult.success(io);
    }

    @SuppressWarnings("unchecked")
    @GetMapping("network")
    public Map<String, Object> network(String id, Long start, Long end, Integer step) {
        HashMap<String, Object> network = metricsService.listBatch(NETWORK, id, start, end, step);
        HashMap<String, Object> table = metricsService.listBatch(NETWORK_TABLE, id, start, end, step);
        HashMap<String, Object> lines = new HashMap<>();
        for (MetricsValue metric : NETWORK_TABLE) {
            var map = (HashMap<String, Object>) table.get(metric.name());
            if (map == null) {
                continue;
            }
            map.forEach((k, v) -> {
                if (!lines.containsKey(k)) {
                    HashMap<String, Object> line = new HashMap<>();
                    line.put("device", k);
                    lines.put(k, line);
                }
                ((HashMap<String, Object>) lines.get(k)).put(metric.name(), v);
            });
        }
        network.put("table", lines.values().stream().collect(Collectors.toList()));
        return AjaxResult.success(network);
    }

    @GetMapping("instance")
    public Map<String, Object> instance(String id, Long start, Long end, Integer step) {
        return AjaxResult.success(metricsService.listBatch(INSTANCE, id, start, end, step));
    }

    @GetMapping("wait_event")
    public Map<String, Object> waitEvent(String id, Long start, Long end, Integer step) {
        return AjaxResult.success(metricsService.listBatch(WAIT_EVENT, id, start, end, step));
    }

}