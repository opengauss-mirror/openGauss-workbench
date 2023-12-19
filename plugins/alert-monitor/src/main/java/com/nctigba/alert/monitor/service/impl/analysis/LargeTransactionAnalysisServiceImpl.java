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
 *  LargeTransactionAnalysisServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/analysis/LargeTransactionAnalysisServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl.analysis;

import com.nctigba.alert.monitor.aspectj.annotation.Ds;
import com.nctigba.alert.monitor.mapper.AlertAnalysisMapper;
import com.nctigba.alert.monitor.model.dto.AlertRelationDTO;
import com.nctigba.alert.monitor.service.AlertAnalysisService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * large transaction analysis
 *
 * @since 2023/12/11 14:36
 */
@Service("largeTransactionAnalysisService")
public class LargeTransactionAnalysisServiceImpl implements AlertAnalysisService {
    @Autowired
    private AlertAnalysisMapper alertAnalysisMapper;

    /**
     * getRelationData
     *
     * @param clusterNodeId String
     * @param limitValue String
     * @return List<AlertRelationDTO>
     */
    @Override
    @Ds
    public List<AlertRelationDTO> getRelationData(String clusterNodeId, String limitValue) {
        List<Map<String, Object>> largeTransactionList = alertAnalysisMapper.largeTransaction();
        AlertRelationDTO relationDto = new AlertRelationDTO();
        relationDto.setName(MessageSourceUtils.get("alertRecord.largeTransaction"));
        List<String> ths = Arrays.asList("sessionid", "start_time", "used_mem_size", "sql");
        relationDto.setTableThs(ths);
        relationDto.setTables(largeTransactionList);
        Map<String, String> thMap = ths.stream().collect(
            Collectors.toMap(item -> item, item -> MessageSourceUtils.get("alertRecord." + item)));
        relationDto.setTableThMap(thMap);
        List<AlertRelationDTO> list = new ArrayList<>();
        list.add(relationDto);
        return list;
    }
}
