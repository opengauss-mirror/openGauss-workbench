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
 *  IoWait.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/IoWait.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.AutoShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ChartShowDataVO;
import com.nctigba.observability.sql.model.vo.point.ChartShowDataYDataVO;
import com.nctigba.observability.sql.model.vo.point.ChartVO;
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.ebpf.IoStatItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IoWait
 *
 * @author luomeng
 * @since 2023/11/7
 */
@Service
public class IoWait implements DiagnosisPointService<Object> {
    private static final float THRESHOLD_VALUE = 10.0f;

    private static final Pattern CHARSET_REG = Pattern.compile("\\b\\d{2}/\\d{2}/\\d{4}\\b");

    @Autowired
    private IoStatItem item;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionEnum.IS_BCC));
        return option;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        Object obj = dataStoreService.getData(item).getCollectionData();
        MultipartFile file = null;
        if (obj instanceof MultipartFile) {
            file = (MultipartFile) obj;
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        ChartVO chartVO = getChartDataByFile(file);
        List<String> dataList = chartVO.getDataList();
        List<String> timeList = chartVO.getTimeList();
        dataList.forEach(f -> {
            BigDecimal load = new BigDecimal(f);
            if (load.floatValue() > THRESHOLD_VALUE) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            }
        });
        AutoShowDataVO dataVO = assemblyData(dataList, timeList);
        analysisDTO.setPointData(dataVO);
        return analysisDTO;
    }

    private AutoShowDataVO assemblyData(List<String> dataList, List<String> timeList) {
        ChartShowDataYDataVO chartData = new ChartShowDataYDataVO();
        chartData.setName(LocaleStringUtils.format("ioWait.name"));
        chartData.setData(dataList);
        List<ChartShowDataYDataVO> series = new ArrayList<>();
        series.add(chartData);
        ChartShowDataVO chartShowDataVO = new ChartShowDataVO();
        chartShowDataVO.setUnit("ms");
        chartShowDataVO.setXData(timeList);
        chartShowDataVO.setSeries(series);
        chartShowDataVO.setDataName("IoWait");
        AutoShowDataVO dataVO = new AutoShowDataVO();
        List<ShowData> list = new ArrayList<>();
        list.add(chartShowDataVO);
        dataVO.setData(list);
        return dataVO;
    }

    private ChartVO getChartDataByFile(MultipartFile file) {
        List<String> dataList = new ArrayList<>();
        List<String> timeList = new ArrayList<>();
        SimpleDateFormat stringFormat = new SimpleDateFormat("MM/dd/yyyy");
        String date = stringFormat.format(new Date());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try (var reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                var line = reader.readLine();
                if (line.isBlank()) {
                    continue;
                }
                if (line.startsWith("Average")) {
                    break;
                }
                if (line.startsWith("Linux")) {
                    Matcher matcher = CHARSET_REG.matcher(line);
                    if (matcher.find()) {
                        date = matcher.group();
                    }
                    continue;
                }
                String time = date + " " + line.substring(0, 11);
                Date date1 = dateFormat.parse(time);
                time = format.format(date1);
                var data = line.substring(11).trim().split("\\s+");
                if (data[0].equals("DEV")) {
                    continue;
                }
                if (timeList.contains(time)) {
                    int a = timeList.indexOf(time);
                    BigDecimal b = new BigDecimal(dataList.get(a));
                    BigDecimal c = new BigDecimal(data[7]);
                    BigDecimal d = b.add(c);
                    dataList.set(a, String.valueOf(d));
                } else {
                    timeList.add(time);
                    dataList.add(data[7]);
                }
            }
        } catch (IOException | ParseException e) {
            throw new CustomException("sarD:", e);
        }
        ChartVO chartVO = new ChartVO();
        chartVO.setDataList(dataList);
        chartVO.setTimeList(timeList);
        return chartVO;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
