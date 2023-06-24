/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */
package org.opengauss.plugin.agent.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CmdUtil {
    public static final String[] cmd(String... cmd) {
        return cmd;
    }

    public static final void readFromCmd(String cmd, Consumer<String> consumer)
            throws FileNotFoundException, IOException {
        readFromCmd(Arrays.asList("sh", "-c", cmd).toArray(new String[3]), consumer);
    }

    public static final void readFromCmd(String cmd, BiConsumer<Integer, String> consumer)
            throws FileNotFoundException, IOException {
        readFromCmd(Arrays.asList("sh", "-c", cmd).toArray(new String[3]), consumer);
    }

    public static final void readFromCmd(String[] cmd, Consumer<String> consumer)
            throws FileNotFoundException, IOException {
        if (consumer != null)
            readFromCmd(cmd, (i, line) -> consumer.accept(line));
    }

    public static final void readFromCmd(String[] cmd, BiConsumer<Integer, String> consumer)
            throws FileNotFoundException, IOException {
        var process = new ProcessBuilder().command(cmd).start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            log.error("", e);
            return;
        }
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));) {
            int i = -1;
            while (reader.ready()) {
                i++;
                String line = reader.readLine();
                if (line.isBlank())
                    continue;
                if (consumer != null)
                    consumer.accept(i, line);
            }
        } catch (Exception e) {
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