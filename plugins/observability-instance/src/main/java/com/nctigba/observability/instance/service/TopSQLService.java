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
 *  TopSQLService.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/TopSQLService.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nctigba.observability.instance.model.vo.PgStatActivityVO;
import com.nctigba.observability.instance.model.vo.StatementHistoryVO;
import org.apache.commons.lang3.StringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.nctigba.observability.instance.aspectj.annotation.Ds;
import com.nctigba.observability.instance.model.query.TopSQLListQuery;
import com.nctigba.observability.instance.model.entity.PgSettingsDO;
import com.nctigba.observability.instance.mapper.PgSettingMapper;
import com.nctigba.observability.instance.mapper.TopSqlMapper;
import com.nctigba.observability.instance.model.dto.ExecutionPlanDTO;
import com.nctigba.observability.instance.model.dto.IndexAdviceDTO;
import com.nctigba.observability.instance.service.TopSQLService.WaitEvent.Event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Generated;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * TopSQL services
 * </p>
 *
 * @since 2022/9/15 15:46
 */
@Service
@RequiredArgsConstructor
public class TopSQLService {
    private final PgSettingMapper pgSettingMapper;
    private final TopSqlMapper topSqlMapper;

    /**
     * fetch TopSQL list from database
     *
     * @param topSQLListReq instance node id
     * @return TopSQL list
     */
    @Ds("id")
    public List<StatementHistoryVO> topSQLList(TopSQLListQuery topSQLListReq) {
        topSqlListPreCheck();
        return topSqlMapper.historyTopsqlList(topSQLListReq);
    }

    /**
     * fetch TopSQL now list from database
     *
     * @param id instance node id
     * @return TopSQL list
     */
    @Ds
    public List<PgStatActivityVO> topSQLNow(String id) {
        return topSqlMapper.currentTopsqlList();
    }

    /**
     * fetch TopSQL statistical information from database, page index
     *
     * @param nodeId instance node id
     * @param sqlId  TopSQL debug query id
     * @return TopSQL statistical information
     */
    @Ds
    public Map<String, Object> detail(String nodeId, String sqlId) {
        var detail = topSqlMapper.currentDetail(sqlId);
        if (CollUtil.isNotEmpty(detail)) {
            return detail;
        }
        return topSqlMapper.historyDetail(sqlId);
    }

    /**
     * fetch TopSQL execution plan from database
     *
     * @param nodeId instance node id
     * @param sqlId  TopSQL debug query id
     * @return TopSQL execution plan
     */
    @Ds
    public ExecutionPlanDTO executionPlan(String nodeId, String sqlId) {
        String plan = topSqlMapper.currentPlan(sqlId);
        if (StrUtil.isNotBlank(plan)) {
            return new ExecutionPlanDTO(plan);
        }
        // pre-check track_stmt_stat_leve full sql level at least L1
        var settings = pgSettingMapper
                .selectOne(Wrappers.<PgSettingsDO>lambdaQuery().eq(PgSettingsDO::getName, "track_stmt_stat_level"));
        var setting = settings.getSetting();
        if (StringUtils.startsWith(setting, "OFF") || StringUtils.startsWith(setting, "L0")) {
            throw new CustomException("failGetExecutionPlan");
        }
        // get prepared statement
        plan = topSqlMapper.historyPlan(sqlId);
        // get base execution plan object
        return new ExecutionPlanDTO(plan);
    }

    /**
     * fetch TopSQL object information from database
     *
     * @param nodeId instance node id
     * @param sqlId  TopSQL debug query id
     * @return TopSQL object information
     */
    @Ds
    public Map<String, Object> objectInfo(String nodeId, String sqlId) {
        // init objectNameList via get execution plan
        var plan = executionPlan(nodeId, sqlId);
        Set<String> curObjectNameList = plan.allAlias();
        List<String> modifyObjectNameList = new LinkedList<>(curObjectNameList);
        Map<String, Object> tableMetadata = new HashMap<>();
        Map<String, Object> tableStructure = new HashMap<>();
        Map<String, Object> tableIndex = new HashMap<>();
        for (String name : curObjectNameList) {
            try {
                tableMetadata.put(name, topSqlMapper.tableMetaData(name));
                tableStructure.put(name, topSqlMapper.tableStructure(name));
                tableIndex.put(name, topSqlMapper.indexInfo(name));
            } catch (Exception e) {
                modifyObjectNameList.remove(name);
            }
        }
        return Map.of("object_name_list", modifyObjectNameList, "table_metadata", tableMetadata, "table_structure",
                tableStructure, "table_index", tableIndex);
    }

    /**
     * fetch TopSQL index advice from database
     *
     * @param nodeId instance node id
     * @param sqlId  TopSQL debug query id
     * @return TopSQL index advice
     */
    @Ds
    public List<String> indexAdvice(String nodeId, String sqlId) {
        List<String> results = new ArrayList<>();
        var queryText = topSqlMapper.sql(sqlId);
        var advises = topSqlMapper.advise(queryText);
        String indexTemplate = "建议为%s模式下的%t表的%c列创建索引";
        String multiColumnIndexTemplate = "建议为%s模式下的%t表的%c创建复合索引";
        if (advises.isEmpty()) {
            results.add("No index suggestions");
            return results;
        }
        // process index advice for every returned line
        for (IndexAdviceDTO advice : advises) {
            String column = advice.getColumn();
            if (StringUtils.isNotEmpty(column)) {
                String result = column.contains(",") ? multiColumnIndexTemplate : indexTemplate;
                result = result.replace("%s", advice.getSchema());
                result = result.replace("%t", advice.getTable());
                result = result.replace("%c", advice.getColumn());
                results.add(result);
            }
        }
        return results;
    }

    @Ds
    public List<Map<String, Object>> waitEvent(String nodeId, String sqlId) {
        List<Map<String, Object>> list = new ArrayList<>();
        String table = topSqlMapper.waitEvent(sqlId);
        if (StrUtil.isBlank(table)) {
            return topSqlMapper.currentWaitEvent(sqlId);
        }
        String[] lines = table.split(",");
        WaitEvent pre = null;
        for (String string : lines) {
            var curr = new WaitEvent(string);
            if (pre != null && pre.getE() == Event.LOCK_START && curr.getE() == Event.LOCK_END) {
                list.add(Map.of("starttime", pre.getTimeStr(), "lockType", pre.getLockType(), "waittime",
                        (Duration.between(pre.getTime(), curr.getTime()).toNanos() / 1000)));
                pre = null;
                continue;
            }
            pre = curr;
        }
        return list;
    }

    /**
     * pre-check top sql list job
     *
     * true when not log; false when can search log
     */
    private void topSqlListPreCheck() {
        var list = pgSettingMapper.selectList(Wrappers.<PgSettingsDO>lambdaQuery().in(PgSettingsDO::getName,
                "enable_stmt_track", "enable_resource_track", "track_stmt_stat_level'"));
        for (PgSettingsDO pgSettingsDO : list) {
            switch (pgSettingsDO.getName()) {
            case "enable_resource_track":
            case "enable_stmt_track":
                if ("off".equals(pgSettingsDO.getSetting())) {
                    throw new CustomException("top sql pre check fail", 602);
                }
                break;
            case "track_stmt_stat_level":
                var setting = pgSettingsDO.getSetting();
                if (StringUtils.isEmpty(setting) || !setting.contains(",")) {
                    throw new CustomException("top sql pre check fail", 602);
                }
                String[] settingArr = setting.split(",");
                if ("off".equalsIgnoreCase(settingArr[0])) {
                    throw new CustomException("top sql pre check fail", 602);
                }
                break;
            default:
            }
        }
    }

    @Data
    @Generated
    public static class WaitEvent {
        private static final DateTimeFormatter[] FORMATTERS = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.Sx")
        };
        private String index;
        private Event e;
        private LocalDateTime time;
        private String timeStr;
        private String id;
        private String lockType;

        public WaitEvent(String string) {
            String[] str = string.split("'\\s+'");
            this.index = str[0].replaceAll("'", "");
            this.e = Event.valueOf(str[1].replaceAll("'", ""));
            for (int i = 0; i < FORMATTERS.length; i++) {
                try {
                    this.time = LocalDateTime.parse(str[2].replaceAll("'", ""), FORMATTERS[i]);
                    break;
                } catch (DateTimeParseException e) {
                    continue;
                }
            }
            this.timeStr = str[2];
            if (str.length > 3)
                this.id = str[3].replaceAll("'", "");
            if (str.length > 4)
                this.lockType = str[4].replaceAll("'", "");
        }

        public enum Event {
            LOCK_START,
            LOCK_END,
            LOCK_RELEASE
        }
    }
}