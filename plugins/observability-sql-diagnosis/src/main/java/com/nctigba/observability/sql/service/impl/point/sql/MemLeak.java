/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.nctigba.observability.sql.service.impl.point.sql;

import com.nctigba.observability.sql.enums.OptionEnum;
import com.nctigba.observability.sql.model.entity.DiagnosisResultDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
import com.nctigba.observability.sql.model.dto.point.AnalysisDTO;
import com.nctigba.observability.sql.service.CollectionItem;
import com.nctigba.observability.sql.service.impl.collection.ebpf.MemLeakItem;
import com.nctigba.observability.sql.service.DataStoreService;
import com.nctigba.observability.sql.service.DiagnosisPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * MemLeak
 *
 * @author luomeng
 * @since 2023/7/28
 */
@Service
public class MemLeak implements DiagnosisPointService<Object> {
    @Autowired
    private MemLeakItem item;

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
        AnalysisDTO analysisDTO = new AnalysisDTO();
        analysisDTO.setPointType(DiagnosisResultDO.PointType.DIAGNOSIS);
        analysisDTO.setIsHint(DiagnosisResultDO.ResultState.NO_ADVICE);
        analysisDTO.setPointData("no data");
        return analysisDTO;
    }

    @Override
    public Object getShowData(int taskId) {
        return null;
    }
}