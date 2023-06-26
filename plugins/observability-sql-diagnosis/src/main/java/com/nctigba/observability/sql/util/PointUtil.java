package com.nctigba.observability.sql.util;

import com.alibaba.fastjson.JSONArray;
import com.nctigba.observability.sql.constants.history.PrometheusConstants;
import com.nctigba.observability.sql.model.history.data.PrometheusData;
import com.nctigba.observability.sql.model.history.point.AspAnalysisDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * PointUtil
 *
 * @author luomeng
 * @since 2023/6/25
 */
@Component
@Slf4j
public class PointUtil {
    public List<AspAnalysisDTO> getAspTimeSlot(List<PrometheusData> prometheusDataList) {
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
        int minute = PrometheusConstants.MINUTE;
        long ms = PrometheusConstants.MS;
        int mCount = minute / Integer.parseInt(PrometheusConstants.STEP) - 1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        if (timeList.size() == 1) {
            AspAnalysisDTO dto = new AspAnalysisDTO();
            dto.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
            dto.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
            dtoList.add(dto);
        } else if (timeList.size() == 2) {
            if (timeList.get(0) + Integer.parseInt(PrometheusConstants.STEP) == timeList.get(1)) {
                AspAnalysisDTO dto = new AspAnalysisDTO();
                dto.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                dto.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                dtoList.add(dto);
            } else {
                AspAnalysisDTO dto1 = new AspAnalysisDTO();
                dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                dtoList.add(dto1);
                AspAnalysisDTO dto2 = new AspAnalysisDTO();
                dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                dtoList.add(dto2);
            }
        } else if (timeList.size() == 3) {
            if (timeList.get(0) + Integer.parseInt(PrometheusConstants.STEP) * 2 == timeList.get(2)) {
                AspAnalysisDTO dto = new AspAnalysisDTO();
                dto.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                dto.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                dtoList.add(dto);
            } else {
                if (timeList.get(0) + Integer.parseInt(PrometheusConstants.STEP) == timeList.get(1)) {
                    AspAnalysisDTO dto1 = new AspAnalysisDTO();
                    dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                    dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                    dtoList.add(dto1);
                    AspAnalysisDTO dto2 = new AspAnalysisDTO();
                    dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                    dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                    dtoList.add(dto2);
                } else if (timeList.get(1) + Integer.parseInt(PrometheusConstants.STEP) == timeList.get(2)) {
                    AspAnalysisDTO dto1 = new AspAnalysisDTO();
                    dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                    dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                    dtoList.add(dto1);
                    AspAnalysisDTO dto2 = new AspAnalysisDTO();
                    dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                    dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                    dtoList.add(dto2);
                } else {
                    AspAnalysisDTO dto1 = new AspAnalysisDTO();
                    dto1.setStartTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                    dto1.setEndTime(simpleDateFormat.format(new Date(timeList.get(0) * ms)));
                    dtoList.add(dto1);
                    AspAnalysisDTO dto2 = new AspAnalysisDTO();
                    dto2.setStartTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                    dto2.setEndTime(simpleDateFormat.format(new Date(timeList.get(1) * ms)));
                    dtoList.add(dto2);
                    AspAnalysisDTO dto3 = new AspAnalysisDTO();
                    dto3.setStartTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                    dto3.setEndTime(simpleDateFormat.format(new Date(timeList.get(2) * ms)));
                    dtoList.add(dto3);
                }
            }
        } else {
            int cursor = 0;
            for (int i = 0; i < timeList.size() - 3; i = cursor) {
                int step = Integer.parseInt(PrometheusConstants.STEP);
                int count = minute / step - 1;
                int startTime = timeList.get(i);
                int realityTime = timeList.get(i + count);
                int expectTime = startTime + step * (count);
                int temp = 0;
                if (realityTime == expectTime) {
                    for (int n = i + count; n < timeList.size() - i - count; n++) {
                        int realityValue = timeList.get(n);
                        int expectValue = startTime + step * (n - i);
                        if (realityValue > expectValue) {
                            cursor = n;
                            temp++;
                            break;
                        }
                    }
                }
                if (temp > 0) {
                    continue;
                } else {
                    cursor = i + mCount;
                }
                AspAnalysisDTO dto = new AspAnalysisDTO();
                if (realityTime > expectTime) {
                    while (count > 0) {
                        if (timeList.get(i + count) == startTime + step * (count)) {
                            int entTime = timeList.get(i + count);
                            dto.setStartTime(simpleDateFormat.format(new Date(startTime * ms)));
                            dto.setEndTime(simpleDateFormat.format(new Date(entTime * ms)));
                            dtoList.add(dto);
                            break;
                        }
                        count--;
                    }
                }
            }
        }
        return dtoList;
    }
}
