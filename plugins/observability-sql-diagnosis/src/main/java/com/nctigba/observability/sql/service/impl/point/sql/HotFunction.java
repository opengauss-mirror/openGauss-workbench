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
 *  HotFunction.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/HotFunction.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.enums.FrameTypeEnum;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.enums.ResultTypeEnum;
import com.nctigba.observability.sql.model.dto.TaskResultDTO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.point.FunctionTableDTO;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.vo.FrameVO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.ebpf.ProfileItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PointUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HotFunction
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class HotFunction implements DiagnosisPointService<Object> {
    @Autowired
    private ProfileItem item;
    @Autowired
    private PointUtils utils;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionEnum.IS_BCC));
        option.add(String.valueOf(OptionEnum.IS_ON_CPU));
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
        FunctionTableDTO table = utils.fileToTable(file);
        List<TaskResultDTO> list = getResultData(task, table);
        FrameVO f = new FrameVO();
        for (TaskResultDTO taskResultDTO : list) {
            f.addChild(taskResultDTO.getBearing(), taskResultDTO.toFrame());
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DISPLAY);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        analysisDTO.setPointData(f);
        return analysisDTO;
    }

    private List<TaskResultDTO> getResultData(DiagnosisTaskDO task, FunctionTableDTO table) {
        List<TaskResultDTO> list = new ArrayList<>();
        if (table.getData().size() > 1) {
            table.getData().sort((o1, o2) -> {
                Integer samples1 = Integer.valueOf(o1.get(CommonConstants.SAMPLES));
                Integer samples2 = Integer.valueOf(o2.get(CommonConstants.SAMPLES));
                return samples2.compareTo(samples1);
            });
            var it = table.getData().iterator();
            int count = 1;
            while (it.hasNext()) {
                it.next();
                if (count > 50) {
                    it.remove();
                }
                count++;
            }
            var center = new TaskResultDTO(
                    task, TaskResultDTO.ResultState.NO_ADVICE, ResultTypeEnum.HotFunction, FrameTypeEnum.Table,
                    FrameVO.bearing.center);
            center.setData(table);
            TaskResultDTO taskResultDTO = new TaskResultDTO();
            taskResultDTO.setTaskId(task.getId());
            taskResultDTO.setResultType(ResultTypeEnum.HotFunction);
            taskResultDTO.setFrameType(FrameTypeEnum.Suggestion);
            taskResultDTO.setState(TaskResultDTO.ResultState.SUGGESTION);
            taskResultDTO.setBearing(FrameVO.bearing.top);
            taskResultDTO.setData(
                    Map.of(CommonConstants.TITLE, LocaleStringUtils.format("HotFunction.title"), "suggestions",
                            LocaleStringUtils.format("HotFunction.name")));
            list.add(center);
        }
        return list;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}