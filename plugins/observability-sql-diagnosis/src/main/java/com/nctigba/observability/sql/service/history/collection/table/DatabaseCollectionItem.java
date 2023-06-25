/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.collection.table;

import com.nctigba.observability.sql.constants.history.ThresholdCommon;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.util.DbUtil;
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
    private DbUtil dbUtil;

    public Object collectData(HisDiagnosisTask task) {
        List<HisDiagnosisThreshold> thresholds = task.getThresholds();
        String during = null;
        for (HisDiagnosisThreshold threshold : thresholds) {
            if (threshold.getThreshold().equals(ThresholdCommon.DURING)) {
                during = threshold.getThresholdValue();
            }
        }
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
        return dbUtil.rangQuery(sql, task.getHisDataStartTime(),
                task.getHisDataEndTime(),
                task.getNodeId());
    }

    public Object queryData(HisDiagnosisTask task) {
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
            List<HisDiagnosisThreshold> thresholds = task.getThresholds();
            String during = null;
            for (HisDiagnosisThreshold threshold : thresholds) {
                if (threshold.getThreshold().equals(ThresholdCommon.DURING)) {
                    during = threshold.getThresholdValue();
                }
            }
            map.put(ThresholdCommon.DURING, during);
        }
        String during = map.get(ThresholdCommon.DURING);
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
        return dbUtil.rangQuery(sql, task.getHisDataStartTime(),
                task.getHisDataEndTime(),
                task.getNodeId());
    }

    abstract String getDatabaseSql();
}
