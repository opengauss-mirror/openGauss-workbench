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
 *  HttpUtils.java
 *
 *  IDENTIFICATION
 *  plugins/observability-instance/src/main/java/com/nctigba/observability/instance/util/HttpUtils.java
 *
 *  -------------------------------------------------------------------------
 */
package com.nctigba.observability.instance.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * General http sending method
 *
 * @author xielibo
 */
public class HttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * Send a request for the GET method to the specified URL
     *
     * @param url   URL
     * @param param Request parameters, which should be in the form of
     *              name1=value1&name2=value2.
     * @return Response result of the remote resource represented
     */
    public static String sendGet(String url, String param) {
        return sendGet(url, param, "UTF-8");
    }

    /**
     * Send a request for the GET method to the specified URL
     *
     * @param url         URL
     * @param param       Request parameters, which should be in the form of
     *                    name1=value1&name2=value2.
     * @param contentType Encoding type
     * @return Response result of the remote resource represented
     */
    public static String sendGet(String url, String param, String contentType) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            String urlNameString = StringUtils.isEmpty(param) ? url : url + "?" + param;
            log.info("sendGet - {}", urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), contentType));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            log.error("use HttpsUtil.sendGet Exception, url=" + url + ",param=" + param, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                log.error("use in.close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return result.toString();
    }
}