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
 *  OsLoad.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/OsLoad.java
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
import com.nctigba.observability.sql.service.impl.collection.ebpf.CpuAvgLoadItem;
import com.nctigba.observability.sql.service.impl.collection.ebpf.CpuCoreCountItem;
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
 * OsLoad
 *
 * @author luomeng
 * @since 2023/11/7
 */
@Service
public class OsLoad implements DiagnosisPointService<Object> {
    private static final Pattern CHARSET_REG = Pattern.compile("\\b\\d{2}/\\d{2}/\\d{4}\\b");

    @Autowired
    private CpuAvgLoadItem cpuAvgLoadItem;
    @Autowired
    private CpuCoreCountItem cpuCoreCountItem;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionEnum.IS_BCC));
        return option;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(cpuAvgLoadItem);
        list.add(cpuCoreCountItem);
        return list;
    }

    @Override
    public AnalysisDTO analysis(DiagnosisTaskDO task, DataStoreService dataStoreService) {
        Object obj = dataStoreService.getData(cpuAvgLoadItem).getCollectionData();
        MultipartFile file = null;
        if (obj instanceof MultipartFile) {
            file = (MultipartFile) obj;
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        Object coreNumObj = dataStoreService.getData(cpuCoreCountItem).getCollectionData();
        int coreNum = getCoreNumByFile(coreNumObj);
        ChartVO chartVO = getChartDataByFile(file);
        List<String> dataList = chartVO.getDataList();
        dataList.forEach(f -> {
            BigDecimal load = new BigDecimal(f);
            BigDecimal num = new BigDecimal(coreNum * 2);
            if (load.compareTo(num) > 0) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            }
        });
        ChartShowDataYDataVO chartData = new ChartShowDataYDataVO();
        chartData.setName(LocaleStringUtils.format("OsLoad.name"));
        chartData.setData(dataList);
        List<ChartShowDataYDataVO> series = new ArrayList<>();
        series.add(chartData);
        ChartShowDataVO chartShowDataVO = new ChartShowDataVO();
        chartShowDataVO.setUnit("num");
        List<String> timeList = chartVO.getTimeList();
        chartShowDataVO.setXData(timeList);
        chartShowDataVO.setSeries(series);
        chartShowDataVO.setDataName("OsLoad");
        AutoShowDataVO dataVO = new AutoShowDataVO();
        List<ShowData> list = new ArrayList<>();
        list.add(chartShowDataVO);
        dataVO.setData(list);
        analysisDTO.setPointData(dataVO);
        return analysisDTO;
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
                if (data[0].equals("runq-sz")) {
                    continue;
                }
                timeList.add(time);
                dataList.add(data[2]);
            }
        } catch (IOException | ParseException e) {
            throw new CustomException("sarQ:", e);
        }
        ChartVO chartVO = new ChartVO();
        chartVO.setDataList(dataList);
        chartVO.setTimeList(timeList);
        return chartVO;
    }

    private int getCoreNumByFile(Object coreNumObj) {
        MultipartFile coreNumFile = null;
        if (coreNumObj instanceof MultipartFile) {
            coreNumFile = (MultipartFile) coreNumObj;
        }
        int coreNum = 0;
        if (coreNumFile != null && !coreNumFile.isEmpty()) {
            try (var reader = new BufferedReader(
                    new InputStreamReader(coreNumFile.getInputStream(), StandardCharsets.UTF_8))) {
                while (reader.ready()) {
                    var line = reader.readLine();
                    coreNum = Integer.parseInt(line);
                }
            } catch (IOException e) {
                throw new CustomException("cpuCoreNum:", e);
            }
        }
        return coreNum;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
