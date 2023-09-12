/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.service.history.point.sql;

import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.constants.CommonConstants;
import com.nctigba.observability.sql.constants.history.OptionCommon;
import com.nctigba.observability.sql.mapper.DiagnosisResourceMapper;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;
import com.nctigba.observability.sql.model.diagnosis.result.Frame;
import com.nctigba.observability.sql.model.diagnosis.result.FrameType;
import com.nctigba.observability.sql.model.diagnosis.result.ResultType;
import com.nctigba.observability.sql.model.history.HisDiagnosisResult;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import com.nctigba.observability.sql.model.history.dto.AnalysisDTO;
import com.nctigba.observability.sql.model.history.result.Resource;
import com.nctigba.observability.sql.model.history.result.TaskResult;
import com.nctigba.observability.sql.service.history.DataStoreService;
import com.nctigba.observability.sql.service.history.HisDiagnosisPointService;
import com.nctigba.observability.sql.service.history.collection.CollectionItem;
import com.nctigba.observability.sql.service.history.collection.ebpf.OffCpuItem;
import com.nctigba.observability.sql.util.LocaleString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OffCpu
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class OffCpu implements HisDiagnosisPointService<Object> {
    @Autowired
    private OffCpuItem item;
    @Autowired
    private DiagnosisResourceMapper resourceMapper;

    @Override
    public List<String> getOption() {
        List<String> option = new ArrayList<>();
        option.add(String.valueOf(OptionCommon.IS_BCC));
        return option;
    }

    @Override
    public List<CollectionItem<?>> getSourceDataKeys() {
        List<CollectionItem<?>> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    @Override
    public AnalysisDTO analysis(HisDiagnosisTask task, DataStoreService dataStoreService) {
        Object obj = dataStoreService.getData(item).getCollectionData();
        MultipartFile file = null;
        if (obj instanceof MultipartFile) {
            file = (MultipartFile) obj;
        }
        Resource resource;
        try {
            resource = new Resource(task, GrabType.offcputime).setF(file.getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new HisDiagnosisException("error:", e);
        }
        resourceMapper.insert(resource);
        TaskResult resultSvg = new TaskResult(task,
                TaskResult.ResultState.SUGGESTION, ResultType.OffCpu,
                FrameType.Flamefigure, Frame.bearing.top).setData(
                Map.of(CommonConstants.TITLE, LocaleString.format("OffCpuAnaly.title"), "id", resource.getId()));
        List<TaskResult> list = new ArrayList<>();
        list.add(resultSvg);
        Frame f = new Frame();
        for (TaskResult taskResult : list) {
            f.addChild(taskResult.getBearing(), taskResult.toFrame());
        }
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(HisDiagnosisResult.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(HisDiagnosisResult.ResultState.SUGGESTIONS);
        analysisDTO.setPointData(f);
        return analysisDTO;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}