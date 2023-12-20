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
 *  AgentUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/util/AgentUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.AgentNodeRelationMapper;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.dto.point.AgentDTO;
import com.nctigba.observability.sql.model.entity.AgentNodeRelationDO;
import com.nctigba.observability.sql.model.entity.NctigbaEnvDO;
import com.nctigba.observability.sql.model.vo.collection.AgentVO;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * AgentUtil
 *
 * @author luomeng
 * @since 2023/6/9
 */
@Component
@Slf4j
public class AgentUtils {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    private AgentNodeRelationMapper agentMapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    private String getAgentUrl(String id) {
        AgentNodeRelationDO relationEntity = agentMapper.selectOne(
                Wrappers.<AgentNodeRelationDO>lambdaQuery().eq(AgentNodeRelationDO::getNodeId, id));
        var env = envMapper.selectOne(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getId, relationEntity.getEnvId()).eq(
                        NctigbaEnvDO::getType,
                        NctigbaEnvDO.envType.EXPORTER));
        if (env == null) {
            throw new HisDiagnosisException("Agent not found");
        }
        var host = hostFacade.getById(env.getHostid());
        return "http://" + host.getPublicIp() + ":" + env.getPort() + "/cmd/";
    }

    public Object rangQuery(String id, String param) {
        String baseUrl = getAgentUrl(id) + param;
        var requestParam = new HashMap<String, Object>();
        requestParam.put("nodeId", id);
        var result = HttpUtil.get(baseUrl, requestParam);
        var data = JSONUtil.parseArray(result);
        JSONArray sysJson = new JSONArray();
        if (data.get(0) instanceof JSONArray) {
            sysJson = (JSONArray) data.get(0);
        }
        List<AgentDTO> sysList = setData((sysJson));
        JSONArray dbJson = new JSONArray();
        if (data.get(1) instanceof JSONArray) {
            dbJson = (JSONArray) data.get(1);
        }
        List<AgentDTO> dbList = setData((dbJson));
        AgentVO agentVO = new AgentVO();
        agentVO.setParamName(param);
        agentVO.setSysValue(sysList);
        agentVO.setDbValue(dbList);
        return agentVO;
    }

    private List<AgentDTO> setData(JSONArray jsonArray) {
        List<HashMap<String, String>> maps = new ArrayList<>();
        for (Object object : jsonArray) {
            HashMap<String, String> hashMap = new HashMap<>();
            JSONObject jsonObject = new JSONObject();
            if (object instanceof JSONObject) {
                jsonObject = (JSONObject) object;
            }
            for (String key : jsonObject.keySet()) {
                Object value = jsonObject.get(key);
                hashMap.put(key, value.toString());
            }
            maps.add(hashMap);
        }
        List<AgentDTO> list = new ArrayList<>();
        for (HashMap<String, String> map : maps) {
            AgentDTO dto = new AgentDTO();
            dto.setRes(map.get("RES"));
            dto.setMem(map.get("%MEM"));
            dto.setPr(map.get("PR"));
            dto.setCpu(map.get("%CPU"));
            dto.setState(map.get("S"));
            dto.setPid(map.get("PID"));
            dto.setCommand(map.get("COMMAND"));
            dto.setNi(map.get("NI"));
            dto.setUser(map.get("USER"));
            dto.setShr(map.get("SHR"));
            dto.setTime(map.get("TIME+"));
            dto.setVirt(map.get("VIRT"));
            list.add(dto);
        }
        return list;
    }
}
