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
 *  UsrCpu.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/UsrCpu.java
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
import com.nctigba.observability.sql.service.impl.collection.ebpf.PidStatItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PercentUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * UsrCpu
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class UsrCpu implements DiagnosisPointService<Object> {
    @Autowired
    private PidStatItem item;

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
        if (file != null) {
            TaskResultDTO usrResult = new TaskResultDTO(
                    task, TaskResultDTO.ResultState.NO_ADVICE, ResultTypeEnum.user, FrameTypeEnum.LineChart,
                    FrameVO.bearing.center);
            LineChartVO lineChartVO = generateLineChart(file);
            usrResult.setData(lineChartVO);
            List<TaskResultDTO> list = new ArrayList<>();
            list.add(usrResult);
            FrameVO f = new FrameVO();
            for (TaskResultDTO taskResultDTO : list) {
                f.addChild(taskResultDTO.getBearing(), taskResultDTO.toFrame());
            }
            if (lineChartVO.getSeries().size() > 0) {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
            } else {
                analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            }
            analysisDTO.setPointData(f);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    private LineChartVO generateLineChart(MultipartFile file) {
        LineChartVO usr = new LineChartVO().setTitle(LocaleStringUtils.format("Pidstat1.usr"));
        try (var reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            while (reader.ready()) {
                var line = reader.readLine();
                if (line.isBlank()) {
                    continue;
                }
                if (line.startsWith("Average") && line.length() > 8) {
                    var data = line.substring(8).trim().split("\\s+");
                    if (data.length > 8 && NumberUtils.toDouble(data[2]) < 1) {
                        usr.delLine(data[8]);
                    }
                    continue;
                }
                if (line.length() < 12) {
                    continue;
                }
                var data = line.substring(11).trim().split("\\s+");
                if (data.length < 9 || data[8].equals("Command")) {
                    continue;
                }
                var time = line.substring(0, 11);
                usr.addX(time);
                usr.addPoint(data[8], PercentUtils.parse(data[2]));
            }
        } catch (IOException e) {
            throw new CustomException("Pidstat1", e);
        }
        usr.parse();
        return usr;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}