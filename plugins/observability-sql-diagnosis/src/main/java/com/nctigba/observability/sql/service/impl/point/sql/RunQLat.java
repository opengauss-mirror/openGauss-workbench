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
 *  RunQLat.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/RunQLat.java
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
import com.nctigba.observability.sql.service.impl.collection.ebpf.RunQlatItem;
import com.nctigba.observability.sql.util.HistogramToHeatmapUtils;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RunQLat
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class RunQLat implements DiagnosisPointService<Object> {
    @Autowired
    private RunQlatItem item;

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
        var heatMap = HistogramToHeatmapUtils.format(file, LocaleStringUtils.format("RunqlatAnaly.unit"));
        var map = heatMap.stream().filter(
                data -> NumberUtils.toInt(data.getName(), 0) == task.getPid()).findFirst().orElse(null);
        heatMap.clear();
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        if (map == null) {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
            return analysisDTO;
        }
        heatMap.add(map);
        map.setName(LocaleStringUtils.format("RunqlatAnaly.name"));
        List<TaskResultDTO> list = new ArrayList<>();
        List<ArrayList<Integer>> source = map.getSource();
        float rate = analysisRate(source);
        if (rate > 5.0f) {
            TaskResultDTO result = new TaskResultDTO(
                    task, TaskResultDTO.ResultState.SUGGESTION, ResultTypeEnum.runqlat, FrameTypeEnum.Suggestion,
                    FrameVO.bearing.top);
            result.setData(Map.of("title", LocaleStringUtils.format("RunqlatAnaly.title"), "suggestions",
                    List.of(LocaleStringUtils.format("RunqlatAnaly.TIP", rate, 5))));
            list.add(result);
        }
        var center = new TaskResultDTO(task, TaskResultDTO.ResultState.NO_ADVICE, ResultTypeEnum.runqlat,
                FrameTypeEnum.HeatMap,
                FrameVO.bearing.center);
        heatMap.forEach(m -> m.setSource(null));
        center.setData(heatMap);
        list.add(center);
        FrameVO f = new FrameVO();
        for (TaskResultDTO taskResultDTO : list) {
            f.addChild(taskResultDTO.getBearing(), taskResultDTO.toFrame());
        }
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
        int r10 = 0;
        int ra = 0;
        for (int i = 0; i < sum.size(); i++) {
            if (i >= 10) {
                r10 += sum.get(i);
            }
            ra += sum.get(i);
        }
        float rate = 0.0f;
        if (ra != 0) {
            BigDecimal improvement = new BigDecimal(r10).divide(
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