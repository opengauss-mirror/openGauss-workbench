/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2022. All rights reserved.
 */

package com.nctigba.ebpf.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;


/**
 * os util
 *
 * @author luomeng@ncti-gba.cn
 * @since 2022/12/06 11:30
 */
@Slf4j
public class OSUtil {

    /**
     * exec os cmd
     *
     * @param cmd os cmd
     */
    public Object exec(String cmd) {
        try {
            String[] cmdA = {"/bin/sh", "-c", cmd};
            Process process = Runtime.getRuntime().exec(cmdA);
            LineNumberReader br = new LineNumberReader(new InputStreamReader(
                    process.getInputStream(), Charset.defaultCharset()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            br.close();
            return sb.toString();
        } catch (IOException | SecurityException | NullPointerException | IndexOutOfBoundsException e) {
            log.info(e.getMessage());
            return "exec fail!";
        }
    }

    /**
     * exec os cmd
     *
     * @param cmd os cmd
     */
    public String execCmd(String cmd) {
        try {
            String[] cmdA = {"/bin/sh", "-c", cmd};
            Process process = Runtime.getRuntime().exec(cmdA);
            Field pid = process.getClass().getDeclaredField("pid");
            pid.setAccessible(true);
            return String.valueOf(pid.getInt(process));
        } catch (IOException | SecurityException | NullPointerException | IndexOutOfBoundsException | NoSuchFieldException | IllegalAccessException e) {
            log.info(e.getMessage());
            return "exec fail!";
        }
    }
}
