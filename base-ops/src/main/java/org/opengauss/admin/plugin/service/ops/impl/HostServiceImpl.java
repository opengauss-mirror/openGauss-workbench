package org.opengauss.admin.plugin.service.ops.impl;

import cn.hutool.core.util.StrUtil;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.exception.ops.OpsException;
import org.opengauss.admin.plugin.domain.model.ops.JschResult;
import org.opengauss.admin.plugin.service.ops.IHostService;
import org.opengauss.admin.plugin.utils.JschUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author lhf
 * @date 2023/2/14 14:28
 **/
@Slf4j
@Service
public class HostServiceImpl implements IHostService {
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private HostFacade hostFacade;
    @Autowired
    private JschUtil jschUtil;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    private EncryptionUtils encryptionUtils;
    @Override
    public boolean pathEmpty(String id, String path, String rootPassword) {
        OpsHostEntity hostEntity = hostFacade.getById(id);
        if (Objects.isNull(hostEntity)){
            throw new OpsException("host information not found");
        }

        Session rootSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), "root", encryptionUtils.decrypt(rootPassword)).orElseThrow(() -> new OpsException("Failed to establish connection with host"));
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
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (jschResult.getExitCode()!=0){
                log.error("Failed to probe data directory,exitCode:{},res:{}",jschResult.getExitCode(),jschResult.getResult());
                throw new OpsException("Failed to probe data directory");
            }

            return StrUtil.equalsIgnoreCase("0",StrUtil.trim(jschResult.getResult())) || jschResult.getResult().contains("No such file or directory");
        } catch (Exception e) {
            log.error("Failed to probe data directory",e);
            throw new OpsException("Failed to probe data directory");
        }
    }

    @Override
    public boolean portUsed(String id, Integer port, String rootPassword) {
        OpsHostEntity hostEntity = hostFacade.getById(id);
        if (Objects.isNull(hostEntity)){
            throw new OpsException("host information not found");
        }

        Session rootSession = jschUtil.getSession(hostEntity.getPublicIp(), hostEntity.getPort(), "root", encryptionUtils.decrypt(rootPassword)).orElseThrow(() -> new OpsException("Failed to establish connection with host"));
        try {
            return portUsed(rootSession,port);
        }finally {
            if (Objects.nonNull(rootSession) && rootSession.isConnected()){
                rootSession.disconnect();
            }
        }
    }

    private boolean portUsed(Session rootSession, Integer port) {
        String command = "lsof -i:"+port;
        try {
            JschResult jschResult = jschUtil.executeCommand(command, rootSession);
            if (jschResult.getExitCode()!=0){
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Failed to probe port",e);
            throw new OpsException("Failed to probe port");
        }
    }
}