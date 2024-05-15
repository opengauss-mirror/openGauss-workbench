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
 *  ClusterSwitchServiceImpl.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/impl
 * /ClusterSwitchServiceImpl.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.nctigba.observability.instance.exception.InstanceException;
import com.nctigba.observability.instance.mapper.ClusterSwitchoverLogReadMapper;
import com.nctigba.observability.instance.mapper.ClusterSwitchoverRecordMapper;
import com.nctigba.observability.instance.model.entity.ClusterSwitchoverLogReadDO;
import com.nctigba.observability.instance.model.entity.ClusterSwitchoverRecordDO;
import com.nctigba.observability.instance.service.ClusterManager;
import com.nctigba.observability.instance.service.ClusterSwitchService;
import com.nctigba.observability.instance.util.SshSessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * ClusterSwitchServiceImpl
 *
 * @author liupengfei
 * @since 2024/5/15
 */
@Service
@Slf4j
public class ClusterSwitchServiceImpl implements ClusterSwitchService {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostUserFacade hostUserFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Autowired
    private ClusterSwitchoverLogReadMapper switchoverLogReadMapper;
    @Autowired
    private ClusterSwitchoverRecordMapper switchoverRecordMapper;
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    @DS("embedded")
    public void recordUpdate(String clusterId, OpsClusterNodeVO clusterNode, ClusterSwitchoverLogReadDO logRead,
                             String envPath, boolean resetLogLastRead) {
        String cmdPattern = "source {0} && awk_command1=''NR>{1} && /(failover|switchover) completed/ "
                + "'{'print FILENAME \">>>\" NR \">>>\" $0'}''';awk_command2=''NR>0 && /(failover|switchover) completed"
                + "/ '{'print FILENAME \">>>\" NR \">>>\" $0'}'''; if [ -f \"$'{'GAUSSLOG'}'/bin/gs_ctl/{2}\" ]; "
                + "then awk \"$awk_command1\" $'{'GAUSSLOG'}'/bin/gs_ctl/{2}; "
                + "else file=$(ls $'{'GAUSSLOG'}'/bin/gs_ctl/gs_ctl-*-current.log);awk \"$awk_command2\" $file; fi";
        if (logRead == null) {
            ClusterSwitchoverLogReadDO initLogRead = new ClusterSwitchoverLogReadDO();
            initLogRead.setClusterId(clusterId);
            initLogRead.setClusterNodeId(clusterNode.getNodeId());
            initLogRead.setLogName("gs_ctl-*-current.log");
            initLogRead.setLogLastRead(0L);
            switchoverLogReadMapper.insert(initLogRead);
            logRead = initLogRead;
        }
        String cmd = MessageFormat.format(cmdPattern, envPath, resetLogLastRead? "0" :
                        logRead.getLogLastRead().toString(), logRead.getLogName());
        SshSessionUtils sshSession = null;
        try {
            sshSession = getSshSession(clusterNode);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (sshSession == null) {
            return;
        }
        String execRes = null;
        try {
            execRes = sshSession.execute(cmd);
        } catch (IOException | CustomException e) {
            log.error(e.getMessage(), e);
        }
        if (execRes == null || execRes.isEmpty()) {
            return;
        }
        String[] logs = execRes.split(StrUtil.LF);
        for (String log : logs) {
            String[] logCol = log.split(">>>");
            ClusterSwitchoverRecordDO record = new ClusterSwitchoverRecordDO();
            record.setClusterId(clusterId);
            record.setPrimaryIp(clusterNode.getPublicIp());
            record.setSwitchoverTime(ClusterSwitchoverRecordDO.getLogTime(logCol[2]));
            record.setSwitchoverReason(ClusterSwitchoverRecordDO.getSwitchoverReason(logCol[2]));
            record.setClusterNodeId(clusterNode.getNodeId());
            switchoverRecordMapper.insert(record);
        }
        logRead.updateAfterReadLog(logs);
        switchoverLogReadMapper.updateById(logRead);
    }

    private SshSessionUtils getSshSession(OpsClusterNodeVO clusterNode) throws IOException, ExecutionException,
            InterruptedException, TimeoutException {
        OpsHostEntity hostEntity = hostFacade.getById(clusterNode.getHostId());
        OpsHostUserEntity userEntity = hostUserFacade.listHostUserByHostId(clusterNode.getHostId()).stream()
                .filter(p -> p.getUsername().equals(clusterNode.getInstallUserName())).findFirst().orElseThrow(
                        () -> new InstanceException("The node information corresponding to the host is not found"));
        return SshSessionUtils.getSessionWithTimeout(hostEntity.getPublicIp(), hostEntity.getPort(), userEntity.getUsername(),
                encryptionUtils.decrypt(userEntity.getPassword()), 5000);
    }
}
