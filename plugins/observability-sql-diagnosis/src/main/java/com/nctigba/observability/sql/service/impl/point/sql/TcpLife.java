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
 *  TcpLife.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/point/sql/TcpLife.java
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
import com.nctigba.observability.sql.service.impl.collection.ebpf.TcpLifeItem;
import com.nctigba.observability.sql.util.LocaleStringUtils;
import com.nctigba.observability.sql.util.TableFormatterUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TcpLife
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class TcpLife implements DiagnosisPointService<Object> {
    private static final String[] TIP = {"TcpLife.TIP0", "TcpLife.TIP1"};

    @Autowired
    private TcpLifeItem item;

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
        if (!CollectionUtils.isEmpty(list)) {
            FrameVO f = new FrameVO();
            for (TaskResultDTO taskResultDTO : list) {
                f.addChild(taskResultDTO.getBearing(), taskResultDTO.toFrame());
            }
            analysisDTO.setPointData(f);
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.SUGGESTIONS);
        } else {
            analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        }
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        return analysisDTO;
    }

    private List<TaskResultDTO> getResultData(DiagnosisTaskDO task, MultipartFile file) {
        List<TaskResultDTO> list = new ArrayList<>();
        var center = new TaskResultDTO(
                task, TaskResultDTO.ResultState.NO_ADVICE, ResultTypeEnum.TcplifeTCP, FrameTypeEnum.Table,
                FrameVO.bearing.center);
        list.add(center);
        var table = TableFormatterUtils.format(file, "TIME");
        center.setData(table);
        var counter = new HashMap<String, Set<String>>();
        var thisThread = new HashSet<>();
        if (!CollectionUtils.isEmpty(table.getData())) {
            table.getData().forEach(row -> {
                var key = row.get("LADDR") + ":" + row.get("LPORT");
                var raddr = row.get("RADDR");
                if (NumberUtils.toInt(row.get("PID"), 0) == task.getPid()) {
                    thisThread.add(raddr);
                }
                if (!counter.containsKey(key)) {
                    counter.put(key, new HashSet<>());
                }
                counter.get(key).add(raddr);
            });
            var suggestions = new ArrayList<>();
            if (thisThread.size() > 1) {
                suggestions.add(LocaleStringUtils.format(TIP[0], task.getPid(), thisThread.size()));
            }
            counter.forEach((key, set) -> {
                if (set.size() > 1) {
                    suggestions.add(LocaleStringUtils.format(TIP[1], key, set.size()));
                }
            });
            if (suggestions.size() > 0) {
                TaskResultDTO top = new TaskResultDTO(
                        task, TaskResultDTO.ResultState.SUGGESTION, ResultTypeEnum.TcplifeTCP, FrameTypeEnum.Suggestion,
                        FrameVO.bearing.top)
                        .setData(
                                Map.of("title", LocaleStringUtils.format("TcpLife.title"), "suggestions", suggestions));
                list.add(top);
            }
        }
        return list;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}