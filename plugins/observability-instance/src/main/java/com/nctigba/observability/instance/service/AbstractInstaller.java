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
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/service/AbstractInstaller.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.service;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.hutool.core.util.StrUtil;
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
import com.nctigba.observability.instance.model.entity.NctigbaEnvDO;
import com.nctigba.observability.instance.mapper.NctigbaEnvMapper;
import com.nctigba.observability.instance.service.AbstractInstaller.Step.Status;
import com.nctigba.observability.instance.util.MessageSourceUtils;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

public abstract class AbstractInstaller {
    public static final String TAR = ".tar.gz";
    public static final String ZIP = ".zip";
    protected ThreadLocal<WsSessionStep> wsSessionStepTl = new ThreadLocal<>();

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

    protected void initWsSessionStepTl(WsSession wsSession, List<Step> steps) {
        WsSessionStep wsSessionStep = new WsSessionStep();
        wsSessionStep.setWsSession(wsSession).setSteps(steps);
        wsSessionStepTl.set(wsSessionStep);
    }

    public static String arch(String str) {
        return "aarch64".equals(str) ? "arm64" : "amd64";
    }

    /**
     * getUser
     *
     * @param hostEntity OpsHostEntity
     * @param username String
     * @return OpsHostUserEntity
     */
    protected OpsHostUserEntity getUser(OpsHostEntity hostEntity, String username) {
        return hostUserFacade.listHostUserByHostId(hostEntity.getHostId()).stream().filter(e ->
            username.equals(e.getUsername())).findFirst().orElse(null);
    }

    /**
     * addMsg
     */
    protected  void sendMsg(Status status, String msg, Object... objs) {
        WsSessionStep wsSessionStep = wsSessionStepTl.get();
        if (wsSessionStep == null) {
            return;
        }
        WsSession wsSession = wsSessionStep.getWsSession();
        List<Step> steps = wsSessionStep.getSteps();
        if (status != null) {
            steps.get(wsSessionStep.getCurr().get()).setState(status);
        }
        if (StrUtil.isNotBlank(msg)) {
            steps.get(wsSessionStep.getCurr().get()).add(msg, objs);
        }
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    /**
     * addMsg
     *
     * @param wsSession WsSession
     * @param steps steps
     * @param curr curr
     * @param msg msg
     */
    protected void addMsg(WsSession wsSession, List<Step> steps, int curr, String msg) {
        steps.get(curr).add(msg);
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    /**
     * skip step
     */
    protected void skipStep() {
        WsSessionStep wsSessionStep = wsSessionStepTl.get();
        if (wsSessionStep == null) {
            return;
        }
        WsSession wsSession = wsSessionStep.getWsSession();
        List<Step> steps = wsSessionStep.getSteps();
        AtomicInteger curr = wsSessionStep.getCurr();
        steps.get(curr.get()).setState(Status.SKIP);
        int cur = wsSessionStep.getCurr().incrementAndGet();
        steps.get(cur).setState(Status.DOING);
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }
    protected int skipStep(WsSession wsSession, List<Step> steps, int curr) {
        steps.get(curr).setState(Status.SKIP);
        curr++;
        sendMsg(wsSession, steps, curr, Status.DOING);
        return curr;
    }

    /**
     * next step
     */
    protected void nextStep() {
        WsSessionStep wsSessionStep = wsSessionStepTl.get();
        if (wsSessionStep == null) {
            return;
        }
        WsSession wsSession = wsSessionStep.getWsSession();
        List<Step> steps = wsSessionStep.getSteps();
        AtomicInteger curr = wsSessionStep.getCurr();
        steps.get(curr.get()).setState(Status.DONE);
        int cur = wsSessionStep.getCurr().incrementAndGet();
        steps.get(cur).setState(Status.DOING);
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    /**
     * skip all and done
     */
    protected void done() {
        WsSessionStep wsSessionStep = wsSessionStepTl.get();
        if (wsSessionStep == null) {
            return;
        }
        WsSession wsSession = wsSessionStep.getWsSession();
        List<Step> steps = wsSessionStep.getSteps();
        while (wsSessionStep.getCurr().get() < steps.size() - 1) {
            skipStep();
        }
        steps.get(wsSessionStep.getCurr().get()).setState(Status.DONE);
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    /**
     * change current step to {@code DONE} and next step to {@code DOING}, send to
     * {@link WebSocket}
     */
    protected int nextStep(WsSession wsSession, List<Step> steps, int curr) {
        steps.get(curr).setState(Status.DONE);
        curr++;
        sendMsg(wsSession, steps, curr, Status.DOING);
        return curr;
    }

    /**
     * change current step to {@code DONE}, send to {@link WebSocket}
     */
    protected synchronized void sendMsg(WsSession wsSession, List<Step> steps, int curr, Status state) {
        steps.get(curr).setState(state);
        wsUtil.sendText(wsSession, JSONUtil.toJsonStr(steps));
    }

    public void save(NctigbaEnvDO env) {
        if (envMapper.selectOne(Wrappers.<NctigbaEnvDO>lambdaQuery().eq(NctigbaEnvDO::getType, env.getType())
                .eq(NctigbaEnvDO::getPath, env.getPath())) == null)
            envMapper.insert(env);
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Step {
        String name;
        Status state = Status.TODO;
        List<String> msgs = new ArrayList<>();

        public Step(String name) {
            this.name = MessageSourceUtils.get(name);
        }

        public void add(String msg, Object... objs) {
            msg = MessageSourceUtils.get(msg, objs);
            msgs.add(msg);
        }

        public enum Status {
            TODO,
            DOING,
            DONE,
            SKIP,
            ERROR
        }
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class WsSessionStep {
        private WsSession wsSession;
        private List<Step> steps;
        private AtomicInteger curr = new AtomicInteger(0);
    }
}