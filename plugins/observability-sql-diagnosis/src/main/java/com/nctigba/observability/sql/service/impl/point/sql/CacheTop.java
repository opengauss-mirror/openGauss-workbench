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
 *  CacheTop.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/CacheTop.java
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
import com.nctigba.observability.sql.service.impl.collection.ebpf.CacheTopItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.PercentUtils;
import com.nctigba.observability.sql.util.TableFormatterUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CacheTop
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class CacheTop implements DiagnosisPointService<Object> {
    @Autowired
    private CacheTopItem item;

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
        List<TaskResultDTO> list = new ArrayList<>();
        var center = new TaskResultDTO(
                task, TaskResultDTO.ResultState.NO_ADVICE, ResultTypeEnum.CACHETOP, FrameTypeEnum.Table,
                FrameVO.bearing.center);
        list.add(center);
        var table = TableFormatterUtils.format(file, "PID");
        center.setData(table);
        if (!CollectionUtils.isEmpty(table.getData())) {
            table.getData().stream().anyMatch(row -> {
                if (NumberUtils.toInt(row.get("PID"), 0) == task.getPid()) {
                    var readHit = PercentUtils.parse(row.get("READ_HIT"), 1);
                    var writeHit = PercentUtils.parse(row.get("WRITE_HIT"), 1);
                    if (readHit < 1 || writeHit < 1) {
                        var top = new TaskResultDTO(task, TaskResultDTO.ResultState.SUGGESTION, ResultTypeEnum.CACHETOP,
                                FrameTypeEnum.Suggestion, FrameVO.bearing.top)
                                .setData(Map.of("title", LocaleStringUtils.format("Cachetop.title"), "suggestions",
                                        List.of(LocaleStringUtils.format(
                                                "Cachetop.TIP", task.getPid(), readHit, writeHit))));
                        list.add(top);
                    }
                    return true;
                }
                return false;
            });
        }
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