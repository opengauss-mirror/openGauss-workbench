/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package org.opengauss.plugin.agent.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.opengauss.plugin.agent.exception.ExporterException;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CmdUtil {
    private static final int SESSION_TIMEOUT = 10000;
    private static final int CHANNEL_TIMEOUT = 1000 * 60 * 5;

    private static SshClient client;
    private static ClientSession session;

    static {
        try {
            client = SshClient.setUpDefaultClient();
            client.start();
            session = client.connect("root", "127.0.0.1", 22).verify().getSession();
            session.addPasswordIdentity("Ncti@001122");
            session.auth().verify(SESSION_TIMEOUT);
            log.info("server init success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final void readFromCmd(String cmd, Consumer<String> consumer)
            throws FileNotFoundException, IOException {
        if (consumer != null) {
            readFromCmd(cmd, (i, line) -> consumer.accept(line));
        }
    }

    public static final void readFromCmd(String cmd, BiConsumer<Integer, String> consumer)
            throws FileNotFoundException, IOException {
        log.debug("exec:" + cmd);
        var channel = session.createExecChannel(cmd);
        channel.setPtyType("ansi");
        channel.setPtyColumns(300);
        channel.setPtyWidth(300);
        channel.open();

        channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), CHANNEL_TIMEOUT);
        for (int i = 0; i < 100 && channel.getExitStatus() == null; i++)
            ThreadUtil.sleep(100L);
        if (channel.getExitStatus() != null && channel.getExitStatus() != 0) {
            throw new ExporterException(cmd + StrUtil.SPACE + StrUtil.LF + StrUtil.SPACE + channel.getInvertedErr());
        }

        try (var reader = new BufferedReader(new InputStreamReader(channel.getInvertedOut()));) {
            int i = -1;
            while (reader.ready()) {
                i++;
                String line = reader.readLine();
                if (line.isBlank()) {
                    continue;
                }
                if (consumer != null) {
                    consumer.accept(i, line);
                }
            }
        } catch (IOException e) {
            log.error("cmd '{}' format error", cmd, e);
            throw e;
        }
    }

    public static final String readFromCmd(String cmd) throws FileNotFoundException, IOException {
        StringBuilder str = new StringBuilder();
        readFromCmd(cmd, line -> {
            str.append(line);
        });
        return str.toString();
    }
}