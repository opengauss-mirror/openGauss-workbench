/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.model.history.data.AgentData;
import com.nctigba.observability.sql.model.history.dto.AgentDTO;
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
public class AgentUtil {
    @Autowired
    private NctigbaEnvMapper envMapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    private String getAgentUrl(String id) {
        var env = envMapper.selectOne(
                Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getNodeid, id).eq(
                        NctigbaEnv::getType,
                        NctigbaEnv.envType.EXPORTER));
        if (env == null) {
            throw new HisDiagnosisException("Agent not found");
        }
        var host = hostFacade.getById(env.getHostid());
        return "http://" + host.getPublicIp() + ":" + env.getPort() + "/cmd/";
    }

    public Object rangQuery(String id, String param) {
        String baseUrl = getAgentUrl(id) + param;
        var result = HttpUtil.get(baseUrl, new HashMap<>());
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
        AgentData agentData = new AgentData();
        agentData.setParamName(param);
        agentData.setSysValue(sysList);
        agentData.setDbValue(dbList);
        return agentData;
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
