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
 *  CmdUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/InstanceExporter/
 *  src/main/java/com/nctigba/observability/instance/agent/util/CmdUtils.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.instance.agent.util;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.nctigba.observability.instance.agent.config.model.TargetConfig;
import com.nctigba.observability.instance.agent.exception.CMDException;
import com.nctigba.observability.instance.agent.exception.CollectException;
import com.nctigba.observability.instance.agent.exception.InitClientException;
import com.nctigba.observability.instance.agent.pool.SshClientNodeSessionPool;
import com.nctigba.observability.instance.agent.pool.SshClientSessionPool;
import com.nctigba.observability.instance.agent.service.TargetService;
import lombok.extern.log4j.Log4j2;
import lombok.var;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Util to call cmd command with SSH
 *
 * @since 2023/12/1
 */
@Log4j2
@Component
public class CmdUtils {
    private static final int CHANNEL_TIMEOUT = 1000 * 3;
    private static TargetService targetService;

    @Autowired
    TargetService targetServiceTemp;

    @PostConstruct
    public void init() {
        targetService = targetServiceTemp;
    }

    /**
     * Clear all ssh sessions
     *
     * @since 2023/12/1
     */
    public void clear() {
        SshClientNodeSessionPool.clear();
    }


    /**
     * Check if the ssh session can be connected for the node id
     *
     * @param targetConfig Target config DTO
     * @return If the ssh session can be connected for the node
     * @since 2023/12/1
     */
    public static boolean checkNodeCanBeConnected(TargetConfig targetConfig) {
        SshClientSessionPool pool = SshClientNodeSessionPool.getNodePool(targetConfig);
        Optional<ClientSession> session = pool.getSession();
        if(!session.isPresent()){
            return false;
        }
        try {
            ChannelExec channel = session.get().createExecChannel("echo 1");
            channel.open();
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 1000);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            pool.releaseSession(session.get());
        }
    }

    /**
     * Call cmd and consume the result
     *
     * @param nodeId   Node Id for database
     * @param cmd      Command text
     * @param consumer Consumer for the result
     * @throws IOException  Read file error
     * @throws CMDException read cmd fail
     * @since 2023/12/1
     */
    public static final void readFromCmd(
            String nodeId, String cmd, Consumer<String> consumer) throws IOException, CMDException {
        if (consumer != null) {
            readFromCmd(nodeId, cmd, (i, line) -> consumer.accept(line));
        }
    }

    /**
     * Call cmd and consume the result
     *
     * @param nodeId   Node Id for database
     * @param cmd      Command text
     * @param consumer Consumer for the result
     * @throws IOException  Create session error
     * @throws CMDException read cmd fail
     * @since 2023/12/1
     */
    public static final void readFromCmd(
            String nodeId, String cmd, BiConsumer<Integer, String> consumer) throws IOException, CMDException {
        String name = Thread.currentThread().getName();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Optional<TargetConfig> targetConfig =
                targetService.getTargetConfigs().stream().filter(z -> z.getNodeId().equals(nodeId))
                        .findFirst();
        if (!targetConfig.isPresent()) {
            throw new InitClientException("No Target Config for " + nodeId);
        }
        SshClientSessionPool pool = SshClientNodeSessionPool.getNodePool(targetConfig.get());

        Optional<ClientSession> opSession = pool.getSession();
        if (!opSession.isPresent()) {
            log.error("{} Get SSH session fail for {}", name, nodeId);
            throw new CollectException("Get SSH session fail for " + nodeId);
        }

        ClientSession session = opSession.get();
        try {
            var channel = session.createExecChannel(cmd);
            channel.setPtyType("ansi");
            channel.setPtyColumns(300);
            channel.setPtyWidth(300);
            channel.open();

            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), CHANNEL_TIMEOUT);
            for (int i = 0; i < 100 && channel.getExitStatus() == null; i++) {
                ThreadUtil.sleep(100L);
            }
            if (channel.getExitStatus() != null && channel.getExitStatus() != 0) {
                log.error("{}: exitStatus is {}", cmd, channel.getExitStatus());
                channel.close();
                pool.releaseSession(session);
                stopWatch.stop();
                log.error("exec {} costs {}ms", cmd, stopWatch.getTotalTimeMillis());
                throw new CMDException(cmd + StrUtil.SPACE + StrUtil.LF + StrUtil.SPACE + channel.getInvertedErr());
            }

            try (var reader = new BufferedReader(
                new InputStreamReader(channel.getInvertedOut(), Charset.defaultCharset()));) {
                int i = -1;
                while (reader.ready()) {
                    i++;
                    String line = reader.readLine();
                    if (StrUtil.isBlank(line)) {
                        continue;
                    }
                    if (consumer != null) {
                        consumer.accept(i, line);
                    }
                }
            } finally {
                channel.close();
            }
        } catch (Exception exp) {
            log.error("exec command fail!");
            try (StringWriter sw = new StringWriter();
                 PrintWriter pw = new PrintWriter(sw)) {
                exp.printStackTrace(pw);
                log.error(sw.toString());
            } catch (IOException ioe) {
                log.error("StringWriter error!");
            }
            throw exp;
        } finally {
            pool.releaseSession(session);
            stopWatch.stop();
            log.info("exec {} costs {}ms", cmd, stopWatch.getTotalTimeMillis());
        }
    }

    /**
     * Read and result the command result
     *
     * @param nodeId Node Id for database
     * @param cmd    Command text
     * @return Command result
     * @throws IOException  Read file error
     * @throws CMDException read cmd fail
     * @since 2023/12/1
     */
    public static final String readFromCmd(String nodeId, String cmd) throws IOException, CMDException {
        StringBuilder str = new StringBuilder();
        readFromCmd(nodeId, cmd, line -> {
            str.append(line);
        });
        return str.toString();
    }
}