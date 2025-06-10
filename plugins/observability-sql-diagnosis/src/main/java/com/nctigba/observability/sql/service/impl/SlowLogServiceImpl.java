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
 *  SlowLogServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/SlowLogServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nctigba.observability.sql.caller.DiagnosisCaller;
import com.nctigba.observability.sql.mapper.AgentNodeRelationMapper;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.mapper.PgSettingsMapper;
import com.nctigba.observability.sql.mapper.SlowLogMapper;
import com.nctigba.observability.sql.model.dto.BasePageDTO;
import com.nctigba.observability.sql.model.dto.SlowSqlDTO;
import com.nctigba.observability.sql.model.entity.PgSettingsDO;
import com.nctigba.observability.sql.model.entity.StatementHistoryDO;
import com.nctigba.observability.sql.model.query.SlowLogQuery;
import com.nctigba.observability.sql.model.vo.StatementHistoryAggVO;
import com.nctigba.observability.sql.model.vo.StatementHistoryDetailVO;
import com.nctigba.observability.sql.service.MyPage;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class SlowLogServiceImpl extends ServiceImpl<SlowLogMapper, StatementHistoryDO> {
    @Autowired
    private ClusterManager clusterManager;
    @Autowired
    private SlowLogMapper slowLogMapper;
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    private AgentNodeRelationMapper agentMapper;
    @Autowired
    private PgSettingsMapper pgSettingsMapper;
    @Autowired
    private DiagnosisCaller diagnosisCaller;
    private Map<SlowLogQuery, cacheAble<Future<BasePageDTO<StatementHistoryDetailVO>>>> cache = new WeakHashMap<>();

    public MyPage<StatementHistoryDetailVO> listSlowSQLs(SlowLogQuery slowLogQuery) {
        if (slowLogQuery.getStartTime() == null || slowLogQuery.getFinishTime() == null) {
            slowLogQuery.setFinishTime(new Date());
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
            Date endDate = Date.from(sevenDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
            slowLogQuery.setStartTime(endDate);
        }
        synchronized (cache) {
            if (cache.containsKey(slowLogQuery)) {
                var cacheAble = cache.get(slowLogQuery);
                if (cacheAble.getObj().isDone() && System.currentTimeMillis() - cacheAble.getCurr() > 1000) {
                    cache.remove(slowLogQuery);
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
            clusterManager.setCurrentDatasource(slowLogQuery.getNodeId(), slowLogQuery.getDbName());
            try {
                var wrapper = Wrappers.<StatementHistoryDO>lambdaQuery()
                        .eq(StringUtils.isNotBlank(slowLogQuery.getDbName()), StatementHistoryDO::getDbName,
                                slowLogQuery.getDbName())
                        .le(slowLogQuery.getFinishTime() != null, StatementHistoryDO::getFinishTime,
                                slowLogQuery.getFinishTime())
                        .ge(slowLogQuery.getStartTime() != null, StatementHistoryDO::getStartTime,
                                slowLogQuery.getStartTime())
                        .eq(StatementHistoryDO::getIsSlowSql, true);
                var page = new BasePageDTO<StatementHistoryDetailVO>();
                if (slowLogQuery.getQueryCount()) {
                    page.setTotal(slowLogMapper.selectCount(wrapper));
                }
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
                List<StatementHistoryDetailVO> list = slowLogMapper.selectAllSlowSql(slowSqlDTO);
                page.setRecords(list);
                page.setCurrent(slowLogQuery.getPageNum()).setSize(slowLogQuery.getPageSize());
                return page;
            } finally {
                clusterManager.pool();
            }
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
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            log.error("ExecutionException!", e);
            throw new RuntimeException(e);
        }
    }

    @Data
    static class cacheAble<T> {
        long curr = System.currentTimeMillis();
        T obj;

        public cacheAble(T obj) {
            this.obj = obj;
        }
    }

    public Map<String, Object> getSlowSqlChart(String nodeId, Long start, Long end, Integer step, String dbName) {
        clusterManager.setCurrentDatasource(nodeId, "");
        LambdaQueryWrapper<PgSettingsDO> wrapper = Wrappers.<PgSettingsDO>lambdaQuery()
                .in(PgSettingsDO::getName, "enable_stmt_track", "track_stmt_stat_level");
        List<PgSettingsDO> pgSettingsDOList = pgSettingsMapper.selectList(wrapper);
        clusterManager.pool();
        for (PgSettingsDO pgSettingsDO : pgSettingsDOList) {
            if ("enable_stmt_track".equals(pgSettingsDO.getName()) && "off".equals(pgSettingsDO.getSetting())) {
                throw new CustomException(LocaleStringUtils.format("slowSql.param.tip"));
            }
            if ("track_stmt_stat_level".equals(pgSettingsDO.getName())) {
                String[] split = pgSettingsDO.getSetting().split(",");
                if ("off".equals(split[1])) {
                    throw new CustomException(LocaleStringUtils.format("slowSql.param.tip"));
                }
            }
        }
        AjaxResult dataResult = diagnosisCaller.getInstanceMetric(
                "nodeId:" + nodeId + ";start:" + start + ";end:" + end + ";step:" + step + ";dbName:" + dbName);
        if (dataResult == null) {
            return null;
        }
        Object data = dataResult.get("data");
        if (data == null) {
            throw new CustomException(LocaleStringUtils.format("slowSql.agent.tip"));
        }
        return (Map<String, Object>) data;
    }

    public MyPage<StatementHistoryAggVO> selectSlowSqlAggData(SlowLogQuery slowLogQuery) {
        if (slowLogQuery.getStartTime() == null || slowLogQuery.getFinishTime() == null) {
            slowLogQuery.setFinishTime(new Date());
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
            Date endDate = Date.from(sevenDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
            slowLogQuery.setStartTime(endDate);
        }
        clusterManager.setCurrentDatasource(slowLogQuery.getNodeId(), slowLogQuery.getDbName());
        try {
            SlowSqlDTO slowSqlDTO = new SlowSqlDTO();
            slowSqlDTO.setDbName(slowLogQuery.getDbName());
            slowSqlDTO.setStartTime(slowLogQuery.getStartTime());
            slowSqlDTO.setFinishTime(slowLogQuery.getFinishTime());
            slowSqlDTO.setOrderByColumn(slowLogQuery.getOrderByColumn());
            slowSqlDTO.setIsAsc(slowLogQuery.getIsAsc());
            List<StatementHistoryAggVO> list = slowLogMapper.selectAggSlowSql(slowSqlDTO);
            BasePageDTO<StatementHistoryAggVO> page = new BasePageDTO<>();
            if (slowLogQuery.getQueryCount()) {
                page.setTotal(list.size());
            }
            int startIndex = (slowLogQuery.getPageNum() - 1) * slowLogQuery.getPageSize();
            int endIndex = Math.min(startIndex + slowLogQuery.getPageSize(), list.size());
            page.setRecords(list.subList(startIndex, endIndex));
            page.setCurrent(slowLogQuery.getPageNum()).setSize(slowLogQuery.getPageSize());
            return page;
        } finally {
            clusterManager.pool();
        }
    }
}