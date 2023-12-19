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
 *  TransactionAnalysisServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/analysis/TransactionAnalysisServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl.analysis;

import cn.hutool.core.util.StrUtil;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TransactionAnalysisServiceImpl
 *
 * @since 2023/12/6 11:03
 */
@Service("transactionAnalysisService")
public class TransactionAnalysisServiceImpl implements AlertAnalysisService {
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
        List<Map<String, Object>> tranCostTimeList = alertAnalysisMapper.getTransactionCostTimeList(limitValue);
        Map<Object, Map<String, Object>> tranCostTimeMap = tranCostTimeList.stream().collect(
            Collectors.toMap(item -> item.get("sessionid"), Function.identity()));
        tranCostTimeList.stream()
            .filter(data -> data.get("psessionid") != null)
            .collect(Collectors.groupingBy(data -> (int) data.get("psessionid")))
            .forEach((parentId, children) -> {
                Map<String, Object> parent = tranCostTimeMap.get(parentId);
                parent.put("children", children);
            });
        tranCostTimeList = tranCostTimeMap.values().stream()
            .filter(item -> StrUtil.isBlank(item.get("psessionid").toString())).collect(Collectors.toList());
        AlertRelationDTO relationDto = new AlertRelationDTO();
        relationDto.setName(MessageSourceUtils.get("alertRecord.transactionCostTime"));
        List<String> ths = Arrays.asList("sessionid", "sql", "start_time", "is_block", "block_sessionid");
        relationDto.setTableThs(ths);
        relationDto.setTables(tranCostTimeList);
        Map<String, String> thMap = ths.stream().collect(
            Collectors.toMap(item -> item, item -> MessageSourceUtils.get("alertRecord." + item)));
        relationDto.setTableThMap(thMap);
        List<AlertRelationDTO> list = new ArrayList<>();
        list.add(relationDto);
        return list;
    }
}
