package com.nctigba.observability.sql.util;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.history.PrometheusConstants;
import com.nctigba.observability.sql.mapper.history.HisThresholdMapper;
import com.nctigba.observability.sql.model.history.HisDiagnosisThreshold;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.point.AspAnalysisDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * PointUtil
 *
 * @author luomeng
 * @since 2023/6/25
 */
@Component
@Slf4j
public class PointUtil {
    @Autowired
    private HisThresholdMapper hisThresholdMapper;

    /**
     * Get asp time slot
     *
     * @param prometheusDataList Prometheus data
     * @return list
     */
    public List<AspAnalysisDTO> aspTimeSlot(List<PrometheusData> prometheusDataList) {
        List<AspAnalysisDTO> dtoList = new ArrayList<>();
        List<Integer> timeList = new ArrayList<>();
        for (PrometheusData data : prometheusDataList) {
            JSONArray values = data.getValues();
            for (Object value : values) {
                JSONArray timeJson = new JSONArray();
                if (value instanceof JSONArray) {
                    timeJson = (JSONArray) value;
                }
                Object time = timeJson.get(0);
                timeList.add(Integer.parseInt(time.toString()));
            }
        }
        int mCount = PrometheusConstants.MINUTE / Integer.parseInt(PrometheusConstants.STEP) - 1;
        int cursor;
        for (int i = 0; i < timeList.size(); i = cursor) {
            int count = 0;
            for (int j = i + 1; j < timeList.size(); j++) {
                if (timeList.get(i) == timeList.get(j) - Integer.parseInt(PrometheusConstants.STEP) * (j - i)) {
                    count++;
                }
            }
            if (count < mCount && count > 0) {
                AspAnalysisDTO dto = new AspAnalysisDTO(timeList.get(i), timeList.get(i + count));
                dtoList.add(dto);
            } else if (count == 0) {
                AspAnalysisDTO dto = new AspAnalysisDTO(timeList.get(i), timeList.get(i));
                dtoList.add(dto);
            } else {
                log.info("not exists slot");
            }
            cursor = i + count + 1;
        }
        return dtoList;
    }

    /**
     * Prometheus' data transform to list
     *
     * @param list Prometheus data
     * @return list
     */
    public List<PrometheusData> dataToObject(List<?> list) {
        List<PrometheusData> prometheusDataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return prometheusDataList;
        }
        PrometheusData prometheusData = new PrometheusData();
        PrometheusData objectData = new PrometheusData();
        if (list.get(0) instanceof PrometheusData) {
            objectData = (PrometheusData) list.get(0);
        }
        prometheusData.setMetric(objectData.getMetric());
        JSONArray jsonArray = new JSONArray();
        for (Object object : list) {
            if (object instanceof PrometheusData) {
                jsonArray.addAll(((PrometheusData) object).getValues());
            }
        }
        prometheusData.setValues(jsonArray);
        prometheusDataList.add(prometheusData);
        return prometheusDataList;
    }

    /**
     * Threshold info transform to maps
     *
     * @param thresholds Threshold info
     * @return HashMap
     */
    public HashMap<String, String> thresholdMap(List<HisDiagnosisThreshold> thresholds) {
        LambdaQueryWrapper<HisDiagnosisThreshold> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(HisDiagnosisThreshold::getThresholdType);
        List<HisDiagnosisThreshold> thresholdList = hisThresholdMapper.selectList(queryWrapper);
        HashMap<String, String> map = new HashMap<>();
        for (HisDiagnosisThreshold threshold : thresholdList) {
            thresholds.forEach(f -> {
                if (f.getThreshold().equals(threshold.getThreshold())) {
                    threshold.setThresholdValue(f.getThresholdValue());
                }
            });
            map.put(threshold.getThreshold(), threshold.getThresholdValue());
        }
        if (map.isEmpty()) {
            throw new HisDiagnosisException("fetch threshold data failed!");
        }
        return map;
    }

    /**
     * Get cpu core num
     *
     * @param list prometheus data
     * @return num
     */
    public int getCpuCoreNum(List<?> list) {
        int coreNum = 8;
        if (!CollectionUtils.isEmpty(list)) {
            return coreNum;
        }
        List<Object> objects = new ArrayList<>();
        for (Object object : list) {
            if (object instanceof PrometheusData) {
                objects = ((PrometheusData) object).getValues();
                break;
            }
        }
        if (!CollectionUtils.isEmpty(objects)) {
            return coreNum;
        }
        Object objectList = objects.get(0);
        if (objectList instanceof List<?>) {
            List<?> valueList = (List<?>) objectList;
            if (!CollectionUtils.isEmpty(valueList)) {
                String value = valueList.get(1).toString();
                coreNum = Integer.parseInt(value);
            }
        }
        return coreNum;
    }
}
