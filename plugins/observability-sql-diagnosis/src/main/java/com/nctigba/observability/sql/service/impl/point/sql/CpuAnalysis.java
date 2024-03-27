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
 *  CpuAnalysis.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/CpuAnalysis.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.enums.FrameTypeEnum;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.enums.ResultTypeEnum;
import com.nctigba.observability.sql.model.dto.TaskResultDTO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.FrameVO;
import com.nctigba.observability.sql.model.vo.LineChartVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.ebpf.MpStatItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PercentUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CpuAnalysis
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class CpuAnalysis implements DiagnosisPointService<Object> {
    private static final String[] KEYS = {"CPU", "%usr", "%nice", "%sys", "%iowait", "%irq", "%soft", "%steal",
            "%guest", "%gnice", "%idle"};

    private static final int[] INDEX = {1, 3, 4, 5, 6, 10};

    @Autowired
    private MpStatItem item;

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
        List<TaskResultDTO> list = getResultData(task, file);
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        if (CollectionUtils.isEmpty(list)) {
            return analysisDTO;
        }
        FrameVO f = new FrameVO();
        for (TaskResultDTO taskResultDTO : list) {
            f.addChild(taskResultDTO.getBearing(), taskResultDTO.toFrame());
        }
        analysisDTO.setPointData(f);
        analysisDTO.setPointType(DiagnosisResultDO.PointType.CENTER);
        return analysisDTO;
    }

    private List<TaskResultDTO> getResultData(DiagnosisTaskDO task, MultipartFile file) {
        try {
            LineChartVO all = new LineChartVO().setTitle(LocaleStringUtils.format("MpstatP.title"));
            LineChartVO idle = new LineChartVO().setTitle(LocaleStringUtils.format("MpstatP.idle"));
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
                    var data = line.substring(11).trim().split("\\s+");
                    if (data[0].equals("CPU")) {
                        continue;
                    }
                    var time = line.substring(0, 11);
                    if (data[0].equals("all")) {
                        all.addX(time);
                        for (int i : INDEX) {
                            all.addPoint(KEYS[i], PercentUtils.parse(data[i]));
                        }
                        idle.addX(time);
                        idle.addPoint(KEYS[10], PercentUtils.parse(data[10]));
                    }
                }
            }
            all.parse();
            idle.parse();
            TaskResultDTO result = new TaskResultDTO(task, TaskResultDTO.ResultState.NO_ADVICE, ResultTypeEnum.CPU,
                    FrameTypeEnum.LineChart,
                    FrameVO.bearing.center);
            result.setData(all);
            List<TaskResultDTO> list = new ArrayList<>();
            list.add(result);
            return list;
        } catch (IOException e) {
            throw new CustomException("MpstatP", e);
        }
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}
