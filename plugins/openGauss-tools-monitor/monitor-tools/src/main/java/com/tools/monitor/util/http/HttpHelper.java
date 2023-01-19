/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.
 */

package com.tools.monitor.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletRequest;

/**
 * HttpHelper
 *
 * @author liu
 * @since 2022-10-01
 */
@Slf4j
public class HttpHelper {
    /**
     * getBodyString
     *
     * @param request request
     * @return String
     */
    public static String getBodyString(ServletRequest request) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try (InputStream inputStream = request.getInputStream()) {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException exception) {
            log.error("getBodyString-->{}", exception.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException exception) {
                    log.error("getBodyString-->{}", exception.getMessage());
                }
            }
        }
        return sb.toString();
    }
}
