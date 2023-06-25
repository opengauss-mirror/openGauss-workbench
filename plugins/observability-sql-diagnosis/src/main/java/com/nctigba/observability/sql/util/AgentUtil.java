/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.model.history.data.AgentData;
import com.nctigba.observability.sql.model.history.dto.AgentDTO;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    protected NctigbaEnvMapper envMapper;

    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    protected HostFacade hostFacade;

    private String getAgentUrl(String id) {
        var env = envMapper.selectOne(
                Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getNodeid, id).eq(NctigbaEnv::getType,
                        NctigbaEnv.envType.EXPORTER));
        if (env == null) {
            throw new HisDiagnosisException("Agent not found");
        }
        var host = hostFacade.getById(env.getHostid());
        return "http://" + host.getPublicIp() + ":" + env.getPort() + "/cmd/";
    }

    public Object rangQuery(String id, String param) {
        List<Object> dataList;
        try {
            final RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(getAgentUrl(id) + param, String.class);
            String responseData = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            dataList = objectMapper.readValue(responseData, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            return "error:" + e.getMessage();
        }
        List<HashMap<String, String>> sysMaps = (List<HashMap<String, String>>) dataList.get(0);
        List<AgentDTO> sysList = setData(sysMaps);
        List<HashMap<String, String>> dbMaps = (List<HashMap<String, String>>) dataList.get(1);
        List<AgentDTO> dbList = setData(dbMaps);
        AgentData agentData = new AgentData();
        agentData.setParamName(param);
        agentData.setSysValue(sysList);
        agentData.setDbValue(dbList);
        return agentData;
    }

    private List<AgentDTO> setData(List<HashMap<String, String>> maps) {
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
