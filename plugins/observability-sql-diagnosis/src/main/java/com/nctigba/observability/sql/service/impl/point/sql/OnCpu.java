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
 *  OnCpu.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/OnCpu.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constant.CommonConstants;
import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.enums.GrabTypeEnum;
import com.nctigba.observability.sql.model.entity.ResourceDO;
import com.nctigba.observability.sql.model.vo.FrameVO;
import com.nctigba.observability.sql.enums.FrameTypeEnum;
import com.nctigba.observability.sql.enums.ResultTypeEnum;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.model.dto.TaskResultDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import com.nctigba.observability.sql.service.impl.collection.ebpf.ProfileItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OnCpu
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class OnCpu implements DiagnosisPointService<Object> {
    @Autowired
    private ProfileItem item;
    @Autowired
    private DiagnosisResourceMapper resourceMapper;

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
        ResourceDO resourceDO;
        try {
            resourceDO = new ResourceDO(task, GrabTypeEnum.profile).setF(file.getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new HisDiagnosisException("error:", e);
        }
        resourceMapper.insert(resourceDO);
        int resourceId = resourceDO.getId() == null ? -1 : resourceDO.getId();
        TaskResultDTO resultSvg = new TaskResultDTO(task,
                TaskResultDTO.ResultState.SUGGESTION, ResultTypeEnum.OnCpu,
                FrameTypeEnum.Flamefigure, FrameVO.bearing.top).setData(
                Map.of(CommonConstants.TITLE, LocaleStringUtils.format("OnCpuAnaly.title"), "id", resourceId));
        List<TaskResultDTO> list = new ArrayList<>();
        list.add(resultSvg);
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

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}