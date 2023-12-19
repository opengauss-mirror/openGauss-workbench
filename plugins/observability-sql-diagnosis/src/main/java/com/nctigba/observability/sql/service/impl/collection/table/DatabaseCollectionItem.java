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
 *  DatabaseCollectionItem.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/collection/table/DatabaseCollectionItem.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.collection.table;

import com.nctigba.observability.sql.constant.ThresholdConstants;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.entity.DiagnosisThresholdDO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.util.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * DatabaseCollectionItem
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Slf4j
public abstract class DatabaseCollectionItem implements CollectionItem<Object> {
    @Autowired
    private DbUtils dbUtils;

    public Object collectData(DiagnosisTaskDO task) {
        HashMap<String, String> map = new HashMap<>();
        List<?> thresholdValue = task.getThresholds();
        for (Object ob : thresholdValue) {
            if (ob instanceof LinkedHashMap) {
                LinkedHashMap<String, String> hashMap = (LinkedHashMap<String, String>) ob;
                map.put(hashMap.get("threshold"), hashMap.get("thresholdValue"));
            }
        }
        String during = map.get(ThresholdConstants.DURING);
        String sql = getDatabaseSql();
        if (during != null && getDatabaseSql().contains("duration")) {
            LocalTime time = LocalTime.ofSecondOfDay(Integer.parseInt(during));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = time.format(formatter);
            sql = sql.replace("duration", formattedTime);
        }
        if (task.getHisDataEndTime() == null) {
            Date endDate = new Date();
            task.setHisDataEndTime(endDate);
        }
        return dbUtils.rangQuery(sql, task.getHisDataStartTime(),
                task.getHisDataEndTime(),
                task.getNodeId());
    }

    public Object queryData(DiagnosisTaskDO task) {
        HashMap<String, String> map = new HashMap<>();
        List<?> thresholdValue = task.getThresholds();
        for (Object ob : thresholdValue) {
            if (ob instanceof LinkedHashMap) {
                LinkedHashMap<String, String> hashMap = (LinkedHashMap<String, String>) ob;
                map.put(hashMap.get("threshold"), hashMap.get("thresholdValue"));
            }
        }
        if (map.isEmpty()) {
            log.info("fetch threshold data failed!");
            List<DiagnosisThresholdDO> thresholds = task.getThresholds();
            String during = null;
            for (DiagnosisThresholdDO threshold : thresholds) {
                if (threshold.getThreshold().equals(ThresholdConstants.DURING)) {
                    during = threshold.getThresholdValue();
                }
            }
            map.put(ThresholdConstants.DURING, during);
        }
        String during = map.get(ThresholdConstants.DURING);
        String sql = getDatabaseSql();
        if (during != null && getDatabaseSql().contains("duration")) {
            LocalTime time = LocalTime.ofSecondOfDay(Integer.parseInt(during));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = time.format(formatter);
            sql = sql.replace("duration", formattedTime);
        }
        if (task.getHisDataEndTime() == null) {
            Date endDate = new Date();
            task.setHisDataEndTime(endDate);
        }
        return dbUtils.rangQuery(sql, task.getHisDataStartTime(),
                task.getHisDataEndTime(),
                task.getNodeId());
    }

    /**
     * Get database sql
     *
     * @return String
     */
    public abstract String getDatabaseSql();
}
