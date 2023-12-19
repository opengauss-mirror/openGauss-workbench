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
 *  RunQLen.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/RunQLen.java
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
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.ebpf.RunQlenItem;
import com.nctigba.observability.sql.util.HistogramToHeatmapUtils;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RunQLen
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class RunQLen implements DiagnosisPointService<Object> {
    @Autowired
    private RunQlenItem item;

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
        var heatMap = HistogramToHeatmapUtils.format(file, LocaleStringUtils.format("RunqlenAnaly.unit"));
        List<ArrayList<Integer>> source = heatMap.get(0).getSource();
        heatMap.get(0).setName(LocaleStringUtils.format("RunqlenAnaly.name"));
        List<TaskResultDTO> list = new ArrayList<>();
        float rate = analysisRate(source);
        if (rate > 5.0f) {
            TaskResultDTO result = new TaskResultDTO(
                    task, TaskResultDTO.ResultState.SUGGESTION, ResultTypeEnum.RUNQLEN,
                    FrameTypeEnum.Suggestion, FrameVO.bearing.top);
            result.setData(Map.of("title", LocaleStringUtils.format("RunqlenAnaly.title"), "suggestions",
                    List.of(LocaleStringUtils.format("RunqlenAnaly.TIP", rate, 5))));
            list.add(result);
        }
        var center = new TaskResultDTO(task, TaskResultDTO.ResultState.NO_ADVICE, ResultTypeEnum.RUNQLEN,
                FrameTypeEnum.HeatMap,
                FrameVO.bearing.center);
        heatMap.forEach(map -> map.setSource(null));
        center.setData(heatMap);
        list.add(center);
        FrameVO f = new FrameVO();
        for (TaskResultDTO taskResultDTO : list) {
            f.addChild(taskResultDTO.getBearing(), taskResultDTO.toFrame());
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        analysisDTO.setPointData(f);
        return analysisDTO;
    }

    private float analysisRate(List<ArrayList<Integer>> source) {
        ArrayList<Integer> sum = new ArrayList<>();
        for (ArrayList<Integer> arrayList : source) {
            for (int i = 0; i < arrayList.size(); i++) {
                if (sum.size() <= i) {
                    sum.add(0);
                }
                sum.set(i, arrayList.get(i) + sum.get(i));
            }
        }
        int r3 = 0;
        int ra = 0;
        for (int i = 0; i < sum.size(); i++) {
            if (i >= 3) {
                r3 += sum.get(i);
            }
            ra += sum.get(i);
        }
        float rate = 0.0f;
        if (ra != 0) {
            BigDecimal improvement = new BigDecimal(r3).divide(
                    BigDecimal.valueOf(ra), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            rate = improvement.floatValue();
        }
        return rate;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}