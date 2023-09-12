/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.util;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.common.web.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.NctigbaEnv;
import com.nctigba.observability.sql.model.history.HisDiagnosisTask;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * EbpfUtil
 *
 * @author luomeng
 * @since 2023/7/31
 */
@Component
@Slf4j
public class EbpfUtil {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    private String getAgentUrl(String nodeId) {
        var env = envMapper.selectOne(
                Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getNodeid, nodeId).eq(
                        NctigbaEnv::getType,
                        NctigbaEnv.envType.AGENT));
        if (env == null) {
            throw new HisDiagnosisException("Agent not found");
        }
        var host = hostFacade.getById(env.getHostid());
        return "http://" + host.getPublicIp() + ":" + env.getPort() + "/ebpf/v1/ebpfMonitor";
    }

    /**
     * Call agent
     *
     * @param task task info
     * @param param collect param
     * @return success
     */
    public String callMonitor(HisDiagnosisTask task, String param) {
        var url = getAgentUrl(task.getNodeId());
        // call post
        String postForObject = HttpUtil.post(
                url,
                Map.of("tid", task.getPid(), "taskid", task.getId(), "monitorType", param));
        task.addRemarks("bccResult for :" + param + ":" + (postForObject == null ? "" : postForObject));
        return "success";
    }
}
