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
 *  DiskUsageAnalysisServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/alert-monitor/src/main/java/com/nctigba/alert/monitor/service/impl/analysis/DiskUsageAnalysisServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.alert.monitor.service.impl.analysis;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.alert.monitor.constant.CommonConstants;
import com.nctigba.alert.monitor.mapper.NctigbaEnvMapper;
import com.nctigba.alert.monitor.model.dto.AlertRelationDTO;
import com.nctigba.alert.monitor.model.entity.NctigbaEnvDO;
import com.nctigba.alert.monitor.service.AlertAnalysisService;
import com.nctigba.alert.monitor.util.MessageSourceUtils;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * DiskUsageAnalysisServiceImpl
 *
 * @since 2023/12/7 14:53
 */
@Service("diskUsageAnalysisService")
public class DiskUsageAnalysisServiceImpl implements AlertAnalysisService {
    private String promQL = "sum(agent_filesystem_used_size_kbytes{instance=~\"${instances}\"}) by (device) / "
        + "sum(agent_filesystem_size_kbytes{instance=~\"${instances}\"}) by (device) * 100";

    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    /**
     * getRelationData
     *
     * @param clusterNodeId String
     * @param limitValue String
     * @return List<AlertRelationDTO>
     */
    @Override
    public List<AlertRelationDTO> getRelationData(String clusterNodeId, String limitValue) {
        AlertRelationDTO relationDto = new AlertRelationDTO();
        List<AlertRelationDTO> list = new ArrayList<>();
        list.add(relationDto);

        JSONArray result = getPromData(clusterNodeId);
        if (CollectionUtil.isEmpty(result)) {
            return list;
        }

        List<Map<String, Object>> tables = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            JSONObject item = result.getJSONObject(i);
            String device = item.getJSONObject("metric").getStr("device");
            Map<String, Object> table = new HashMap<>();
            table.put("filesystem", device);
            String value = item.getJSONArray("value").getStr(1);
            if (StrUtil.isBlank(value) || value.equals("NaN")) {
                table.put("diskUsage", "");
                continue;
            }
            table.put("diskUsage", new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP));
            tables.add(table);
        }
        relationDto.setTables(tables);
        relationDto.setName(MessageSourceUtils.get("diskUsage"));
        List<String> ths = Arrays.asList("filesystem", "diskUsage");
        relationDto.setTableThs(ths);
        Map<String, String> thMap = ths.stream().collect(
            Collectors.toMap(item -> item, item -> MessageSourceUtils.get("alertRecord." + item)));
        relationDto.setTableThMap(thMap);
        return list;
    }

    private JSONArray getPromData(String clusterNodeId) {
        NctigbaEnvDO promEnv = envMapper.selectOne(
            Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, NctigbaEnvDO.Type.PROMETHEUS_MAIN));
        if (promEnv == null) {
            return new JSONArray();
        }
        String hostid = promEnv.getHostid();
        OpsHostEntity hostEntity = hostFacade.getById(hostid);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("query", promQL.replaceAll(Pattern.quote("${instances}"), clusterNodeId));
        BigDecimal decimal = BigDecimal.valueOf(System.currentTimeMillis()).divide(new BigDecimal(1000))
            .setScale(3, BigDecimal.ROUND_HALF_UP);
        paramMap.put("time", decimal);
        String httpUrl = "http://" + CommonConstants.LOCAL_IP + ":" + promEnv.getPort() + "/api/v1/query";
        String response = HttpUtil.get(httpUrl, paramMap);
        if (StrUtil.isBlank(response)) {
            return new JSONArray();
        }
        JSONObject resJson = new JSONObject(response);
        if (StrUtil.isBlank(resJson.getStr("status")) || !"success".equals(resJson.getStr("status"))
            || CollectionUtil.isEmpty(resJson.getJSONObject("data"))) {
            return new JSONArray();
        }
        JSONArray result = resJson.getJSONObject("data").getJSONArray("result");
        if (CollectionUtil.isEmpty(result)) {
            return new JSONArray();
        }
        return result;
    }
}
