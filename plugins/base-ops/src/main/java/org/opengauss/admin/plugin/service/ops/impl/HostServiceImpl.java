/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * HostServiceImpl.java
 *
 * IDENTIFICATION
 * base-ops/src/main/java/org/opengauss/admin/plugin/service/ops/impl/HostServiceImpl.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.enums.ops.DiskQueryMethodEnum;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.domain.model.ops.LunPathManager;
import org.opengauss.admin.plugin.domain.model.ops.SshCommandConstants;
import org.opengauss.admin.plugin.service.ops.IHostService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author lhf
 * @date 2023/2/14 14:28
 **/
@Slf4j
@Service
public class HostServiceImpl implements IHostService {
    @Autowired
    private JschUtil jschUtil;

    @Resource
    private OpsHostRemoteService opsHostRemoteService;

    @Override
    public boolean pathEmpty(String id, String path, String rootPassword) {
        Session rootSession = opsHostRemoteService.createRootSession(id, rootPassword);
        try {
            return pathEmpty(rootSession,path);
        }finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()){
                rootSession.disconnect();
            }
        }
    }

    private boolean pathEmpty(Session rootSession, String path) {
        String command = "ls " + path + " | wc -l";
        try {
            String pathEmpty = opsHostRemoteService.executeCommand(command, rootSession, "path empty");
            return StrUtil.equals("0", pathEmpty) || pathEmpty.contains("No such file or directory");
        } catch (Exception e) {
            log.error("Failed to probe data directory",e);
            throw new OpsException("Failed to probe data directory");
        }
    }

    @Override
    public boolean portUsed(String id, Integer port, String rootPassword) {
        Session rootSession = opsHostRemoteService.createRootSession(id, rootPassword);
        try {
            return portUsed(rootSession,port);
        }finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()){
                rootSession.disconnect();
            }
        }
    }

    @Override
    public boolean fileExist(String id, String file, String rootPassword) {
        Session rootSession = opsHostRemoteService.createRootSession(id, rootPassword);
        try {
            return fileExist(rootSession,file);
        } catch (OpsException e) {
            log.error("Failed to find file",e);
            return false;
        } finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()){
                rootSession.disconnect();
            }
        }
    }

    private List<String> nvmeLunQuery(Session rootSession, String command) {
        List<String> res = new ArrayList<>();
        try {
            JschResult result = jschUtil.executeCommand(command, rootSession);
            if (result.getExitCode() != 0 || result.getResult().contains("command not found")) {
                log.warn(result.getResult());
                return res;
            }

            String[] parts = result.getResult().split(System.lineSeparator());
            res = Arrays.asList(parts);
            return res;
        } catch (Exception e) {
            log.warn("Can't execute command ", e);
        }
        return res;
    }

    private List<String> nvmeLunQuery(Session rootSession) {
        List<String> res = nvmeLunQuery(rootSession, SshCommandConstants.UPADMIN);
        if (!res.isEmpty()) {
            LunPathManager.lunQueryMethod = DiskQueryMethodEnum.UPADMIN;
            return res;
        }
        res = nvmeLunQuery(rootSession, SshCommandConstants.UPADMIN_PLUS);
        if (!res.isEmpty()) {
            LunPathManager.lunQueryMethod = DiskQueryMethodEnum.UPADMIN_PLUS;
            return res;

        }
        return res;
    }

    private List<String> scsiLunQuery(Session rootSession) {
        List<String> res = new ArrayList<>();
        try {
            JschResult result = jschUtil.executeCommand(SshCommandConstants.LS_SCSI, rootSession);
            if (result.getExitCode() != 0 || result.getResult().contains("command not found")) {
                log.error(result.getResult());
                return res;
            }
            String[] parts = result.getResult().split(System.lineSeparator());
            for (String part : parts) {
                if (part.split(" ")[1].length() == LunPathManager.WWN_LEN_IN_HEX + 1) {
                    res.add(part);
                }
            }
            return res;
        } catch (Exception e) {
            log.error("Can't execute command ", e);
            return res;
        }
    }

    @Override
    public List<String> scsiLunQuery(String id, String rootPassword) {
        Session rootSession = opsHostRemoteService.createRootSession(id, rootPassword);
        try {
            return scsiLunQuery(rootSession);
        } finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()){
                rootSession.disconnect();
            }
        }
    }

    @Override
    public List<String> nvmeLunQuery(String id, String rootPassword) {
        Session rootSession = opsHostRemoteService.createRootSession(id, rootPassword);
        try {
            return nvmeLunQuery(rootSession);
        } finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()){
                rootSession.disconnect();
            }
        }
    }

    @Override
    public List<String> multiPathQuery(String id, String rootPassword) {
        try {
            List<String> res = nvmeLunQuery(id, rootPassword);
            if (!res.isEmpty()) {
                return res;
            } else {
                res = scsiLunQuery(id, rootPassword);
                if (!res.isEmpty()) {
                    LunPathManager.lunQueryMethod = DiskQueryMethodEnum.SCSI;
                    return res;
                }
            }
            log.error("Can't obtain the disk info by {}, {} and {} ", SshCommandConstants.UPADMIN,
                    SshCommandConstants.UPADMIN_PLUS, SshCommandConstants.LS_SCSI);
            throw new OpsException("Failed to obtain the disk info");
        } catch (Exception e) {
            log.error("Can't execute command ", e);
            throw new OpsException("Failed to obtain the disk info");
        }
    }

    private boolean fileExist(Session rootSession, String file) {
        String command = "[ -f '"+ file +"' ]";
        try {
            jschUtil.executeCommand(command, rootSession);
            return true;
        } catch (Exception e) {
            log.error("Failed to find file",e);
            throw new OpsException("Failed to find file");
        }
    }

    /**
     * @param rootSession root user session
     * @param port port number
     * @return boolean is port used
     */
    private boolean portUsed(Session rootSession, Integer port) {
        ArrayList<String> dependencies = new ArrayList<>();
        dependencies.add("lsof");

        ArrayList<String> missingDependencies = jschUtil.checkDependencies(rootSession, dependencies);
        jschUtil.installDependencies(rootSession, missingDependencies);

        String command = "lsof -i:"+port;
        try {
            String checkPortUsed = opsHostRemoteService.executeCommandThenReturnEmpty(command, rootSession,
                    null, "check port used");
            return StrUtil.isNotEmpty(checkPortUsed);
        } catch (Exception e) {
            log.error("Failed to probe port", e.getMessage());
            return false;
        }
    }
}
