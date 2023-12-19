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
 *  Swap.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/Swap.java
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
import com.nctigba.observability.sql.model.vo.point.ShowData;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.ebpf.VmStatItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.TableFormatterUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Swap
 *
 * @author luomeng
 * @since 2023/11/7
 */
@Service
public class Swap implements DiagnosisPointService<Object> {
    @Autowired
    private VmStatItem item;

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
        var table = TableFormatterUtils.format(file, " r");
        List<Map<String, String>> tableData = table.getData();
        List<String> inList = new ArrayList<>();
        List<String> outList = new ArrayList<>();
        for (Map<String, String> map : tableData) {
            int swapIn = Integer.parseInt(map.get("si"));
            inList.add(String.valueOf(swapIn));
            int swapOut = Integer.parseInt(map.get("so"));
            outList.add(String.valueOf(swapOut));
            if (swapIn != 0 || swapOut != 0) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            }
        }
        String time = getTimeByFile(file);
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat stringFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date timeDate;
        try {
            timeDate = dataFormat.parse(time);
        } catch (ParseException e) {
            throw new CustomException("swap:", e);
        }
        List<String> timeList = new ArrayList<>();
        int length = tableData.size();
        for (int i = 0; i < length; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(timeDate);
            calendar.add(Calendar.SECOND, i);
            Date newDate = calendar.getTime();
            timeList.add(stringFormat.format(newDate));
        }
        AutoShowDataVO dataVO = assemblyData(inList, outList, timeList);
        analysisDTO.setPointData(dataVO);
        return analysisDTO;
    }

    private String getTimeByFile(MultipartFile file) {
        String time = null;
        if (file != null && !file.isEmpty()) {
            try (var reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                while (reader.ready()) {
                    var line = reader.readLine();
                    if (line.contains("time:")) {
                        time = line.substring(5);
                    }
                }
            } catch (IOException e) {
                throw new CustomException("swap:", e);
            }
        }
        return time;
    }

    private AutoShowDataVO assemblyData(List<String> inList, List<String> outList, List<String> timeList) {
        ChartShowDataYDataVO inChartData = new ChartShowDataYDataVO();
        inChartData.setName(LocaleStringUtils.format("sql.swap.in"));
        inChartData.setData(inList);
        ChartShowDataYDataVO outChartData = new ChartShowDataYDataVO();
        outChartData.setName(LocaleStringUtils.format("sql.swap.out"));
        outChartData.setData(outList);
        List<ChartShowDataYDataVO> series = new ArrayList<>();
        series.add(inChartData);
        series.add(outChartData);
        ChartShowDataVO chartShowDataVO = new ChartShowDataVO();
        chartShowDataVO.setUnit("pages");
        chartShowDataVO.setXData(timeList);
        chartShowDataVO.setSeries(series);
        chartShowDataVO.setDataName("Swap");
        AutoShowDataVO dataVO = new AutoShowDataVO();
        List<ShowData> list = new ArrayList<>();
        list.add(chartShowDataVO);
        dataVO.setData(list);
        return dataVO;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
