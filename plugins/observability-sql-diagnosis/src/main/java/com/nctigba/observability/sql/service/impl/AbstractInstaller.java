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
 *  AbstractInstaller.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/service/impl/AbstractInstaller.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.gitee.starblues.bootstrap.annotation.AutowiredType;
import com.gitee.starblues.bootstrap.annotation.AutowiredType.Type;
import com.nctigba.observability.sql.mapper.NctigbaEnvMapper;
import com.nctigba.observability.sql.model.entity.NctigbaEnvDO;
import com.nctigba.observability.sql.service.impl.AbstractInstaller.Step.status;
import com.nctigba.observability.sql.util.MessageSourceUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostEntity;
import org.opengauss.admin.common.core.domain.entity.ops.OpsHostUserEntity;
import org.opengauss.admin.common.core.domain.model.ops.HostUserBody;
import org.opengauss.admin.common.core.domain.model.ops.WsSession;
import org.opengauss.admin.common.exception.CustomException;
import org.opengauss.admin.common.utils.ops.WsUtil;
import org.opengauss.admin.system.plugin.facade.HostFacade;
import org.opengauss.admin.system.plugin.facade.HostUserFacade;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractInstaller {
    /**
     * Tar name
     */
    protected static final String TAR = ".tar.gz";

    /**
     * Zip name
     */
    protected static final String ZIP = ".zip";


    /**
     * record install or uninstall steps and send the steps info by websocket
     */
    protected ThreadLocal<WsSessionStep> wsSessionStepTl = new ThreadLocal<>();

    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    protected HostFacade hostFacade;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    protected EncryptionUtils encryptionUtils;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    protected HostUserFacade hostUserFacade;
    @Autowired
    protected NctigbaEnvMapper envMapper;
    @Autowired
    @AutowiredType(Type.PLUGIN_MAIN)
    protected WsUtil wsUtil;

    /**
     * initWsSessionStepTl
     *
     * @param wsSession WsSession
     * @param steps List<Step>
     */
    protected void initWsSessionStepTl(WsSession wsSession, List<Step> steps) {
        WsSessionStep wsSessionStep = new WsSessionStep();
        wsSessionStep.setWsSession(wsSession).setSteps(steps);
        wsSessionStepTl.set(wsSessionStep);
    }

    /**
     * Get arch
     *
     * @param str String
     * @return String
     */
    protected String arch(String str) {
        return "aarch64".equals(str) ? "arm64" : "amd64";
    }

    /**
     * Get user
     *
     * @param hostEntity   Host
     * @param username     User name
     * @param rootPassword Root password
     * @return OpsHostUserEntity
     */
    protected OpsHostUserEntity getUser(OpsHostEntity hostEntity, String username, String rootPassword) {
        var user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
            return username.equals(e.getUsername());
        }).findFirst().orElse(null);
        if (user == null && rootPassword != null) {
            var body = new HostUserBody();
            body.setHostId(hostEntity.getHostId());
            body.setPassword(encryptionUtils.encrypt(StrUtil.uuid()));
            body.setRootPassword(rootPassword);
            body.setUsername(username);
            hostUserFacade.add(body);
            user = hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e -> {
                return username.equals(e.getUsername());
            }).findFirst().orElse(null);
        }
        if (user == null) {
            throw new CustomException("user not found");
        }
        if ("root".equals(user.getUsername())) {
            user.setPassword(rootPassword);
        }
        return user;
    }

    /**
     * Skip step
     */
    protected void skipStep() {
        WsSessionStep wsSessionStep = wsSessionStepTl.get();
        if (wsSessionStep == null) {
            return;
        }
        WsSession wsSession = wsSessionStep.getWsSession();
        List<Step> steps = wsSessionStep.getSteps();
        AtomicInteger curr = wsSessionStep.getCurr();
        steps.get(curr.get()).setState(status.SKIP);
        int cur = wsSessionStep.getCurr().incrementAndGet();
        steps.get(cur).setState(status.DOING);
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    /**
     * Save data
     *
     * @param env NctigbaEnvDO
     */
    public void save(NctigbaEnvDO env) {
        if (envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, env.getType())
                .eq(NctigbaEnvDO::getPath, env.getPath())) == null) {
            envMapper.insert(env);
        }
    }

    /**
     * change current step to {@code DONE} and next step to {@code DOING}, send to
     * {@link WebSocket}
     */
    protected void nextStep() {
        WsSessionStep wsSessionStep = wsSessionStepTl.get();
        if (wsSessionStep == null) {
            return;
        }
        WsSession wsSession = wsSessionStep.getWsSession();
        List<Step> steps = wsSessionStep.getSteps();
        AtomicInteger curr = wsSessionStep.getCurr();
        steps.get(curr.get()).setState(status.DONE);
        int cur = wsSessionStep.getCurr().incrementAndGet();
        steps.get(cur).setState(status.DOING);
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    /**
     * change current step to {@code DONE}, send to {@link WebSocket}
     *
     * @param state status
     * @param msg String
     * @param objs Object[]
     */
    protected synchronized void sendMsg(status state, String msg, Object... objs) {
        WsSessionStep wsSessionStep = wsSessionStepTl.get();
        if (wsSessionStep == null) {
            return;
        }
        WsSession wsSession = wsSessionStep.getWsSession();
        List<Step> steps = wsSessionStep.getSteps();
        if (state != null) {
            steps.get(wsSessionStep.getCurr().get()).setState(state);
        }
        if (StrUtil.isNotBlank(msg)) {
            steps.get(wsSessionStep.getCurr().get()).add(msg, objs);
        }
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    /**
     * Skip step
     *
     * @param wsSession Websocket
     * @param msg       Message
     */
    protected synchronized void sendStatusMsg(WsSession wsSession, HashMap<String, String> msg) {
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(msg));
    }

    /**
     * Step
     *
     * @author luomeng
     * @since 2024/1/30
     */
    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Step {
        String name;
        status state = status.TODO;
        List<String> msg = new ArrayList<>();

        /**
         * Step
         *
         * @param name String
         */
        public Step(String name) {
            this.name = MessageSourceUtils.get(name);
        }

        /**
         * Add
         *
         * @param e    String
         * @param objs Object
         */
        public void add(String e, Object... objs) {
            msg.add(MessageSourceUtils.get(e, objs));
        }

        /**
         * status
         *
         * @author luomeng
         * @since 2024/1/30
         */
        public enum status {
            TODO,
            DOING,
            DONE,
            SKIP,
            ERROR
        }
    }

    /**
     * WsSessionStep
     */
    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class WsSessionStep {
        private WsSession wsSession;
        private List<Step> steps;
        private AtomicInteger curr = new AtomicInteger(0);
    }
}