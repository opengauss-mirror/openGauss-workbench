/*
 *  Copyright (c) Huawei Technologies Co. 2025-2025.
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
 *  HisSlowsqlServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/mapper/HisSlowsqlServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.observability.sql.mapper.DynamicHisSlowSqlMapper;
import com.nctigba.observability.sql.mapper.OpengaussAllSlowsqlMapper;
import com.nctigba.observability.sql.mapper.TimePointMapper;
import com.nctigba.observability.sql.model.dto.BasePageDTO;
import com.nctigba.observability.sql.model.dto.SlowSqlDTO;
import com.nctigba.observability.sql.model.entity.HisSlowsqlInfoDO;
import com.nctigba.observability.sql.model.entity.TimePointDO;
import com.nctigba.observability.sql.model.query.SlowLogQuery;
import com.nctigba.observability.sql.model.vo.StatementHistoryAggVO;
import com.nctigba.observability.sql.model.vo.StatementHistoryDetailVO;
import com.nctigba.observability.sql.service.MyPage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * HisSlowqlServiceImpl
 *
 * @author jianghongbo
 * @since 2025-06-30
 */
@Slf4j
@Service
public class HisSlowsqlServiceImpl extends ServiceImpl<DynamicHisSlowSqlMapper, HisSlowsqlInfoDO> {
    private static final String TABLE_PREFIX = "tb_slowsqls_";
    private static final Integer TIME_OFFSET = 60;

    @Autowired
    private OpengaussAllSlowsqlMapper opengaussAllSlowsqlMapper;
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private DynamicHisSlowSqlMapper dynamicHisSlowSqlMapper;
    @Autowired
    private TimePointMapper timePointMapper;
    @Autowired
    private TimeConfigServiceImpl timeConfigService;
    private Map<SlowLogQuery, cacheAble<Future<BasePageDTO<StatementHistoryDetailVO>>>> cache
            = new WeakHashMap<>();

    @Data
    static class cacheAble<T> {
        long curr = System.currentTimeMillis();
        T obj;

        public cacheAble(T obj) {
            this.obj = obj;
        }
    }

    /**
     * collect and clean slow sqls
     *
     * @param node OpsClusterNodeVO database node
     */
    public void cleanAndCollectSlowsqls(OpsClusterNodeVO node) {
        collectNodeSlowsqls(node.getNodeId());
        cleanExpiredSlowInfo(node.getNodeId());
    }

    /**
     * clean expired records
     *
     * @param nodeId String
     */
    public void cleanExpiredSlowInfo(String nodeId) {
        int peroid = timeConfigService.getPeroid();
        String tableName = getTableNameByNodeId(nodeId);
        int expiredNumber = dynamicHisSlowSqlMapper.cleanExpiredData(tableName, peroid);
        log.info("clean {} expired records for table {}", expiredNumber, tableName);
    }

    /**
     * collect slow sql infos
     *
     * @param nodeId String
     */
    public void collectNodeSlowsqls(String nodeId) {
        String tableName = getTableNameByNodeId(nodeId);
        TimePointDO timePointDO = timePointMapper.selectTimePoint(tableName);
        Date startPoint;
        Date finishPoint;
        if (timePointDO == null) {
            startPoint = new Date(0);
            finishPoint = new Date(0);
        } else {
            startPoint = getOneMinuteAgo(timePointDO.getStartTimePoint());
            finishPoint = getOneMinuteAgo(timePointDO.getFinishTimePoint());
        }
        log.debug("timeShot is {}, {} for node {}", startPoint, finishPoint, nodeId);
        List<HisSlowsqlInfoDO> statementRows = new ArrayList<>();
        statementRows = queryNodeSlowsqls(nodeId, startPoint, finishPoint);
        dynamicHisSlowSqlMapper.createTable(tableName);
        AtomicReference<Date> maxStartTime = new AtomicReference<>();
        AtomicReference<Date> maxFinishTime = new AtomicReference<>();
        log.info("collect {} records for node {}", statementRows.size(), nodeId);
        statementRows.forEach(row -> {
            Date startTimeRecord = row.getStartTime();
            Date finishTimeRecord = row.getFinishTime();
            if (maxStartTime.get() == null || startTimeRecord.after(maxStartTime.get())) {
                maxStartTime.set(startTimeRecord);
            }
            if (maxFinishTime.get() == null || finishTimeRecord.after(maxFinishTime.get())) {
                maxFinishTime.set(finishTimeRecord);
            }
            dynamicHisSlowSqlMapper.insert(tableName, row);
        });
        if (statementRows.size() > 0) {
            timePointMapper.insertTimePoint(tableName, maxStartTime.get().getTime(), maxFinishTime.get().getTime());
        }
    }

    private List<HisSlowsqlInfoDO> queryNodeSlowsqls(String nodeId, Date startPoint, Date finishPoint) {
        List<HisSlowsqlInfoDO> statementRows = new ArrayList<>();
        try {
            clusterManager.setCurrentDatasource(nodeId, "postgres");
            statementRows = opengaussAllSlowsqlMapper.selectSlowSqls(startPoint, finishPoint);
        } catch (BadSqlGrammarException e) {
            statementRows = queryNodeSlowsqlsOldVersion(startPoint, finishPoint);
        } finally {
            clusterManager.pool();
        }
        return statementRows;
    }

    private List<HisSlowsqlInfoDO> queryNodeSlowsqlsOldVersion(Date startPoint, Date finishPoint) {
        List<HisSlowsqlInfoDO> statementRows = new ArrayList<>();
        String nodeRole = opengaussAllSlowsqlMapper.selectNodeStatus().toLowerCase(Locale.ROOT);
        if (isPrimary(nodeRole)) {
            statementRows = opengaussAllSlowsqlMapper.selectPrimarySlowSqls(
                    convertToDateTime(String.valueOf(startPoint.getTime()), ZoneId.systemDefault()));
        } else if (isStandby(nodeRole)) {
            statementRows = opengaussAllSlowsqlMapper.selectStandbySlowSqls(finishPoint);
        } else {
            throw new CustomException("unknown node role: " + nodeRole);
        }
        return statementRows;
    }

    private boolean isPrimary(String nodeRole) {
        return "primary".equalsIgnoreCase(nodeRole) || "normal".equalsIgnoreCase(nodeRole);
    }

    private boolean isStandby(String nodeRole) {
        return "standby".equalsIgnoreCase(nodeRole) || "cascade standby".equalsIgnoreCase(nodeRole)
                || "main standby".equalsIgnoreCase(nodeRole);
    }

    private Date getOneMinuteAgo(Date date) {
        Instant oneMinuteAgo = date.toInstant().minus(TIME_OFFSET, ChronoUnit.SECONDS);
        return Date.from(oneMinuteAgo);
    }

    /**
     * list slow sqls to frontend
     *
     * @param slowLogQuery SlowLogQuery
     * @return MyPage<StatementHistoryDetailVO>
     */
    public MyPage<StatementHistoryDetailVO> listSlowSQLs(SlowLogQuery slowLogQuery) {
        SlowLogQuery finalSlowLogQuery = setDefaultSlowlogQuery(slowLogQuery);
        synchronized (cache) {
            if (cache.containsKey(finalSlowLogQuery)) {
                var cacheAble = cache.get(finalSlowLogQuery);
                if (cacheAble.getObj().isDone() && System.currentTimeMillis() - cacheAble.getCurr() > 1000) {
                    cache.remove(finalSlowLogQuery);
                } else {
                    try {
                        return cacheAble.getObj().get();
                    } catch (InterruptedException e) {
                        log.error("Interrupted!", e);
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        log.error("ExecutionException!", e);
                    }
                }
            }
        }
        var future = Executors.newSingleThreadExecutor().submit(() -> {
            var wrapper = Wrappers.<HisSlowsqlInfoDO>lambdaQuery()
                    .eq(StringUtils.isNotBlank(finalSlowLogQuery.getDbName()), HisSlowsqlInfoDO::getDbName,
                            finalSlowLogQuery.getDbName())
                    .le(finalSlowLogQuery.getFinishTime() != null, HisSlowsqlInfoDO::getFinishTime,
                            finalSlowLogQuery.getFinishTime())
                    .ge(finalSlowLogQuery.getStartTime() != null, HisSlowsqlInfoDO::getStartTime,
                            finalSlowLogQuery.getStartTime())
                    .eq(HisSlowsqlInfoDO::getIsSlowSql, true);
            var page = new BasePageDTO<StatementHistoryDetailVO>();
            String tableName = getTableNameByNodeId(finalSlowLogQuery.getNodeId());
            if (finalSlowLogQuery.getQueryCount()) {
                page.setTotal(dynamicHisSlowSqlMapper.selectCount(tableName, wrapper));
            }
            SlowSqlDTO slowSqlDTO = constructSlowSqlDTO(finalSlowLogQuery);
            List<StatementHistoryDetailVO> list = dynamicHisSlowSqlMapper.selectAllSlowSql(tableName, slowSqlDTO);
            page.setRecords(list);
            page.setCurrent(slowLogQuery.getPageNum()).setSize(slowLogQuery.getPageSize());
            return page;
        });
        var c = new cacheAble<>(future);
        cache.put(slowLogQuery, c);

        try {
            BasePageDTO<StatementHistoryDetailVO> page = future.get();
            c.setCurr(System.currentTimeMillis());
            return page;
        } catch (InterruptedException e) {
            log.error("Interrupted!", e);
            Thread.currentThread().interrupt();
            throw new CustomException(e.getMessage());
        } catch (ExecutionException e) {
            log.error("ExecutionException!", e);
            throw new CustomException(e.getMessage());
        }
    }

    private SlowSqlDTO constructSlowSqlDTO(SlowLogQuery slowLogQuery) {
        int pageNum = (slowLogQuery.getPageNum() - 1) * slowLogQuery.getPageSize();
        int pageSize = slowLogQuery.getPageSize();
        SlowSqlDTO slowSqlDTO = new SlowSqlDTO();
        slowSqlDTO.setDbName(slowLogQuery.getDbName());
        slowSqlDTO.setStartTime(slowLogQuery.getStartTime());
        slowSqlDTO.setFinishTime(slowLogQuery.getFinishTime());
        slowSqlDTO.setLimit(pageSize);
        slowSqlDTO.setOffset(pageNum);
        slowSqlDTO.setOrderByColumn(slowLogQuery.getOrderByColumn());
        slowSqlDTO.setIsAsc(slowLogQuery.getIsAsc());
        return slowSqlDTO;
    }

    private SlowLogQuery setDefaultSlowlogQuery(SlowLogQuery slowLogQuery) {
        SlowLogQuery defaultSlowLogQuery = slowLogQuery;
        if (defaultSlowLogQuery.getStartTime() == null || defaultSlowLogQuery.getFinishTime() == null) {
            defaultSlowLogQuery.setFinishTime(new Date());
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
            Date endDate = Date.from(sevenDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
            defaultSlowLogQuery.setStartTime(endDate);
        }
        return defaultSlowLogQuery;
    }

    /**
     * list statistics slow sql info to frontend
     *
     * @param slowLogQuery SlowLogQuery
     * @return MyPage<StatementHistoryAggVO>
     */
    public MyPage<StatementHistoryAggVO> selectSlowSqlAggData(SlowLogQuery slowLogQuery) {
        SlowLogQuery finalSlowLogQuery = setDefaultSlowlogQuery(slowLogQuery);
        SlowSqlDTO slowSqlDTO = constructSlowSqlDTO(finalSlowLogQuery);
        String tableName = getTableNameByNodeId(slowLogQuery.getNodeId());
        List<StatementHistoryAggVO> list = dynamicHisSlowSqlMapper.selectAggSlowSql(tableName, slowSqlDTO);
        list.forEach(elem -> {
            elem.setFirstExecuteTime(convertToDateTime(elem.getFirstExecuteTime(), ZoneId.of("UTC")));
            elem.setFinalExecuteTime(convertToDateTime(elem.getFinalExecuteTime(), ZoneId.of("UTC")));
        });
        BasePageDTO<StatementHistoryAggVO> page = new BasePageDTO<>();
        if (slowLogQuery.getQueryCount()) {
            page.setTotal(list.size());
        }
        int startIndex = (slowLogQuery.getPageNum() - 1) * slowLogQuery.getPageSize();
        int endIndex = Math.min(startIndex + slowLogQuery.getPageSize(), list.size());
        page.setRecords(list.subList(startIndex, endIndex));
        page.setCurrent(slowLogQuery.getPageNum()).setSize(slowLogQuery.getPageSize());
        return page;
    }

    private String convertToDateTime(String timestampStr, ZoneId zone) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            Instant instant = Instant.ofEpochMilli(timestamp);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zone);
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("illegal timestampStr", e);
        }
    }

    private String getTableNameByNodeId(String nodeId) {
        return TABLE_PREFIX
                + nodeId.replaceAll("-", "_");
    }

    /**
     * list statistics slow sql info to frontend
     *
     * @param nodeId String
     * @param start Long
     * @param end Long
     * @param step Integer
     * @param dbName String
     * @return Map<String, Object>
     */
    public Map<String, Object> getSlowSqlChart(String nodeId, Long start, Long end, Integer step, String dbName) {
        List<Date> timePints = new ArrayList<>();
        List<Integer> activeSqls = new ArrayList<>();
        String tableName = getTableNameByNodeId(nodeId);
        for (long point = start; point <= end; point += step) {
            long millsTimePoint = point * 1000;
            timePints.add(new Date(millsTimePoint));
            activeSqls.add(dynamicHisSlowSqlMapper.selectActiveSlowsqls(tableName, millsTimePoint, dbName));
        }
        clusterManager.setCurrentDatasource(nodeId, "postgres");
        String slowSqlThreshold = opengaussAllSlowsqlMapper.getSlowsqlThreshold();
        clusterManager.pool();
        Map<String, Object> data = new HashMap<>();
        data.put("time", timePints);
        data.put("slowSqlThreshold", slowSqlThreshold);
        data.put("INSTANCE_DB_SLOWSQL", activeSqls);
        return data;
    }
}
