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
 *  CommonUtil.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/util/CommonUtil.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.util;

import org.opengauss.admin.common.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * CommonUtils
 *
 * @author luomeng
 * @since 2024/4/3
 */
@Component
public class CommonUtils {
    @Autowired
    private ResourceLoader loader;

    /**
     * upload Shell Script
     *
     * @param session    session
     * @param path       path
     * @param scriptName scriptName
     */
    public void uploadShellScript(SshSession session, String path, String scriptName) {
        try (InputStream in = loader.getResource(scriptName).getInputStream();
             BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
            StringBuilder strBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                strBuilder.append(line);
                strBuilder.append(System.lineSeparator());
            }
            ByteArrayInputStream inStream = new ByteArrayInputStream(
                    strBuilder.toString().getBytes(Charset.defaultCharset()));
            session.upload(inStream, path + scriptName);
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
    }
}
