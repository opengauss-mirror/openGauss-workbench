/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.instance.service;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;

import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.instance.entity.NctigbaEnv;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.status;
import com.nctigba.observability.instance.util.MessageSourceUtil;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

public abstract class AbstractInstaller {
    public static final String TAR = ".tar.gz";
    public static final String ZIP = ".zip";
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    protected HostFacade hostFacade;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    protected EncryptionUtils encryptionUtils;
    @Autowired
    @AutowiredType(AutowiredType.Type.PLUGIN_MAIN)
    protected HostUserFacade hostUserFacade;
    @Autowired
    protected NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    protected WsUtil wsUtil;

    public static String arch(String str) {
        return "aarch64".equals(str) ? "arm64" : "amd64";
    }

    protected OpsHostUserEntity getUser(OpsHostEntity hostEntity, String username, String rootPassword,
            List<Step> steps, int curr) {
        var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
            return username.equals(e.getUsername());
        }).findFirst().orElse(null);
        if (user != null && steps != null) {
            steps.get(curr).add("install.use", username);
        }
        return user;
    }

    protected void addMsg(WsSession wsSession, List<Step> steps, int curr, String msg) {
        steps.get(curr).add(msg);
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    protected int skipStep(WsSession wsSession, List<Step> steps, int curr) {
        steps.get(curr).setState(status.SKIP);
        curr++;
        sendMsg(wsSession, steps, curr, status.DOING);
        return curr;
    }

    /**
     * change current step to {@code DONE} and next step to {@code DOING}, send to
     * {@link WebSocket}
     */
    protected int nextStep(WsSession wsSession, List<Step> steps, int curr) {
        steps.get(curr).setState(status.DONE);
        curr++;
        sendMsg(wsSession, steps, curr, status.DOING);
        return curr;
    }

    /**
     * change current step to {@code DONE}, send to {@link WebSocket}
     */
    protected synchronized void sendMsg(WsSession wsSession, List<Step> steps, int curr, status state) {
        steps.get(curr).setState(state);
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    public void save(NctigbaEnv env) {
        if (envMapper.selectOne(Wrappers.<NctigbaEnv>lambdaQuery().eq(NctigbaEnv::getType, env.getType())
                .eq(NctigbaEnv::getPath, env.getPath())) == null)
            envMapper.insert(env);
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Step {
        String name;
        status state = status.TODO;
        List<String> msg = new ArrayList<>();

        public Step(String name) {
            this.name = MessageSourceUtil.get(name);
        }

        public void add(String e, Object... objs) {
            e = MessageSourceUtil.get(e, objs);
            msg.add(e);
        }

        public enum status {
            TODO,
            DOING,
            DONE,
            SKIP,
            ERROR
        }
    }
}