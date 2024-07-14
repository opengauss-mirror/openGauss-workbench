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
 *  PageController.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/controller/PageController.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nctigba.observability.instance.service.DbConfigService;
import com.nctigba.observability.instance.service.TopSQLService;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nctigba.observability.instance.enums.MetricsLine;
import com.nctigba.observability.instance.enums.MetricsValue;
import com.nctigba.observability.instance.mapper.DbConfigMapper;
import com.nctigba.observability.instance.service.MetricsService;
import com.nctigba.observability.instance.util.LanguageUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/instanceMonitoring/api/v1/")
@RequiredArgsConstructor
public class PageController extends ControllerConfig {
    @Autowired
    private TopSQLService topSQLService;
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
            MetricsLine.IO_AVG_REPONSE_TIME_RW,
            MetricsLine.IO_DISK_USAGE,
            MetricsLine.IO_DISK_INODE_USAGE,
            MetricsLine.IO_DISK_DB_VOLUME_DATA,
            MetricsLine.IO_DISK_DB_VOLUME_XLOG,
            MetricsLine.IO_DISK_DB_USAGE_DATA,
            MetricsLine.IO_DISK_DB_USAGE_XLOG,
            MetricsLine.IO_DISK_DB_VOLUME_TOTAL,
            MetricsLine.IO_DISK_XLOG_VOLUME_TOTAL,
            MetricsLine.IO_DISK_DB_VOLUME_USED,
            MetricsLine.IO_DISK_DB_VOLUME_FREE,
            MetricsLine.IO_DISK_DB_USAGE_TOTAL,
            MetricsLine.IO_DISK_XLOG_USAGE_TOTAL,
            MetricsLine.IO_DISK_DB_USAGE_USED,
            MetricsLine.IO_DISK_DB_USAGE_FREE,
            MetricsLine.IO_DISK_XLOG_VOLUME_USED,
            MetricsLine.IO_DISK_XLOG_USAGE_USED,
            MetricsLine.IO_DISK_DB_VOLUME_OTHER,
            MetricsLine.IO_DISK_DB_USAGE_OTHER,
            MetricsLine.IO_DISK_XLOG_VOLUME_OTHER,
            MetricsLine.IO_DISK_XLOG_USAGE_OTHER,
            MetricsLine.IO_DISK_DB_VOLUME_OTHER_TWO,
            MetricsLine.IO_DISK_DB_USAGE_OTHER_TWO,
            MetricsLine.IO_DISK_XLOG_VOLUME_OTHER_TWO,
            MetricsLine.IO_DISK_XLOG_USAGE_OTHER_TWO
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
            MetricsLine.NETWORK_UDP_SOCKET,
            MetricsLine.NETWORK_LOST_PACKAGE_IN,
            MetricsLine.NETWORK_LOST_PACKAGE_OUT,
            MetricsLine.NETWORK_ERR_PACKAGE_IN,
            MetricsLine.NETWORK_ERR_PACKAGE_OUT
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
        MetricsLine.INSTANCE_DB_CONNECTION_ACTIVE,
        MetricsLine.INSTANCE_DB_CONNECTION_IDLE,
        MetricsLine.INSTANCE_DB_CONNECTION_CURR,
        MetricsLine.INSTANCE_DB_CONNECTION_TOTAL,
        MetricsLine.INSTANCE_DB_SLOWSQL,
        MetricsLine.INSTANCE_DB_RESPONSETIME_P80,
        MetricsLine.INSTANCE_DB_RESPONSETIME_P95,
        MetricsLine.INSTANCE_DB_DATABASE_BLK
    };

    private static final MetricsLine[] INSTANCE_OVERLOAD = {
        MetricsLine.INSTANCE_TPS_COMMIT,
        MetricsLine.INSTANCE_TPS_ROLLBACK,
        MetricsLine.INSTANCE_TPS_CR,
        MetricsLine.INSTANCE_QPS,
        MetricsLine.INSTANCE_DB_DATABASE_INS,
        MetricsLine.INSTANCE_DB_DATABASE_UPD,
        MetricsLine.INSTANCE_DB_DATABASE_DEL,
        MetricsLine.INSTANCE_DB_DATABASE_RETURN,
        MetricsLine.INSTANCE_DB_DATABASE_FECTH,
        MetricsLine.INSTANCE_DB_BGWRITER_CHECKPOINT,
        MetricsLine.INSTANCE_DB_BGWRITER_CLEAN,
        MetricsLine.INSTANCE_DB_BGWRITER_BACKEND
    };
    private static final MetricsLine[] INSTANCE_TABLESPACE = {
        MetricsLine.PG_TABLESPACE_SIZE
    };
    private static final MetricsLine[] WAIT_EVENT = {
            MetricsLine.WAIT_EVENT_COUNT
    };

    private final MetricsService metricsService;
    private final DbConfigMapper dbConfigMapper;
    private final MessageSource messageSource;
    private final LanguageUtils language;
    private final DbConfigService dbConfigService;

    /**
     * get memory index data
     *
     * @param id nodeId
     * @param start start time
     * @param end end time
     * @param step step
     * @return Map<String, Object>
     */
    @GetMapping("memory")
    public Map<String, Object> memory(String id, Long start, Long end, Integer step) {
        Map<String, Object> batch = metricsService.listBatch(MEMORY, id, start, end, step);
        batch.putAll(dbConfigService.getMemoryDetail(id));
        // db memory
        if (batch.get(MetricsValue.MEM_TOTAL.name()) == null) {
            return AjaxResult.success(batch);
        }
        Long total = Long.parseLong(batch.get(MetricsValue.MEM_TOTAL.name()).toString());
        var percents = batch.get(MetricsLine.MEMORY_DB_USED.name());
        if (percents instanceof List) {
            var listPercents = (List<?>) percents;
            var percent = listPercents.get(listPercents.size() - 1);
            if (percent instanceof Number) {
                batch.put("MEMORY_DB_USED_CURR", total * ((Number) percent).doubleValue() / 100);
            }
        }
        return AjaxResult.success(batch);
    }

    /**
     * get IO index data
     *
     * @param id nodeId
     * @param start start time
     * @param end end time
     * @param step step
     * @return Map<String, Object>
     */
    @SuppressWarnings("unchecked")
    @GetMapping("io")
    public Map<String, Object> io(String id, Long start, Long end, Integer step) {
        Map<String, Object> io = metricsService.listBatch(IO, id, start, end, step);
        Map<String, Object> table = metricsService.listBatch(IO_TABLE, id, start, end, step);
        Map<String, Object> lines = new HashMap<>();
        for (MetricsValue metric : IO_TABLE) {
            var map = (Map<String, Object>) table.get(metric.name());
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

    /**
     * get network index data
     *
     * @param id nodeId
     * @param start start time
     * @param end end time
     * @param step step
     * @return Map<String, Object>
     */
    @SuppressWarnings("unchecked")
    @GetMapping("network")
    public Map<String, Object> network(String id, Long start, Long end, Integer step) {
        Map<String, Object> network = metricsService.listBatch(NETWORK, id, start, end, step);
        Map<String, Object> table = metricsService.listBatch(NETWORK_TABLE, id, start, end, step);
        Map<String, Object> lines = new HashMap<>();
        for (MetricsValue metric : NETWORK_TABLE) {
            var map = (Map<String, Object>) table.get(metric.name());
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

    /**
     * get instance data
     *
     * @param id nodeId
     * @param start start time
     * @param end end time
     * @param step step
     * @return Map<String, Object>
     */
    @GetMapping("instance")
    public Map<String, Object> instance(String id, Long start, Long end, Integer step) {
        Map<String, Object> result = metricsService.listBatch(INSTANCE, id, start, end, step);
        result.put("slowSqlThreshold", topSQLService.getSlowSqlThreshold(id));
        List<Map<String, Object>> cacheHitList = dbConfigService.cacheHit(id);
        result.put("cacheHit", cacheHitList);
        return AjaxResult.success(result);
    }

    /**
     * get instance overload data
     *
     * @param id nodeId
     * @param start start time
     * @param end end time
     * @param step step
     * @return Map<String, Object>
     */
    @GetMapping("instanceOverload")
    public Map<String, Object> instanceInfo(String id, Long start, Long end, Integer step) {
        return AjaxResult.success(metricsService.listBatch(INSTANCE_OVERLOAD, id, start, end, step));
    }

    /**
     * get instance tablespace data
     *
     * @param id nodeId
     * @param start start time
     * @param end end time
     * @param step step
     * @return Map<String, Object>
     */
    @GetMapping("instanceTablespace")
    public Map<String, Object> instanceTablespace(String id, Long start, Long end, Integer step) {
        Map<String, Object> result = metricsService.listBatch(INSTANCE_TABLESPACE, id, start, end, step);
        Map<String, Object> tablespaceData = dbConfigService.tablespaceData(id);
        result.putAll(tablespaceData);
        return AjaxResult.success(result);
    }

    /**
     * get wait event data
     *
     * @param id nodeId
     * @param start start time
     * @param end end time
     * @param step step
     * @return Map<String, Object>
     */
    @GetMapping("wait_event")
    public Map<String, Object> waitEvent(String id, Long start, Long end, Integer step) {
        return AjaxResult.success(metricsService.listBatch(WAIT_EVENT, id, start, end, step));
    }

}