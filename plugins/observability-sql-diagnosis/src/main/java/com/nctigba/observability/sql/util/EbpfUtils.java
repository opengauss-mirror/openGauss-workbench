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
 *  EbpfUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/util/EbpfUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.util;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.sql.exception.HisDiagnosisException;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.entity.NctigbaEnvDO;
import com.nctigba.observability.sql.model.entity.DiagnosisTaskDO;
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
public class EbpfUtils {
    @Autowired
    private NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;

    private String getAgentUrl(String nodeId) {
        var env = envMapper.selectOne(
                Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getNodeid, nodeId).eq(
                        NctigbaEnvDO::getType,
                        NctigbaEnvDO.envType.AGENT));
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
    public String callMonitor(DiagnosisTaskDO task, String param) {
        var url = getAgentUrl(task.getNodeId());
        // call post
        String postForObject = HttpUtil.post(
                url,
                Map.of("tid", task.getPid(), "taskId", task.getId(), "monitorType", param));
        task.addRemarks("bccResult for :" + param + ":" + (postForObject == null ? "" : postForObject));
        return "success";
    }
}
