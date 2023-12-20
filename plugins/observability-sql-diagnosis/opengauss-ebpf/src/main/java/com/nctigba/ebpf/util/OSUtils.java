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
 *  OSUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/opengauss-ebpf/src/main/java/com/nctigba/ebpf/util/OSUtils.java
 *
 *  -------------------------------------------------------------------------
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
public class OSUtils {

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
